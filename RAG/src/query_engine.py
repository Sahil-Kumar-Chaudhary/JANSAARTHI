#!/usr/bin/env python3
"""
JANSAARTHI AI - Government Scheme Retrieval and RAG Query Engine
Supports genuine semantic retrieval (FastEmbed with ONNX), weighted lexical retrieval
with token boundaries and synonym expansion, and calibrated hybrid retrieval.
Synthesizes grounded answers using Groq LLM.
"""

import sys
import os
import json
import sqlite3
import re
import argparse
from pathlib import Path
import numpy as np

# Resolve project directories
RAG_DIR = Path(__file__).resolve().parents[1]
CHROMA_DB_PATH = RAG_DIR / "chroma_db" / "chroma.sqlite3"
SEMANTIC_INDEX_DIR = RAG_DIR / "semantic_index"
VEC_PATH = SEMANTIC_INDEX_DIR / "scheme_embeddings.npy"
META_PATH = SEMANTIC_INDEX_DIR / "scheme_metadata.json"

# Load environment variables safely
try:
    from dotenv import load_dotenv
    load_dotenv(RAG_DIR / ".env")
    load_dotenv(RAG_DIR.parent / "backend" / ".env")
except ImportError:
    pass

# Domain synonym expansion map mapping citizen language to scheme vocabulary
SYNONYM_MAP = {
    "female": ["women", "woman", "mahila"],
    "females": ["women", "woman", "mahila"],
    "woman": ["women", "female"],
    "women": ["female", "woman", "mahila"],
    "founder": ["entrepreneur", "enterprise"],
    "founders": ["entrepreneurs", "enterprise"],
    "financial": ["finance", "credit", "grant"],
    "cultivator": ["farmer", "kisan", "krishi", "agriculture"],
    "cultivators": ["farmers", "kisan", "krishi", "agriculture"],
    "grower": ["farmer", "kisan", "agriculture"],
    "growers": ["farmers", "kisan", "agriculture"],
    "farming": ["agriculture", "farmer", "kisan"],
    "shopkeeper": ["retail", "micro", "enterprise", "vendor", "trader"],
    "shopkeepers": ["retail", "micro", "enterprise", "vendors", "traders"],
    "loan": ["credit", "mudra", "advance", "borrowing"],
    "loans": ["credit", "mudra", "advances", "borrowings"],
    "subsidy": ["grant", "rebate", "subvention"],
    "pension": ["maandhan", "old-age", "vridha"],
    "youth": ["young", "student", "employment", "skill"],
    "drought": ["crop", "insurance", "fasal", "agriculture"],
    "calamity": ["crop", "insurance", "fasal", "agriculture"],
    "housing": ["awas", "shelter", "pucca"],
    "medical": ["health", "hospital", "arogya", "treatment"],
    "healthcare": ["health", "hospital", "arogya", "treatment"],
    "scholarship": ["education", "matric", "studies", "students"],
    "kheti": ["agriculture", "farmer", "kisan"],
}

STOP_WORDS = {
    'what', 'when', 'where', 'which', 'who', 'whom', 'this', 'that', 'these',
    'those', 'am', 'is', 'are', 'was', 'were', 'be', 'being', 'have',
    'has', 'had', 'having', 'do', 'does', 'did', 'doing', 'a', 'an', 'the',
    'and', 'but', 'if', 'or', 'because', 'as', 'until', 'while', 'of', 'at',
    'by', 'for', 'with', 'about', 'against', 'between', 'into', 'through',
    'during', 'before', 'after', 'above', 'below', 'to', 'from', 'up', 'down',
    'in', 'out', 'on', 'off', 'over', 'under', 'again', 'further', 'then',
    'once', 'here', 'there', 'all', 'any', 'both', 'each', 'few', 'more',
    'most', 'other', 'some', 'such', 'no', 'nor', 'not', 'only', 'own',
    'same', 'so', 'than', 'too', 'very', 'can', 'will', 'just', 'should',
    'now', 'tell', 'me', 'about', 'explain', 'schemes', 'scheme', 'government',
    'sarkari', 'yojana', 'help', 'give', 'need', 'want', 'available', 'details',
    'liye', 'hai', 'kare', 'kaise'
}

LOW_WEIGHT_TERMS = {'support', 'assistance', 'india', 'pradhan', 'mantri', 'national', 'state', 'centre', 'central'}

# Cached global resources
_SCHEMES_CACHE = None
_VECTORS_CACHE = None
_EMBED_MODEL = None

def clean_scheme_content(raw_text: str) -> str:
    """Removes administrative boilerplate lines from search matching."""
    lines = raw_text.split('\n')
    filtered = []
    for line in lines:
        lower = line.strip().lower()
        if lower.startswith(('scheme_launch_date:', 'data_status:', 'source_platform:', 'scheme_id:')):
            continue
        filtered.append(line)
    return '\n'.join(filtered)

def tokenize(text: str) -> set[str]:
    """Tokenizes text using exact alphanumeric word boundaries."""
    return set(re.findall(r'\b[a-zA-Z0-9]{2,}\b', text.lower()))

def build_concept_groups(tokens: list[str]) -> list[set[str]]:
    """Builds deduplicated concept groups with synonym expansion."""
    groups = []
    seen = set()
    for t in tokens:
        group = {t}
        if t in SYNONYM_MAP:
            group.update(SYNONYM_MAP[t])
        frozen = frozenset(group)
        if frozen not in seen:
            seen.add(frozen)
            groups.append(group)
    return groups

def load_scheme_data():
    """Loads scheme metadata and pre-tokenized representations."""
    global _SCHEMES_CACHE
    if _SCHEMES_CACHE is not None:
        return _SCHEMES_CACHE

    if META_PATH.exists():
        with open(META_PATH, "r", encoding="utf-8") as f:
            schemes = json.load(f)
    elif CHROMA_DB_PATH.exists():
        conn = sqlite3.connect(str(CHROMA_DB_PATH))
        cur = conn.cursor()
        rows = cur.execute("""
            SELECT
                e.embedding_id,
                MAX(CASE WHEN em.key = 'scheme_id' THEN em.string_value END) as scheme_id,
                MAX(CASE WHEN em.key = 'scheme_name' THEN em.string_value END) as scheme_name,
                MAX(CASE WHEN em.key = 'category' THEN em.string_value END) as category,
                MAX(CASE WHEN em.key = 'source_platform' THEN em.string_value END) as source_platform,
                MAX(CASE WHEN em.key = 'chroma:document' THEN em.string_value END) as document
            FROM embeddings e
            JOIN segments s ON e.segment_id = s.id
            LEFT JOIN embedding_metadata em ON e.id = em.id
            WHERE s.id = 'f1205b9c-cace-4c51-8936-a510ddee7a73'
            GROUP BY e.embedding_id
            ORDER BY e.id
        """).fetchall()
        conn.close()
        schemes = []
        for r in rows:
            schemes.append({
                "embedding_id": r[0],
                "scheme_id": r[1] or r[0].replace("scheme-", ""),
                "scheme_name": r[2] or "Unknown Scheme",
                "category": r[3] or "General",
                "source_platform": r[4] or "myScheme.gov.in",
                "document": r[5] or ""
            })
    else:
        raise FileNotFoundError(f"Neither semantic metadata nor Chroma SQLite database found at {CHROMA_DB_PATH}")

    # Add tokenized caches
    for s in schemes:
        s["clean_content"] = clean_scheme_content(s["document"])
        s["title_tokens"] = tokenize(s["scheme_name"])
        s["cat_tokens"] = tokenize(s["category"])
        s["body_tokens"] = tokenize(s["clean_content"])

    _SCHEMES_CACHE = schemes
    return _SCHEMES_CACHE

def load_semantic_vectors():
    """Loads or generates normalized 384-dim semantic embeddings."""
    global _VECTORS_CACHE
    if _VECTORS_CACHE is not None:
        return _VECTORS_CACHE

    if VEC_PATH.exists():
        _VECTORS_CACHE = np.load(str(VEC_PATH))
        return _VECTORS_CACHE

    # If vector file does not exist, build it from documents
    schemes = load_scheme_data()
    embed_model = get_embed_model()
    if embed_model is None:
        return None

    docs = [s["document"] for s in schemes]
    vectors = list(embed_model.embed(docs))
    matrix = np.array(vectors, dtype=np.float32)
    norms = np.linalg.norm(matrix, axis=1, keepdims=True)
    matrix = matrix / np.maximum(norms, 1e-12)

    SEMANTIC_INDEX_DIR.mkdir(parents=True, exist_ok=True)
    np.save(str(VEC_PATH), matrix)
    _VECTORS_CACHE = matrix
    return _VECTORS_CACHE

def get_embed_model():
    """Lazy-initializes FastEmbed TextEmbedding model (ONNX)."""
    global _EMBED_MODEL
    if _EMBED_MODEL is not None:
        return _EMBED_MODEL
    try:
        from fastembed import TextEmbedding
        _EMBED_MODEL = TextEmbedding(model_name="sentence-transformers/all-MiniLM-L6-v2")
        return _EMBED_MODEL
    except Exception as e:
        print(f"[Warning] Could not initialize FastEmbed: {e}. Falling back to lexical retrieval.", file=sys.stderr)
        return None

def compute_lexical_scores(query: str, schemes: list[dict]):
    """Calculates weighted lexical scores with domain synonym expansion and coverage."""
    raw_tokens = [t.lower() for t in re.findall(r'\b[a-zA-Z0-9]+\b', query) if len(t) >= 2]
    meaningful_tokens = [t for t in raw_tokens if t not in STOP_WORDS]
    if not meaningful_tokens:
        meaningful_tokens = raw_tokens

    concept_groups = build_concept_groups(meaningful_tokens)
    scores = np.zeros(len(schemes), dtype=np.float32)
    matched_terms_map = {}

    for i, s in enumerate(schemes):
        base_score = 0
        matched_groups_count = 0
        matched_terms = []
        used_doc_tokens = set()

        for group in concept_groups:
            best_group_score = 0
            best_token_matched = None

            for t in group:
                if t in used_doc_tokens:
                    continue
                weight_mult = 0.5 if t in LOW_WEIGHT_TERMS else 1.0

                if t in s["title_tokens"]:
                    best_group_score = 16 * weight_mult
                    best_token_matched = t
                    break
                elif t in s["cat_tokens"]:
                    best_group_score = 8 * weight_mult
                    best_token_matched = t
                    break
                elif t in s["body_tokens"]:
                    best_group_score = 5 * weight_mult
                    best_token_matched = t
                    break

            if best_group_score > 0:
                base_score += best_group_score
                matched_groups_count += 1
                matched_terms.append(best_token_matched)
                used_doc_tokens.add(best_token_matched)

        coverage_ratio = matched_groups_count / len(concept_groups) if concept_groups else 0
        total_score = base_score * (1.0 + (coverage_ratio * 1.5))
        min_concepts_needed = 1 if len(concept_groups) == 1 else 2

        if total_score >= 8.0 and matched_groups_count >= min_concepts_needed:
            scores[i] = total_score
            matched_terms_map[i] = matched_terms

    return scores, matched_terms_map

def compute_semantic_similarities(query: str):
    """Calculates cosine similarities between runtime query embedding and scheme vectors."""
    vectors = load_semantic_vectors()
    if vectors is None:
        return None

    embed_model = get_embed_model()
    if embed_model is None:
        return None

    try:
        q_vec = list(embed_model.embed([query]))[0]
        q_norm = np.linalg.norm(q_vec)
        if q_norm > 0:
            q_vec = q_vec / q_norm
        # Dot product with normalized document vectors = Cosine similarity
        sims = np.dot(vectors, q_vec)
        return sims
    except Exception as e:
        print(f"[Warning] Semantic embedding error: {e}", file=sys.stderr)
        return None

def retrieve_schemes(query: str, k: int = 5, mode: str = "hybrid") -> list[dict]:
    """
    Main scheme retrieval function supporting 'hybrid', 'semantic', and 'lexical' modes.
    Returns ranked list of scheme objects with metadata, relevance scores, and distances.
    """
    schemes = load_scheme_data()
    lex_scores, matched_terms_map = compute_lexical_scores(query, schemes)
    sem_sims = compute_semantic_similarities(query) if mode in ("hybrid", "semantic") else None

    # Fallback to lexical if semantic model is unavailable
    if sem_sims is None and mode in ("hybrid", "semantic"):
        mode = "lexical"

    scored_candidates = []

    if mode == "lexical":
        valid_indices = np.where(lex_scores >= 8.0)[0]
        for idx in valid_indices:
            s = schemes[idx]
            scored_candidates.append({
                "score": round(float(lex_scores[idx]), 1),
                "schemeId": s["scheme_id"],
                "schemeName": s["scheme_name"],
                "category": s["category"],
                "sourcePlatform": s["source_platform"],
                "content": s["document"],
                "matchedTerms": list(set(matched_terms_map.get(idx, [])))
            })

    elif mode == "semantic":
        # Calibrated semantic threshold: reject noise/OOD queries below 0.235 similarity
        max_sim = float(np.max(sem_sims))
        if max_sim >= 0.235:
            valid_indices = np.where(sem_sims >= 0.235)[0]
            for idx in valid_indices:
                s = schemes[idx]
                sim = float(sem_sims[idx])
                dist = round(1.0 - sim, 4)
                scored_candidates.append({
                    "score": round(sim * 100.0, 1),
                    "similarity": round(sim, 4),
                    "distance": dist,
                    "schemeId": s["scheme_id"],
                    "schemeName": s["scheme_name"],
                    "category": s["category"],
                    "sourcePlatform": s["source_platform"],
                    "content": s["document"],
                    "matchedTerms": []
                })

    else: # Hybrid mode (default)
        max_sim = float(np.max(sem_sims)) if sem_sims is not None else 0.0
        max_lex = float(np.max(lex_scores))

        # OOD Guard: When neither modality finds sufficient evidence, cleanly return 0 sources
        if max_sim < 0.235 and max_lex < 8.0:
            return []

        # Scale semantic similarity to 0..50 score
        sem_scaled = np.maximum(0.0, (sem_sims - 0.23) / 0.50 * 50.0)

        for i, s in enumerate(schemes):
            lex = float(lex_scores[i])
            sem = float(sem_scaled[i])
            raw_sim = float(sem_sims[i])
            dist = round(1.0 - raw_sim, 4)

            # Skip candidates with no lexical support and weak semantic signal
            if lex == 0 and raw_sim < 0.235:
                continue

            # Concordance bonus when both modalities support the candidate
            if lex > 0 and sem > 0:
                final_score = (0.50 * lex) + (0.50 * sem) + 12.0
            elif lex > 0:
                final_score = lex * 0.80
            else:
                final_score = sem * 0.80

            if final_score >= 8.0:
                scored_candidates.append({
                    "score": round(final_score, 1),
                    "lexicalScore": round(lex, 1),
                    "similarity": round(raw_sim, 4),
                    "distance": dist,
                    "schemeId": s["scheme_id"],
                    "schemeName": s["scheme_name"],
                    "category": s["category"],
                    "sourcePlatform": s["source_platform"],
                    "content": s["document"],
                    "matchedTerms": list(set(matched_terms_map.get(i, [])))
                })

    # Sort descending by score
    scored_candidates.sort(key=lambda d: d["score"], reverse=True)
    return scored_candidates[:k]

def retrieve_schemes_from_chroma(query: str, k: int = 5, db_path: Path = CHROMA_DB_PATH):
    """Backward-compatible entry point preserving earlier signature."""
    return retrieve_schemes(query, k=k, mode="hybrid")

def generate_answer_with_groq(query: str, contexts: list[dict], api_key: str = None) -> tuple[str, str]:
    """Generates grounded RAG response using Groq cloud inference."""
    key = api_key or os.environ.get("GROQ_API_KEY")
    if not key:
        return (
            "No Groq API key configured. The retrieved scheme details are provided in the sources list.",
            "unconfigured_api_key"
        )

    try:
        from groq import Groq
        client = Groq(api_key=key)

        context_text = "\n\n---\n\n".join([
            f"Scheme Name: {c['schemeName']}\nScheme ID: {c['schemeId']}\nCategory: {c['category']}\nSource: {c['sourcePlatform']}\n\nDetails:\n{c['content']}"
            for c in contexts
        ])

        system_prompt = (
            "You are JANSAARTHI AI, an official government scheme assistance specialist in India.\n"
            "Rules:\n"
            "1. Answer the question using ONLY the provided scheme context.\n"
            "2. Do NOT hallucinate or assume unstated eligibility or loan criteria.\n"
            "3. If the context does not contain enough information, state: "
            "'I do not have enough verified scheme information in the database to answer that completely.'\n"
            "4. Cite the scheme name and ID for each fact you state.\n"
            "5. Keep the response factual, structured, and easy to read for citizens."
        )

        user_prompt = f"<context>\n{context_text}\n</context>\n\n<question>\n{query}\n</question>"

        models_to_try = ["llama-3.3-70b-versatile", "llama-3.1-8b-instant", "openai/gpt-oss-20b"]
        last_error = None

        for model in models_to_try:
            try:
                response = client.chat.completions.create(
                    model=model,
                    messages=[
                        {"role": "system", "content": system_prompt},
                        {"role": "user", "content": user_prompt}
                    ],
                    temperature=0.1,
                    max_tokens=600
                )
                answer = response.choices[0].message.content.strip()
                return answer, "generated"
            except Exception as e:
                last_error = e
                continue

        return (
            f"Unable to generate response from model: {str(last_error)}",
            "model_error"
        )

    except Exception as e:
        return (f"Groq service error: {str(e)}", "service_error")

def main():
    parser = argparse.ArgumentParser(description="JANSAARTHI Scheme RAG Engine")
    parser.add_argument("query", nargs="?", default="", help="Natural language query")
    parser.add_argument("--k", type=int, default=5, help="Number of documents to retrieve")
    parser.add_argument("--mode", type=str, default="hybrid", choices=["hybrid", "semantic", "lexical"], help="Retrieval mode")
    parser.add_argument("--stdin", action="store_true", help="Read JSON payload from stdin")

    args = parser.parse_args()

    if args.stdin:
        try:
            raw = sys.stdin.read().strip()
            if raw:
                payload = json.loads(raw)
                query = payload.get("query", "").strip()
                k = int(payload.get("k", 5))
                mode = payload.get("mode", "hybrid").lower()
                if mode not in ["hybrid", "semantic", "lexical"]:
                    mode = "hybrid"
            else:
                query = ""
                k = 5
                mode = "hybrid"
        except Exception as e:
            print(json.dumps({"status": "error", "message": f"Invalid JSON stdin: {str(e)}"}), file=sys.stdout)
            sys.exit(1)
    else:
        query = args.query.strip()
        k = args.k
        mode = args.mode

    if not query:
        print(json.dumps({"status": "error", "code": "EMPTY_QUERY", "message": "Query cannot be empty."}), file=sys.stdout)
        sys.exit(0)

    try:
        retrieved_docs = retrieve_schemes(query, k=k, mode=mode)

        if not retrieved_docs:
            response = {
                "status": "success",
                "query": query,
                "retrieval_mode": mode,
                "answer": "I don't have enough information in the verified scheme database to answer that. No matching government schemes were found for your query.",
                "sources_count": 0,
                "sources": [],
                "llm_status": "no_context"
            }
            print(json.dumps(response), file=sys.stdout)
            sys.exit(0)

        # Generate answer using retrieved context
        answer, llm_status = generate_answer_with_groq(query, retrieved_docs)

        # Format sources for clean API consumption
        sources = []
        for doc in retrieved_docs:
            source_item = {
                "schemeId": doc["schemeId"],
                "schemeName": doc["schemeName"],
                "category": doc["category"],
                "sourcePlatform": doc["sourcePlatform"],
                "snippet": doc["content"][:300].strip() + ("..." if len(doc["content"]) > 300 else "")
            }
            if "score" in doc:
                source_item["score"] = doc["score"]
            if "similarity" in doc:
                source_item["similarity"] = doc["similarity"]
            if "distance" in doc:
                source_item["distance"] = doc["distance"]
            sources.append(source_item)

        response = {
            "status": "success",
            "query": query,
            "retrieval_mode": mode,
            "answer": answer,
            "sources_count": len(sources),
            "sources": sources,
            "llm_status": llm_status
        }
        print(json.dumps(response), file=sys.stdout)
        sys.exit(0)

    except Exception as e:
        print(json.dumps({
            "status": "error",
            "code": "RAG_PROCESSING_ERROR",
            "message": str(e)
        }), file=sys.stdout)
        sys.exit(1)

if __name__ == "__main__":
    main()

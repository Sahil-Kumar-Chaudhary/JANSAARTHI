#!/usr/bin/env python3
"""
JANSAARTHI AI - Government Scheme Retrieval and RAG Query Engine
Supports genuine semantic retrieval (FastEmbed with ONNX), weighted lexical retrieval
with token boundaries, target group prioritization, synonym expansion, exact scheme title bonuses,
and calibrated hybrid retrieval with dynamic relevance pruning.
Synthesizes grounded answers using Groq LLM (when API key is present).
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

# Generic administrative and modifier terms that carry low specific information
LOW_WEIGHT_TERMS = {
    'support', 'assistance', 'india', 'pradhan', 'mantri', 'national',
    'state', 'centre', 'central', 'scheme', 'schemes', 'yojana', 'yojna',
    'pariyojana', 'mission', 'programme', 'program', 'bharat', 'sarkar'
}

# Generic financial product words that should not be treated as unique scheme titles
GENERIC_FINANCIAL_TERMS = {
    'financial', 'finance', 'loan', 'loans', 'credit', 'subsidy', 'subsidies',
    'grant', 'grants', 'training', 'scheme', 'schemes', 'project', 'programme',
    'program', 'fund', 'funds', 'capital'
}

# Known scheme acronyms mapped to canonical Scheme IDs
ACRONYM_MAP = {
    "pmmy": "SCH002",
    "pmegp": "SCH004",
    "pmfme": "SCH006",
    "zed": "SCH008",
    "sfurti": "SCH010",
    "aspire": "SCH011",
    "pmkisan": "SCH021",
    "pm-kisan": "SCH021",
    "pmfby": "SCH023",
    "pmksy": "SCH024",
    "kcc": "SCH025",
    "pmmsy": "SCH027",
    "kusum": "SCH028",
    "pm-kusum": "SCH028",
    "pmay": "SCH040",
    "pmay-u": "SCH040",
    "pmay-g": "SCH041",
    "pmuy": "SCH042",
    "ujjwala": "SCH042",
    "jjm": "SCH043",
    "sbm": "SCH044",
    "pmjay": "SCH046",
    "ayushman": "SCH046",
    "pmbjp": "SCH047",
    "pmmvy": "SCH048",
    "bbbp": "SCH049",
    "pm-poshan": "SCH052",
    "poshan": "SCH052",
    "nmmss": "SCH054",
    "pmkvy": "SCH065",
    "pm-daksh": "SCH066",
    "pm-vikas": "SCH068",
    "smile": "SCH069",
    "apy": "SCH070",
    "pmjjby": "SCH071",
    "pmsby": "SCH072",
    "ssa": "SCH073",
    "nsap": "SCH074",
    "svanidhi": "SCH005",
    "pm-svanidhi": "SCH005"
}

# Domain synonym expansion map mapping citizen language to scheme vocabulary
SYNONYM_MAP = {
    "female": ["women", "woman", "mahila", "lady", "girl"],
    "females": ["women", "woman", "mahila", "ladies", "girls"],
    "woman": ["women", "female", "mahila"],
    "women": ["female", "woman", "mahila", "matru"],
    "pregnant": ["maternity", "matru", "mother", "vandana", "pregnancy"],
    "maternity": ["pregnant", "matru", "mother", "vandana", "pregnancy"],
    "mother": ["maternity", "matru", "vandana", "mother"],
    "mothers": ["maternity", "matru", "vandana", "mothers"],
    "founder": ["entrepreneur", "enterprise", "business", "startup"],
    "founders": ["entrepreneurs", "enterprise", "business", "startups"],
    "entrepreneur": ["founder", "enterprise", "business", "startup"],
    "entrepreneurs": ["founders", "enterprise", "business", "startups"],
    "enterprise": ["business", "entrepreneur", "venture", "firm"],
    "business": ["enterprise", "entrepreneur", "commercial", "trade", "shop", "micro"],
    "financial": ["finance", "credit", "grant", "fund", "funds"],
    "capital": ["credit", "finance", "loan", "funds", "fund"],
    "working": ["capital", "credit", "cash", "operating"],
    "credit": ["loan", "mudra", "advance", "borrowing", "lending"],
    "loan": ["credit", "mudra", "advance", "borrowing", "lending"],
    "loans": ["credit", "mudra", "advances", "borrowings", "lending"],
    "cultivator": ["farmer", "kisan", "krishi", "agriculture"],
    "cultivators": ["farmers", "kisan", "krishi", "agriculture"],
    "grower": ["farmer", "kisan", "agriculture"],
    "growers": ["farmers", "kisan", "agriculture"],
    "farming": ["agriculture", "farmer", "kisan", "cultivation"],
    "farmer": ["kisan", "krishi", "agriculture", "cultivator"],
    "farmers": ["kisan", "krishi", "agriculture", "cultivators"],
    "crop": ["crops", "fasal", "agriculture", "harvest", "produce"],
    "crops": ["crop", "fasal", "agriculture", "harvest", "produce"],
    "insurance": ["bima", "coverage", "assurance", "claim"],
    "drought": ["crop", "fasal", "bima", "calamity", "disaster"],
    "flood": ["crop", "fasal", "bima", "calamity", "disaster"],
    "irrigation": ["sinchayee", "sinchai", "water", "irrigation", "sprinkler", "drip"],
    "drip": ["sinchayee", "sinchai", "irrigation", "water"],
    "sprinkler": ["sinchayee", "sinchai", "irrigation", "water"],
    "shopkeeper": ["retail", "micro", "enterprise", "vendor", "trader", "shopkeepers", "svanidhi"],
    "shopkeepers": ["retail", "micro", "enterprise", "vendors", "traders", "shopkeeper", "svanidhi"],
    "vendor": ["svanidhi", "street", "hawker", "retail", "shopkeeper"],
    "vendors": ["svanidhi", "street", "hawkers", "retail", "shopkeepers"],
    "hawker": ["vendor", "svanidhi", "street"],
    "hawkers": ["vendors", "svanidhi", "street"],
    "street": ["vendor", "vendors", "svanidhi", "urban"],
    "subsidy": ["grant", "rebate", "subvention", "assistance"],
    "subsidies": ["grants", "rebates", "subventions", "assistance"],
    "grant": ["subsidy", "subvention", "assistance", "fund"],
    "grants": ["subsidies", "subventions", "assistance", "funds"],
    "pension": ["maandhan", "old-age", "vridha", "pensioners", "retiree"],
    "senior": ["old-age", "elderly", "aged", "vridha", "pension"],
    "seniors": ["old-age", "elderly", "aged", "vridha", "pension"],
    "elderly": ["old-age", "senior", "aged", "vridha", "pension"],
    "youth": ["young", "student", "employment", "skill", "internship"],
    "housing": ["awas", "shelter", "pucca", "house", "home"],
    "house": ["housing", "awas", "shelter", "pucca", "home"],
    "houses": ["housing", "awas", "shelter", "pucca", "homes"],
    "homeless": ["shelter", "housing", "awas", "gramin", "urban"],
    "pucca": ["awas", "housing", "shelter", "pucca"],
    "shelter": ["housing", "awas", "pucca", "home"],
    "urban": ["city", "town", "municipal"],
    "rural": ["gramin", "village", "panchayat", "countryside"],
    "gramin": ["rural", "village", "panchayat"],
    "medical": ["health", "hospital", "arogya", "treatment", "ayushman"],
    "healthcare": ["health", "hospital", "arogya", "treatment", "ayushman"],
    "health": ["medical", "hospital", "arogya", "treatment", "ayushman"],
    "hospital": ["medical", "health", "arogya", "treatment", "ayushman"],
    "patient": ["hospital", "health", "medical", "treatment", "arogya"],
    "patients": ["hospital", "health", "medical", "treatment", "arogya"],
    "scholarship": ["education", "matric", "studies", "students", "merit"],
    "scholarships": ["education", "matric", "studies", "students", "merit"],
    "college": ["higher", "university", "graduation", "undergraduate"],
    "student": ["scholarship", "education", "studies", "matric"],
    "students": ["scholarships", "education", "studies", "matric"],
    "disability": ["disabled", "handicapped", "divyang", "special"],
    "disabled": ["disability", "handicapped", "divyang", "special"],
    "obc": ["backward", "classes", "caste"],
    "backward": ["obc", "classes", "caste"],
    "artisan": ["vishwakarma", "craftsman", "craftsperson", "artisan", "karigar"],
    "artisans": ["vishwakarma", "craftsmen", "craftspeople", "artisans", "karigar"],
    "craftsperson": ["vishwakarma", "artisan", "artisans", "craftsman", "karigar"],
    "craftspeople": ["vishwakarma", "artisan", "artisans", "craftsmen", "karigar"],
    "toolkit": ["vishwakarma", "toolkit", "tools"],
    "kheti": ["agriculture", "farmer", "kisan"],
    "krishi": ["agriculture", "farmer", "kisan"]
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
    return set(re.findall(r'\b[a-zA-Z0-9-]{2,}\b', text.lower()))

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
    """Loads scheme metadata, target purposes, and pre-tokenized representations."""
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

    # Add tokenized caches including target_tokens
    for s in schemes:
        doc = s["document"]
        s["clean_content"] = clean_scheme_content(doc)
        s["title_tokens"] = tokenize(s["scheme_name"])
        s["cat_tokens"] = tokenize(s["category"])
        s["body_tokens"] = tokenize(s["clean_content"])

        # Extract target_group_or_purpose
        target_line = ""
        for line in doc.splitlines():
            if line.lower().startswith("target_group_or_purpose:"):
                target_line = line.split(":", 1)[1].strip()
                break
        s["target_tokens"] = tokenize(target_line)

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
        cache_dir = os.environ.get("FASTEMBED_CACHE_PATH") or str(RAG_DIR / ".cache")
        _EMBED_MODEL = TextEmbedding(model_name="sentence-transformers/all-MiniLM-L6-v2", cache_dir=cache_dir)
        return _EMBED_MODEL
    except Exception as e:
        print(f"[Warning] Could not initialize FastEmbed: {e}. Falling back to lexical retrieval.", file=sys.stderr)
        return None

def compute_lexical_scores(query: str, schemes: list[dict]):
    """
    Calculates weighted lexical scores with domain synonym expansion,
    target group coverage, exact title phrase matching, and generic-prefix filtering.
    """
    normalized_q = query.lower().strip()
    raw_tokens = [t for t in re.findall(r'\b[a-zA-Z0-9-]+\b', normalized_q) if len(t) >= 2]
    meaningful_tokens = [t for t in raw_tokens if t not in STOP_WORDS]
    if not meaningful_tokens:
        meaningful_tokens = raw_tokens

    concept_groups = build_concept_groups(meaningful_tokens)

    # Identify informative concept groups (not composed entirely of low-weight generic prefix terms)
    informative_groups_indices = set()
    for idx, group in enumerate(concept_groups):
        if not all(tok in LOW_WEIGHT_TERMS for tok in group):
            informative_groups_indices.add(idx)

    has_informative_groups = len(informative_groups_indices) > 0

    scores = np.zeros(len(schemes), dtype=np.float32)
    matched_terms_map = {}

    for i, s in enumerate(schemes):
        base_score = 0.0
        matched_groups_count = 0
        matched_informative_count = 0
        matched_terms = []
        used_doc_tokens = set()

        scheme_name_lower = s["scheme_name"].lower()
        title_tokens = s["title_tokens"]
        distinctive_title_tokens = title_tokens - LOW_WEIGHT_TERMS - GENERIC_FINANCIAL_TERMS - STOP_WORDS

        # 1. Exact phrase and acronym bonuses
        phrase_bonus = 0.0
        clean_scheme_title = re.sub(r'\s*\([^)]*\)', '', scheme_name_lower).strip()
        if clean_scheme_title and clean_scheme_title in normalized_q:
            phrase_bonus += 45.0
        elif len(distinctive_title_tokens) >= 2 and all(tok in normalized_q for tok in distinctive_title_tokens):
            phrase_bonus += 30.0

        for raw_tok in raw_tokens:
            tok_clean = raw_tok.replace("-", "")
            if tok_clean in ACRONYM_MAP and ACRONYM_MAP[tok_clean] == s["scheme_id"]:
                phrase_bonus += 35.0

        # 2. Concept group matching across structured fields
        for grp_idx, group in enumerate(concept_groups):
            best_group_score = 0.0
            best_token_matched = None
            is_informative = grp_idx in informative_groups_indices

            for t in group:
                if t in used_doc_tokens:
                    continue
                weight_mult = 0.35 if t in LOW_WEIGHT_TERMS else 1.0

                if t in s["title_tokens"]:
                    is_distinctive_title = t in distinctive_title_tokens
                    # Highly reward distinctive title token matches (e.g. mudra, svanidhi, ayushman)
                    score_val = (24.0 if is_distinctive_title else 12.0) * weight_mult
                    best_group_score = score_val
                    best_token_matched = t
                    break
                elif t in s.get("target_tokens", set()):
                    # Crucial priority for matching target_group_or_purpose!
                    score_val = 16.0 * weight_mult
                    best_group_score = score_val
                    best_token_matched = t
                    break
                elif t in s["cat_tokens"]:
                    best_group_score = 8.0 * weight_mult
                    best_token_matched = t
                    break
                elif t in s["body_tokens"]:
                    best_group_score = 5.0 * weight_mult
                    best_token_matched = t
                    break

            if best_group_score > 0:
                base_score += best_group_score
                matched_groups_count += 1
                if is_informative:
                    matched_informative_count += 1
                matched_terms.append(best_token_matched)
                used_doc_tokens.add(best_token_matched)

        # 3. Critical filter: If query contains informative terms, schemes matching ONLY generic prefix terms are noise
        if has_informative_groups and matched_informative_count == 0 and phrase_bonus == 0.0:
            scores[i] = 0.0
            continue

        coverage_ratio = matched_groups_count / len(concept_groups) if concept_groups else 0
        total_score = (base_score + phrase_bonus) * (1.0 + (coverage_ratio * 1.5))

        # Minimum concepts required:
        # Require 2 concepts unless query has only 1 concept OR a multi-token phrase bonus was earned
        min_concepts_needed = 1 if (len(concept_groups) == 1 or phrase_bonus > 0.0) else 2

        if total_score >= 14.0 and matched_groups_count >= min_concepts_needed:
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
        sims = np.dot(vectors, q_vec)
        return sims
    except Exception as e:
        print(f"[Warning] Semantic embedding error: {e}", file=sys.stderr)
        return None

def retrieve_schemes(
    query: str,
    k: int = 5,
    mode: str = "hybrid",
    min_score: float = None,
    relative_threshold: float = None
) -> list[dict]:
    """
    Main scheme retrieval function supporting 'hybrid', 'semantic', and 'lexical' modes.
    Applies configurable relevance thresholds and dynamic relative drop-off pruning
    so that only genuinely relevant schemes are returned without arbitrary list padding.
    """
    schemes = load_scheme_data()
    lex_scores, matched_terms_map = compute_lexical_scores(query, schemes)
    sem_sims = compute_semantic_similarities(query) if mode in ("hybrid", "semantic") else None

    # Fallback to lexical if semantic model is unavailable
    if sem_sims is None and mode in ("hybrid", "semantic"):
        mode = "lexical"

    scored_candidates = []

    # Configurable threshold resolution from args, env, or calibrated defaults
    env_min_score = os.environ.get("RAG_MIN_SCORE")
    env_rel_thresh = os.environ.get("RAG_RELATIVE_THRESHOLD")

    if mode == "lexical":
        min_thresh = min_score or (float(env_min_score) if env_min_score else 18.0)
        rel_thresh = relative_threshold or (float(env_rel_thresh) if env_rel_thresh else 0.50)

        valid_indices = np.where(lex_scores >= min_thresh)[0]
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
        min_thresh = min_score or (float(env_min_score) if env_min_score else 23.5)
        rel_thresh = relative_threshold or (float(env_rel_thresh) if env_rel_thresh else 0.80)

        max_sim = float(np.max(sem_sims))
        if max_sim >= 0.235:
            for idx in range(len(schemes)):
                sim = float(sem_sims[idx])
                score = sim * 100.0
                if score >= min_thresh:
                    s = schemes[idx]
                    dist = round(1.0 - sim, 4)
                    scored_candidates.append({
                        "score": round(score, 1),
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
        min_thresh = min_score or (float(env_min_score) if env_min_score else 22.0)
        rel_thresh = relative_threshold or (float(env_rel_thresh) if env_rel_thresh else 0.55)

        max_sim = float(np.max(sem_sims)) if sem_sims is not None else 0.0
        max_lex = float(np.max(lex_scores))

        # OOD Guard: When neither modality finds sufficient evidence, cleanly return 0 sources
        if max_sim < 0.235 and max_lex < 14.0:
            return []

        # Scale semantic similarity to 0..50
        sem_scaled = np.maximum(0.0, (sem_sims - 0.235) / 0.40 * 50.0)

        for i, s in enumerate(schemes):
            lex = float(lex_scores[i])
            sem = float(sem_scaled[i])
            raw_sim = float(sem_sims[i])
            dist = round(1.0 - raw_sim, 4)

            # Skip candidates with no lexical support and weak semantic signal
            if lex == 0 and raw_sim < 0.35:
                continue

            # Concordance bonus when both modalities support the candidate
            if lex > 0 and sem > 0:
                final_score = (0.55 * lex) + (0.45 * sem) + 10.0
            elif lex > 0:
                final_score = lex * 0.85
            else:
                # Pure semantic match without lexical evidence: only reward if raw_sim is high
                if raw_sim >= 0.45:
                    final_score = (raw_sim * 50.0) - 5.0
                else:
                    final_score = 0.0

            if final_score >= min_thresh:
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

    if not scored_candidates:
        return []

    # Sort descending by score
    scored_candidates.sort(key=lambda d: d["score"], reverse=True)
    top_score = scored_candidates[0]["score"]

    # Dynamic pruning: filter out noise candidates falling far below the top candidate
    if mode == "semantic":
        cutoff = max(min_thresh, top_score * rel_thresh)
    elif top_score >= 60.0:
        cutoff = max(min_thresh, top_score * rel_thresh)
    else:
        cutoff = max(min_thresh, top_score * 0.45)

    pruned = [c for c in scored_candidates if c["score"] >= cutoff]
    return pruned[:k]

def retrieve_schemes_from_chroma(query: str, k: int = 5, db_path: Path = CHROMA_DB_PATH):
    """Backward-compatible entry point preserving earlier signature."""
    return retrieve_schemes(query, k=k, mode="hybrid")

def generate_answer_with_groq(query: str, contexts: list[dict], api_key: str = None) -> tuple[str, str]:
    """Generates grounded RAG response using Groq cloud inference (if configured)."""
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
    parser.add_argument("--min-score", type=float, default=None, help="Minimum score threshold")
    parser.add_argument("--relative-threshold", type=float, default=None, help="Relative score drop-off threshold")
    parser.add_argument("--stdin", action="store_true", help="Read JSON payload from stdin")

    args = parser.parse_args()

    min_score = args.min_score
    relative_threshold = args.relative_threshold

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
                if "min_score" in payload:
                    min_score = float(payload["min_score"])
                elif "relevance_threshold" in payload:
                    min_score = float(payload["relevance_threshold"])
                if "relative_threshold" in payload:
                    relative_threshold = float(payload["relative_threshold"])
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
        retrieved_docs = retrieve_schemes(
            query,
            k=k,
            mode=mode,
            min_score=min_score,
            relative_threshold=relative_threshold
        )

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

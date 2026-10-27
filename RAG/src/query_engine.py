#!/usr/bin/env python3
"""
JANSAARTHI AI - Government Scheme Retrieval and RAG Query Engine
Interrogates the Chroma SQLite database for matching government schemes
and synthesizes grounded answers using Groq LLM.
"""

import sys
import os
import json
import sqlite3
import re
import argparse
from pathlib import Path

# Resolve project directories
RAG_DIR = Path(__file__).resolve().parents[1]
CHROMA_DB_PATH = RAG_DIR / "chroma_db" / "chroma.sqlite3"

# Load environment variables safely
try:
    from dotenv import load_dotenv
    load_dotenv(RAG_DIR / ".env")
    load_dotenv(RAG_DIR.parent / "backend" / ".env")
except ImportError:
    pass

def retrieve_schemes_from_chroma(query: str, k: int = 5, db_path: Path = CHROMA_DB_PATH):
    """
    Retrieves matching schemes from the local Chroma database.
    Ranks documents based on keyword/token matches in title, category, purpose, and body.
    """
    if not db_path.exists():
        raise FileNotFoundError(f"Chroma SQLite database not found at: {db_path}")

    # Extract alphanumeric tokens of length >= 3
    tokens = [t.lower() for t in re.findall(r'[a-zA-Z0-9]+', query) if len(t) >= 3]
    # Filter common stop words
    stop_words = {'what', 'when', 'where', 'which', 'who', 'whom', 'this', 'that', 'these',
                  'those', 'am', 'is', 'are', 'was', 'were', 'be', 'been', 'being', 'have',
                  'has', 'had', 'having', 'do', 'does', 'did', 'doing', 'a', 'an', 'the',
                  'and', 'but', 'if', 'or', 'because', 'as', 'until', 'while', 'of', 'at',
                  'by', 'for', 'with', 'about', 'against', 'between', 'into', 'through',
                  'during', 'before', 'after', 'above', 'below', 'to', 'from', 'up', 'down',
                  'in', 'out', 'on', 'off', 'over', 'under', 'again', 'further', 'then',
                  'once', 'here', 'there', 'all', 'any', 'both', 'each', 'few', 'more',
                  'most', 'other', 'some', 'such', 'no', 'nor', 'not', 'only', 'own',
                  'same', 'so', 'than', 'too', 'very', 'can', 'will', 'just', 'should',
                  'now', 'tell', 'me', 'about', 'explain', 'schemes', 'scheme', 'government',
                  'sarkari', 'yojana'}
    meaningful_tokens = [t for t in tokens if t not in stop_words]
    if not meaningful_tokens:
        meaningful_tokens = tokens or ['yojana']

    conn = sqlite3.connect(str(db_path))
    cur = conn.cursor()

    # Query all schemes from the jansaarthi_schemes collection
    rows = cur.execute("""
        SELECT
            e.embedding_id,
            MAX(CASE WHEN em.key = 'scheme_id' THEN em.string_value END) as scheme_id,
            MAX(CASE WHEN em.key = 'scheme_name' THEN em.string_value END) as scheme_name,
            MAX(CASE WHEN em.key = 'category' THEN em.string_value END) as category,
            MAX(CASE WHEN em.key = 'source_platform' THEN em.string_value END) as source_platform,
            MAX(CASE WHEN em.key = 'chroma:document' THEN em.string_value END) as document
        FROM collections c
        JOIN segments s ON c.id = s.collection
        JOIN embeddings e ON s.id = e.segment_id
        JOIN embedding_metadata em ON e.id = em.id
        WHERE c.name = 'jansaarthi_schemes'
        GROUP BY e.embedding_id
    """).fetchall()

    conn.close()

    # Score each document based on term matches with title/category weighting
    scored_docs = []
    for row in rows:
        embedding_id, scheme_id, scheme_name, category, source_platform, document = row
        if not document:
            continue

        doc_lower = (document or "").lower()
        title_lower = (scheme_name or "").lower()
        cat_lower = (category or "").lower()

        score = 0
        matched_terms = []
        for token in meaningful_tokens:
            if token in title_lower:
                score += 10
                matched_terms.append(token)
            elif token in cat_lower:
                score += 5
                matched_terms.append(token)
            elif token in doc_lower:
                score += 2
                matched_terms.append(token)

        if score > 0:
            scored_docs.append({
                "score": score,
                "schemeId": scheme_id or embedding_id,
                "schemeName": scheme_name or "Unknown Scheme",
                "category": category or "General",
                "sourcePlatform": source_platform or "myScheme.gov.in",
                "content": document,
                "matchedTerms": list(set(matched_terms))
            })

    # Sort descending by relevance score
    scored_docs.sort(key=lambda d: d["score"], reverse=True)
    return scored_docs[:k]

def generate_answer_with_groq(query: str, contexts: list[dict], api_key: str = None) -> tuple[str, str]:
    """
    Generates a grounded RAG response using Groq cloud inference.
    Returns (answer, status).
    """
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

        # Primary model selection with safe fallback
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
    parser.add_argument("--stdin", action="store_true", help="Read JSON payload from stdin")

    args = parser.parse_args()

    if args.stdin:
        try:
            raw = sys.stdin.read().strip()
            if raw:
                payload = json.loads(raw)
                query = payload.get("query", "").strip()
                k = int(payload.get("k", 5))
            else:
                query = ""
                k = 5
        except Exception as e:
            print(json.dumps({"status": "error", "message": f"Invalid JSON stdin: {str(e)}"}), file=sys.stdout)
            sys.exit(1)
    else:
        query = args.query.strip()
        k = args.k

    if not query:
        print(json.dumps({"status": "error", "code": "EMPTY_QUERY", "message": "Query cannot be empty."}), file=sys.stdout)
        sys.exit(0)

    try:
        retrieved_docs = retrieve_schemes_from_chroma(query, k=k)

        if not retrieved_docs:
            response = {
                "status": "success",
                "query": query,
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
        sources = [
            {
                "schemeId": doc["schemeId"],
                "schemeName": doc["schemeName"],
                "category": doc["category"],
                "sourcePlatform": doc["sourcePlatform"],
                "snippet": doc["content"][:300].strip() + ("..." if len(doc["content"]) > 300 else "")
            }
            for doc in retrieved_docs
        ]

        response = {
            "status": "success",
            "query": query,
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

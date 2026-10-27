"""
Builds a persistent, portable ONNX-based semantic vector index for the 100 government schemes.
Uses FastEmbed with model: sentence-transformers/all-MiniLM-L6-v2 (384-dimensional).
The index is saved in RAG/semantic_index/ so the original RAG/chroma_db/ is NOT overwritten.
"""

import json
import sqlite3
import numpy as np
from pathlib import Path
from fastembed import TextEmbedding

RAG_DIR = Path(__file__).resolve().parents[1]
CHROMA_DB = RAG_DIR / "chroma_db" / "chroma.sqlite3"
INDEX_DIR = RAG_DIR / "semantic_index"

def build_index():
    INDEX_DIR.mkdir(parents=True, exist_ok=True)
    
    conn = sqlite3.connect(str(CHROMA_DB))
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
    documents = []
    for r in rows:
        scheme_id = r[1] or r[0].replace("scheme-", "")
        scheme_name = r[2] or "Unknown Scheme"
        category = r[3] or "General"
        source_platform = r[4] or "myScheme.gov.in"
        document = r[5] or ""
        
        schemes.append({
            "embedding_id": r[0],
            "scheme_id": scheme_id,
            "scheme_name": scheme_name,
            "category": category,
            "source_platform": source_platform,
            "document": document
        })
        documents.append(document)

    print(f"Loaded {len(schemes)} schemes from SQLite metadata.")
    
    print("Generating embeddings using FastEmbed (sentence-transformers/all-MiniLM-L6-v2)...")
    embed_model = TextEmbedding(model_name="sentence-transformers/all-MiniLM-L6-v2")
    vectors = list(embed_model.embed(documents))
    
    matrix = np.array(vectors, dtype=np.float32)
    # L2 normalize so cosine similarity is simply dot product
    norms = np.linalg.norm(matrix, axis=1, keepdims=True)
    matrix = matrix / np.maximum(norms, 1e-12)

    vec_path = INDEX_DIR / "scheme_embeddings.npy"
    meta_path = INDEX_DIR / "scheme_metadata.json"
    
    np.save(str(vec_path), matrix)
    with open(meta_path, "w", encoding="utf-8") as f:
        json.dump(schemes, f, indent=2, ensure_ascii=False)
        
    print(f"[SUCCESS] Built semantic index:")
    print(f"  Vectors: {vec_path} ({matrix.shape}, {vec_path.stat().st_size} bytes)")
    print(f"  Metadata: {meta_path} ({len(schemes)} records, {meta_path.stat().st_size} bytes)")

if __name__ == "__main__":
    build_index()

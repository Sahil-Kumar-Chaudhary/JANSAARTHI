from pathlib import Path
from langchain_chroma import Chroma
from .loader import load_data
from .splitter import splitter
from .embeddings import generate_embedding

RAG_DIR = Path(__file__).resolve().parents[1]
KNOWLEDGE_BASE_PATH = RAG_DIR / "knowledge_base" / "data.txt"
CHROMA_PATH = RAG_DIR / "chroma_db"

def create_vectore_store():
    docx = load_data(str(KNOWLEDGE_BASE_PATH))
    chunks = splitter(docx)
    embedding = generate_embedding()

    vc = Chroma.from_documents(
        documents=chunks,
        embedding=embedding,
        persist_directory=str(CHROMA_PATH)
    )

    return vc

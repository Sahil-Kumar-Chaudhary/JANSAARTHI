
from pathlib import Path
from langchain_chroma import Chroma
from .embeddings import generate_embedding

RAG_DIR = Path(__file__).resolve().parents[1]
CHROMA_PATH = RAG_DIR / "chroma_db"


def retriver():
    # Keep the existing GitHub/data.txt RAG behavior
    from .chroma import create_vectore_store

    return create_vectore_store().as_retriever(
        search_type="similarity",
        search_kwargs={"k": 5}
    )


def scheme_retriver():
    # Retrieve government schemes from the separate collection
    vector_store = Chroma(
        collection_name="jansaarthi_schemes",
        embedding_function=generate_embedding(),
        persist_directory=str(CHROMA_PATH),
    )

    return vector_store.as_retriever(
        search_type="similarity",
        search_kwargs={"k": 5}
    )

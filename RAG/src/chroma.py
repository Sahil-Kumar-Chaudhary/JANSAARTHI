from langchain_chroma import Chroma
from loader import load_data
from splitter import splitter
from embeddings import generate_embedding

def create_vectore_store():
    docx = load_data("/home/s/Desktop/JANSAARTHI/RAG/knowledge_base/data.txt")
    chunks = splitter(docx)
    embedding = generate_embedding()

    vc = Chroma.from_documents(
        documents=chunks,
        embedding=embedding
    )

    return vc
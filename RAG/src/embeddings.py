from langchain_huggingface import HuggingFaceEmbeddings

def generate_embedding():
    embedding = HuggingFaceEmbeddings(
        model_name="sentence-transformers/all-MiniLM-L6-v2"
    )
    return embedding

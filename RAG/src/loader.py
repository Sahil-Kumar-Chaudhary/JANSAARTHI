from langchain_community.document_loaders import TextLoader

def load_data(path: str):
    data = TextLoader(path)
    return data.load()

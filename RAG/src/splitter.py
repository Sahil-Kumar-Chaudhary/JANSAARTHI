from langchain_text_splitters import RecursiveCharacterTextSplitter

def splitter(loader_data):
    data = RecursiveCharacterTextSplitter(
        chunk_size=1000, chunk_overlap=200
    )
    return data.split_documents(loader_data)
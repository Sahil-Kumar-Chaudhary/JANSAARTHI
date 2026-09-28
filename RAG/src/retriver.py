from .chroma import create_vectore_store

def retriver():
    retriver_data = create_vectore_store().as_retriever(
        search_type ="similarity",
        search_kwargs={"k":5}
    )
    return retriver_data


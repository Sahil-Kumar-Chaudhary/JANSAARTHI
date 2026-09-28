from .retriver import retriver
from .generator import generate


def rag_with_context(query: str):

    retriever_model = retriver()

    documents = retriever_model.invoke(query)

    context = [
        document.page_content
        for document in documents
    ]

    answer = generate(
        query,
        context
    )

    return answer, context

from .retriver import scheme_retriver
from .generator import generate


def scheme_rag_with_context(query: str):
    retriever_model = scheme_retriver()
    documents = retriever_model.invoke(query)

    context = [document.page_content for document in documents]
    answer = generate(query, context)

    return answer, context

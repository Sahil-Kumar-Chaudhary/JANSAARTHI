from .retriver import retriver
from .generator import generate


def rag(query):
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

    return answer


def run():
    ans = rag("what is git and github what is teh difference")
    print(ans)


run()
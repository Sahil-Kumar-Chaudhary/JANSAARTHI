from dotenv import load_dotenv
from langchain.chat_models import init_chat_model
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

load_dotenv()


llm = init_chat_model(
    model="openai/gpt-oss-20b",
    model_provider="groq",
    temperature=0
)


prompt = ChatPromptTemplate.from_template(
    """
You are a helpful question-answering assistant.

Answer the question using only the information provided in the context.

Rules:
- Do not use outside knowledge.
- Do not make up information.
- If the context does not contain enough information to answer the question, say:
"I don't have enough information in the provided context to answer that."
- Answer all parts of the question when possible.
- Keep the answer clear and concise.

<context>
{context}
</context>

<question>
{question}
</question>

Answer:
"""
)


chain = prompt | llm | StrOutputParser()


def generate(query: str, context: list[str]) -> str:
    context_text = "\n\n".join(context)

    return chain.invoke(
        {
            "question": query,
            "context": context_text
        }
    )


if __name__ == "__main__":
    context = [
        """
        Git is a distributed version control system.
        It is used to track changes in source code.
        """
    ]

    question = "What is Git and explain?"

    answer = generate(question, context)

    print(answer)
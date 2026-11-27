
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from src.scheme_rag_pipeline import scheme_rag_with_context

app = FastAPI(
    title="JANSAARTHI AI API",
    description="Government scheme assistant powered by RAG",
    version="1.0.0",
)


class ChatRequest(BaseModel):
    question: str


class ChatResponse(BaseModel):
    answer: str
    sources: list[str]


@app.get("/")
def home():
    return {"message": "JANSAARTHI AI API is running"}


@app.post("/chat", response_model=ChatResponse)
def chat(request: ChatRequest):
    question = request.question.strip()

    if not question:
        raise HTTPException(
            status_code=400,
            detail="Question cannot be empty",
        )

    try:
        answer, context = scheme_rag_with_context(question)

        sources = list(dict.fromkeys(
            line.split(":", 1)[1].strip()
            for document in context
            for line in document.splitlines()
            if line.startswith("scheme_name:")
        ))

        return ChatResponse(
            answer=str(answer),
            sources=sources,
        )

    except Exception:
        raise HTTPException(
            status_code=500,
            detail="Unable to generate an answer. Please try again.",
        )

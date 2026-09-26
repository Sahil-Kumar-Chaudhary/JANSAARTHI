import os
import json
from dotenv import load_dotenv
from groq import Groq

from deepeval import evaluate
from deepeval.test_case import LLMTestCase
from deepeval.metrics import ContextualRecallMetric, ContextualPrecisionMetric
from deepeval.models.base_model import DeepEvalBaseLLM

load_dotenv()

from src.retriver import retriver

class GroqJudge(DeepEvalBaseLLM):
    def __init__(self, model_name: str = "openai/gpt-oss-20b"):
        self.model_name = model_name
        self.client = Groq(
            api_key=os.environ.get("GROQ_API_KEY")
        )

    def load_model(self):
        return self.client

    def generate(self, prompt: str, schema=None) -> str:
        client = self.load_model()

        response_format = (
            {"type": "json_object"}
            if schema
            else None
        )

        chat_completion = client.chat.completions.create(
            model=self.model_name,
            messages=[
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            response_format=response_format
        )

        return chat_completion.choices[0].message.content

    async def a_generate(self, prompt: str, schema=None) -> str:
        return self.generate(prompt, schema)

    def get_model_name(self):
        return self.model_name


GOLDEN_PATH = ("/home/s/Desktop/JANSAARTHI/RAG/goldens/retriever_deepeval_goldens.json")
TXT_PATH = ("/home/s/Desktop/JANSAARTHI/RAG/knowledge_base/data.txt")

THRESHOLD = 0.7

# Groq LLM used as DeepEval judge
GROQ_JUDGE = GroqJudge(
    model_name="openai/gpt-oss-20b"
)

retriever = retriver()

# Load golden dataset
with open(GOLDEN_PATH, "r", encoding="utf-8") as f:
    goldens = json.load(f)

test_cases = []

# Run retrieval for every golden question
for g in goldens:

    retrieved = retriever.invoke(
        g["query"]
    )

    retrieval_context = [
        doc.page_content
        for doc in retrieved
    ]

    test_cases.append(
        LLMTestCase(
            input=g["query"],
            expected_output=g["ideal_answer"],
            retrieval_context=retrieval_context,
            expected_retrieval_context=g["context"],
            actual_output="(generator not evaluated in this run)",
        )
    )


# DeepEval metrics
metrics = [
    ContextualRecallMetric(
        threshold=THRESHOLD,
        model=GROQ_JUDGE,
        include_reason=True
    ),

    ContextualPrecisionMetric(
        threshold=THRESHOLD,
        model=GROQ_JUDGE,
        include_reason=True
    ),
]


# Run evaluation
evaluate(
    test_cases=test_cases,
    metrics=metrics,
    hyperparameters={
        "retriever": "base_k3",
        "embedding_model": "HuggingFace",
        "chunk_size": 1000,
        "chunk_overlap": 150,
        "top_k": 3,
        "judge_model": GROQ_JUDGE.get_model_name(),
        "golden_set": GOLDEN_PATH,
    },
)
import os
import json

from dotenv import load_dotenv
from groq import Groq

from deepeval import evaluate
from deepeval.test_case import LLMTestCase
from deepeval.metrics import (
    ContextualRecallMetric,
    ContextualPrecisionMetric
)
from deepeval.models.base_model import DeepEvalBaseLLM

load_dotenv()

from src.retriver import retriver


class GroqJudge(DeepEvalBaseLLM):

    def __init__(
        self,
        # llama-3.3-70b-versatile was deprecated/shut down by Groq on
        # 2026-08-16. openai/gpt-oss-120b is Groq's recommended
        # replacement, and being 6x larger than gpt-oss-20b it should
        # also be far less prone to the verdict-repetition loop.
        model_name: str = "openai/gpt-oss-120b"
    ):
        self.model_name = model_name

        self.client = Groq(
            api_key=os.environ.get("GROQ_API_KEY")
        )

    def load_model(self):
        return self.client

    def _fix_schema(self, schema):
        """
        Groq strict JSON schema requires
        additionalProperties=false on every object.
        """
        if isinstance(schema, dict):
            if schema.get("type") == "object":
                schema["additionalProperties"] = False

            for key, value in schema.items():
                if isinstance(value, dict):
                    self._fix_schema(value)
                elif isinstance(value, list):
                    for item in value:
                        if isinstance(item, dict):
                            self._fix_schema(item)

        return schema

    def _call(self, prompt: str, schema, temperature: float) -> str:
        """One raw call to Groq. Split out so generate() can retry
        it with a different temperature without duplicating the
        request-building logic."""

        if schema:
            json_schema = schema.model_json_schema()
            json_schema = self._fix_schema(json_schema)

            response_format = {
                "type": "json_schema",
                "json_schema": {
                    "name": "deepeval_output",
                    "schema": json_schema,
                    "strict": True,
                },
            }
        else:
            response_format = None

        chat_completion = self.client.chat.completions.create(
            model=self.model_name,
            messages=[{"role": "user", "content": prompt}],
            response_format=response_format,
            temperature=temperature,
            max_tokens=2048,
        )

        return chat_completion.choices[0].message.content

    def generate(self, prompt: str, schema=None) -> str:
        """Retries once, at a nonzero temperature, if the first
        attempt comes back as invalid/empty JSON. temperature=0 is
        deterministic, so retrying at 0 would just reproduce the
        same empty/looping response."""
        import time

        last_error = None

        for attempt, temperature in enumerate([0, 0.4]):
            try:
                raw = self._call(prompt, schema, temperature)

                if schema:
                    if not raw or not raw.strip():
                        raise ValueError("Empty response from judge model")
                    json.loads(raw)

                return raw

            except Exception as e:
                last_error = e
                if attempt == 0:
                    time.sleep(1)
                    continue
                raise last_error

    async def a_generate(self, prompt: str, schema=None) -> str:
        return self.generate(prompt, schema)

    def get_model_name(self):
        return self.model_name


# --------------------------------------------------
# Configuration
# --------------------------------------------------

GOLDEN_PATH = (
    "/home/s/Desktop/JANSAARTHI/RAG/"
    "evals/goldens/retriever_deepeval_goldens.json"
)

THRESHOLD = 0.7


# --------------------------------------------------
# Initialize judge
# --------------------------------------------------

# IMPORTANT: no model_name passed here anymore -- this now uses the
# class default (llama-3.3-70b-versatile) instead of overriding it
# back to the 20B model.
GROQ_JUDGE = GroqJudge()


# --------------------------------------------------
# Initialize retriever
# --------------------------------------------------

retriever = retriver()


# --------------------------------------------------
# Load golden dataset
# --------------------------------------------------

with open(
    GOLDEN_PATH,
    "r",
    encoding="utf-8"
) as f:

    goldens = json.load(f)


print(
    f"Loaded {len(goldens)} golden test cases."
)


# --------------------------------------------------
# Create DeepEval test cases
# --------------------------------------------------

test_cases = []


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

            # Actual chunks returned by retriever
            retrieval_context=retrieval_context,

            # Expected chunk from golden dataset
            expected_retrieval_context=[
                g["source"]["text"]
            ],

            # Generator is not being evaluated
            actual_output=(
                "(generator not evaluated "
                "in this run)"
            )
        )
    )


# --------------------------------------------------
# Metrics
# --------------------------------------------------

metrics = [

    ContextualRecallMetric(
        threshold=THRESHOLD,
        model=GROQ_JUDGE,
        # include_reason=True
    ),

    ContextualPrecisionMetric(
        threshold=THRESHOLD,
        model=GROQ_JUDGE,
        # include_reason=True
    )

]


# --------------------------------------------------
# Run evaluation
# --------------------------------------------------

evaluate(
    test_cases=test_cases,

    metrics=metrics,

    hyperparameters={
        "retriever": "base_k3",
        "embedding_model": "HuggingFace",
        "chunk_size": 1000,
        "chunk_overlap": 150,
        "top_k": 3,
        "judge_model": (
            GROQ_JUDGE.get_model_name()
        ),
        "golden_set": GOLDEN_PATH,
    }
)
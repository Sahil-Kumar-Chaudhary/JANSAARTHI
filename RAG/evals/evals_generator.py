import os
import json

from dotenv import load_dotenv
from groq import Groq

from deepeval import evaluate
from deepeval.test_case import LLMTestCase
from deepeval.metrics import (
    FaithfulnessMetric,
    AnswerRelevancyMetric
)
from deepeval.models.base_model import DeepEvalBaseLLM

from src.generator import generate


load_dotenv(override=True)


class GroqJudge(DeepEvalBaseLLM):

    def __init__(
        self,
        model_name="openai/gpt-oss-20b"
    ):
        self.model_name = model_name

        self.client = Groq(
            api_key=os.getenv("GROQ_API_KEY")
        )

    def load_model(self):
        return self.client

    def generate(self, prompt, schema=None):
        """
        NOTE: deliberately NOT using response_format="json_schema"
        with strict=True here. Groq's constrained decoder for
        openai/gpt-oss-20b does not reliably enforce optional/
        nullable fields under strict mode (confirmed: both the
        type-array and anyOf-null patterns failed identically), so
        fighting that at the schema level is a dead end for this
        model.

        Plain "json_object" mode removes that rigid grammar. The
        model instead follows DeepEval's own prompt text (which
        already describes the exact JSON shape it wants), and
        DeepEval's Pydantic parsing on the other end already treats
        fields like "reason" as Optional[str] = None -- so a missing
        key parses fine without needing to be forced into existence.
        """

        response_format = (
            {"type": "json_object"}
            if schema
            else None
        )

        response = self.client.chat.completions.create(
            model=self.model_name,
            messages=[
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            response_format=response_format,
            temperature=0
        )

        return response.choices[0].message.content

    async def a_generate(self, prompt, schema=None):

        return self.generate(
            prompt,
            schema
        )

    def get_model_name(self):

        return self.model_name


GOLDEN_PATH = "/home/s/Desktop/JANSAARTHI/RAG/evals/goldens/generator_golden.json"
THRESHOLD = 0.7


judge = GroqJudge(
    model_name="openai/gpt-oss-20b"
)


def load_goldens():

    with open(
        GOLDEN_PATH,
        "r",
        encoding="utf-8"
    ) as f:

        return json.load(f)


def main():

    goldens = load_goldens()

    print(
        f"Loaded {len(goldens)} golden test cases."
    )

    test_cases = []

    for golden in goldens:

        query = golden["query"]

        context = golden["ideal_context"]

        actual_output = generate(
            query,
            context
        )

        test_cases.append(
            LLMTestCase(
                input=query,
                actual_output=actual_output,
                retrieval_context=context
            )
        )

    metrics = [

        FaithfulnessMetric(
            threshold=THRESHOLD,
            model=judge,
            include_reason=False
        ),

        AnswerRelevancyMetric(
            threshold=THRESHOLD,
            model=judge,
            include_reason=False
        )
    ]

    print("\nRunning generator evaluation...\n")

    evaluate(
        test_cases=test_cases,
        metrics=metrics
    )


if __name__ == "__main__":

    main()
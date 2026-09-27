# import os
# import json
# from dotenv import load_dotenv
# from groq import Groq

# from deepeval import evaluate
# from deepeval.test_case import LLMTestCase
# from deepeval.metrics import ContextualRecallMetric, ContextualPrecisionMetric
# from deepeval.models.base_model import DeepEvalBaseLLM

# load_dotenv()

# from src.retriver import retriver

# class GroqJudge(DeepEvalBaseLLM):
#     def __init__(self, model_name: str = "openai/gpt-oss-20b"):
#         self.model_name = model_name
#         self.client = Groq(
#             api_key=os.environ.get("GROQ_API_KEY")
#         )

#     def load_model(self):
#         return self.client

#     def generate(self, prompt: str, schema=None) -> str:
#         client = self.load_model()

#         response_format = (
#             {"type": "json_object"}
#             if schema
#             else None
#         )

#         chat_completion = client.chat.completions.create(
#             model=self.model_name,
#             messages=[
#                 {
#                     "role": "user",
#                     "content": prompt
#                 }
#             ],
#             response_format=response_format
#         )

#         return chat_completion.choices[0].message.content

#     async def a_generate(self, prompt: str, schema=None) -> str:
#         return self.generate(prompt, schema)

#     def get_model_name(self):
#         return self.model_name


# GOLDEN_PATH = ("/home/s/Desktop/JANSAARTHI/RAG/evals/goldens/retriever_deepeval_goldens.json")
# TXT_PATH = ("/home/s/Desktop/JANSAARTHI/RAG/knowledge_base/data.txt")

# THRESHOLD = 0.7

# # Groq LLM used as DeepEval judge
# GROQ_JUDGE = GroqJudge(
#     model_name="openai/gpt-oss-20b"
# )

# retriever = retriver()

# # Load golden dataset
# with open(GOLDEN_PATH, "r", encoding="utf-8") as f:
#     goldens = json.load(f)

# test_cases = []

# # Run retrieval for every golden question
# for g in goldens:

#     retrieved = retriever.invoke(
#         g["query"]
#     )

#     retrieval_context = [
#         doc.page_content
#         for doc in retrieved
#     ]

#     test_cases.append(
#         LLMTestCase(
#             input=g["query"],
#             expected_output=g["ideal_answer"],
#             retrieval_context=retrieval_context,
#             expected_retrieval_context=[g["source"]["text"]],
#             actual_output="(generator not evaluated in this run)",
#         )
#     )


# # DeepEval metrics
# metrics = [
#     ContextualRecallMetric(
#         threshold=THRESHOLD,
#         model=GROQ_JUDGE,
#         include_reason=True
#     ),

#     ContextualPrecisionMetric(
#         threshold=THRESHOLD,
#         model=GROQ_JUDGE,
#         include_reason=True
#     ),
# ]


# # Run evaluation
# evaluate(
#     test_cases=test_cases,
#     metrics=metrics,
#     hyperparameters={
#         "retriever": "base_k3",
#         "embedding_model": "HuggingFace",
#         "chunk_size": 1000,
#         "chunk_overlap": 150,
#         "top_k": 3,
#         "judge_model": GROQ_JUDGE.get_model_name(),
#         "golden_set": GOLDEN_PATH,
#     },
# )
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
        model_name: str = "openai/gpt-oss-20b"
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

            # Every object must explicitly disable
            # additional properties.
            if schema.get("type") == "object":
                schema["additionalProperties"] = False

            # Recursively fix nested schemas.
            for key, value in schema.items():
                if isinstance(value, dict):
                    self._fix_schema(value)

                elif isinstance(value, list):
                    for item in value:
                        if isinstance(item, dict):
                            self._fix_schema(item)

        return schema

    def generate(
        self,
        prompt: str,
        schema=None
    ) -> str:

        client = self.load_model()

        if schema:

            json_schema = schema.model_json_schema()

            # Fix DeepEval's schema for Groq
            json_schema = self._fix_schema(
                json_schema
            )

            response_format = {
                "type": "json_schema",
                "json_schema": {
                    "name": "deepeval_output",
                    "schema": json_schema,
                    "strict": True
                }
            }

        else:
            response_format = None

        chat_completion = client.chat.completions.create(
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

        return chat_completion.choices[0].message.content

    async def a_generate(
        self,
        prompt: str,
        schema=None
    ) -> str:

        return self.generate(
            prompt,
            schema
        )

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

GROQ_JUDGE = GroqJudge(
    model_name="openai/gpt-oss-20b"
)


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
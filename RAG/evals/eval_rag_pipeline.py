
# import os

# from dotenv import load_dotenv
# from groq import Groq, AsyncGroq

# from deepeval import evaluate
# from deepeval.test_case import LLMTestCase
# from deepeval.models import DeepEvalBaseLLM

# from deepeval.metrics import (
#     ContextualRelevancyMetric,
#     FaithfulnessMetric,
#     AnswerRelevancyMetric
# )

# from src.rag_pipeline import rag_with_context


# load_dotenv(override=True)


# # ============================================
# # GROQ JUDGE FOR DEEPEVAL
# # ============================================

# class GroqJudge(DeepEvalBaseLLM):

#     def __init__(self):
#         self.model_name = "openai/gpt-oss-20b"

#         self.client = Groq(
#             api_key=os.getenv("GROQ_API_KEY")
#         )

#         self.async_client = AsyncGroq(
#             api_key=os.getenv("GROQ_API_KEY")
#         )

#     def load_model(self):
#         return self.client

#     def generate(self, prompt: str, schema=None):

#         response = self.client.chat.completions.create(
#             model=self.model_name,
#             messages=[
#                 {
#                     "role": "user",
#                     "content": prompt
#                 }
#             ],
#             temperature=0
#         )

#         return response.choices[0].message.content

#     async def a_generate(self, prompt: str, schema=None):

#         response = await self.async_client.chat.completions.create(
#             model=self.model_name,
#             messages=[
#                 {
#                     "role": "user",
#                     "content": prompt
#                 }
#             ],
#             temperature=0
#         )

#         return response.choices[0].message.content

#     def get_model_name(self):
#         return self.model_name

# # Create one Groq judge
# groq_judge = GroqJudge()


# # ============================================
# # TEST QUESTIONS
# # ============================================

# questions = [
#     "What is Git?",
#     "What is GitHub?",
#     "What is the difference between Git and GitHub?",
#     "What is a Git repository?",
#     "What is a Git commit?"
# ]


# # ============================================
# # CREATE DEEPEVAL TEST CASES
# # ============================================

# test_cases = []


# for question in questions:

#     answer, context = rag_with_context(question)

#     test_case = LLMTestCase(
#         input=question,
#         actual_output=answer,
#         retrieval_context=context
#     )

#     test_cases.append(test_case)


# # ============================================
# # RAG TRIAD METRICS
# # ============================================

# contextual_relevancy = ContextualRelevancyMetric(
#     threshold=0.7,
#     model=groq_judge,
#     include_reason=True
# )


# faithfulness = FaithfulnessMetric(
#     threshold=0.7,
#     model=groq_judge,
#     include_reason=True
# )


# answer_relevancy = AnswerRelevancyMetric(
#     threshold=0.7,
#     model=groq_judge,
#     include_reason=True
# )


# # ============================================
# # RUN EVALUATION
# # ============================================

# evaluate(
#     test_cases=test_cases,
#     metrics=[
#         contextual_relevancy,
#         faithfulness,
#         answer_relevancy
#     ]
# )
import os

# ============================================
# NEW: DeepEval time budgets (must be set BEFORE importing deepeval)
# The free Groq tier is slow (8k tokens/min), so give DeepEval
# much more time before it gives up with a TimeoutError.
# ============================================
os.environ["DEEPEVAL_PER_TASK_TIMEOUT_SECONDS_OVERRIDE"] = "3600"     # total budget per task, incl. retries
os.environ["DEEPEVAL_PER_ATTEMPT_TIMEOUT_SECONDS_OVERRIDE"] = "600"   # budget for one judge call, incl. my 429 waits
os.environ["DEEPEVAL_TASK_GATHER_BUFFER_SECONDS_OVERRIDE"] = "60"

import time
import asyncio

from dotenv import load_dotenv
from groq import Groq, AsyncGroq, RateLimitError

from deepeval import evaluate
from deepeval.evaluate import AsyncConfig
from deepeval.test_case import LLMTestCase
from deepeval.models import DeepEvalBaseLLM

from deepeval.metrics import (
    ContextualRelevancyMetric,
    FaithfulnessMetric,
    AnswerRelevancyMetric
)

from src.rag_pipeline import rag_with_context


load_dotenv(override=True)


# ============================================
# GROQ JUDGE FOR DEEPEVAL
# ============================================

MAX_RETRIES = 8          # how many times to retry on a 429
BASE_WAIT_SECONDS = 4    # wait grows: 4s, 8s, 12s, ...


def _is_daily_limit(error: Exception) -> bool:
    """A per-day limit won't recover in seconds, so don't retry it."""
    return "per day" in str(error).lower() or "(TPD)" in str(error)


class GroqJudge(DeepEvalBaseLLM):

    def __init__(self):
        self.model_name = "openai/gpt-oss-20b"

        self.client = Groq(
            api_key=os.getenv("GROQ_API_KEY")
        )

        self.async_client = AsyncGroq(
            api_key=os.getenv("GROQ_API_KEY")
        )

    def load_model(self):
        return self.client

    def generate(self, prompt: str, schema=None):

        for attempt in range(MAX_RETRIES):
            try:
                response = self.client.chat.completions.create(
                    model=self.model_name,
                    messages=[
                        {
                            "role": "user",
                            "content": prompt
                        }
                    ],
                    temperature=0
                )
                return response.choices[0].message.content

            except RateLimitError as e:
                if _is_daily_limit(e) or attempt == MAX_RETRIES - 1:
                    raise
                time.sleep(BASE_WAIT_SECONDS * (attempt + 1))

    async def a_generate(self, prompt: str, schema=None):

        for attempt in range(MAX_RETRIES):
            try:
                response = await self.async_client.chat.completions.create(
                    model=self.model_name,
                    messages=[
                        {
                            "role": "user",
                            "content": prompt
                        }
                    ],
                    temperature=0
                )
                return response.choices[0].message.content

            except RateLimitError as e:
                if _is_daily_limit(e) or attempt == MAX_RETRIES - 1:
                    raise
                await asyncio.sleep(BASE_WAIT_SECONDS * (attempt + 1))

    def get_model_name(self):
        return self.model_name


# Create one Groq judge
groq_judge = GroqJudge()


# ============================================
# TEST QUESTIONS
# ============================================

questions = [
    "What is Git?",
    "What is GitHub?",
    "What is the difference between Git and GitHub?",
    "What is a Git repository?",
    "What is a Git commit?"
]


# ============================================
# CREATE DEEPEVAL TEST CASES
# ============================================

test_cases = []


for question in questions:

    answer, context = rag_with_context(question)

    test_case = LLMTestCase(
        input=question,
        actual_output=answer,
        retrieval_context=context
    )

    test_cases.append(test_case)


# ============================================
# RAG TRIAD METRICS
# ============================================

contextual_relevancy = ContextualRelevancyMetric(
    threshold=0.7,
    model=groq_judge,
    include_reason=False
)


faithfulness = FaithfulnessMetric(
    threshold=0.7,
    model=groq_judge,
    include_reason=False
)


answer_relevancy = AnswerRelevancyMetric(
    threshold=0.7,
    model=groq_judge,
    include_reason=False
)


# ============================================
# RUN EVALUATION
# ============================================

# CHANGED: max_concurrent 1 -> 2 so the run finishes faster.
# The 429 retry logic above absorbs any rate-limit hits.
evaluate(
    test_cases=test_cases,
    metrics=[
        contextual_relevancy,
        faithfulness,
        answer_relevancy
    ],
    async_config=AsyncConfig(
        run_async=True,
        max_concurrent=2,
        throttle_value=2
    )
)
import os
import json
import random

from dotenv import load_dotenv

from deepeval.synthesizer import Synthesizer
from deepeval.models.base_model import DeepEvalBaseLLM

from langchain_text_splitters import RecursiveCharacterTextSplitter
from groq import Groq

load_dotenv()


# ============================================================
# CONFIG
# ============================================================

TXT_PATH = "/home/s/Desktop/JANSAARTHI/RAG/knowledge_base/data.txt"   # <-- your knowledge base file

OUTPUT_PATH = "goldens/retriever_deepeval_goldens.json"

NUM_CHUNKS = 8

CHUNK_SIZE = 1000
CHUNK_OVERLAP = 150


# ============================================================
# GROQ MODEL FOR DEEPEVAL
# ============================================================

class GroqDeepEvalModel(DeepEvalBaseLLM):

    def __init__(self, model_name="openai/gpt-oss-120b"):
        self.model_name = model_name
        self.client = Groq(api_key=os.getenv("GROQ_API_KEY"))

    def load_model(self):
        return self.client

    def generate(self, prompt: str) -> str:
        response = self.client.chat.completions.create(
            model=self.model_name,
            messages=[{"role": "user", "content": prompt}],
            temperature=0
        )
        return response.choices[0].message.content

    async def a_generate(self, prompt: str) -> str:
        return self.generate(prompt)

    def get_model_name(self):
        return self.model_name


# ============================================================
# LOAD TXT
# ============================================================

def load_txt():

    print("Loading TXT file...")

    with open(TXT_PATH, "r", encoding="utf-8") as f:
        text = f.read()

    print(f"TXT length: {len(text)} characters")

    return text


# ============================================================
# CHUNK TXT
# ============================================================

def load_chunks():

    text = load_txt()

    splitter = RecursiveCharacterTextSplitter(
        chunk_size=CHUNK_SIZE,
        chunk_overlap=CHUNK_OVERLAP
    )

    split_texts = splitter.split_text(text)

    chunks = []

    for chunk_index, chunk_text in enumerate(split_texts):

        chunks.append({
            "chunk_id": f"chunk_{chunk_index:04d}",
            "chunk_index": chunk_index,
            "text": chunk_text
        })

    print(f"Total chunks created: {len(chunks)}")

    return chunks


# ============================================================
# GENERATE GOLDENS USING DEEPEVAL
# ============================================================

def generate_goldens(chunks):

    if not chunks:
        raise ValueError("No chunks were created from the TXT file.")

    number_to_sample = min(NUM_CHUNKS, len(chunks))

    print(f"Sampling {number_to_sample} chunks...")

    random.seed(42)

    selected_chunks = random.sample(chunks, number_to_sample)

    contexts = [[chunk["text"]] for chunk in selected_chunks]

    print("Creating DeepEval Synthesizer...")

    llm = GroqDeepEvalModel(model_name="openai/gpt-oss-120b")

    synthesizer = Synthesizer(model=llm)

    print("Generating goldens...")

    goldens = synthesizer.generate_goldens_from_contexts(
        contexts=contexts,
        include_expected_output=True,
        max_goldens_per_context=1
    )

    print(f"Goldens generated: {len(goldens)}")

    return goldens, selected_chunks


# ============================================================
# CONVERT TO OUR JSON SCHEMA
# ============================================================

def create_dataset(goldens, selected_chunks):

    rows = []

    for i, golden in enumerate(goldens, start=1):

        source_chunk = selected_chunks[i - 1]

        rows.append({
    "id": f"g{i:03d}",
    "query": golden.input,
    "ideal_answer": golden.expected_output,
    "source": {
        "chunk_id": source_chunk["chunk_id"],
        "chunk_index": source_chunk["chunk_index"],
        "text": source_chunk["text"]
    }
})

    return rows


# ============================================================
# SAVE JSON
# ============================================================

def save_dataset(rows):

    os.makedirs(os.path.dirname(OUTPUT_PATH), exist_ok=True)

    with open(OUTPUT_PATH, "w", encoding="utf-8") as f:
        json.dump(rows, f, indent=2, ensure_ascii=False)

    print()
    print("=" * 60)
    print("DONE")
    print("=" * 60)
    print(f"Goldens written: {len(rows)}")
    print(f"File: {OUTPUT_PATH}")
    print("=" * 60)


# ============================================================
# MAIN
# ============================================================

def main():

    print("=" * 60)
    print("DEEPEVAL GOLDEN DATASET GENERATOR (TXT)")
    print("=" * 60)

    chunks = load_chunks()
    goldens, selected_chunks = generate_goldens(chunks)
    rows = create_dataset(goldens, selected_chunks)
    save_dataset(rows)


if __name__ == "__main__":
    main()
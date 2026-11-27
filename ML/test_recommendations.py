
from src.similarity_model import SchemeRecommendationModel

model = SchemeRecommendationModel()

test_queries = [
    "I am a woman entrepreneur starting a new business",
    "I belong to SC category and want to start a startup",
    "I need a loan for my small manufacturing business",
    "I am a street vendor and need working capital",
    "I am a young entrepreneur looking for startup support"
]

for i, query in enumerate(test_queries, start=1):

    print("\n" + "=" * 75)
    print(f"TEST CASE {i}")
    print("User:", query)

    results = model.recommend(query, top_n=5)

    print("\nRecommendations:")

    if not results:
        print("No relevant schemes found.")
        continue

    for result in results:
        print(
            f"{result['scheme_id']} | "
            f"{result['scheme_name']} | "
            f"{result['similarity_score']}%"
        )

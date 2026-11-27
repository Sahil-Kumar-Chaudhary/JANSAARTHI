
from src.similarity_model import SchemeRecommendationModel

print("=" * 65)
print("JANSAARTHI - GOVERNMENT SCHEME RECOMMENDER")
print("=" * 65)

model = SchemeRecommendationModel()

while True:
    print("\nEnter your query (or type 'exit' to quit):")
    query = input("You: ").strip()

    if query.lower() == "exit":
        print("Thank you for using JANSAARTHI!")
        break

    if not query:
        print("Please enter a valid query.")
        continue

    results = model.recommend(query, top_n=5)

    print("\nRelevant Government Schemes:")
    print("-" * 65)

    if not results:
        print("No relevant schemes found.")
        continue

    for i, scheme in enumerate(results, start=1):
        print(f"\n{i}. {scheme['scheme_name']}")
        print(f"   Scheme ID: {scheme['scheme_id']}")
        print(f"   Category: {scheme['category']}")
        print(f"   Relevance Score: {scheme['similarity_score']}%")

    print("\n" + "=" * 65)

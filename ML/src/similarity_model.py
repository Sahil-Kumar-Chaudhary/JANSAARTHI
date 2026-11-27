
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

from src.preprocessing import load_scheme_data
from src.query_processor import normalize_query
from src.category_filter import (
    detect_categories,
    filter_by_categories
)


class SchemeRecommendationModel:

    def __init__(self):

        self.schemes = load_scheme_data()

        self.vectorizer = TfidfVectorizer(
            stop_words="english",
            ngram_range=(1, 2),
            sublinear_tf=True
        )

        self.scheme_vectors = self.vectorizer.fit_transform(
            self.schemes["combined_features"]
        )

        print("Improved TF-IDF model initialized!")
        print("Total schemes indexed:", len(self.schemes))

    def recommend(self, user_query, top_n=5, min_score=5.0):

        processed_query = normalize_query(user_query)

        user_vector = self.vectorizer.transform(
            [processed_query]
        )

        scores = cosine_similarity(
            user_vector,
            self.scheme_vectors
        ).flatten()
        print("\nDEBUG - Top 10 raw similarity scores:")

        for index in scores.argsort()[::-1][:10]:
            print(
                self.schemes.iloc[index]["scheme_name"],
                round(float(scores[index]) * 100, 2),
                self.schemes.iloc[index]["category"]
            )
        
        # DEBUG: Inspect every housing scheme
        print("\nDEBUG - All Housing Schemes:")

        housing_indices = self.schemes[
            self.schemes["category"] == "Housing & Shelter"
        ].index

        for index in housing_indices:
            print(
                self.schemes.iloc[index]["scheme_name"],
                round(float(scores[index]) * 100, 2),
                self.schemes.iloc[index]["combined_features"]
            )


        ranked_indices = scores.argsort()[::-1]
        

        results = []

        # Collect all matching schemes first
        for index in ranked_indices:

            score = float(scores[index]) * 100

            if score <= min_score:
                continue

            scheme = self.schemes.iloc[index]

            results.append({
                "scheme_id": scheme["scheme_id"],
                "scheme_name": scheme["scheme_name"],
                "category": scheme["category"],
                "similarity_score": round(score, 2)
            })

        # Filter business schemes before selecting top N
        detected_categories = detect_categories(user_query)

        if detected_categories:
            results = filter_by_categories(results, detected_categories)

        return results[:top_n]


if __name__ == "__main__":

    model = SchemeRecommendationModel()

    query = "I am an entrepreneur starting a new business"

    recommendations = model.recommend(query)

    print("\nQuery:", query)
    print("\nRecommendations:")

    for item in recommendations:
        print(item)

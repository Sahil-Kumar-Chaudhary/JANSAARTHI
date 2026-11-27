
CATEGORY_KEYWORDS = {
    "Agriculture, Rural & Environment": [
        "farmer", "farmers", "farming", "agriculture",
        "agricultural", "crop", "kisan", "irrigation"
    ],

    "Women and Child": [
        "woman", "women", "female", "girl",
        "mother", "child", "maternity"
    ],

    "Housing & Shelter": [
        "housing", "house", "home", "shelter",
        "homeless"
    ],

    "Business & Entrepreneurship": [
        "business", "entrepreneur", "startup",
        "start up", "manufacturing", "loan",
        "credit", "vendor", "enterprise"
    ],
    "Skills & Employment": [
        "unemployed",
        "unemployment",
        "employment",
        "job",
        "jobs",
        "jobless",
        "skill",
        "training",
        "career",
        "livelihood"
    ],
    "Education & Learning": [
        "student",
        "students",
        "scholarship",
        "scholarships",
        "education",
        "school",
        "college",
        "university",
        "tuition",
        "academic"
    ]
}



def detect_categories(query):
    query = query.lower()
    detected = []

    for category, keywords in CATEGORY_KEYWORDS.items():
        if any(keyword in query for keyword in keywords):
            detected.append(category)

    return detected


def filter_by_categories(results, categories):
    if not categories:
        return results

    return [
        result for result in results
        if result["category"].lower() in [
            category.lower() for category in categories
        ]
    ]

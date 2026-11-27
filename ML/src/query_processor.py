import re

SYNONYMS = {
    "woman": "women",
    "female": "women",
    "startup": "start up",
    "start-up": "start up",
    "loan": "credit finance funding",
    "loans": "credit finance funding",
    "funding": "credit finance",
    "manufacturing": "manufacturing msme enterprise",
    "vendor": "street vendor",
    "vendors": "street vendor",
    "sc": "scheduled caste",
    "st": "scheduled tribe",
    "entrepreneur": "business entrepreneur",
    "entrepreneurs": "business entrepreneur"
}


def normalize_query(query):

    query = query.lower()
    query = re.sub(r"[^a-z0-9\s-]", " ", query)

    words = query.split()

    processed_words = []

    for word in words:
        processed_words.append(word)

        if word in SYNONYMS:
            processed_words.extend(SYNONYMS[word].split())

    return " ".join(processed_words)

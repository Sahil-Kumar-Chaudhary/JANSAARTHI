
import pandas as pd
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parents[1]

DATASET_PATH = BASE_DIR / "dataset" / "schemes.xlsx"


def load_scheme_data():

    df = pd.read_excel(
        DATASET_PATH,
        sheet_name="Scheme Data"
    )

    required_columns = [
        "scheme_id",
        "scheme_name",
        "category",
        "ministry_or_department",
        "target_group_or_purpose"
    ]

    df = df[required_columns].copy()

    # Handle missing text safely
    for column in required_columns:
        df[column] = df[column].fillna("").astype(str)

    # Combine useful scheme information
    df["combined_features"] = (
        df["scheme_name"] + " " +
        df["category"] + " " +
        df["ministry_or_department"] + " " +
        df["target_group_or_purpose"]
    )

    return df


if __name__ == "__main__":

    schemes = load_scheme_data()

    print("Dataset loaded successfully!")
    print("Total schemes:", len(schemes))

    print("\nProcessed data:")
    print(
        schemes[
            ["scheme_id", "scheme_name", "combined_features"]
        ].head().to_string(index=False)
    )

import pandas as pd

file_path = "dataset/schemes.xlsx"

excel = pd.ExcelFile(file_path)

print("Available sheets:", excel.sheet_names)

df = pd.read_excel(file_path, sheet_name="Scheme Data")

print("\nDataset shape:", df.shape)

print("\nColumns:")
print(df.columns.tolist())

print("\nFirst 5 records:")
print(df.head().to_string())

print("\nMissing values:")
print(df.isnull().sum())

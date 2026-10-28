import sys
from pathlib import Path
import json

sys.path.insert(0, str(Path(__file__).resolve().parent))
from query_engine import retrieve_schemes

BENCHMARK_CASES = [
    # 1. Exact Scheme Names
    {
        "id": "Q01",
        "query": "Pradhan Mantri Mudra Yojana loan eligibility",
        "category": "Exact Name",
        "relevant": {"SCH002"}
    },
    {
        "id": "Q02",
        "query": "Stand Up India Scheme",
        "category": "Exact Name",
        "relevant": {"SCH003"}
    },
    {
        "id": "Q03",
        "query": "PM-KISAN Samman Nidhi",
        "category": "Exact Name",
        "relevant": {"SCH021"}
    },
    {
        "id": "Q04",
        "query": "Ayushman Bharat Pradhan Mantri Jan Arogya Yojana",
        "category": "Exact Name",
        "relevant": {"SCH046"}
    },
    {
        "id": "Q05",
        "query": "PM SVANidhi scheme for urban street vendors",
        "category": "Exact Name",
        "relevant": {"SCH005"}
    },
    # 2. Synonyms & Paraphrased Queries
    {
        "id": "Q06",
        "query": "Financial credit support for female business founders",
        "category": "Synonym / Paraphrase",
        "relevant": {"SCH003", "SCH002"}
    },
    {
        "id": "Q07",
        "query": "Small business working capital for retail shopkeepers",
        "category": "Synonym / Paraphrase",
        "relevant": {"SCH002", "SCH005", "SCH007"}
    },
    {
        "id": "Q08",
        "query": "Old age pension for senior citizens",
        "category": "Synonym / Paraphrase",
        "relevant": {"SCH070", "SCH075", "SCH022"}
    },
    {
        "id": "Q09",
        "query": "Pucca house assistance for rural homeless families",
        "category": "Housing",
        "relevant": {"SCH041"}
    },
    {
        "id": "Q10",
        "query": "Healthcare insurance coverage for poor hospital patients",
        "category": "Health",
        "relevant": {"SCH046"}
    },
    # 3. Women Entrepreneurs & Welfare
    {
        "id": "Q11",
        "query": "Subsidies and loans for women starting a new enterprise",
        "category": "Women",
        "relevant": {"SCH003", "SCH002", "SCH085"}
    },
    {
        "id": "Q12",
        "query": "Maternity financial assistance for pregnant women",
        "category": "Women",
        "relevant": {"SCH048"}
    },
    # 4. Farming & Agriculture
    {
        "id": "Q13",
        "query": "Crop insurance compensation for drought and flood crop loss",
        "category": "Farming",
        "relevant": {"SCH023"}
    },
    {
        "id": "Q14",
        "query": "Short term credit card loan for farmers cultivating crops",
        "category": "Farming",
        "relevant": {"SCH025"}
    },
    {
        "id": "Q15",
        "query": "Financial assistance for drip and sprinkler irrigation in agriculture",
        "category": "Farming",
        "relevant": {"SCH024"}
    },
    # 5. Education & Scholarships
    {
        "id": "Q16",
        "query": "Higher education scholarship for students with disabilities",
        "category": "Education",
        "relevant": {"SCH062", "SCH063"}
    },
    {
        "id": "Q17",
        "query": "College scholarship financial aid for backward class and OBC students",
        "category": "Education",
        "relevant": {"SCH060", "SCH061"}
    },
    # 6. Housing
    {
        "id": "Q18",
        "query": "Urban housing subsidy for constructing affordable houses",
        "category": "Housing",
        "relevant": {"SCH040", "SCH081"}
    },
    # 7. Skills & Artisans
    {
        "id": "Q19",
        "query": "Skill training and toolkit incentive for traditional artisans and craftspeople",
        "category": "Skills",
        "relevant": {"SCH018", "SCH068"}
    },
    # 8. Out-of-Domain
    {
        "id": "Q20",
        "query": "Galactic space exploration satellite launch orbit trajectory",
        "category": "OOD",
        "relevant": set()
    },
    {
        "id": "Q21",
        "query": "Quantum cryptography blockchain token trading cryptocurrency",
        "category": "OOD",
        "relevant": set()
    },
    {
        "id": "Q22",
        "query": "Best Italian pasta recipe with parmesan cheese and olive oil",
        "category": "OOD",
        "relevant": set()
    }
]

def run_benchmark(mode="hybrid"):
    print(f"\n=======================================================")
    print(f"   EVALUATION BENCHMARK: MODE = {mode.upper()}")
    print(f"=======================================================")
    
    total_precisions = []
    total_recalls = []
    total_returned_counts = []
    in_domain_precisions = []
    in_domain_recalls = []
    ood_correct = 0
    ood_total = 0

    print(f"{'ID':4} | {'Category':18} | {'Ret':3} | {'Prec@5':7} | {'Rec@5':7} | {'Returned IDs':32} | {'Relevant IDs':20}")
    print("-" * 105)

    for case in BENCHMARK_CASES:
        cid = case["id"]
        cat = case["category"]
        query = case["query"]
        relevant = case["relevant"]

        results = retrieve_schemes(query, k=5, mode=mode)
        ret_ids = [r["schemeId"] for r in results]
        ret_set = set(ret_ids)
        k_ret = len(ret_ids)
        total_returned_counts.append(k_ret)

        if len(relevant) == 0: # OOD query
            ood_total += 1
            if k_ret == 0:
                ood_correct += 1
                p = 1.0 # Cleanly rejected
                r = 1.0
            else:
                p = 0.0
                r = 0.0
            total_precisions.append(p)
            total_recalls.append(r)
        else: # In-domain query
            true_positives = len(ret_set.intersection(relevant))
            p = (true_positives / k_ret) if k_ret > 0 else 0.0
            r = (true_positives / len(relevant)) if len(relevant) > 0 else 0.0
            total_precisions.append(p)
            total_recalls.append(r)
            in_domain_precisions.append(p)
            in_domain_recalls.append(r)

        ret_str = ", ".join(ret_ids[:4]) + ("..." if len(ret_ids) > 4 else "")
        rel_str = ", ".join(sorted(relevant)) if relevant else "(OOD: None)"
        print(f"{cid:4} | {cat:18} | {k_ret:3} | {p:7.1%} | {r:7.1%} | {ret_str:32} | {rel_str:20}")

    mean_p = sum(total_precisions) / len(total_precisions)
    mean_r = sum(total_recalls) / len(total_recalls)
    in_mean_p = sum(in_domain_precisions) / len(in_domain_precisions) if in_domain_precisions else 0.0
    in_mean_r = sum(in_domain_recalls) / len(in_domain_recalls) if in_domain_recalls else 0.0
    avg_k = sum(total_returned_counts) / len(total_returned_counts)

    print("-" * 105)
    print(f"Overall Precision@5:           {mean_p:6.1%}")
    print(f"Overall Recall@5:              {mean_r:6.1%}")
    print(f"In-Domain Precision@5:         {in_mean_p:6.1%}")
    print(f"In-Domain Recall@5:            {in_mean_r:6.1%}")
    print(f"OOD Rejection Accuracy:        {ood_correct}/{ood_total} ({ood_correct/ood_total:6.1%})")
    print(f"Avg Sources Returned per query: {avg_k:4.1f}")
    return {
        "mode": mode,
        "mean_precision": mean_p,
        "mean_recall": mean_r,
        "in_domain_precision": in_mean_p,
        "in_domain_recall": in_mean_r,
        "ood_accuracy": ood_correct / ood_total if ood_total else 0.0,
        "avg_returned": avg_k
    }

if __name__ == "__main__":
    modes = ["hybrid", "semantic", "lexical"]
    if len(sys.argv) > 1 and sys.argv[1] in modes:
        modes = [sys.argv[1]]
    for m in modes:
        run_benchmark(m)

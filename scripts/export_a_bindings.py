"""Faz 1 — Export A sheet year column → H column bindings."""
import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from excel_io import ASSETS, load_workbook, num

MONTHS = [
    "OCAK", "ŞUBAT", "MART", "NİSAN", "MAYIS", "HAZİRAN",
    "TEMMUZ", "AĞUSTOS", "EYLÜL", "EKİM", "KASIM", "ARALIK",
]


def parse_h_ref(formula: str):
    if not formula or not isinstance(formula, str):
        return None, None
    m = re.search(r"H!([A-Z]+)(\d+)", formula.upper())
    if not m:
        return None, None
    return m.group(1), int(m.group(2))


def main():
    wb_f = load_workbook(data_only=False, read_only=True)
    wb_v = load_workbook(data_only=True, read_only=True)
    af = wb_f["A"]
    av = wb_v["A"]

    years = []
    for c in range(5, (av.max_column or 0) + 1):
        y = av.cell(3, c).value
        if isinstance(y, (int, float)):
            years.append((c, int(y)))

    bindings = []
    for c, year in years:
        sample_f = af.cell(4, c).value
        h_col, h_row = parse_h_ref(str(sample_f or ""))
        monthly = {}
        for i, mname in enumerate(MONTHS):
            v = num(av.cell(4 + i, c).value)
            if v is not None:
                monthly[mname] = v
        bindings.append({
            "year": year,
            "aColumn": c,
            "hColumn": h_col,
            "monthlyNetRowStart": h_row or 166,
            "monthlyNet": monthly,
            "firstHalfAvg": num(av.cell(16, c).value),
            "yearlyBrutGold": num(av.cell(28, c).value),
            "yearlyNetGold": num(av.cell(29, c).value),
            "yearlyBrutUsd": num(av.cell(30, c).value),
            "yearlyNetUsd": num(av.cell(31, c).value),
        })

    payload = {"years": bindings, "monthLabels": MONTHS}
    ASSETS.mkdir(parents=True, exist_ok=True)
    out = ASSETS / "a_year_columns.json"
    out.write_text(json.dumps(payload, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    print(f"wrote {out} years={len(bindings)}")
    wb_f.close()
    wb_v.close()


if __name__ == "__main__":
    main()

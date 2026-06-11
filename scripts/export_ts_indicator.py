"""Export TS!G → yıllık gösterge (H satır 76 vb. VLOOKUP kaynağı)."""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from excel_io import ASSETS, load_workbook

YEAR_COL_START = 10


def main():
    wb = load_workbook(data_only=True, read_only=True)
    ts = wb["TS"]
    year_cols = {}
    for c in range(YEAR_COL_START, (ts.max_column or 0) + 1):
        y = ts.cell(2, c).value
        if isinstance(y, (int, float)):
            year_cols[c] = int(y)

    by_kod: dict[str, dict[str, float]] = {}
    for r in range(3, (ts.max_row or 0) + 1):
        kod = ts.cell(r, 7).value
        if not kod:
            continue
        kod = str(kod).strip()
        per_year = {}
        for c, year in year_cols.items():
            v = ts.cell(r, c).value
            if isinstance(v, (int, float)):
                per_year[str(year)] = float(v)
        if per_year:
            by_kod[kod] = per_year

    ASSETS.mkdir(parents=True, exist_ok=True)
    out = ASSETS / "ts_indicator_by_kod.json"
    out.write_text(json.dumps(by_kod, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    print(f"wrote {out} kods={len(by_kod)}")
    wb.close()


if __name__ == "__main__":
    main()

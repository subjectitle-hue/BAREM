"""Faz 1 — BL row source hints from H!BL column formulas."""
import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from excel_io import ASSETS, load_workbook, num

BL_COL = 64
PAY_ROWS = list(range(68, 104)) + [127]


def classify_bl_formula(formula: str, row: int) -> dict:
    if not formula or not str(formula).startswith("="):
        return {"source": "static", "row": row}
    f = str(formula).upper()
    if "VLOOKUP" in f or "V!" in f:
        return {"source": "v_derece_kademe", "row": row}
    if "M!" in f:
        return {"source": "m_kadro", "row": row}
    if "TS!" in f:
        return {"source": "ts_meslek", "row": row}
    if "A!" in f:
        return {"source": "a_input", "row": row}
    if "İ!" in f or "I!" in f:
        return {"source": "index", "row": row}
    if row == 127:
        return {"source": "tyo_tier", "row": row, "formula": formula}
    if "BL69" in f or "BL68" in f:
        return {"source": "derived", "row": row, "formula": formula}
    return {"source": "unknown", "row": row, "formula": formula}


def main():
    wb_f = load_workbook(data_only=False, read_only=False)
    wb_v = load_workbook(data_only=True, read_only=False)
    hf = wb_f["H"]
    hv = wb_v["H"]

    hints = []
    for r in PAY_ROWS:
        label = hf.cell(r, 2).value
        if not label:
            continue
        formula = hf.cell(r, BL_COL).value
        cached = num(hv.cell(r, BL_COL).value)
        hint = classify_bl_formula(str(formula or ""), r)
        hint["label"] = str(label).strip()
        hint["cached"] = cached
        if formula and str(formula).startswith("="):
            hint["formula"] = str(formula)
        hints.append(hint)

    ASSETS.mkdir(parents=True, exist_ok=True)
    out = ASSETS / "bl_sources.json"
    out.write_text(json.dumps({"rows": hints}, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"wrote {out} hints={len(hints)}")
    wb_f.close()
    wb_v.close()


if __name__ == "__main__":
    main()

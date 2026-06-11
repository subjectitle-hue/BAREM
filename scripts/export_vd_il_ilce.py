"""Faz 5 — Export VD!AG:AH il → ilçe → bölgesel kod (A!B13→B14→B16)."""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from excel_io import load_workbook

COL_AG = 33
COL_AH = 34
OUT = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "assets" / "memur"


def cell_str(v):
    if v is None:
        return None
    s = str(v).strip()
    return s if s else None


def main():
    wb = load_workbook(data_only=True, read_only=True)
    vd = wb["VD"]

    ilceler_by_il: dict[str, list[str]] = {}
    bolgesel_by_key: dict[str, str | None] = {}

    for r in range(2, 910):
        combined = cell_str(vd.cell(r, COL_AG).value)
        if not combined or "-" not in combined:
            continue
        il, ilce = combined.split("-", 1)
        il = il.strip()
        ilce = ilce.strip()
        if not il or not ilce:
            continue
        bolgesel = cell_str(vd.cell(r, COL_AH).value)
        ilceler_by_il.setdefault(il, [])
        if ilce not in ilceler_by_il[il]:
            ilceler_by_il[il].append(ilce)
        bolgesel_by_key[f"{il}-{ilce}"] = bolgesel

    for il in ilceler_by_il:
        ilceler_by_il[il].sort()

    payload = {
        "iller": sorted(ilceler_by_il.keys()),
        "ilcelerByIl": ilceler_by_il,
        "bolgeselKodByKey": bolgesel_by_key,
    }

    OUT.mkdir(parents=True, exist_ok=True)
    out = OUT / "vd_il_ilce.json"
    out.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"wrote {out} iller={len(payload['iller'])} keys={len(bolgesel_by_key)}")
    wb.close()


if __name__ == "__main__":
    main()

"""Export M sheet → H BL lookup keys (ek gösterge kodu, TS prime kodu) per kadro."""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from excel_io import ASSETS, load_workbook

COL_EK = 8
COL_TAZMINAT = 11
COL_ILAVE_TAZ_PRIME = 12
COL_ILAVE_TAZ_HARIC = 13
COL_BOLGESEL = 14
COL_TS_PRIME = 15
COL_YAN_ODEME = 17
COL_EK_ODEME = 20


def cell_str(v):
    if v is None:
        return None
    s = str(v).strip()
    return s if s and s.lower() != "none" else None


def kadro_key(unvan, detay, derece):
    d = str(detay).strip() if detay else ""
    return f"{str(unvan).strip()}-{d}-{int(derece)}"


def main():
    wb = load_workbook(data_only=True, read_only=True)
    m = wb["M"]
    headers = {}
    for c in range(6, (m.max_column or 0) + 1):
        h = m.cell(2, c).value
        if h:
            headers[c] = str(h).strip()

    rows = []
    for r in range(3, (m.max_row or 0) + 1):
        hs = m.cell(r, 1).value
        unvan = m.cell(r, 3).value
        detay = m.cell(r, 4).value
        derece = m.cell(r, 5).value
        if not hs or not unvan or not isinstance(derece, (int, float)):
            continue
        key = kadro_key(unvan, detay, derece)
        rows.append({
            "key": key,
            "hizmetSinifi": str(hs).strip(),
            "unvan": str(unvan).strip(),
            "detay": str(detay).strip() if detay else "",
            "derece": int(derece),
            "ekCode": cell_str(m.cell(r, COL_EK).value),
            "tazminatCode": cell_str(m.cell(r, COL_TAZMINAT).value),
            "ilaveTazPrimeDahilCode": cell_str(m.cell(r, COL_ILAVE_TAZ_PRIME).value),
            "ilaveTazPrimeHaricCode": cell_str(m.cell(r, COL_ILAVE_TAZ_HARIC).value),
            "bolgeselCode": cell_str(m.cell(r, COL_BOLGESEL).value),
            "yanOdemeCode": cell_str(m.cell(r, COL_YAN_ODEME).value),
            "ekOdemeCode": cell_str(m.cell(r, COL_EK_ODEME).value),
            "tsPrimeKod": cell_str(m.cell(r, COL_TS_PRIME).value),
        })

    ASSETS.mkdir(parents=True, exist_ok=True)
    out = ASSETS / "m_h_lookup.json"
    out.write_text(json.dumps({"rows": rows}, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"wrote {out} rows={len(rows)}")
    wb.close()


if __name__ == "__main__":
    main()

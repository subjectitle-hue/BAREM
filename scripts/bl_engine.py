"""Python mirror of Kotlin BlResolver + HPayEngine + HDeductionEngine (Faz 1–2)."""
import json
from pathlib import Path

from engine_calc import compute_cross_column_net, load_brackets, r2

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "app" / "src" / "main" / "assets" / "engine"

CM122 = 707.0
CM124 = 2273.0
CM125 = 250.0
CM126 = 500.0


def _load_json(name: str):
    return json.loads((ASSETS / name).read_text(encoding="utf-8"))


def vlookup_pay(code, derece, v_table, year="2026"):
    if not code:
        return 0.0
    val = v_table.get(f"{code}-{derece}", {}).get(year, 0) or 0
    if val:
        return val
    return v_table.get(code, {}).get(year, 0) or 0


def ilave_odeme(v_table, year="2026"):
    for key in v_table:
        if "lave" in key.lower() and "deme" in key.lower():
            return v_table[key].get(year, 0) or 0
    return 15965.0


def kidem_gosterge(kidem_birim, kidem_yili):
    if kidem_birim <= 0:
        return 0.0
    yil = 1 if kidem_yili <= 0 else min(kidem_yili, 25)
    return min(500.0, kidem_birim * yil)


def tyo_bl(ek_gosterge):
    if ek_gosterge <= 0:
        return 55.0
    if ek_gosterge >= 8400:
        return 255.0
    if ek_gosterge >= 7600:
        return 215.0
    if ek_gosterge >= 6400:
        return 195.0
    if ek_gosterge >= 4800:
        return 165.0
    if ek_gosterge >= 3600:
        return 145.0
    if ek_gosterge >= 2200:
        return 85.0
    return 55.0


def resolve_bl(engine_inputs, year="2026"):
    unvan = engine_inputs["unvan"]
    detay = engine_inputs["detay"]
    derece = engine_inputs["derece"]
    kademe = engine_inputs["kademe"]
    kidem_yili = engine_inputs.get("kidemYili", 0)

    mrows = {r["key"]: r for r in _load_json("m_h_lookup.json")["rows"]}
    kad_list = _load_json("m_kadro_full.json")
    v_table = _load_json("v_derece_kademe.json")

    key = f"{unvan}-{detay}-{derece}"
    m_row = mrows[key]
    kadro = next(
        x
        for x in kad_list
        if x["unvan"] == unvan and x.get("detay", "") == detay and x["derece"] == derece
    )

    bl = {}
    bl[68] = v_table.get(f"{derece}/{kademe}", {}).get(year, 0) or 0
    bl[69] = vlookup_pay(m_row.get("ekCode"), derece, v_table, year)
    bl[70] = kadro.get("taban", 0) or 0
    bl[71] = kidem_gosterge(kadro.get("kidem", 0) or 0, kidem_yili)
    bl[72] = vlookup_pay(m_row.get("tazminatCode"), derece, v_table, year)
    bl[73] = vlookup_pay(m_row.get("ilaveTazPrimeDahilCode"), derece, v_table, year)
    bl[74] = vlookup_pay(m_row.get("ilaveTazPrimeHaricCode"), derece, v_table, year)
    bolgesel_kod = engine_inputs.get("bolgeselKod")
    bolgesel_code = m_row.get("bolgeselCode")
    if bolgesel_code and bolgesel_kod:
        bl[75] = v_table.get(f"{bolgesel_code}-{bolgesel_kod}", {}).get(year, 0) or 0
    else:
        bl[75] = 0.0
    ts_kod = m_row.get("tsPrimeKod")
    if ts_kod:
        ts_table = _load_json("ts_indicator_by_kod.json")
        bl[76] = ts_table.get(ts_kod, {}).get(year, 0) or 0
    else:
        bl[76] = 0.0
    bl[78] = vlookup_pay(m_row.get("yanOdemeCode"), derece, v_table, year)
    bl[81] = vlookup_pay(m_row.get("ekOdemeCode"), derece, v_table, year)
    for r in range(82, 104):
        bl[r] = 0.0
    for row_key, code in (m_row.get("payCodes") or {}).items():
        row_num = int(row_key)
        if 82 <= row_num <= 103:
            bl[row_num] = vlookup_pay(code, derece, v_table, year)
    bl[122] = CM122
    bl[124] = CM124
    bl[125] = CM125
    bl[126] = CM126
    bl[127] = tyo_bl(bl[69])
    tazminat_path = bl[72] > 0
    bl[120] = ilave_odeme(v_table, year) if tazminat_path else 0.0
    return bl


def compute_standard_rows(bl, coeff):
    mk, tk, yk, ey = coeff["memurK"], coeff["tabanK"], coeff["yanK"], coeff["enYuksek"]
    bn = {}
    bn[68] = r2(bl.get(68, 0) * mk)
    bn[69] = r2(bl.get(69, 0) * mk)
    bn[70] = r2(bl.get(70, 0) * tk)
    bn[71] = r2(bl.get(71, 0) * mk)
    for r in (72, 73, 74, 75, 76, 77, 81):
        bn[r] = r2(bl.get(r, 0) * ey / 100)
    for r in (78, 79, 80):
        bn[r] = r2(bl.get(r, 0) * yk)
    for r in range(82, 104):
        b = bl.get(r, 0) or 0
        if b <= 0:
            bn[r] = 0.0
        elif r in (92, 96):
            bn[r] = r2((bn[68] + bn[69]) * b / 100)
        elif 101 <= r <= 103:
            bn[r] = r2(b * mk / 100)
        else:
            bn[r] = r2(b * ey / 100)
    return bn


def compute_kistas_branch(bl, bn, mk):
    if (bl.get(105) or 0) <= 0:
        return
    bn[110] = r2((bl.get(110) or 0) * mk)
    bn[105] = r2((bl.get(105) or 0) / 100 * bn[110])
    bn[106] = r2((bl.get(106) or 0) / 100 * bn[110])
    bn[107] = r2((bl.get(107) or 0) / 100 * bn[105])
    bn[108] = r2((bl.get(108) or 0) * mk)
    bn[109] = r2((bl.get(109) or 0) * mk)


def compute_ek10_branch(bl, bn, mk):
    if (bl.get(113) or 0) <= 0:
        return
    for r in range(113, 118):
        cm = bl.get(r) or 0
        bn[r] = r2(cm * mk) if cm > 0 else 0.0


def compute_sp_branch(bl, bn, ey):
    if (bl.get(128) or 0) <= 0:
        return
    bn[128] = r2((bl.get(128) or 0) * (ey / 100 + 1))
    for r in (129, 130, 131):
        bn[r] = r2((bl.get(r) or 0) * ey / 100)


def select_brut_base(bn):
    if (bn.get(105) or 0) > 0:
        return r2(sum(bn.get(r, 0) for r in range(105, 110)))
    if (bn.get(113) or 0) > 0:
        return r2(sum(bn.get(r, 0) for r in range(113, 118)))
    if (bn.get(128) or 0) > 0:
        return r2(sum(bn.get(r, 0) for r in range(128, 132)))
    return r2(sum(bn.get(r, 0) for r in range(68, 104)))


def brut_rows_118_126(bl, bn, mk, engine_inputs):
    extra = 0.0
    if (bl.get(120) or 0) > 0:
        pay = r2(bl[120] * mk)
        bn[120] = pay
        extra += pay
    if engine_inputs.get("topluSozlesme") == "VAR":
        pay = r2((bl.get(122) or CM122) * mk)
        bn[122] = pay
        extra += pay
    if engine_inputs.get("medeniHal") == "Evli-Eşi Çalışmayan":
        pay = r2((bl.get(124) or CM124) * mk)
        bn[124] = pay
        extra += pay
    ust6 = engine_inputs.get("cocukUst6") or 0
    if ust6 > 0:
        pay = r2((bl.get(125) or CM125) * mk * ust6)
        bn[125] = pay
        extra += pay
    alt6 = engine_inputs.get("cocukAlt6") or 0
    if alt6 > 0:
        pay = r2((bl.get(126) or CM126) * mk * alt6)
        bn[126] = pay
        extra += pay
    return extra


def compute_pay(bl, coeff, engine_inputs=None):
    engine_inputs = engine_inputs or {}
    mk, ey = coeff["memurK"], coeff["enYuksek"]
    bn = compute_standard_rows(bl, coeff)
    compute_kistas_branch(bl, bn, mk)
    compute_ek10_branch(bl, bn, mk)
    compute_sp_branch(bl, bn, ey)
    bn127 = r2(bl.get(127, 0) * ey / 100) if bl.get(127, 0) > 0 else 0.0
    base = select_brut_base(bn)
    extra = brut_rows_118_126(bl, bn, mk, engine_inputs)
    return r2(base + extra), bn, bn127


def prim_matrah(bn, bn127):
    return r2(
        bn127
        + bn.get(68, 0)
        + bn.get(69, 0)
        + bn.get(70, 0)
        + bn.get(71, 0)
    )


GV_BASE_ROWS = [68, 69, 70, 71, 78, 79, 80, 97, 98, 99, 100, 101, 102, 103]


def gv_matrah_from_pay(bn, prim):
    if (bn.get(113) or 0) > 0:
        base = (bn.get(113) or 0) + (bn.get(114) or 0)
    elif (bn.get(111) or 0) > 0:
        base = bn.get(111) or 0
    elif (bn.get(128) or 0) > 0:
        base = bn.get(128) or 0
    else:
        base = sum(bn.get(r, 0) for r in GV_BASE_ROWS) + (bn.get(118) or 0)
    return r2(max(0.0, base - prim))


def compute_scenario(engine_inputs, brackets=None):
    h = _load_json("h_engine_full.json")
    cn = h["coefficients"]["ocak"]
    co = h["coefficients"]["temmuz"]
    brackets = brackets or load_brackets()
    prim_rate = h["gvMeta"].get("primOran5434", 16.0)

    bl = resolve_bl(engine_inputs)
    cn_brut, cn_bn, cn127 = compute_pay(bl, cn, engine_inputs)
    co_brut, co_bn, co127 = compute_pay(bl, co, engine_inputs)

    prim_o = r2(prim_matrah(cn_bn, cn127) * prim_rate / 100)
    prim_t = r2(prim_matrah(co_bn, co127) * prim_rate / 100)
    mat_o = gv_matrah_from_pay(cn_bn, prim_o)
    mat_t = gv_matrah_from_pay(co_bn, prim_t)
    nets = compute_cross_column_net(cn_brut, co_brut, prim_o, prim_t, mat_o, mat_t, brackets)

    return {
        "cn132": cn_brut,
        "co132": co_brut,
        "monthlyNetCo": nets,
        "co178": r2(cn_brut * 6 + co_brut * 6),
        "co179": r2(sum(nets)),
        "bl72": bl[72],
    }

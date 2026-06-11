"""Python mirror of Kotlin GV/DV engine for validate_all.py."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
BRACKETS = ROOT / "app" / "src" / "main" / "assets" / "engine" / "gv_brackets.json"


def r2(v):
    return round(v * 100) / 100


def cumulative_gv_tax(bl, b):
    limits = b["dilimLimits"]
    base = b["dilimBaseTax"]
    rates = b["dilimRates"]
    if bl > limits[3]:
        return (bl - limits[3]) * rates[4] + base[3]
    if bl > limits[2]:
        return (bl - limits[2]) * rates[3] + base[2]
    if bl > limits[1]:
        return (bl - limits[1]) * rates[2] + base[1]
    if bl > limits[0]:
        return (bl - limits[0]) * rates[1] + base[0]
    return bl * rates[0]


def gv_matrah(cells, prim):
    base = sum(cells.get(r, 0) for r in [68, 69, 70, 71, 78, 79, 80, 97, 98, 99, 100, 101, 102, 103])
    return r2(max(0.0, base - prim))


def monthly_gv(bm140_ocak, bm140_tem, agi, brackets):
    gv = []
    prev = 0.0
    for m in range(12):
        if m < 6:
            cum_bl = bm140_ocak * (m + 1)
        else:
            cum_bl = bm140_ocak * 6 + bm140_tem * (m - 5)
        tax = cumulative_gv_tax(cum_bl, brackets)
        gv.append(r2(max(0.0, (tax - prev) - agi)))
        prev = tax
    return gv


def monthly_dv(brut_ocak, brut_tem, rate, muaf_bm, muaf_bn):
    dv = []
    prev_bm = 0.0
    for m in range(12):
        if m < 6:
            cum_brut = brut_ocak * (m + 1)
        else:
            cum_brut = brut_ocak * 6 + brut_tem * (m - 5)
        bm = r2(cum_brut * rate)
        muaf = muaf_bm if m < 6 else muaf_bn
        inc = max(0.0, (bm - prev_bm) - muaf)
        dv.append(r2(inc))
        prev_bm = bm
    return dv


def compute_net(brut_ocak, brut_tem, prim_ocak, prim_tem, cells_ocak, cells_tem, brackets):
    bm140_o = gv_matrah(cells_ocak, prim_ocak)
    bm140_t = gv_matrah(cells_tem, prim_tem)
    agi = brackets["agiMonthly"]
    gv = monthly_gv(bm140_o, bm140_t, agi, brackets)
    dv = monthly_dv(brut_ocak, brut_tem, brackets["damgaRate"], brackets.get("damgaMuafiyetBm") or 0,
                    brackets.get("damgaMuafiyetBn") or 0)
    nets = []
    for m in range(12):
        brut = brut_ocak if m < 6 else brut_tem
        prim = prim_ocak if m < 6 else prim_tem
        nets.append(r2(brut - prim - gv[m] - dv[m]))
    return nets


def load_brackets():
    b = json.loads(BRACKETS.read_text(encoding="utf-8"))
    b["damgaMuafiyetBn"] = b.get("damgaMuafiyetBn", 0)
    return b


def compute_cross_column_net(brut_ocak, brut_tem, prim_ocak, prim_tem, matrah_ocak, matrah_tem, brackets):
    """H!CO166–177 — Ocak sütunu brüt + Temmuz sütunu GV/DV eşikleri."""
    th = brackets["crossColumnThresholds"]
    gv_f = th["gvFirstHalf"]
    gv_s = th["gvSecondHalf"]
    dv_o = th["dvMuafOcak"]
    dv_t = th["dvMuafTem"]
    rate = brackets["damgaRate"]

    cn_tax = []
    for m in range(6):
        cn_tax.append(cumulative_gv_tax(matrah_ocak * (m + 1), brackets))
    for m in range(6):
        cn_tax.append(cumulative_gv_tax(matrah_ocak * 6 + matrah_tem * (m + 1), brackets))

    cn_dv = []
    for m in range(6):
        cn_dv.append(r2(brut_ocak * (m + 1) * rate))
    for m in range(6):
        cn_dv.append(r2((brut_ocak * 6 + brut_tem * (m + 1)) * rate))

    nets = []
    for m in range(6):
        if m == 0:
            gv = max(0.0, cn_tax[0] - gv_f[0])
        else:
            gv = max(0.0, cn_tax[m] - cn_tax[m - 1] - gv_f[m])
        if m == 0:
            dv = max(0.0, cn_dv[0] - dv_o)
        else:
            dv = max(0.0, cn_dv[m] - cn_dv[m - 1] - dv_o)
        nets.append(r2(brut_ocak - prim_ocak - gv - dv))

    for m in range(6):
        idx = 6 + m
        if m == 0:
            gv = max(0.0, cn_tax[idx] - cn_tax[5] - (gv_s[0] or 0.0))
        else:
            gv = max(0.0, cn_tax[idx] - cn_tax[idx - 1] - (gv_s[m] or 0.0))
        if m == 0:
            dv = max(0.0, cn_dv[idx] - cn_dv[5] - dv_t)
        else:
            dv = max(0.0, cn_dv[idx] - cn_dv[5 + m] - dv_t)
        nets.append(r2(brut_tem - prim_tem - gv - dv))

    return nets

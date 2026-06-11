"""Run all Faz 1 export scripts."""
import subprocess
import sys
from pathlib import Path

SCRIPTS = [
    "export_index_full.py",
    "export_engine_bundle.py",
    "export_h_matrix.py",
    "export_a_bindings.py",
    "export_bl_sources.py",
    "export_gv_brackets.py",
    "export_m_h_lookup.py",
    "export_ts_indicator.py",
    "export_h_paylines.py",
    "export_h_engine_full.py",
    "export_memur_data.py",
    "export_ts_oran.py",
]

ROOT = Path(__file__).resolve().parent


def main():
    failed = []
    for name in SCRIPTS:
        path = ROOT / name
        if not path.exists():
            print(f"SKIP {name}")
            continue
        print(f"\n>>> {name}")
        r = subprocess.run([sys.executable, str(path)], cwd=ROOT.parent)
        if r.returncode != 0:
            failed.append(name)
    if failed:
        print(f"\nFAILED: {failed}")
        sys.exit(1)
    print("\nFaz 1 export complete.")
    # verify counts
    import json
    assets = ROOT.parent / "app" / "src" / "main" / "assets" / "engine"
    hf = json.loads((assets / "h_formulas.json").read_text(encoding="utf-8"))
    print("h_formulas stats:", hf["stats"])


if __name__ == "__main__":
    main()

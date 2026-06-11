import io
import re
import zipfile
from pathlib import Path

import msoffcrypto

SOURCE = Path(r"C:\Users\MEMO\Downloads\Mehmet Gürer.xlsx")


def main():
    with SOURCE.open("rb") as f:
        o = msoffcrypto.OfficeFile(f)
        o.load_key(password="erdal")
        b = io.BytesIO()
        o.decrypt(b)
        b.seek(0)
        z = zipfile.ZipFile(b)

    links = sorted(n for n in z.namelist() if n.startswith("xl/externalLinks/"))
    print("count", len(links))
    for n in links:
        xml = z.read(n).decode("utf-8", errors="replace")
        print("---", n)
        for m in re.finditer(r'(?:file:///[^<\s]+|TargetMode="External"[^/]*/>)', xml):
            print(" ", m.group(0)[:200])
        # fallback: whole snippet
        if "externalBook" in xml:
            idx = xml.find("externalBook")
            print(xml[idx : idx + 600])


if __name__ == "__main__":
    main()

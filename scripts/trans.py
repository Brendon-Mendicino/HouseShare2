#!/usr/bin/env python

import asyncio
import googletrans
import re
from pathlib import Path

SRC = "en"
LANGS = ["it", "de_DE", "es_ES", "fr_FR"]

PATTERN = r"^(\s*<[^>]*>)(.*?)(</[^>]*>\s*)$"


async def trans_line(line: str, dest="auto") -> str:
    m = re.match(PATTERN, line)
    if not m:
        return line

    start, to_trans, end = m.groups()

    async with googletrans.Translator() as translator:
        result = await translator.translate(to_trans, dest=dest, src=SRC)

    return f"{start}{result.text}{end}"


async def trans():
    base = Path(__file__).parent.parent
    for dest in LANGS:
        with open(f"{base}/app/src/main/res/values/strings.xml", "r") as f:
            tasks = []
            for line in f.readlines():
                tasks.append(asyncio.create_task(trans_line(line, dest)))

            res = await asyncio.gather(*tasks)
            print(''.join(res))


if __name__ == "__main__":
    asyncio.run(trans())

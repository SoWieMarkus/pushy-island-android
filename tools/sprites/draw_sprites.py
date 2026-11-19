"""Draws every game sprite into app/src/main/assets/sprites.

    python tools/sprites/draw_sprites.py [--sheet OUT.png]

`<name>.png` is frame 0; animation frames are written as `<name>@1.png`, `<name>@2.png`, …
Sprites with an overhang are larger than 16×16 and get drawn centred on their cell.
Terrain transition overlays (not part of Sprite.kt, the renderer picks them) go to `sprites/edges/`.
The script is deterministic, so re-running it reproduces the committed PNGs.
"""
import argparse
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from canvas import SIZE  # noqa: E402
from png import blank, paste, scale, write_png  # noqa: E402
import terrain  # noqa: E402

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
OUT = os.path.join(ROOT, "app", "src", "main", "assets", "sprites")
EDGES = os.path.join(OUT, "edges")
SPRITE_KT = os.path.join(ROOT, "engine", "src", "main", "kotlin", "markus", "wieland", "pushygame", "engine", "model", "Sprite.kt")


def expected_names():
    text = open(SPRITE_KT, encoding="utf-8").read()
    body = text[text.index("enum class Sprite {") + len("enum class Sprite {"):text.index(";")]
    return [n.strip().lower() for n in body.split(",") if n.strip()]


def collect(only=None):
    sprites = {}
    modules = [terrain]
    for name in ("entities", "logic"):
        try:
            modules.append(__import__(name))
        except ImportError:
            pass
    for module in modules:
        sprites.update(module.build())
    if only:
        sprites = {k: v for k, v in sprites.items() if k in only}
    return sprites


def contact_sheet(sprites, path, zoom=4, columns=8):
    names = sorted(sprites)
    max_frames = 1
    cell = SIZE * zoom + 4
    # each entry shows frame 0 big, plus a strip of further frames
    width = columns * (cell * 2)
    rows = (len(names) + columns - 1) // columns
    sheet = blank(width, rows * cell, (54, 52, 64, 255))
    for i, name in enumerate(names):
        frames = sprites[name]
        x = (i % columns) * cell * 2
        y = (i // columns) * cell
        # checker under transparent sprites
        bg = blank(SIZE * zoom, SIZE * zoom, (118, 170, 96, 255) if name.startswith(("pushy", "tree", "box")) else (226, 206, 160, 255))
        paste(sheet, bg, x, y)
        m = (len(frames[0].rows()) - SIZE) // 2
        paste(sheet, scale(frames[0].rows(), zoom), x - m * zoom, y - m * zoom)
        for f, frame in enumerate(frames[1:8]):
            fx = x + SIZE * zoom + 2 + (f % 2) * (SIZE + 1)
            fy = y + (f // 2) * (SIZE + 1)
            paste(sheet, blank(SIZE, SIZE, (226, 206, 160, 255)), fx, fy)
            paste(sheet, frame.rows(), fx, fy)
    write_png(path, sheet)


def write_all(sprites, folder):
    for name, frames in sprites.items():
        for old in os.listdir(folder):
            if re.fullmatch(re.escape(name) + r"@\d+\.png", old):
                os.remove(os.path.join(folder, old))
        for i, frame in enumerate(frames):
            suffix = "" if i == 0 else f"@{i}"
            write_png(os.path.join(folder, f"{name}{suffix}.png"), frame.rows())


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--sheet", help="also write a contact sheet PNG here")
    parser.add_argument("--dry-run", action="store_true", help="don't touch the assets folder")
    parser.add_argument("--only", nargs="*", help="limit to these sprite names")
    args = parser.parse_args()

    sprites = collect(args.only)
    expected = expected_names()
    unknown = sorted(set(sprites) - set(expected))
    if unknown:
        sys.exit(f"Sprites not in Sprite.kt: {unknown}")
    missing = sorted(set(expected) - set(sprites))
    if missing and not args.only:
        print(f"warning: {len(missing)} sprites not drawn yet: {missing}")

    if not args.dry_run:
        write_all(sprites, OUT)
        print(f"wrote {len(sprites)} sprites ({sum(len(f) for f in sprites.values())} frames)")
        if not args.only:
            edges = terrain.edges()
            os.makedirs(EDGES, exist_ok=True)
            for old in os.listdir(EDGES):
                os.remove(os.path.join(EDGES, old))
            write_all(edges, EDGES)
            print(f"wrote {len(edges)} edge overlays")
    if args.sheet:
        contact_sheet(sprites, args.sheet)


if __name__ == "__main__":
    main()

"""Draws the launcher icon (adaptive icon layers) from the game's pixel art.

    python tools/sprites/draw_icon.py [--preview OUT.png]

The art is a 36x36 grid = the 108dp adaptive icon canvas; only the middle ~22 pixels (66dp safe
zone) are guaranteed to be visible, so the island scene lives there and the sea fills the rest.
"""
import argparse
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from canvas import P, mix, noise  # noqa: E402
from png import write_png  # noqa: E402
import entities  # noqa: E402
import terrain  # noqa: E402

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
RES = os.path.join(ROOT, "app", "src", "main", "res")
GRID = 36
DENSITIES = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}
CLEAR = (0, 0, 0, 0)


def blank(color=CLEAR):
    return [[color] * GRID for _ in range(GRID)]


def put(img, x, y, color):
    if 0 <= x < GRID and 0 <= y < GRID and color[3]:
        if color[3] == 255 or img[y][x][3] == 0:
            img[y][x] = color if color[3] == 255 or img[y][x][3] else color
        else:
            k = color[3] / 255
            b = img[y][x]
            img[y][x] = tuple(round(color[i] * k + b[i] * (1 - k)) for i in range(3)) + (255,)


def stamp(img, sprite, ox, oy):
    for y, row in enumerate(sprite.rows()):
        for x, c in enumerate(row):
            put(img, ox + x, oy + y, c)


def background():
    tile = terrain.water(0)
    img = blank()
    for y in range(GRID):
        for x in range(GRID):
            img[y][x] = tile.get(x % 16, y % 16)
    # a touch darker towards the corners, lighter around the island
    for y in range(GRID):
        for x in range(GRID):
            d = math.hypot(x + 0.5 - 18, y + 0.5 - 20) / 18
            if d > 0.95:
                img[y][x] = mix(img[y][x], P["water_deep"], min(0.5, (d - 0.95) * 1.2))
            elif d < 0.55:
                img[y][x] = mix(img[y][x], P["water_light"], (0.55 - d) * 0.5)
    return img


def island(img):
    cx, cy = 18, 24
    for y in range(GRID):
        for x in range(GRID):
            dx = (x + 0.5 - cx) / 10
            dy = (y + 0.5 - cy) / 4.8
            d = dx * dx + dy * dy
            if 1.0 < d <= 1.35:
                put(img, x, y, P["foam"][:3] + (200,))
            elif d <= 1.0:
                c = P["sand"]
                if dy > 0.45 and d > 0.55:
                    c = P["sand_shadow"]
                elif dy < -0.3 and d > 0.5:
                    c = P["sand_light"]
                elif noise(x, y, 7) < 0.1:
                    c = P["sand_dark"]
                put(img, x, y, c)


def palm(img, shift=-3):
    """Palm tree behind Pushy; [shift] moves it sideways."""

    def plot(x, y, color):
        put(img, x + shift, y, color)

    # curved trunk
    trunk = [(23, 25), (23, 24), (23, 23), (23, 22), (23, 21), (24, 20), (24, 19), (24, 18), (24, 17), (25, 16), (25, 15), (25, 14)]
    for (x, y) in trunk:
        plot(x, y, P["wood_light"])
        plot(x + 1, y, P["wood_dark"])
        if y % 2 == 0:
            plot(x + 1, y, P["wood"])
    plot(21, 25, P["shadow"])
    # fronds
    fronds = [
        [(25, 13), (24, 12), (23, 12), (22, 12), (21, 13), (20, 14)],
        [(25, 13), (26, 12), (27, 12), (28, 12), (29, 13), (30, 14)],
        [(25, 13), (25, 12), (24, 11), (23, 10), (22, 10)],
        [(25, 13), (26, 11), (27, 10), (28, 10)],
        [(25, 13), (24, 14), (23, 15), (22, 16)],
        [(25, 13), (26, 14), (27, 15), (28, 16)],
    ]
    for frond in fronds:
        for i, (x, y) in enumerate(frond):
            plot(x, y, P["leaf_light"] if i < len(frond) // 2 else P["leaf"])
            plot(x, y + 1, P["leaf_dark"])
    for (x, y) in [(24, 14), (26, 14)]:
        plot(x, y, P["wood_dark"])  # coconuts
    # outline the crown and trunk
    outline(img, lambda c: c in (P["leaf_light"], P["leaf"], P["leaf_dark"], P["wood_light"], P["wood_dark"], P["wood"]))


def outline(img, is_part):
    marks = []
    for y in range(GRID):
        for x in range(GRID):
            if is_part(img[y][x]):
                continue
            if any(0 <= x + dx < GRID and 0 <= y + dy < GRID and is_part(img[y + dy][x + dx])
                   for dx, dy in [(1, 0), (-1, 0), (0, 1), (0, -1)]):
                marks.append((x, y))
    for (x, y) in marks:
        img[y][x] = P["outline"]


def foreground():
    img = blank()
    island(img)
    palm(img)
    stamp(img, entities.pushy("south"), 7, 9)
    return img


def monochrome():
    """Themed-icon silhouette: Pushy with its eyes cut out."""
    pushy = entities.pushy("south").rows()
    img = blank()
    for y, row in enumerate(pushy):
        for x, c in enumerate(row):
            if c[3] > 200 and c not in (P["white"], P["black"], P["shadow"]):
                img[9 + y][10 + x] = (255, 255, 255, 255)
    return img


def scale(img, size):
    return [[img[y * GRID // size][x * GRID // size] for x in range(size)] for y in range(size)]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--preview", help="write a composed preview PNG here instead of the resources")
    args = parser.parse_args()
    bg, fg, mono = background(), foreground(), monochrome()
    if args.preview:
        composed = [row[:] for row in bg]
        for y in range(GRID):
            for x in range(GRID):
                if fg[y][x][3]:
                    put(composed, x, y, fg[y][x])
        # show the round mask a launcher would apply
        masked = [[c if math.hypot(x + 0.5 - 18, y + 0.5 - 18) <= 12 else (40, 40, 48, 255)
                   for x, c in enumerate(row)] for y, row in enumerate(composed)]
        side = [a + [(40, 40, 48, 255)] * 2 + b for a, b in zip(scale(composed, 216), scale(masked, 216))]
        write_png(args.preview, side)
        return
    for density, size in DENSITIES.items():
        folder = os.path.join(RES, f"mipmap-{density}")
        write_png(os.path.join(folder, "ic_launcher_background.png"), scale(bg, size))
        write_png(os.path.join(folder, "ic_launcher_foreground.png"), scale(fg, size))
        write_png(os.path.join(folder, "ic_launcher_monochrome.png"), scale(mono, size))
    print("wrote launcher icon layers")


if __name__ == "__main__":
    main()

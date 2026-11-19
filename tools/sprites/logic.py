"""Logic parts: cables, gates, lamps, levers … in a shared riveted-metal style."""
import math

from canvas import DIGITS, P, SIZE, Canvas
import terrain

DIRECTIONS = ["north", "east", "south", "west"]


def rotate(c, direction):
    for _ in range(DIRECTIONS.index(direction)):
        c = c.rot90()
    return c


def block(screen=True):
    """Opaque riveted metal block filling the cell."""
    c = Canvas("metal")
    c.rect(0, 0, 15, 0, "metal_light")
    c.rect(0, 0, 0, 15, "metal_light")
    c.rect(15, 0, 15, 15, "metal_dark")
    c.rect(0, 15, 15, 15, "metal_dark")
    for (x, y) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        c.px(x, y, "metal_light")
        c.px(x + 1, y + 1, "metal_dark")
    if screen:
        c.rect(2, 4, 13, 11, "metal_dark")
        c.rect(2, 4, 13, 4, "black")
    return c


# --- cables ---------------------------------------------------------------------------------

def cable_half(direction, active, frame=0):
    """Wire from the centre to one edge (drawn north, then rotated)."""
    c = Canvas()
    c.rect(6, 0, 9, 9, "metal_dark")
    core, hot = ("orange", "yellow_light") if active else ("led_off", None)
    c.rect(7, 0, 8, 8, core)
    c.rect(5, 5, 10, 10, "metal_dark")
    c.rect(6, 6, 9, 9, "metal")
    c.rect(7, 7, 8, 8, core)
    if hot:
        # a spark travelling outwards along the wire
        y = 7 - (frame * 2) % 8
        c.px(7, y, hot)
        c.px(8, y, hot)
        c.px(7, y + 1, "orange_light")
        c.px(8, y + 1, "orange_light")
    return rotate(c, direction)


def cable_icon(active):
    c = terrain.sand()
    for d in DIRECTIONS:
        c.over(cable_half(d, active))
    return c


def port(kind, direction):
    """Small marker at the edge of a gate: inputs point in (cyan), outputs point out (orange)."""
    c = Canvas()
    if kind == "input":
        c.art(["kkkkkk", ".kcck.", "..kk.."], {"k": "metal_dark", "c": "cyan"}, 5, 0)
        c.rect(6, 0, 9, 0, "cyan")
    else:
        c.art(["..kk..", ".kook.", "kooook"], {"k": "metal_dark", "o": "orange_light"}, 5, 0)
    return rotate(c, direction)


GATES = {
    "and": [
        "..#####.....",
        "..#....#....",
        "###.....#...",
        "..#.....####",
        "###.....#...",
        "..#....#....",
        "..#####.....",
    ],
    "or": [
        ".#####......",
        "..#...##....",
        "###.....#...",
        "...#....####",
        "###.....#...",
        "..#...##....",
        ".#####......",
    ],
    "xor": [
        "#.#####.....",
        ".#.#...##...",
        "##.#.....#..",
        "..#.#....###",
        "##.#.....#..",
        ".#.#...##...",
        "#.#####.....",
    ],
    "not": [
        "..#.........",
        "..##........",
        "..#.#..##...",
        "###..##..###",
        "..#.#..##...",
        "..##........",
        "..#.........",
    ],
}


def gate(kind):
    c = block()
    for j, row in enumerate(GATES[kind]):
        for i, v in enumerate(row):
            if v == "#":
                c.px(2 + i, 4 + j, "cyan")
    c.rect(2, 4, 13, 4, "black")
    for j, row in enumerate(GATES[kind]):
        for i, v in enumerate(row):
            if v == "#":
                c.px(2 + i, 4 + j + 1 - 1, "cyan")
    # small glare on the screen
    c.px(12, 5, "metal_light")
    return c


def lamp(on, frame=0):
    c = block(screen=False)
    if on:
        glow = [5.5, 6.3, 7.0][frame]
        c.circle(8, 8, glow, (255, 230, 120, 110))
        c.circle(8, 8, 4.2, "yellow_light", shade=(P["white"], P["yellow"]))
        c.px(6, 6, "white")
    else:
        c.circle(8, 8, 4.2, "glass_dark", shade=(P["glass"], P["metal_light"]))
        c.line(7, 9, 9, 7, "metal_dark")
    c.rect(6, 12, 9, 13, "metal_dark")
    return c


def countdown(n):
    c = block(screen=False)
    c.rect(3, 2, 12, 13, "metal_dark")
    c.rect(3, 2, 12, 2, "black")
    for j, row in enumerate(DIGITS["5"]):  # faint unlit segments behind the digit
        for i, v in enumerate(row):
            c.rect(5 + i * 2, 3 + j * 2, 6 + i * 2, 4 + j * 2, "led_off")
    for j, row in enumerate(DIGITS[str(n)]):
        for i, v in enumerate(row):
            if v == "#":
                c.rect(5 + i * 2, 3 + j * 2, 6 + i * 2, 4 + j * 2, "red")
                c.px(5 + i * 2, 3 + j * 2, "red_light")
    return c


def lever(active):
    c = Canvas()
    c.rect(3, 10, 12, 13, "metal")
    c.rect(3, 10, 12, 10, "metal_light")
    c.rect(3, 13, 12, 13, "metal_dark")
    c.px(4, 12, "red" if not active else "green_light")
    c.px(11, 12, "green_light" if active else "led_off")
    if active:
        c.line(8, 10, 11, 4, "stone_light")
        c.line(9, 10, 12, 4, "stone")
        c.circle(12, 3.5, 1.8, "green", shade=(P["green_light"], P["green_dark"]))
    else:
        c.line(7, 10, 4, 4, "stone_light")
        c.line(8, 10, 5, 4, "stone")
        c.circle(4, 3.5, 1.8, "red", shade=(P["red_light"], P["red_dark"]))
    c.outline()
    return c.shadow(cy=14.4, rx=5.5, ry=1)


def button(pressed):
    c = Canvas()
    c.rect(3, 4, 12, 13, "metal")
    c.rect(3, 4, 12, 4, "metal_light")
    c.rect(3, 13, 12, 13, "metal_dark")
    if pressed:
        c.ellipse(8, 9.5, 3.5, 2, "green", shade=(P["green_light"], P["green_dark"]))
    else:
        c.ellipse(8, 9, 3.6, 3, "red_dark")
        c.ellipse(8, 8, 3.6, 3, "red", shade=(P["red_light"], None))
    c.outline()
    return c.shadow(cy=14.4, rx=5.5, ry=1)


def power_block(frame):
    c = block(screen=False)
    c.rect(4, 3, 11, 13, "metal_dark")
    c.rect(6, 2, 9, 2, "metal_light")
    level = [0, 1, 2, 1][frame]
    c.rect(5, 4, 10, 12, "green_dark")
    c.rect(5, 12 - 2 - level * 2, 10, 12, "green")
    c.art([
        "..yy",
        ".yy.",
        "yyyy",
        ".yy.",
        "yy..",
    ], {"y": "yellow_light"}, 6, 6)
    return c


def repeater(direction, frame):
    c = block()
    arrow = Canvas()
    arrow.art([
        ".......o........",
        "......ooo.......",
        ".....ooOoo......",
        "....ooOOOoo.....",
        "......OOO.......",
        "......OOO.......",
        "......OOO.......",
    ], {"o": "orange", "O": "yellow_light" if frame else "orange_light"}, 0, 4)
    c.over(rotate(arrow, direction))
    return c


def build():
    out = {}
    for d in DIRECTIONS:
        out[f"cable_{d}"] = [cable_half(d, False)]
        out[f"cable_{d}_active"] = [cable_half(d, True, f) for f in range(4)]
        out[f"input_{d}"] = [port("input", d)]
        out[f"output_{d}"] = [port("output", d)]
        out[f"repeater_{d}_active"] = [repeater(d, f) for f in range(2)]
    out["cable"] = [cable_icon(False)]
    out["cable_active"] = [cable_icon(True)]
    for kind in GATES:
        out[f"logic_gate_{kind}"] = [gate(kind)]
    out["lamp"] = [lamp(False)]
    out["lamp_active"] = [lamp(True, f) for f in range(3)]
    for n, name in [(1, "one"), (2, "two"), (3, "three")]:
        out[f"count_down_{name}"] = [countdown(n)]
    out["lever"] = [lever(False)]
    out["lever_active"] = [lever(True)]
    out["button"] = [button(False)]
    out["button_pressed"] = [button(True)]
    out["power_block"] = [power_block(f) for f in range(4)]
    return out

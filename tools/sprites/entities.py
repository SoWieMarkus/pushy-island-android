"""Entities: transparent 16x16 sprites drawn on top of terrain."""
import math

from canvas import P, SIZE, Canvas, digit, mix
import terrain


def finish(c, shadow=True, **shadow_args):
    c.outline()
    if shadow:
        c.shadow(**shadow_args)
    return c


def star_polygon(cx, cy, outer, inner, points=5, rotation=-math.pi / 2):
    verts = []
    for i in range(points * 2):
        r = outer if i % 2 == 0 else inner
        a = rotation + i * math.pi / points
        verts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
    return verts


def fill_polygon(c, verts, color):
    for y in range(SIZE):
        for x in range(SIZE):
            px, py = x + 0.5, y + 0.5
            inside = False
            j = len(verts) - 1
            for i in range(len(verts)):
                xi, yi = verts[i]
                xj, yj = verts[j]
                if (yi > py) != (yj > py) and px < (xj - xi) * (py - yi) / (yj - yi) + xi:
                    inside = not inside
                j = i
            if inside:
                c.px(x, y, color)


# --- Pushy ---------------------------------------------------------------------------------

PUSHY_FRAMES = 10


def pushy(facing, blink=False, squash=0):
    c = Canvas()
    c.ellipse(8, 8.5 + squash * 0.5, 6.5 + squash * 0.3, 6.3 - squash * 0.5, "green",
              shade=(P["green_light"], P["green_dark"]))
    c.px(4, 5, "white")
    c.px(5, 4, "white")
    eyes = {
        "south": [(5, 7), (9, 7)],
        "north": [(5, 3), (9, 3)],
        "east": [(8, 6), (11, 6)],
        "west": [(3, 6), (6, 6)],
    }[facing]
    pupil = {"south": (0, 2), "north": (0, 0), "east": (1, 1), "west": (0, 1)}[facing]
    for (x, y) in eyes:
        if blink:
            c.rect(x, y + 2, x + 1, y + 2, "outline")
        else:
            c.rect(x, y, x + 1, y + 2, "white")
            c.px(x + pupil[0], y + pupil[1], "black")
            c.px(x + pupil[0], y + min(pupil[1] + 1, 2), "black")
    if facing == "south":
        c.art(["k..k", ".kk."], {"k": "green_dark"}, 6, 11)
    if facing in ("east", "west"):
        mouth_x = 10 if facing == "east" else 4
        c.rect(mouth_x, 11, mouth_x + 1, 11, "green_dark")
    return finish(c, cy=14.2, rx=5.5)


def pushy_frames(facing):
    frames = []
    for f in range(PUSHY_FRAMES):
        frames.append(pushy(facing, blink=(f == PUSHY_FRAMES - 1), squash=1 if f in (3, 4) else 0))
    return frames


# --- simple blocks -------------------------------------------------------------------------

def box():
    c = Canvas()
    c.rect(2, 2, 13, 13, "wood")
    c.rect(2, 2, 13, 3, "wood_light")
    c.frame(2, 2, 13, 13, "wood_dark")
    c.frame(3, 4, 12, 12, "wood_dark")
    c.line(4, 5, 11, 11, "wood_light")
    c.line(4, 4, 11, 10, "wood_dark")
    c.rect(4, 12, 11, 12, "wood_dark")
    for (x, y) in [(3, 3), (12, 3), (3, 12), (12, 12)]:
        c.px(x, y, "stone_light")
    return finish(c, cy=14, rx=6.5)


def stone():
    c = Canvas()
    c.ellipse(8, 8.5, 6.5, 5.8, "stone", shade=(P["stone_light"], P["stone_dark"]))
    c.line(9, 6, 10, 8, "stone_dark")
    c.line(10, 8, 9, 10, "stone_dark")
    c.px(5, 10, "stone_dark")
    c.px(6, 10, "stone_dark")
    c.px(5, 5, "white")
    return finish(c, cy=14, rx=6.5)


def barrel():
    c = Canvas()
    for y in range(2, 14):
        half = 4 + round(1.5 * math.sin(math.pi * (y - 2) / 11))
        for x in range(8 - half, 8 + half):
            color = "wood"
            if x in (8 - half,):
                color = "wood_dark"
            elif x == 8 - half + 1 or x == 6:
                color = "wood_light"
            elif x == 8 + half - 1:
                color = "wood_dark"
            elif x in (9, 11):
                color = "wood_dark" if y % 2 else "wood"
            c.px(x, y, color)
    for y in (4, 11):
        for x in range(2, 14):
            if c.get(x, y)[3]:
                c.px(x, y, "metal")
        c.px(5, y, "metal_light")
    c.ellipse(8, 2.6, 4, 1.3, "wood_light")
    return finish(c, cy=14, rx=5.5)


def coconut():
    c = Canvas()
    c.circle(8, 9, 5, "wood", shade=(P["wood_light"], P["wood_dark"]))
    for (x, y) in [(6, 7), (9, 7), (7, 9)]:
        c.px(x, y, "wood_dark")
    c.px(5, 6, "wood_light")
    return finish(c, cy=14, rx=4.5)


def shot():
    c = Canvas()
    c.circle(8, 8.5, 2.6, "stone", shade=(P["stone_light"], P["stone_dark"]))
    return finish(c, cy=11.2, rx=2.5, ry=1)


# --- nature --------------------------------------------------------------------------------

def tree():
    c = Canvas()
    c.art([
        "................",
        "...LLL....LLL...",
        "..LGGGL..LGGGL..",
        ".LGL.LGLLGL.LGL.",
        ".L..LGGccGGL..L.",
        "...LGL.cc.LGL...",
        "..LGL..tT..LGL..",
        "..LL...tT...LL..",
        ".......tT.......",
        "........tT......",
        "........tT......",
        "........tT......",
        "........tT......",
        ".......tTT......",
        "......ttTTT.....",
        "................",
    ], {"L": "leaf", "G": "leaf_light", "c": "wood_dark", "t": "wood_light", "T": "wood_dark"})
    return finish(c, cx=8.5, cy=14.5, rx=4, ry=1.3)


def leaf_up():
    c = Canvas()
    c.art([
        "................",
        "........G.......",
        ".......GGl......",
        "......GGrll.....",
        ".....GGGrlll....",
        ".....GvGrlvl....",
        "....GGGvrvlll...",
        "....GvGGrlvll...",
        "....GGvGrvlll...",
        ".....GGvrvll....",
        "......GGrll.....",
        "........r.......",
        "........r.......",
        ".........r......",
        ".........r......",
        "................",
    ], {"G": "leaf_light", "l": "leaf", "r": "leaf_dark", "v": "leaf_dark"})
    return finish(c, shadow=False)


def flower(color, light, dark):
    c = Canvas()
    c.art([
        "....ll....ll....",
        "...lGl...lGGl...",
        "...lGGl.lGGl....",
        "....lGGlGGl.....",
    ], {"l": "leaf", "G": "leaf_light"}, 0, 10)
    for (dx, dy) in [(0, -3), (3, -1), (2, 2.5), (-2, 2.5), (-3, -1)]:
        c.circle(8 + dx, 7 + dy, 2.3, color)
    c.px(7, 3, light)
    c.px(8, 3, light)
    c.px(4, 6, light)
    c.px(11, 9, dark)
    c.px(9, 10, dark)
    c.circle(8, 7.2, 1.6, "yellow_light" if color != P["yellow"] else "orange")
    return finish(c, cy=14.6, rx=4.5, ry=1.2)


def seed():
    c = Canvas()
    c.ellipse(8, 10, 2.2, 1.6, "wood", shade=(P["wood_light"], P["wood_dark"]))
    c.art(["G.G", ".l.", ".l."], {"G": "leaf_light", "l": "leaf"}, 7, 6)
    return finish(c, shadow=False)


def sea_star():
    c = Canvas()
    fill_polygon(c, star_polygon(8, 8.6, 7, 3, rotation=-math.pi / 2), P["orange"])
    fill_polygon(c, star_polygon(8, 8.6, 4.5, 1.6, rotation=-math.pi / 2), P["orange_light"])
    for (x, y) in [(8, 4), (4, 7), (12, 7), (6, 12), (10, 12)]:
        c.px(x, y, "yellow_light")
    c.px(7, 8, "black")
    c.px(9, 8, "black")
    c.px(8, 10, "red_dark")
    return finish(c, cy=14.5, rx=5.5, ry=1.4)


# --- creatures -----------------------------------------------------------------------------

def crab(big, mood, frame=0):
    c = Canvas()
    rx, ry, cy = (6, 4, 10) if big else (4, 2.8, 10.5)
    # legs
    for i in range(3):
        for side in (-1, 1):
            x0 = 8 + side * (rx - 1)
            c.line(x0, cy + i - 1, x0 + side * 2, cy + i + (1 if (i + frame) % 2 else 0), "red_dark")
    # claws
    claw_y = cy - ry - (1 if frame else 0)
    for side in (-1, 1):
        cx = 8 + side * (rx + (0.5 if big else 0))
        c.circle(cx, claw_y, 2.4 if big else 1.8, "red", shade=(P["red_light"], None))
        c.px(round(cx - 0.5), round(claw_y - (2 if big else 1.5)), None)
        c.line(round(8 + side * (rx - 2)), round(cy - 1), round(cx), round(claw_y + 1), "red_dark")
    c.ellipse(8, cy, rx, ry, "red", shade=(P["red_light"], P["red_dark"]))
    # eyes on stalks
    ex = [6, 10] if big else [7, 9]
    ey = round(cy - ry - (2 if big else 1))
    for x in ex:
        c.px(x, ey + 1, "red_dark")
        c.px(x, ey, "white")
        c.px(x, ey - 1, "white")
        c.px(x, ey, "black")
    if big:
        if mood == "happy":
            c.art(["k...k", ".kkk."], {"k": "red_dark"}, 6, round(cy))
            c.px(4, round(cy), "pink_light")
            c.px(12, round(cy), "pink_light")
            c.art(["p.p", "ppp", ".p."], {"p": "pink"}, 7, 0)
        else:
            c.art([".kkk.", "k...k"], {"k": "red_dark"}, 6, round(cy))
            c.px(10, round(cy) + 1, "cyan_light")
    return finish(c, cy=14.6, rx=rx + 0.5, ry=1.3)


def octopus(frame):
    c = Canvas()
    for i, x in enumerate([3, 6, 9, 12]):
        wiggle = 1 if (i + frame) % 2 else -1
        for y in range(9, 14):
            c.px(x + (wiggle if y in (11, 12) else 0), y, "pink")
            c.px(x + 1 + (wiggle if y in (11, 12) else 0), y, "pink_dark" if y > 11 else "pink")
    c.ellipse(8, 6.8, 5.5, 5, "pink", shade=(P["pink_light"], P["pink_dark"]))
    c.rect(5, 6, 6, 8, "white")
    c.rect(9, 6, 10, 8, "white")
    c.px(6, 7, "black")
    c.px(6, 8, "black")
    c.px(10, 7, "black")
    c.px(10, 8, "black")
    c.art(["k..k", ".kk."], {"k": "pink_dark"}, 6, 9)
    return finish(c, cy=14.6, rx=6, ry=1.2)


def pirate():
    c = Canvas()
    c.art([
        "................",
        ".....KKKKKK.....",
        "....KKKKwKKK....",
        "..KKKKKKKKKKKK..",
        "....ssssssss....",
        "....sPPsssEs....",
        "....ssssssss....",
        ".....bbbbbb.....",
        "....RWRWRWRW....",
        "...sRWRWRWRWs...",
        "...sRWRWRWRWs...",
        "....RWRWRWRW....",
        "....nnnnnnnn....",
        "....nnn..nnn....",
        "....bbb..bbb....",
        "................",
    ], {"K": "black", "w": "white", "s": "skin", "P": "black", "E": "black", "b": "wood_dark",
        "R": "red", "W": "white", "n": "blue_dark"})
    c.px(4, 6, "skin_dark")
    c.px(5, 4, "black")
    return finish(c, cy=14.8, rx=4.5, ry=1.1)


def pirate_hut():
    c = Canvas()
    c.art([
        "................",
        ".......yy.......",
        "......yYYy......",
        ".....yYYlYy.....",
        "....yYlYYYYy....",
        "...yYYYYYlYYy...",
        "..yYlYYYYYYYYy..",
        ".yyyyyyyyyyyyyy.",
        "..wWWWWWWWWWWw..",
        "..wWWddWWWWWWw..",
        "..wWWddWWggWWw..",
        "..wWWddWWggWWw..",
        "..wWWddWWWWWWw..",
        "..wwwddwwwwwww..",
        "................",
        "................",
    ], {"y": "yellow_dark", "Y": "yellow", "l": "yellow_light", "W": "wood", "w": "wood_dark",
        "d": "black", "g": "cyan_light"})
    return finish(c, cy=14.3, rx=6.5, ry=1.2)


def statue(colour):
    c = Canvas()
    base, light, dark = P[colour], P[f"{colour}_light"], P[f"{colour}_dark"]
    c.rect(4, 12, 11, 13, "stone")
    c.rect(4, 12, 11, 12, "stone_light")
    c.rect(5, 3, 10, 11, base)
    c.rect(6, 2, 9, 2, base)
    c.rect(5, 3, 5, 11, light)
    c.rect(6, 2, 6, 2, light)
    c.rect(10, 3, 10, 11, dark)
    c.rect(6, 5, 7, 6, "black")
    c.rect(8, 5, 9, 6, "black")
    c.px(6, 5, "white")
    c.px(8, 5, "white")
    c.rect(6, 9, 9, 9, dark)
    c.px(7, 8, dark)
    c.px(8, 8, dark)
    c.rect(3, 7, 4, 8, base)
    c.rect(11, 7, 12, 8, dark)
    return finish(c, cy=14.5, rx=5, ry=1.2)


# --- items ---------------------------------------------------------------------------------

def coin(frame):
    c = Canvas()
    c.circle(8, 8, 5, "yellow", shade=(P["yellow_light"], P["yellow_dark"]))
    c.ring(8, 8, 3.6, "yellow_dark", 0.9)
    c.rect(7, 6, 8, 9, "yellow_dark")
    c.px(7, 6, "yellow_light")
    glint = [(4, 5), (6, 4), (9, 4), (11, 6), (12, 9)]
    if frame < len(glint):
        x, y = glint[frame]
        c.px(x, y, "white")
        c.px(x + 1, y, "white")
    finish(c, cy=13.8, rx=4, ry=1.1)
    return c


def pearl(frame):
    c = Canvas()
    c.circle(8, 8.5, 3.6, "white", shade=(P["white"], P["glass_dark"]))
    c.px(7, 7, "pink_light")
    if frame in (0, 1):
        c.px(6, 6 + frame, "white")
        c.px(10 - frame, 5, "cyan_light")
    return finish(c, cy=12.6, rx=3, ry=1)


def key():
    c = Canvas()
    c.circle(5, 8, 3, "yellow", shade=(P["yellow_light"], P["yellow_dark"]))
    c.circle(5, 8, 1.2, (0, 0, 0, 0))
    c.rect(7, 7, 13, 8, "yellow")
    c.rect(7, 8, 13, 8, "yellow_dark")
    c.rect(11, 9, 11, 10, "yellow")
    c.rect(13, 9, 13, 11, "yellow")
    c.px(8, 7, "yellow_light")
    return finish(c, cy=12.8, rx=5, ry=1)


def chest(is_open):
    c = Canvas()
    c.rect(2, 7, 13, 13, "wood")
    c.rect(2, 12, 13, 13, "wood_dark")
    if is_open:
        c.rect(2, 2, 13, 4, "wood_dark")
        c.rect(3, 3, 12, 3, "wood")
        c.rect(3, 5, 12, 7, "black")
        for (x, y) in [(4, 6), (6, 5), (8, 6), (10, 5), (11, 6), (5, 7), (9, 7)]:
            c.px(x, y, "yellow")
        c.px(7, 5, "yellow_light")
    else:
        c.ellipse(8, 6, 6.5, 3.5, "wood_light")
        c.rect(2, 6, 13, 7, "wood_light")
        c.rect(2, 7, 13, 7, "wood_dark")
    for x in (4, 11):
        c.rect(x, 4 if not is_open else 7, x, 13, "yellow_dark")
    c.rect(7, 7, 8, 9, "yellow")
    c.px(7, 8, "black")
    return finish(c, cy=14.2, rx=6.5, ry=1.3)


def bottle(filled):
    c = Canvas()
    c.ellipse(8, 10, 4.2, 4.3, "glass")
    if filled:
        c.ellipse(8, 11, 3.6, 3.2, "water")
        c.rect(5, 9, 11, 9, "water_light")
    c.rect(7, 3, 8, 6, "glass")
    c.rect(7, 2, 8, 3, "wood")
    c.px(5, 8, "white")
    c.px(5, 9, "white")
    c.px(7, 4, "white")
    c.px(11, 11, "glass_dark")
    return finish(c, cy=14.4, rx=4, ry=1)


def bomb(frame):
    c = Canvas()
    c.circle(7.5, 9.5, 5, "metal_dark", shade=(P["metal_light"], P["black"]))
    c.rect(8, 3, 10, 4, "metal")
    c.line(10, 3, 12, 1, "rope_dark")
    spark = [("yellow_light", "orange"), ("white", "yellow"), ("orange", "red")][frame % 3]
    c.px(13, 0, spark[0])
    c.px(12, 0, spark[1])
    c.px(13, 1, spark[1])
    c.px(14, 0, spark[1] if frame else None)
    c.px(5, 7, "white")
    return finish(c, cx=7.5, cy=14.5, rx=4.5, ry=1.2)


def buoy(frame):
    c = Canvas()
    bob = 1 if frame in (1, 2) else 0
    c.ellipse(8, 12.5, 6, 1.6, (212, 241, 255, 160))
    c.circle(8, 8 + bob, 4.3, "red", shade=(P["red_light"], P["red_dark"]))
    c.rect(4, 7 + bob, 12, 8 + bob, "white")
    c.rect(7, 2 + bob, 8, 4 + bob, "metal")
    c.px(7, 1 + bob, "yellow_light" if frame % 2 == 0 else "yellow")
    return finish(c, shadow=False)


def energy(frame):
    c = Canvas()
    glow = [0.0, 0.35, 0.6, 0.35][frame]
    if glow:
        c.circle(8, 8, 6.5, (255, 240, 143, int(90 * glow)))
    c.art([
        ".........yy.....",
        "........yYy.....",
        ".......yYy......",
        "......yYy.......",
        ".....yYYyyyy....",
        "....yYYYYYYy....",
        "....yyyyYYy.....",
        ".......yYy......",
        "......yYy.......",
        ".....yYy........",
        ".....yy.........",
    ], {"y": "yellow", "Y": "yellow_light"}, 0, 2)
    c.outline("yellow_dark")
    return c


def slingshot():
    c = Canvas()
    c.art([
        "................",
        "...tt......tt...",
        "...tT......tT...",
        "...tTr....rtT...",
        "...tT.r..r.tT...",
        "....tT.rr.tT....",
        ".....tTppTT.....",
        "......tTTT......",
        ".......tT.......",
        ".......tT.......",
        ".......tT.......",
        ".......tT.......",
        "......ttTT......",
        "................",
    ], {"t": "wood_light", "T": "wood_dark", "r": "red_dark", "p": "wood"})
    return finish(c, cy=13.6, rx=3, ry=1)


def shell_clam(state):
    c = Canvas()
    if state != "closed":
        # upper half tilted open
        c.ellipse(8, 4.5, 5.5, 3, "pink", shade=(P["pink_light"], P["pink_dark"]))
        for x in (5, 8, 11):
            c.line(8, 7, x, 2, "pink_dark")
        c.ellipse(8, 10, 6, 4, "pink_light")
        c.ellipse(8, 10, 4.8, 3, mix(P["pink_light"], P["white"], 0.5))
        if state == "pearl":
            c.circle(8, 10, 2.2, "white", shade=(P["white"], P["glass_dark"]))
            c.px(7, 9, "white")
    else:
        c.ellipse(8, 9, 6.5, 5, "pink", shade=(P["pink_light"], P["pink_dark"]))
        for x in (3, 5, 8, 11, 13):
            c.line(8, 13, x, 5, "pink_dark")
        c.rect(6, 13, 9, 13, "pink_dark")
    return finish(c, cy=14.3, rx=6, ry=1.2)


def snail_shell(mood):
    """Push shell. Each push makes it happier (sad → meh → happy); it must be left happy."""
    base, light, dark = {
        "sad": (P["red"], P["red_light"], P["red_dark"]),
        "meh": (P["blue"], P["blue_light"], P["blue_dark"]),
        "happy": (P["orange"], P["orange_light"], P["yellow_dark"]),
    }[mood]
    c = Canvas()
    c.ellipse(5, 13, 3.4, 1.6, dark)  # little foot peeking out
    c.circle(8.5, 8, 5.8, base, shade=(light, dark))
    for i in range(30):  # spiral at the back
        t = i / 30 * 2.4 * math.pi
        r = 3.0 * (1 - i / 34)
        c.px(round(10.5 + math.cos(t) * r), round(9.5 + math.sin(t) * r), light)
    k = P["outline"]
    for (x, y) in [(4, 5), (7, 4)]:  # eyes
        c.rect(x, y, x + 1, y + 1, "white")
        c.px(x + 1, y + 1, "black")
    if mood == "sad":
        c.line(4, 4, 5, 3, k)  # worried brows
        c.line(7, 2, 8, 3, k)
        c.art([".kk.", "k..k"], {"k": k}, 4, 8)
        c.px(3, 7, "cyan_light")  # a tear
        c.px(3, 8, "cyan")
    elif mood == "meh":
        c.line(4, 9, 7, 8, k)
    else:
        c.art(["k...k", ".kkk."], {"k": k}, 3, 8)
        c.px(3, 7, "pink_light")  # blush
        c.px(9, 6, "pink_light")
    return finish(c, cy=14.5, rx=5.5, ry=1.1)


def tablet(ch, color="purple"):
    c = Canvas()
    base, light, dark = P[color], P[f"{color}_light"], P[f"{color}_dark"]
    c.rect(3, 3, 12, 13, base)
    c.rect(4, 2, 11, 2, base)
    c.rect(3, 3, 3, 13, light)
    c.rect(4, 2, 11, 2, light)
    c.rect(12, 3, 12, 13, dark)
    c.rect(3, 13, 12, 13, dark)
    glyph = "yellow" if ch != "?" else light
    from canvas import DIGITS
    for j, row in enumerate(DIGITS[ch]):
        for i, v in enumerate(row):
            if v == "#":
                c.rect(6 + i * 2, 4 + j * 2, 7 + i * 2, 5 + j * 2, dark)
    for j, row in enumerate(DIGITS[ch]):
        for i, v in enumerate(row):
            if v == "#":
                c.rect(5 + i * 2, 3 + j * 2, 6 + i * 2, 4 + j * 2, glyph)
    return finish(c, cy=14.5, rx=5.5, ry=1.2)


def tower(lit, frame=0):
    c = Canvas()
    c.rect(4, 12, 11, 13, "stone")
    c.rect(4, 12, 11, 12, "stone_light")
    for y in range(4, 12):
        half = 2 + (y - 4) // 4
        for x in range(8 - half - 1, 8 + half + 1):
            stripe = (y // 2) % 2 == 0
            c.px(x, y, "red" if stripe else "white")
        c.px(8 - half - 1, y, "red_dark" if (y // 2) % 2 == 0 else "stone_light")
    c.rect(6, 2, 9, 3, "glass_dark" if not lit else "yellow_light")
    c.rect(5, 1, 10, 1, "red_dark")
    c.px(7, 0, "red_dark")
    c.px(8, 0, "red_dark")
    if lit:
        c.px(7, 2, "white")
        reach = [2, 3, 4][frame % 3]
        for i in range(1, reach + 1):
            c.px(5 - i, 2, (255, 240, 143, 220 - i * 40))
            c.px(10 + i, 2, (255, 240, 143, 220 - i * 40))
    return finish(c, cy=14.4, rx=4.5, ry=1)


def finish_hut(frame):
    """Goal hut with an open archway at the bottom: Pushy can only walk in from the south."""
    c = Canvas()
    c.art([
        "................",
        "................",
        "................",
        "...yyyyyyyyyy...",
        "..yYlYYYlYYYYy..",
        ".yYYYYlYYYYlYYy.",
        ".dddddddddddddd.",
        "..hWWWWWWWWWWw..",
        "..hWWWWkkWWWWw..",
        "..hWWkkkkkkWWw..",
        "..hWkkkkkkkkWw..",
        "..hWkkkkkkkkWw..",
        "..hWkkkkkkkkWw..",
        "..hWkkkkkkkkWw..",
        "..wwkkkkkkkkww..",
        "....ssssssss....",
    ], {"y": "yellow_dark", "Y": "yellow", "l": "yellow_light", "d": "wood_dark", "W": "wood",
        "h": "wood_light", "w": "wood_dark", "k": "outline", "s": "sand_shadow"})
    # Pole with a little checkered pennant on the roof.
    c.rect(7, 0, 7, 2, "wood_dark")
    for y in range(0, 2):
        for x in range(8, 12):
            wave = round(0.6 * math.sin((x - 8) / 1.5 + frame * 2 * math.pi / 3)) if x > 8 else 0
            c.px(x, y + wave, "white" if (x + y) % 2 == 0 else "red")
    return finish(c, shadow=False)


def leaf_changer(direction):
    c = Canvas()
    c.rect(2, 3, 13, 13, "stone")
    c.rect(2, 3, 13, 3, "stone_light")
    c.rect(2, 3, 2, 13, "stone_light")
    c.rect(13, 3, 13, 13, "stone_dark")
    c.rect(2, 13, 13, 13, "stone_dark")
    arrow = Canvas()
    arrow.art([
        "................",
        "................",
        "................",
        "................",
        ".......G........",
        "......GGl.......",
        ".....GGGll......",
        "....GGGrlll.....",
        ".......r........",
        ".......r........",
        ".......r........",
        "................",
    ], {"G": "leaf_light", "l": "leaf", "r": "leaf_dark"}, 0, 0)
    turns = {"north": 0, "east": 1, "south": 2, "west": 3}[direction]
    for _ in range(turns):
        arrow = arrow.rot90()
    c.over(arrow, 0, 0 if direction != "north" else 0)
    return finish(c, cy=14.6, rx=6.5, ry=1.1)


def string_piece(edges):
    """Twine across the cell between the given edges (or from the centre if just one)."""
    c = Canvas()
    reach = {
        "north": [(x, y) for y in range(0, 9) for x in (7, 8)],
        "south": [(x, y) for y in range(7, 16) for x in (7, 8)],
        "west": [(x, y) for x in range(0, 9) for y in (7, 8)],
        "east": [(x, y) for x in range(7, 16) for y in (7, 8)],
    }
    for edge in edges:
        for (x, y) in reach[edge]:
            c.px(x, y, "rope_dark" if (x + y) % 3 == 0 else "rope")
    if len(edges) == 1:
        c.rect(6, 6, 9, 9, "rope")
        c.px(7, 7, "rope_dark")
        c.px(8, 8, "rope_dark")
    c.outline("rope_dark")
    return c


def build():
    out = {}
    out["pushy_south"] = pushy_frames("south")
    out["pushy"] = pushy_frames("north")
    out["pushy_east"] = pushy_frames("east")
    out["pushy_west"] = pushy_frames("west")

    out["box"] = [box()]
    out["stone"] = [stone()]
    out["barrel"] = [barrel()]
    out["coconut"] = [coconut()]
    out["shot"] = [shot()]
    out["tree"] = [tree()]

    up = leaf_up()
    out["leaf_up"] = [up]
    out["leaf_right"] = [up.rot90()]
    out["leaf_down"] = [up.rot90().rot90()]
    out["leaf_left"] = [up.rot90().rot90().rot90()]
    for d in ("north", "east", "south", "west"):
        out[f"leaf_changer_{d}"] = [leaf_changer(d)]

    out["flower_red"] = [flower(P["red"], P["red_light"], P["red_dark"])]
    out["flower_yellow"] = [flower(P["yellow"], P["yellow_light"], P["yellow_dark"])]
    out["seed"] = [seed()]
    farm = terrain.build()["farm"][0].copy()
    out["farm_with_seed"] = [farm.over(seed())]
    out["sea_star"] = [sea_star()]

    out["crab_baby"] = [crab(False, None, 0), crab(False, None, 1)]
    out["crab_mother"] = [crab(True, "sad", 0), crab(True, "sad", 1)]
    out["crab_mother_happy"] = [crab(True, "happy", 0), crab(True, "happy", 1)]
    out["octopus"] = [octopus(0), octopus(1)]
    out["pirate"] = [pirate()]
    out["pirate_hut"] = [pirate_hut()]
    for colour in ("red", "green", "blue"):
        out[f"statue_{colour}"] = [statue(colour)]

    out["coin"] = [coin(f) for f in range(8)]
    out["pearl"] = [pearl(f) for f in range(6)]
    out["key"] = [key()]
    out["chest"] = [chest(False)]
    out["chest_open"] = [chest(True)]
    out["bottle"] = [bottle(False)]
    out["bottle_with_water"] = [bottle(True)]
    out["bomb"] = [bomb(f) for f in range(3)]
    out["buoy"] = [buoy(f) for f in range(4)]
    out["energy"] = [energy(f) for f in range(4)]
    out["slingshot"] = [slingshot()]

    out["shell"] = [shell_clam("closed")]
    out["shell_open"] = [shell_clam("open")]
    out["shell_open_with_pearl"] = [shell_clam("pearl")]
    out["shell_1"] = [snail_shell("sad")]
    out["shell_2"] = [snail_shell("meh")]
    out["shell_3"] = [snail_shell("happy")]

    for n, ch in [("one", "1"), ("two", "2"), ("three", "3"), ("four", "4"), ("five", "5")]:
        out[f"count_{n}"] = [tablet(ch)]
    out["count_hidden"] = [tablet("?")]

    out["tower"] = [tower(False)]
    out["tower_with_pearl"] = [tower(True, f) for f in range(3)]
    out["finish"] = [finish_hut(f) for f in range(3)]

    out["string_north_south"] = [string_piece(["north", "south"])]
    out["string_east_west"] = [string_piece(["east", "west"])]
    out["string_north_east"] = [string_piece(["north", "east"])]
    out["string_east_south"] = [string_piece(["east", "south"])]
    out["string_south_west"] = [string_piece(["south", "west"])]
    out["string_west_north"] = [string_piece(["west", "north"])]
    for d in ("north", "east", "south", "west"):
        out[f"string_half_{d}"] = [string_piece([d])]
    return out

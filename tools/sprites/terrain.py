"""Terrain tiles: opaque 16x16 ground pieces (plus a few transparent terrain overlays)."""
import math

from canvas import P, SIZE, Canvas, noise, texture, mix

WATER_FRAMES = 12


# --- base materials ------------------------------------------------------------------------

def sand():
    c = texture("sand", ["sand_light", "sand_dark", "sand_light", "sand_shadow"], seed=3, density=0.10)
    return c


def grass():
    c = texture("grass", ["grass_light", "grass_dark"], seed=11, density=0.10)
    # little blades
    for (x, y) in [(2, 3), (10, 2), (6, 8), (13, 10), (3, 12), (9, 13), (14, 5)]:
        c.px(x, y, "grass_dark")
        c.px(x, y - 1, "grass_dark")
        c.px(x + 1, y - 1, "grass_light")
    return c


# Wavelets: (x, y, length, phase). Irregular on purpose, and they never cross the tile edge, so
# neighbouring tiles can run with different timing (the renderer offsets each tile).
WAVELETS = [(2, 3, 3, 0), (10, 2, 2, 7), (6, 7, 4, 3), (12, 10, 2, 9), (3, 12, 3, 5), (9, 14, 2, 1)]


def water(frame):
    """Calm water: a few wavelets swell up, crest and fade out again, each on its own beat."""
    c = Canvas("water")
    # a few large, soft darker patches instead of per-pixel noise
    for (cx, cy, rx, ry) in [(5, 6, 3.5, 1.4)]:
        c.ellipse(cx, cy, rx, ry, mix(P["water"], P["water_deep"], 0.22))
    for (x, y, length, phase) in WAVELETS:
        life = (frame + phase) % WATER_FRAMES  # 0..11; visible for the first 7 steps
        if life >= 7:
            continue
        drift = life // 3  # drifts one pixel every few frames
        x += drift
        if life in (0, 6):  # faint start / end
            c.px(x + length // 2, y, mix(P["water"], P["water_light"], 0.5))
            continue
        for i in range(length):
            c.px(x + i, y, "water_light")
        if life in (2, 3, 4):  # crest
            c.px(x + 1, y - 1, "water_light")
            if life == 3:
                c.px(x + 1, y - 1, "foam")
        c.px(x + length, y + 1, mix(P["water"], P["water_deep"], 0.6))
    if frame == 4:
        c.px(13, 5, "white")
    return c


def water_frames():
    return [water(f) for f in range(WATER_FRAMES)]


# --- edges ---------------------------------------------------------------------------------

def corner_mask(corner, radius=7):
    """True for pixels *outside* the rounded [corner] ("top_left", …)."""
    cx = radius if "left" in corner else SIZE - radius
    cy = radius if "top" in corner else SIZE - radius

    def outside(x, y):
        in_x = x < cx if "left" in corner else x >= cx
        in_y = y < cy if "top" in corner else y >= cy
        return in_x and in_y and math.hypot(x + 0.5 - cx, y + 0.5 - cy) > radius

    def edge(x, y):
        in_x = x < cx if "left" in corner else x >= cx
        in_y = y < cy if "top" in corner else y >= cy
        return in_x and in_y and radius - 1.3 < math.hypot(x + 0.5 - cx, y + 0.5 - cy) <= radius

    return outside, edge


def rounded(top, bottom, corner, edge_color, rim=None):
    """[top] tile with one rounded corner showing [bottom] underneath."""
    out, edge = corner_mask(corner)
    c = top.copy()
    for y in range(SIZE):
        for x in range(SIZE):
            if out(x, y):
                c.p[y][x] = bottom.p[y][x]
            elif edge(x, y):
                c.px(x, y, edge_color)
    if rim:
        # one pixel of foam / shadow just outside the edge
        for y in range(SIZE):
            for x in range(SIZE):
                if out(x, y) and any(0 <= x + dx < SIZE and 0 <= y + dy < SIZE and not out(x + dx, y + dy)
                                     for dx, dy in [(1, 0), (-1, 0), (0, 1), (0, -1)]):
                    c.px(x, y, rim)
    return c


# --- decorated ground ----------------------------------------------------------------------

def plate(base, inner="stone_light", ring="stone_dark"):
    """Round stone pressure plate set into the ground."""
    c = base.copy()
    c.circle(8, 8.5, 6, ring)
    c.circle(8, 8, 5.5, inner)
    c.circle(8, 8, 4.5, "stone")
    c.px(5, 5, "white")
    c.px(6, 4, "white")
    return c


def flower_icon(c, color, light, cx=8, cy=8):
    for (dx, dy) in [(0, -2), (0, 2), (-2, 0), (2, 0)]:
        c.circle(cx + dx, cy + dy, 1.6, color)
    c.px(cx - 2, cy - 3, light)
    c.circle(cx, cy, 1.2, "yellow")


def build():
    s = sand()
    g = grass()
    w = water_frames()
    out = {}

    out["sand"] = [s]
    out["grass"] = [g]
    out["water"] = w

    for corner in ["top_left", "top_right", "bottom_left", "bottom_right"]:
        out[f"sand_{corner}"] = [rounded(s, wf, corner, "sand_light", rim="foam") for wf in w]
        out[f"grass_{corner}"] = [rounded(g, s, corner, "grass_edge", rim="sand_shadow")]

    # Sunken box: a soggy crate under the surface.
    def box_water(frame):
        c = w[frame].copy()
        c.rect(3, 3, 12, 12, mix(P["wood_dark"], P["water_deep"], 0.45))
        c.rect(4, 4, 11, 11, mix(P["wood"], P["water"], 0.5))
        c.line(4, 4, 11, 11, mix(P["wood_dark"], P["water_deep"], 0.4))
        c.line(11, 4, 4, 11, mix(P["wood_dark"], P["water_deep"], 0.4))
        sway = round(math.sin(2 * math.pi * frame / WATER_FRAMES))
        for x in range(5 + sway, 10 + sway):
            c.px(x, 2, "water_light")
        c.px(7 + sway, 13, "foam")
        return c
    out["box_water"] = [box_water(f) for f in range(WATER_FRAMES)]

    # Boat (terrain on water) – top-down rowing boat, water moving underneath.
    def boat(frame):
        c = w[frame].copy()
        hull = Canvas()
        hull.art([
            "................",
            "......kkkk......",
            ".....kwwwwk.....",
            "....kwllllwk....",
            "....kwlddlwk....",
            "...kwlddddlwk...",
            "...kwbbbbbbwk...",
            "...kwlddddlwk...",
            "...kwlddddlwk...",
            "...kwbbbbbbwk...",
            "...kwlddddlwk...",
            "...kwlddddlwk...",
            "....kwllllwk....",
            "....kwwwwwwk....",
            ".....kkkkkk.....",
            "................",
        ], {"k": "outline", "w": "wood_dark", "l": "wood_light", "d": "wood", "b": "wood_dark"})
        bob = 1 if frame in (2, 3, 4) else 0
        c.over(hull, 0, bob)
        return c
    out["boat"] = [boat(f) for f in range(WATER_FRAMES)]

    # Ponds on sand: spring, water hole.
    def pond(frame, spring):
        c = s.copy()
        c.ellipse(8, 8.5, 6.5, 5.5, "sand_shadow")
        c.ellipse(8, 8.5, 5.5, 4.6, "water")
        c.ellipse(8, 9, 4.5, 3.4, "water_deep")
        t = frame / WATER_FRAMES
        if spring:
            r = 1 + 3.2 * t
            c.ring(8, 8.5, r, "foam" if t < 0.6 else "water_light", 0.9)
            c.circle(8, 8.5, 1.2, "foam")
            c.px(7, 6 - (frame % 2), "white")
        else:
            c.px(5 + frame % 4, 7, "water_light")
            c.px(6 + frame % 4, 7, "water_light")
            c.px(10 - frame % 3, 10, "water_light")
        c.px(4, 6, "sand_light")
        return c
    out["spring"] = [pond(f, True) for f in range(WATER_FRAMES)]
    out["water_hole"] = [pond(f, False) for f in range(WATER_FRAMES)]

    # Farm: one small patch of dug-up soil – room for a single seed.
    farm = s.copy()
    farm.ellipse(8, 9, 5.5, 4, "sand_shadow")
    farm.ellipse(8, 8.6, 4.8, 3.3, "wood")
    farm.ellipse(8, 9.2, 3.4, 2.2, "wood_dark")
    for (x, y) in [(5, 7), (9, 6), (11, 9), (4, 10)]:
        farm.px(x, y, "wood_light")
    farm.px(8, 9, P["black"])
    farm.px(7, 9, "wood_dark")
    out["farm"] = [farm]

    # Statue goals: carved pedestal outline in the statue's colour.
    for colour in ["red", "green", "blue"]:
        c = s.copy()
        c.rect(2, 2, 13, 13, "sand_shadow")
        c.rect(3, 3, 12, 12, "sand_dark")
        c.frame(3, 3, 12, 12, P[colour])
        c.frame(4, 4, 11, 11, P[f"{colour}_light"])
        c.circle(8, 8, 2, P[f"{colour}_dark"])
        out[f"statue_finish_{colour}"] = [c]

    # Bomb field: scorched ground with a warning mark.
    bf = s.copy()
    bf.circle(8, 8, 6.5, "sand_shadow")
    bf.circle(8, 8, 5.5, mix(P["sand_shadow"], P["black"], 0.35))
    bf.ring(8, 8, 5.5, "red", 1)
    bf.line(5, 5, 10, 10, "red_dark")
    bf.line(10, 5, 5, 10, "red_dark")
    bf.line(5, 6, 10, 11, "red")
    bf.line(10, 6, 5, 11, "red")
    out["bomb_field"] = [bf]

    # Holes.
    hole = s.copy()
    hole.ellipse(8, 8.5, 6.5, 6, "sand_shadow")
    hole.ellipse(8, 9, 5.5, 5, "black")
    hole.ellipse(8, 7.5, 5, 3, mix(P["black"], P["sand_shadow"], 0.35))
    hole.ellipse(8, 10, 3.5, 2.5, P["black"])
    out["hole"] = [hole]
    filled = s.copy()
    filled.ellipse(8, 8.5, 6, 5.5, "sand_shadow")
    filled.ellipse(8, 8.2, 5, 4.6, "sand_dark")
    for (x, y) in [(6, 7), (9, 9), (7, 10), (10, 6)]:
        filled.px(x, y, "sand_shadow")
    out["hole_filled"] = [filled]

    # Spikes: steel cones out of the sand; retracted: just the holes.
    spikes_down = s.copy()
    spikes = s.copy()
    for (cx, cy) in [(4, 4), (11, 4), (4, 11), (11, 11), (8, 8)]:
        spikes_down.rect(cx - 1, cy, cx + 1, cy + 1, "sand_shadow")
        spikes_down.px(cx, cy, "black")
        spikes.ellipse(cx + 0.5, cy + 2, 2.5, 1.2, P["shadow"])
        spikes.art([
            "..w..",
            ".lsd.",
            ".lsd.",
            "lssdd",
        ], {"w": "white", "l": "stone_light", "s": "stone", "d": "stone_dark"}, cx - 2, cy - 2)
    out["spikes"] = [spikes]
    out["spikes_deactivated"] = [spikes_down]

    # Spike pressure plate: overlay (drawn on top of sand).
    spp = Canvas()
    spp.rect(3, 3, 12, 12, "stone_dark")
    spp.rect(4, 4, 11, 11, "stone")
    spp.frame(4, 4, 11, 11, "stone_light")
    for x in (6, 9):
        spp.art(["w.", "ls", "ld"], {"w": "white", "l": "stone_light", "s": "stone_dark", "d": "metal_dark"}, x, 6)
    out["spike_pressure_plate"] = [spp]

    # Teleporters: swirling rings.
    def portal(frame, a, b, glow):
        c = s.copy()
        c.circle(8, 8.5, 6.8, "sand_shadow")
        c.circle(8, 8, 6.4, "stone_dark")
        c.circle(8, 8, 5.4, a)
        for i in range(10):
            ang = 2 * math.pi * (i / 10) + frame * math.pi / 4
            r = 1.5 + 3.2 * (i % 5) / 4
            c.px(round(7.5 + math.cos(ang) * r), round(7.5 + math.sin(ang) * r), b)
        c.circle(8, 8, 1.5, glow)
        return c
    out["teleporter"] = [portal(f, P["blue"], P["cyan_light"], P["white"]) for f in range(8)]
    out["item_teleporter"] = [portal(f, P["purple"], P["pink_light"], P["white"]) for f in range(8)]

    # Invisible water: damp sand with puddles – walkable until it floods.
    iw = texture("wet_sand", ["sand_dark", "glass_dark"], seed=5, density=0.14)
    for (x, y, l) in [(2, 3, 4), (9, 6, 5), (4, 11, 3), (11, 12, 3)]:
        for i in range(l):
            iw.px(x + i, y, "glass_dark")
        iw.px(x + 1, y - 1, "glass")
    out["water_invisible"] = [iw]
    wip = plate(s)
    wip.art([".w.", "wbw", "bbb", ".b."], {"w": "water_light", "b": "water"}, 7, 6)
    wip.px(8, 6, "foam")
    out["water_invisible_pressure_plate"] = [wip]

    # Coconut tunnel: a carved channel opening.
    ct = s.copy()
    ct.circle(8, 8, 6, "sand_shadow")
    ct.circle(8, 8.5, 5, "wood_dark")
    ct.circle(8, 9, 3.6, P["black"])
    out["coconut_tunnel"] = [ct]
    ctf = ct.copy()
    ctf.ring(8, 8, 6.4, "yellow", 1)
    ctf.circle(8, 9, 1.4, "yellow_dark")
    out["coconut_tunnel_finish"] = [ctf]

    # Ice floe: an irregular slab with snow on top and a thick bluish edge, drifting on the water
    # (drawn on top of the animated water tile, which shows through around it).
    ice = Canvas()
    ice.art([
        "................",
        "..oooooooooo....",
        ".otwwwwhtttttoo.",
        ".otwwhhttttcttto",
        ".otttttttcctttto",
        "..ottttttcttwwto",
        "..otthttttttwwto",
        ".ottttttttttttto",
        ".otcctttthtttto.",
        ".ottcctttttttto.",
        ".otttttttttwwwo.",
        "..ottwwttttwwto.",
        "..osttttttttsso.",
        "...osssssssssdo.",
        "....oodddddddo..",
        "................",
    ], {
        "o": mix(P["glass_dark"], P["water_deep"], 0.35),
        "t": "glass",
        "w": "white",
        "h": "cyan_light",
        "c": "glass_dark",
        "s": mix(P["cyan"], P["water"], 0.35),
        "d": mix(P["water"], P["water_deep"], 0.4),
    })
    # soft shadow of the floe in the water, down and to the right
    shadow = Canvas()
    for y in range(SIZE):
        for x in range(SIZE):
            if ice.get(x, y)[3] == 0 and x > 0 and y > 0 and ice.get(x - 1, y - 1)[3]:
                shadow.px(x, y, P["water_deep"][:3] + (150,))
    shadow.over(ice)
    out["ice"] = [shadow]

    # Flower pressure plates (on grass).
    for name, colour, light in [("red", P["red"], P["red_light"]), ("yellow", P["yellow"], P["yellow_light"])]:
        c = plate(g)
        flower_icon(c, colour, light)
        out[f"flower_{name}_pressure_plate"] = [c]

    # String pressure plate: a spool of twine.
    sp = plate(s)
    sp.rect(5, 6, 10, 10, "rope")
    for y in (7, 9):
        sp.line(5, y, 10, y, "rope_dark")
    sp.rect(4, 5, 11, 5, "wood")
    sp.rect(4, 11, 11, 11, "wood")
    out["string_pressure_plate"] = [sp]

    # Changeable flowers growing in sand.
    for name, petal, light, centre in [("red", "red", "red_light", "yellow"), ("green", "leaf", "leaf_light", "leaf_dark")]:
        c = s.copy()
        c.ellipse(8, 12, 4.5, 1.6, "sand_shadow")
        c.line(8, 9, 8, 12, "leaf_dark")
        c.art(["ll...", ".lll."], {"l": "leaf_light"}, 4, 10)
        if name == "red":
            for (dx, dy) in [(0, -2), (0, 2), (-2, 0), (2, 0)]:
                c.circle(8 + dx, 7 + dy, 1.8, petal)
            c.px(7, 4, light)
            c.px(5, 6, light)
            c.circle(8, 7, 1.2, centre)
        else:
            c.ellipse(8, 7, 2.6, 3.6, petal, shade=(P["leaf_light"], P["leaf_dark"]))
            c.line(8, 4, 8, 9, "leaf_dark")
        out[f"changeable_flower_{name}"] = [c]

    # Barrel goal: painted circle.
    bfin = s.copy()
    for i in range(16):
        ang = 2 * math.pi * i / 16
        if i % 2 == 0:
            bfin.px(round(7.5 + 5.5 * math.cos(ang)), round(7.5 + 5.5 * math.sin(ang)), "wood_dark")
            bfin.px(round(7.5 + 5.0 * math.cos(ang)), round(7.5 + 5.0 * math.sin(ang)), "wood")
    bfin.circle(8, 8, 1.5, "wood")
    out["barrel_finish"] = [bfin]

    # Door (closed): stone frame with a plank door.
    door = Canvas("stone_dark")
    door.rect(0, 0, 15, 1, "stone_light")
    door.rect(2, 2, 13, 15, "wood_dark")
    door.rect(3, 3, 12, 15, "wood")
    for x in (5, 8, 11):
        door.line(x, 3, x, 15, "wood_dark")
    door.rect(3, 5, 12, 5, "metal")
    door.rect(3, 12, 12, 12, "metal")
    door.px(10, 9, "yellow")
    door.px(10, 8, "yellow_light")
    out["door"] = [door]

    # Logic pressure plate: steel plate with a circuit pattern.
    lp = s.copy()
    lp.rect(2, 2, 13, 13, "metal_dark")
    lp.rect(3, 3, 12, 12, "metal")
    lp.frame(3, 3, 12, 12, "metal_light")
    lp.line(5, 8, 10, 8, "orange")
    lp.line(8, 5, 8, 10, "orange")
    lp.rect(7, 7, 8, 8, "orange_light")
    out["logic_pressure_plate"] = [lp]

    return out

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


# --- automatic transitions ------------------------------------------------------------------
# Transparent overlays the renderer lays over the terrain wherever two materials meet, so plain
# sand next to plain water gets a shoreline instead of a hard square edge. They live in
# sprites/edges/ and are named <kind>_<low>_<high>_<where>:
#   edge   – on the lower cell, the higher material lies towards <where> (n/e/s/w); a "_cut_<ends>"
#            variant stops short at those ends because the neighbour's corner there is rounded off
#   lip    – on the higher cell, the lower material lies towards <where>
#   bite   – on the higher cell, both sides of corner <where> (nw/ne/se/sw) are lower: rounds it off
# Inside corners stay square: the two straight edges meeting there already read as one bend.

EDGE_FRAMES = 4
ROTATE = {"n": 0, "e": 1, "s": 2, "w": 3, "nw": 0, "ne": 1, "se": 2, "sw": 3}


def rotated(c, where):
    for _ in range(ROTATE[where]):
        c = c.rot90()
    return c


def alpha(colour, a):
    return P[colour][:3] + (a,)


def foam_edge(frame):
    """Surf along the top of a water cell, gently lapping over [EDGE_FRAMES] frames."""
    c = Canvas()
    for x in range(SIZE):
        gap = noise(x, frame, 21) < 0.18
        c.px(x, 0, "water_light" if gap else "foam")
        if (x + frame * 3) % 6 < 3:
            c.px(x, 1, alpha("foam", 170))
        else:
            c.px(x, 1, alpha("water_light", 150))
        if (x + frame * 2) % 4 != 0:
            c.px(x, 2, alpha("water_light", 90))
    return c


def land_lip(high):
    """The very edge of the land facing north into lower ground."""
    c = Canvas()
    for x in range(SIZE):
        if high == "sand":
            c.px(x, 0, alpha("wet_sand", 220))
            if noise(x, 1, 5) < 0.5:
                c.px(x, 1, alpha("wet_sand", 110))
        else:
            c.px(x, 0, "grass_edge")
            if noise(x, 2, 7) < 0.35:
                c.px(x, 1, "grass_dark")
    return c


# Grass hills cast a drop shadow down and to the right (light from the top-left). It is worked
# out from the actual outline of the grass, rounded corners included, so straight edges, curves
# and inside corners all join up. Masks are functions (x, y) -> bool in the coordinates of the
# cell being drawn; they may look into the neighbouring cells (negative or ≥ SIZE).
# The shadow is translucent so decorated ground below a hill (a teleporter, a hole, …) stays visible
# under it instead of being cut off. Over plain sand it gives exactly sand_shadow (and a softer rim).
SHADOW_ALPHA = 0.45
SHADOW_INK = tuple(round(P["sand"][i] + (P["sand_shadow"][i] - P["sand"][i]) / SHADOW_ALPHA) for i in range(3))
HARD_SHADOW = SHADOW_INK + (round(255 * SHADOW_ALPHA),)
SOFT_SHADOW = SHADOW_INK + (round(255 * SHADOW_ALPHA * 0.45),)
SIDE_OFFSET = {"n": (0, -SIZE), "e": (SIZE, 0), "s": (0, SIZE), "w": (-SIZE, 0)}


def rounded_out(x, y, ox, oy, corner, radius):
    """True if (x, y) lies in [corner] of the cell at (ox, oy) but outside its rounding."""
    cx = ox + (radius if "w" in corner else SIZE - radius)
    cy = oy + (radius if "n" in corner else SIZE - radius)
    in_box = (x < cx if "w" in corner else x >= cx) and (y < cy if "n" in corner else y >= cy)
    return in_box and math.hypot(x + 0.5 - cx, y + 0.5 - cy) > radius


def cell_mask(ox=0, oy=0, rounded=(), radius=None):
    """The cell at offset (ox, oy) filled with grass, with the [rounded] corners cut off."""
    radius = radius or BITE_RADIUS

    def mask(x, y):
        if not (ox <= x < ox + SIZE and oy <= y < oy + SIZE):
            return False
        return not any(rounded_out(x, y, ox, oy, corner, radius) for corner in rounded)
    return mask


def drop_shadow(c, grass):
    """Paints the shadow of the [grass] mask onto the non-grass pixels of [c]."""
    for y in range(SIZE):
        for x in range(SIZE):
            if grass(x, y):
                continue
            if grass(x, y - 1):  # looking at the hill's face
                c.px(x, y, "grass_edge")
            elif grass(x, y - 2) or grass(x - 1, y) or grass(x - 1, y - 1):
                c.px(x, y, HARD_SHADOW)
            elif grass(x, y - 3) or grass(x - 1, y - 2) or grass(x - 2, y) or grass(x - 2, y - 1):
                c.px(x, y, SOFT_SHADOW)
    return c


def ledge_edge(where, cut=""):
    """Sand next to a grass cell that lies towards [where]; that cell's corners at the [cut] ends are rounded."""
    side = {"n": "s", "s": "n", "e": "w", "w": "e"}[where]
    rounded = [side + e if where in "ns" else e + side for e in cut]
    return drop_shadow(Canvas(), cell_mask(*SIDE_OFFSET[where], rounded=rounded))


BITE_RADIUS = 6


def bite(low, high, where, radius=BITE_RADIUS):
    """Corner [where] (nw/ne/se/sw) of a [high] cell rounded off into [low].

    The rim follows the curve the same way the straight edges do: surf on the water, and below a
    grass hill the same face and shadow as [ledge_edge] on the sides facing away from the light.
    """
    base = {"water": Canvas("water"), "sand": sand()}[low]
    c = Canvas()
    cx = radius if "w" in where else SIZE - radius
    cy = radius if "n" in where else SIZE - radius

    def beyond(x, y):
        """How far pixel (x, y) lies outside the rounded corner (≤ 0: still land)."""
        in_box = (x < cx if "w" in where else x >= cx) and (y < cy if "n" in where else y >= cy)
        return math.hypot(x + 0.5 - cx, y + 0.5 - cy) - radius if in_box else -1

    for y in range(SIZE):
        for x in range(SIZE):
            if beyond(x, y) > 0:
                c.p[y][x] = base.p[y][x]
    if low == "sand":
        drop_shadow(c, cell_mask(rounded=[where], radius=radius))
    for y in range(SIZE):
        for x in range(SIZE):
            d = beyond(x, y)
            if d <= 0:
                if any(beyond(x + dx, y + dy) > 0 for dx, dy in [(1, 0), (-1, 0), (0, 1), (0, -1)]):
                    c.px(x, y, "wet_sand" if high == "sand" else "grass_edge")
                continue
            if low == "water":
                if d < 1.2:
                    c.px(x, y, "foam")
                elif d < 2.2:
                    c.px(x, y, alpha("foam", 150))
    return c


def cut_ends(strip, ends, radius=BITE_RADIUS):
    """[strip] without the stretches next to a rounded-off corner at its [ends] (n/e/s/w)."""
    c = strip.copy()
    for y in range(SIZE):
        for x in range(SIZE):
            if ("w" in ends and x < radius) or ("e" in ends and x >= SIZE - radius) or                     ("n" in ends and y < radius) or ("s" in ends and y >= SIZE - radius):
                c.p[y][x] = (0, 0, 0, 0)
    return c


def edges():
    out = {}
    for low, high in [("water", "sand"), ("water", "grass"), ("sand", "grass")]:
        for where in ["n", "e", "s", "w"]:
            name = f"edge_{low}_{high}_{where}"
            # Where the neighbour's corner is rounded off, its bite carries the rim round the
            # curve and the straight strip has to follow it.
            ends = "ew" if where in "ns" else "ns"
            for cut in ["", ends[0], ends[1], ends]:
                if low == "water":
                    frames = [cut_ends(rotated(foam_edge(f), where), cut) for f in range(EDGE_FRAMES)]
                else:
                    frames = [ledge_edge(where, cut)]
                if any(px[3] for f in frames for row in f.p for px in row):
                    out[name + (f"_cut_{cut}" if cut else "")] = frames
            out[f"lip_{low}_{high}_{where}"] = [rotated(land_lip(high), where)]
        for where in ["nw", "ne", "se", "sw"]:
            out[f"bite_{low}_{high}_{where}"] = [bite(low, high, where)]
    return out


# --- decorated ground ----------------------------------------------------------------------

VINE_FRAMES = 4


def vine(sand_tile, frame):
    """Spring: a vine grown from the watered seed, twisted into a coil like a spring.

    The coil squashes and stretches a little so it looks ready to fling Pushy.
    """
    c = sand_tile.copy()
    # the farm soil it grew from
    c.ellipse(8, 12, 5.5, 2.6, "sand_shadow")
    c.ellipse(8, 11.8, 4.6, 1.9, "wood")
    c.ellipse(8, 12.2, 3, 1.1, "wood_dark")
    # leaves at the base
    for (lx, ly, dx) in [(3.6, 11.4, -1), (12.4, 11.4, 1)]:
        c.ellipse(lx, ly, 2.4, 1.2, "leaf_dark")
        c.ellipse(lx, ly - 0.4, 2.0, 0.9, "leaf")
        c.px(int(lx) - dx, int(ly) - 1, "leaf_light")

    squash = [0.0, 0.5, 1.0, 0.5][frame % VINE_FRAMES]
    bottom, top = 11.0, 2.5 + squash
    loops = 2
    pitch = (bottom - top) / loops

    def helix(front):
        steps = 160
        for i in range(steps + 1):
            t = i / steps
            a = t * loops * 2 * math.pi
            is_front = math.cos(a) > 0
            if is_front != front:
                continue
            rx = 4.6 - 1.4 * t
            x = 8 + rx * math.sin(a)
            y = bottom - t * (bottom - top) + 1.5 * math.cos(a)
            if front:
                color = "leaf_light" if math.sin(a) < -0.3 else "leaf"
            else:
                color = "leaf_dark"
            c.px(int(round(x)), int(round(y)), color)
            if front and abs(math.sin(a)) < 0.7:  # the front strands are a bit thicker
                c.px(int(round(x)), int(round(y)) + 1, "leaf_dark")

    helix(front=False)
    helix(front=True)
    # stem out of the soil, a curly tip and a little leaf on top
    c.rect(8, int(bottom) + 1, 8, int(bottom) + 1, "leaf_dark")
    tip = int(round(top)) - 1
    c.px(8, tip, "leaf")
    c.px(9, tip - 1, "leaf")
    c.px(10, tip, "leaf_dark")
    c.px(6, tip, "leaf_light")
    c.px(5, tip - 1, "leaf_light")
    c.px(6, tip - 1, "leaf")
    return c


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
        # Same rounding the renderer applies automatically (it draws these as plain sand/grass plus a
        # "bite" overlay); the PNGs are what the editors show in their palettes.
        where = {"top_left": "nw", "top_right": "ne", "bottom_right": "se", "bottom_left": "sw"}[corner]
        out[f"sand_{corner}"] = [s.copy().over(bite("water", "sand", where))]
        out[f"grass_{corner}"] = [g.copy().over(bite("sand", "grass", where))]

    # Sunken box: the very crate Pushy pushed in, sunk until it lies just under the surface – a little
    # raft Pushy can walk over. Same size as the pushable box, seen through a thin film of water.
    # Deliberately still: no bobbing and no wavelets around it.
    def box_water():
        from entities import crate  # entities imports this module, so not at the top
        c = Canvas("water")
        lid = crate()
        lid.outline()
        # a thin layer of water over the whole crate, outline included
        for y in range(SIZE):
            for x in range(SIZE):
                base = lid.get(x, y)
                if base[3]:
                    lid.px(x, y, mix(base, P["water"], 0.25))
        c.over(lid)
        # a few faint glints on the film
        for (x, y) in [(5, 4), (6, 4), (10, 8), (11, 8), (4, 11)]:
            c.px(x, y, mix(c.get(x, y), P["water_light"], 0.5))
        return c
    out["box_water"] = [box_water()]

    # Boat (terrain on water) – top-down rowing boat on a transparent background; the renderer draws
    # the water below it, so a sailing boat doesn't drag a square of water along. One sprite per
    # heading: the bow points where the boat last sailed (north is the default).
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
    hulls = {"boat": hull, "boat_east": hull.rot90()}
    hulls["boat_south"] = hulls["boat_east"].rot90()
    hulls["boat_west"] = hulls["boat_south"].rot90()

    def boat(frame, shape):
        c = Canvas()
        bob = 1 if frame in (2, 3, 4) else 0
        # soft shadow in the water, down and to the right
        for y in range(SIZE):
            for x in range(SIZE):
                if shape.get(x, y)[3]:
                    c.px(x + 1, y + 1 + bob, P["water_deep"][:3] + (150,))
        c.over(shape, 0, bob)
        return c
    for name, shape in hulls.items():
        out[name] = [boat(f, shape) for f in range(WATER_FRAMES)]

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
    out["spring"] = [vine(s, f) for f in range(VINE_FRAMES)]
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

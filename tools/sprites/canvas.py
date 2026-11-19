"""A tiny pixel-art canvas with the shared palette used by every sprite."""
import math

SIZE = 16


def hexc(value, alpha=255):
    value = value.lstrip("#")
    return (int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16), alpha)


# Shared palette – keep every sprite on these colours so the whole game looks like one set.
P = {
    "outline": hexc("2b1d2e"),
    "shadow": hexc("1a1020", 70),
    "white": hexc("fbf7ef"),
    "black": hexc("1b1420"),
    # water
    "water_deep": hexc("2f6fb3"),
    "water": hexc("3e8fd6"),
    "water_light": hexc("68b9ef"),
    "foam": hexc("d4f1ff"),
    # sand
    "sand": hexc("efd39b"),
    "sand_light": hexc("f9e7bd"),
    "sand_dark": hexc("dcb879"),
    "sand_shadow": hexc("c39a5c"),
    "wet_sand": hexc("c9c3a6"),
    # grass
    "grass": hexc("5fb04b"),
    "grass_light": hexc("85cd5d"),
    "grass_dark": hexc("43903d"),
    "grass_edge": hexc("2f6c31"),
    # wood
    "wood": hexc("9a6438"),
    "wood_light": hexc("c08a52"),
    "wood_dark": hexc("62391f"),
    # stone
    "stone": hexc("8f909c"),
    "stone_light": hexc("b8bac4"),
    "stone_dark": hexc("5f6170"),
    # metal / logic
    "metal": hexc("4a4e60"),
    "metal_light": hexc("6c7186"),
    "metal_dark": hexc("2c2f3c"),
    "led_off": hexc("5a2a30"),
    # accents
    "red": hexc("d9443c"),
    "red_light": hexc("f27a5c"),
    "red_dark": hexc("982b2f"),
    "orange": hexc("f08a2c"),
    "orange_light": hexc("ffb45a"),
    "yellow": hexc("f6c843"),
    "yellow_light": hexc("fff08f"),
    "yellow_dark": hexc("c48c25"),
    "green": hexc("4fb35a"),
    "green_light": hexc("8be07a"),
    "green_dark": hexc("2f7a3d"),
    "leaf": hexc("3f9a3e"),
    "leaf_light": hexc("72c656"),
    "leaf_dark": hexc("276b2e"),
    "blue": hexc("4a7bd8"),
    "blue_light": hexc("82a9f2"),
    "blue_dark": hexc("2f4f9c"),
    "cyan": hexc("54d6e8"),
    "cyan_light": hexc("b8f6ff"),
    "purple": hexc("8a5cd1"),
    "purple_light": hexc("b89af0"),
    "purple_dark": hexc("5a3a96"),
    "pink": hexc("e46aa6"),
    "pink_light": hexc("f7a6cc"),
    "pink_dark": hexc("a8427a"),
    "skin": hexc("f2c29a"),
    "skin_dark": hexc("cf946c"),
    "glass": hexc("cfe6ee"),
    "glass_dark": hexc("8fb4c2"),
    "rope": hexc("d8b27a"),
    "rope_dark": hexc("9c7444"),
}

TRANSPARENT = (0, 0, 0, 0)


def noise(x, y, seed=0):
    """Deterministic per-pixel hash in [0, 1)."""
    n = (x * 374761393 + y * 668265263 + seed * 2147483647) & 0xFFFFFFFF
    n = (n ^ (n >> 13)) * 1274126177 & 0xFFFFFFFF
    return ((n ^ (n >> 16)) & 0xFFFF) / 65536


def mix(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(4))


class Canvas:
    def __init__(self, fill=None):
        color = P[fill] if isinstance(fill, str) else fill
        self.p = [[color or TRANSPARENT for _ in range(SIZE)] for _ in range(SIZE)]

    # -- basics ------------------------------------------------------------------------------
    @staticmethod
    def _c(color):
        return P[color] if isinstance(color, str) else color

    def px(self, x, y, color):
        if 0 <= x < SIZE and 0 <= y < SIZE and color is not None:
            c = self._c(color)
            if c[3] == 255 or c[3] == 0:
                self.p[y][x] = c
            else:
                base = self.p[y][x]
                if base[3] == 0:
                    self.p[y][x] = c
                else:
                    k = c[3] / 255
                    self.p[y][x] = (
                        round(c[0] * k + base[0] * (1 - k)),
                        round(c[1] * k + base[1] * (1 - k)),
                        round(c[2] * k + base[2] * (1 - k)),
                        max(base[3], c[3]),
                    )

    def get(self, x, y):
        return self.p[y][x]

    def rect(self, x0, y0, x1, y1, color):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.px(x, y, color)

    def frame(self, x0, y0, x1, y1, color):
        for x in range(x0, x1 + 1):
            self.px(x, y0, color)
            self.px(x, y1, color)
        for y in range(y0, y1 + 1):
            self.px(x0, y, color)
            self.px(x1, y, color)

    def ellipse(self, cx, cy, rx, ry, color, shade=None):
        """Filled ellipse centred on (cx, cy) in pixel-centre coordinates.

        shade=(light, dark) adds a top-left highlight and bottom-right shadow.
        """
        for y in range(SIZE):
            for x in range(SIZE):
                dx = (x + 0.5 - cx) / rx
                dy = (y + 0.5 - cy) / ry
                if dx * dx + dy * dy <= 1:
                    c = color
                    if shade:
                        light, dark = shade
                        lx, ly = dx + 0.45, dy + 0.5
                        if light and lx * lx + ly * ly < 0.28:
                            c = light
                        elif dark and (dx * 0.6 + dy) > 0.55 and dx * dx + dy * dy > 0.45:
                            c = dark
                    self.px(x, y, c)

    def circle(self, cx, cy, r, color, shade=None):
        self.ellipse(cx, cy, r, r, color, shade)

    def ring(self, cx, cy, r, color, width=1.0):
        for y in range(SIZE):
            for x in range(SIZE):
                d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
                if r - width <= d <= r:
                    self.px(x, y, color)

    def line(self, x0, y0, x1, y1, color):
        steps = max(abs(x1 - x0), abs(y1 - y0), 1)
        for i in range(steps + 1):
            t = i / steps
            self.px(round(x0 + (x1 - x0) * t), round(y0 + (y1 - y0) * t), color)

    def art(self, rows, mapping, ox=0, oy=0):
        """Paints ASCII art. '.' and ' ' are transparent; other characters look up [mapping]."""
        for j, row in enumerate(rows):
            for i, ch in enumerate(row):
                if ch in ". ":
                    continue
                self.px(ox + i, oy + j, mapping[ch])
        return self

    # -- whole-canvas helpers ----------------------------------------------------------------
    def copy(self):
        c = Canvas()
        c.p = [row[:] for row in self.p]
        return c

    def over(self, other, dx=0, dy=0):
        """Draws [other] on top of this canvas."""
        for y in range(SIZE):
            for x in range(SIZE):
                c = other.p[y][x]
                if c[3]:
                    self.px(x + dx, y + dy, c)
        return self

    def flip_h(self):
        c = Canvas()
        c.p = [row[::-1] for row in self.p]
        return c

    def flip_v(self):
        c = Canvas()
        c.p = [row[:] for row in self.p[::-1]]
        return c

    def rot90(self):
        """Rotates clockwise."""
        c = Canvas()
        c.p = [[self.p[SIZE - 1 - x][y] for x in range(SIZE)] for y in range(SIZE)]
        return c

    def outline(self, color="outline", diagonal=False):
        """Adds a 1px outline around every opaque shape."""
        col = self._c(color)
        out = [row[:] for row in self.p]
        neighbours = [(1, 0), (-1, 0), (0, 1), (0, -1)]
        if diagonal:
            neighbours += [(1, 1), (-1, -1), (1, -1), (-1, 1)]
        for y in range(SIZE):
            for x in range(SIZE):
                if self.p[y][x][3] > 100:
                    continue
                for dx, dy in neighbours:
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < SIZE and 0 <= ny < SIZE and self.p[ny][nx][3] > 200 and self.p[ny][nx] != col:
                        out[y][x] = col
                        break
        self.p = out
        return self

    def shadow(self, cx=8, cy=13.5, rx=5.5, ry=2):
        """Soft drop shadow drawn *below* existing pixels."""
        under = Canvas()
        under.ellipse(cx, cy, rx, ry, P["shadow"])
        under.over(self)
        self.p = under.p
        return self

    def shift(self, dx, dy):
        c = Canvas()
        for y in range(SIZE):
            for x in range(SIZE):
                nx, ny = x + dx, y + dy
                if 0 <= nx < SIZE and 0 <= ny < SIZE:
                    c.p[ny][nx] = self.p[y][x]
        return c

    def rows(self):
        return self.p


def texture(base, specks, seed, density=0.12):
    """Opaque tile of [base] with deterministic speckles from [specks] (list of colours)."""
    c = Canvas(base)
    for y in range(SIZE):
        for x in range(SIZE):
            n = noise(x, y, seed)
            if n < density:
                c.px(x, y, specks[int(n / density * len(specks)) % len(specks)])
    return c


# 3x5 digit font for numbered tiles.
DIGITS = {
    "1": [".#.", "##.", ".#.", ".#.", "###"],
    "2": ["##.", "..#", ".#.", "#..", "###"],
    "3": ["##.", "..#", ".#.", "..#", "##."],
    "4": ["#.#", "#.#", "###", "..#", "..#"],
    "5": ["###", "#..", "##.", "..#", "##."],
    "?": ["##.", "..#", ".#.", "...", ".#."],
}


def digit(canvas, ch, ox, oy, color, shadow=None):
    for j, row in enumerate(DIGITS[ch]):
        for i, v in enumerate(row):
            if v == "#":
                if shadow:
                    canvas.px(ox + i + 1, oy + j + 1, shadow)
                canvas.px(ox + i, oy + j, color)

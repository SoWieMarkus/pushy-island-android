"""Minimal PNG reader/writer (RGBA, 8 bit) using only the standard library."""
import struct
import zlib


def write_png(path, pixels):
    """pixels: list of rows, each a list of (r, g, b, a) tuples."""
    height = len(pixels)
    width = len(pixels[0])
    raw = bytearray()
    for row in pixels:
        raw.append(0)
        for r, g, b, a in row:
            raw += bytes((r, g, b, a))

    def chunk(kind, data):
        body = kind + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(png)


def read_png(path):
    """Reads a non-interlaced 8-bit PNG (RGB, RGBA, gray, gray+alpha or palette) into RGBA rows."""
    with open(path, "rb") as f:
        data = f.read()
    assert data[:8] == b"\x89PNG\r\n\x1a\n", path
    pos = 8
    idat = b""
    palette = None
    trns = None
    while pos < len(data):
        length, kind = struct.unpack(">I4s", data[pos:pos + 8])
        body = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if kind == b"IHDR":
            width, height, depth, color, _, _, interlace = struct.unpack(">IIBBBBB", body)
        elif kind == b"PLTE":
            palette = [tuple(body[i:i + 3]) for i in range(0, len(body), 3)]
        elif kind == b"tRNS":
            trns = body
        elif kind == b"IDAT":
            idat += body
    assert interlace == 0, path
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[color]
    if color == 3:
        assert depth in (1, 2, 4, 8), path
        bits = depth
    else:
        assert depth == 8, f"{path}: depth {depth}"
        bits = 8 * channels
    stride = (width * bits + 7) // 8
    bpp = max(1, bits // 8)
    raw = zlib.decompress(idat)
    rows = []
    prev = bytearray(stride)
    i = 0
    for _ in range(height):
        ftype = raw[i]
        line = bytearray(raw[i + 1:i + 1 + stride])
        i += 1 + stride
        for x in range(stride):
            a = line[x - bpp] if x >= bpp else 0
            b = prev[x]
            c = prev[x - bpp] if x >= bpp else 0
            if ftype == 1:
                line[x] = (line[x] + a) & 255
            elif ftype == 2:
                line[x] = (line[x] + b) & 255
            elif ftype == 3:
                line[x] = (line[x] + (a + b) // 2) & 255
            elif ftype == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                pr = a if pa <= pb and pa <= pc else (b if pb <= pc else c)
                line[x] = (line[x] + pr) & 255
        prev = line
        out = []
        for x in range(width):
            if color == 3:
                per = 8 // depth
                byte = line[x // per]
                shift = (per - 1 - x % per) * depth
                idx = (byte >> shift) & ((1 << depth) - 1)
                r, g, b = palette[idx]
                a = trns[idx] if trns and idx < len(trns) else 255
                out.append((r, g, b, a))
            elif color == 6:
                out.append(tuple(line[x * 4:x * 4 + 4]))
            elif color == 2:
                out.append(tuple(line[x * 3:x * 3 + 3]) + (255,))
            elif color == 0:
                v = line[x]
                out.append((v, v, v, 255))
            elif color == 4:
                v, a = line[x * 2], line[x * 2 + 1]
                out.append((v, v, v, a))
        rows.append(out)
    return rows


def scale(pixels, factor):
    return [[p for p in row for _ in range(factor)] for row in pixels for _ in range(factor)]


def blank(width, height, color=(0, 0, 0, 0)):
    return [[color] * width for _ in range(height)]


def paste(dst, src, x, y):
    for j, row in enumerate(src):
        for i, (r, g, b, a) in enumerate(row):
            if a == 0:
                continue
            if 0 <= y + j < len(dst) and 0 <= x + i < len(dst[0]):
                if a == 255:
                    dst[y + j][x + i] = (r, g, b, 255)
                else:
                    br, bg, bb, ba = dst[y + j][x + i]
                    k = a / 255
                    dst[y + j][x + i] = (round(r * k + br * (1 - k)), round(g * k + bg * (1 - k)), round(b * k + bb * (1 - k)), 255)

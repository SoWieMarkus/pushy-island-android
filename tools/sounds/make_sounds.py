"""Synthesises every sound effect into app/src/main/res/raw as small mono WAV files.

    python tools/sounds/make_sounds.py

No samples or third-party audio: everything is built from sine/square waves and filtered noise.
The script is deterministic (fixed random seed), so re-running it reproduces the committed files.
To tweak a sound, change its function below and re-run.
"""
import math
import os
import random
import struct
import wave

RATE = 22050
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
OUT = os.path.join(ROOT, "app", "src", "main", "res", "raw")


# --- building blocks -------------------------------------------------------------------------

def silence(seconds):
    return [0.0] * int(RATE * seconds)


def tone(freq, seconds, wave_form="sine", volume=1.0, freq_end=None, vibrato=0.0, vibrato_hz=0.0):
    """A tone whose frequency glides from [freq] to [freq_end] (exponentially)."""
    n = int(RATE * seconds)
    out = []
    phase = 0.0
    end = freq_end or freq
    for i in range(n):
        t = i / n
        f = freq * (end / freq) ** t
        if vibrato:
            f *= 1 + vibrato * math.sin(2 * math.pi * vibrato_hz * i / RATE)
        phase += f / RATE
        p = phase % 1.0
        if wave_form == "square":
            v = 1.0 if p < 0.5 else -1.0
        elif wave_form == "triangle":
            v = 4 * abs(p - 0.5) - 1
        else:
            v = math.sin(2 * math.pi * p)
        out.append(v * volume)
    return out


def noise(seconds, volume=1.0, cutoff=1.0, cutoff_end=None, seed=1):
    """White noise through a one-pole low-pass; [cutoff] 0..1 (1 = unfiltered)."""
    rng = random.Random(seed)
    n = int(RATE * seconds)
    out = []
    y = 0.0
    end = cutoff if cutoff_end is None else cutoff_end
    for i in range(n):
        k = cutoff + (end - cutoff) * i / n
        y += k * (rng.uniform(-1, 1) - y)
        out.append(y * volume)
    return out


def envelope(samples, attack=0.005, release=None, curve=2.0):
    """Linear attack, then a decay to zero over the rest (or the last [release] seconds)."""
    n = len(samples)
    a = max(1, int(RATE * attack))
    r = n - a if release is None else int(RATE * release)
    out = []
    for i, s in enumerate(samples):
        if i < a:
            g = i / a
        elif i >= n - r:
            g = ((n - i) / r) ** curve
        else:
            g = 1.0
        out.append(s * g)
    return out


def mix(*tracks):
    n = max(len(t) for t in tracks)
    return [sum(t[i] for t in tracks if i < len(t)) for i in range(n)]


def then(*parts):
    out = []
    for p in parts:
        out += p
    return out


def at(seconds, samples):
    return silence(seconds) + samples


def write(name, samples, peak=0.8):
    top = max(1e-9, max(abs(s) for s in samples))
    gain = peak / top
    path = os.path.join(OUT, f"sfx_{name}.wav")
    with wave.open(path, "wb") as f:
        f.setnchannels(1)
        f.setsampwidth(2)
        f.setframerate(RATE)
        f.writeframes(b"".join(struct.pack("<h", int(max(-1, min(1, s * gain)) * 32767)) for s in samples))
    return path


# --- the sounds ------------------------------------------------------------------------------

def step():
    """Soft footstep in the sand."""
    return envelope(noise(0.05, cutoff=0.25, cutoff_end=0.05, seed=3), attack=0.002)


def push():
    """A crate scraping over sand, with a wooden knock."""
    scrape = envelope(noise(0.14, cutoff=0.18, cutoff_end=0.06, seed=5), attack=0.01, curve=1.5)
    knock = envelope(tone(170, 0.09, freq_end=95), attack=0.001, curve=3)
    return mix(scrape, [s * 0.9 for s in knock])


def bump():
    """Walking into something solid."""
    return envelope(tone(110, 0.08, "square", 0.5, freq_end=70), attack=0.001, curve=2.5)


def click():
    """Pulling a lever, opening a chest, …"""
    return mix(
        envelope(tone(1400, 0.03, "square", 0.4), attack=0.001, curve=3),
        at(0.035, envelope(tone(900, 0.04, "square", 0.35), attack=0.001, curve=3)),
    )


def splash():
    """Something plopping into the water."""
    body = envelope(noise(0.38, cutoff=0.6, cutoff_end=0.04, seed=7), attack=0.004, curve=2.2)
    plop = envelope(tone(420, 0.08, freq_end=180), attack=0.002, curve=2)
    bubbles = mix(*[
        at(0.08 + 0.05 * k, envelope(tone(500 + 140 * k, 0.04, volume=0.3, freq_end=900 + 160 * k), attack=0.002))
        for k in range(4)
    ])
    return mix(body, plop, bubbles)


def drop():
    """Something dropping into a hole or onto the ground."""
    return mix(
        envelope(tone(140, 0.16, freq_end=60), attack=0.001, curve=2.5),
        envelope(noise(0.08, 0.6, cutoff=0.2, seed=11), attack=0.001),
    )


def pickup():
    """Collecting an item: a bright little arpeggio."""
    notes = [1318.5, 1975.5]
    return then(*[envelope(tone(f, 0.07, "square", 0.45), attack=0.002, curve=1.2) for f in notes])


def explosion():
    rumble = envelope(tone(80, 0.7, freq_end=35, volume=0.8), attack=0.004, curve=1.6)
    blast = envelope(noise(0.7, cutoff=0.9, cutoff_end=0.03, seed=13), attack=0.002, curve=1.8)
    return mix(rumble, blast)


def teleport():
    """A shimmering sweep up."""
    sweep = envelope(tone(300, 0.42, "triangle", 0.6, freq_end=1600, vibrato=0.06, vibrato_hz=28), attack=0.03, release=0.15)
    sparkle = mix(*[
        at(0.1 * k, envelope(tone(1800 + 300 * k, 0.05, "sine", 0.25), attack=0.002)) for k in range(4)
    ])
    return mix(sweep, sparkle)


def jump():
    """Boing! off a spring."""
    return envelope(tone(180, 0.32, "triangle", 0.8, freq_end=720, vibrato=0.12, vibrato_hz=22), attack=0.003, release=0.2)


def shot():
    """Slingshot twang and a whoosh."""
    twang = envelope(tone(330, 0.12, "triangle", 0.7, freq_end=220, vibrato=0.08, vibrato_hz=40), attack=0.001, curve=2)
    whoosh = envelope(noise(0.22, 0.6, cutoff=0.05, cutoff_end=0.35, seed=17), attack=0.12, release=0.1)
    return mix(twang, whoosh)


def hit():
    """The slingshot stone hitting something."""
    return envelope(mix(tone(260, 0.06, "square", 0.5, freq_end=120), noise(0.06, 0.6, cutoff=0.5, seed=19)), attack=0.001, curve=2.5)


def sail():
    """Paddling across the water: a soft wave swish."""
    return envelope(noise(0.55, cutoff=0.04, cutoff_end=0.12, seed=23), attack=0.18, release=0.3, curve=1.4)


def win():
    """A short happy fanfare."""
    notes = [(523.3, 0.1), (659.3, 0.1), (784.0, 0.1), (1046.5, 0.32)]
    lead = then(*[envelope(tone(f, d, "square", 0.4), attack=0.004, release=d * 0.6, curve=1.2) for f, d in notes])
    bass = then(*[envelope(tone(f / 2, d, "triangle", 0.5), attack=0.004, release=d * 0.5) for f, d in notes])
    return mix(lead, bass)


def ice_melt():
    """An ice floe cracking and sinking away."""
    cracks = mix(*[
        at(0.03 * k + 0.01 * (k % 2), envelope(noise(0.025, 0.9 - 0.15 * k, cutoff=0.95, seed=31 + k), attack=0.001, curve=3))
        for k in range(4)
    ])
    sink = at(0.12, envelope(noise(0.3, 0.5, cutoff=0.3, cutoff_end=0.03, seed=37), attack=0.03, curve=2))
    glug = at(0.16, envelope(tone(600, 0.12, volume=0.35, freq_end=250), attack=0.005))
    return mix(cracks, sink, glug)


def spikes_down():
    """Metal spikes sliding into the ground."""
    scrape = envelope(tone(900, 0.22, "square", 0.25, freq_end=260), attack=0.003, curve=1.5)
    hiss = envelope(noise(0.22, 0.35, cutoff=0.7, cutoff_end=0.2, seed=41), attack=0.003)
    thunk = at(0.18, envelope(tone(120, 0.07, volume=0.7, freq_end=70), attack=0.001, curve=3))
    return mix(scrape, hiss, thunk)


def spikes_up():
    """Spikes shooting out of the ground: a short metallic shing."""
    shing = envelope(tone(400, 0.16, "square", 0.25, freq_end=1500), attack=0.002, curve=2)
    ring = at(0.08, envelope(tone(2200, 0.18, volume=0.25), attack=0.002, curve=2))
    return mix(shing, ring, envelope(noise(0.1, 0.3, cutoff=0.9, seed=43), attack=0.001))


def door_open():
    """A heavy wooden gate grinding open."""
    grind = envelope(noise(0.35, cutoff=0.08, cutoff_end=0.03, seed=47), attack=0.04, curve=1.5)
    creak = envelope(tone(180, 0.3, "triangle", 0.4, freq_end=260, vibrato=0.08, vibrato_hz=18), attack=0.03)
    clunk = at(0.3, envelope(tone(110, 0.08, volume=0.8, freq_end=60), attack=0.001, curve=3))
    return mix(grind, creak, clunk)


def door_close():
    """The gate slamming shut."""
    return mix(
        envelope(noise(0.12, cutoff=0.1, seed=53), attack=0.02, curve=1.5),
        at(0.08, envelope(tone(95, 0.14, volume=0.9, freq_end=50), attack=0.001, curve=2.5)),
    )


def reveal():
    """Something appearing as if by magic (invisible bridges, flowers changing colour)."""
    notes = [784.0, 1046.5, 1318.5, 1568.0]
    return mix(*[
        at(0.06 * k, envelope(tone(f, 0.25, "sine", 0.4, vibrato=0.01, vibrato_hz=7), attack=0.004, curve=2))
        for k, f in enumerate(notes)
    ])


def grow():
    """The vine sprouting: a rustle and a springy rising wobble."""
    rustle = envelope(noise(0.35, 0.5, cutoff=0.5, cutoff_end=0.2, seed=59), attack=0.05, curve=1.5)
    wobble = envelope(tone(200, 0.4, "triangle", 0.6, freq_end=500, vibrato=0.15, vibrato_hz=12), attack=0.02, release=0.2)
    return mix(rustle, wobble)


def plant():
    """A seed pressed into the soil."""
    return mix(
        envelope(noise(0.07, cutoff=0.15, seed=61), attack=0.002, curve=2),
        at(0.05, envelope(tone(520, 0.05, volume=0.4, freq_end=700), attack=0.002)),
    )


def lamp_on():
    """A lamp lighting up: an electric blip."""
    buzz = envelope(tone(120, 0.12, "square", 0.25), attack=0.002, curve=2)
    blip = envelope(tone(880, 0.12, "square", 0.35, freq_end=1760), attack=0.002, curve=1.5)
    return mix(buzz, blip)


SOUNDS = {
    "ice_melt": (ice_melt, 0.7),
    "spikes_down": (spikes_down, 0.6),
    "spikes_up": (spikes_up, 0.6),
    "door_open": (door_open, 0.7),
    "door_close": (door_close, 0.7),
    "reveal": (reveal, 0.55),
    "grow": (grow, 0.65),
    "plant": (plant, 0.55),
    "lamp_on": (lamp_on, 0.5),
    "step": (step, 0.35),
    "push": (push, 0.7),
    "bump": (bump, 0.5),
    "click": (click, 0.6),
    "splash": (splash, 0.75),
    "drop": (drop, 0.7),
    "pickup": (pickup, 0.6),
    "explosion": (explosion, 0.9),
    "teleport": (teleport, 0.6),
    "jump": (jump, 0.65),
    "shot": (shot, 0.65),
    "hit": (hit, 0.55),
    "sail": (sail, 0.6),
    "win": (win, 0.7),
}


def main():
    os.makedirs(OUT, exist_ok=True)
    for name, (build, peak) in SOUNDS.items():
        write(name, build(), peak)
    print(f"wrote {len(SOUNDS)} sounds to {os.path.relpath(OUT, ROOT)}")


if __name__ == "__main__":
    main()

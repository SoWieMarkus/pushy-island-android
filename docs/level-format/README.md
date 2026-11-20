# Level format v2

What each tile does is described in [Tiles](../tiles.md).

A level is a JSON object. Both layers are written as one string per row with one character per cell; the
`legend` maps characters to tile ids.

```json
{
  "format": 2,
  "name": "Maluaki",
  "width": 20,
  "height": 12,
  "legend": {
    "~": "water",
    "-": "sand",
    "=": "grass",
    "a": "sand_top_left",
    "T": "tree",
    "F": "finish",
    "P": "pushy"
  },
  "terrain": ["~~~~a---", "..."],
  "entities": ["....T.F.", "..."]
}
```

| Field      | Meaning                                                                              |
| ---------- | ------------------------------------------------------------------------------------ |
| `format`   | Always `2`.                                                                          |
| `name`     | Level name, 1–20 characters.                                                         |
| `width`    | Number of columns (20 for all levels made with the editors).                          |
| `height`   | Number of rows (12 for all levels made with the editors).                             |
| `legend`   | Single character → tile id. One legend for both layers.                              |
| `terrain`  | `height` strings of `width` characters. Every cell needs a terrain tile.             |
| `entities` | `height` strings of `width` characters. `.` means "no entity" and is never in the legend. |

A playable level has exactly one `pushy` and a `finish`; some tiles may only appear a limited number of
times (`maxInstances` in `tiles.json`, e.g. two teleporters).

## Tile ids

[`tiles.json`](./tiles.json) lists every tile: its id, layer, sprite, instance limit, logic ports and the
codes used by the old format. It is generated from the Kotlin engine
(`engine/src/main/kotlin/markus/wieland/pushygame/engine/model/TileType.kt`) – don't edit it by hand, run
`UPDATE_GENERATED=1 ./gradlew :engine:test` instead.

## Writing files

Any valid legend can be read. Both writers (Kotlin `LevelFormat.encode`, TypeScript `encodeLevel`) produce
identical files: tiles are collected in order of first appearance (terrain row by row, then entities) and get
their preferred `symbol` from `tiles.json`, or the next unused character of `symbolPool`.

## Share codes

```
PUSHY2:<base64url without padding of raw DEFLATE (RFC 1951) of the compact level JSON>
```

Whitespace inside a code is ignored, so codes survive line breaks in chat apps.

## Fixtures

[`fixtures/`](./fixtures) holds one level plus share codes written by the Android side (`*.kotlin.code`), the web side
(`*.web.code`) and the old app (`*.v1.code`). Engine and web tests decode all of them, which keeps both
implementations compatible.

## Format v1 (old app)

One version byte (`01`), then 12×20 terrain and 12×20 entity tiles with 6 bits each (row-major), followed by
the level name with 8 bits per character – all written as a hex string. The 6-bit values are the `v1Code`s
in `tiles.json` (`5` = no entity). The web editor ([`editor/src/format/v1.ts`](../../editor/src/format/v1.ts)) decodes it.

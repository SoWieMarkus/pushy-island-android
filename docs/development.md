# Development

## Project layout

| Folder          | What it is                                                                                      |
| --------------- | ----------------------------------------------------------------------------------------------- |
| `engine/`       | Pure Kotlin game engine (no Android): rules, level format, share codes, editor logic, tests     |
| `app/`          | Android app – Jetpack Compose, Navigation 3, Room. Campaign levels and sprites live in `assets` |
| `editor/`       | Web level editor (React + TypeScript + Vite) and the converter for levels of the old app        |
| `tools/`        | Python scripts that draw the sprites and synthesise the sounds                                  |
| `docs/`         | This documentation, plus the shared tile table and test fixtures in `docs/level-format/`       |

## Android app and engine

Requirements: JDK 17+ (Android Studio's bundled JDK works) and the Android SDK with platform 37.

```sh
./gradlew :engine:test          # engine tests, incl. loading all campaign levels
./gradlew :app:assembleDebug    # build the app
./gradlew :app:lintDebug
```

`docs/level-format/tiles.json` is generated from the engine. After changing tiles in
`engine/.../model/TileType.kt` regenerate it (and the share code fixture) with
`UPDATE_GENERATED=1 ./gradlew :engine:test`.

## Web level editor

Requirements: Node.js 24+.

```sh
cd editor
npm install
npm run dev           # editor on http://localhost:5173
npm test              # format, share code and editor tests
npm run lint          # oxlint
npm run format        # oxfmt (npm run format:check in CI)
npm run build         # static build in editor/dist
```

The web editor uses the same sprites as the app (`app/src/main/assets/sprites`) and reads and writes the same
level files and share codes.

### Levels from the old app

The old app used a hex "level code" (format v1). The web editor imports those codes directly. To convert
codes or a folder of the old level JSON files on the command line:

```sh
cd editor
node scripts/convert-v1.ts <v1 code>
node scripts/convert-v1.ts --campaign <old level folder> <output folder>
```

## Sprites

All sprites are pixel art drawn by code, so they share one palette and can be changed in one place:

```sh
python tools/sprites/draw_sprites.py                    # writes app/src/main/assets/sprites
python tools/sprites/draw_sprites.py --dry-run --sheet sheet.png --only boat bomb   # preview only
```

- Every entry of `Sprite.kt` needs a `<name>.png`; animation frames are `<name>@1.png`, `<name>@2.png`, …
- Sprites are 16×16. Sprites that overhang their cell (palm crowns, the hut's flag, the bomb's fuse) are
  bigger and are drawn centred on their cell.
- Shore and corner transitions go to `sprites/edges/`; the renderer picks them automatically.

## Sounds

The sound effects are synthesised as well – no samples, just waves and filtered noise:

```sh
python tools/sounds/make_sounds.py                     # writes app/src/main/res/raw/sfx_*.wav
```

`app/.../audio/SoundEffects.kt` lists the sounds and their volume. The board animation
(`render/AnimatedBoard.kt`) decides which sound plays when, so a splash plays when the box actually reaches
the water. Players can turn sounds off in the settings.

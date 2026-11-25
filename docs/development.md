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

### Releases (downloadable APK)

`.github/workflows/release.yml` builds the APK on GitHub Actions. Pushing a tag that starts with `v`
publishes it as a GitHub Release with the APK attached; every run also keeps the APK as a workflow
artifact. Tags with a `-` (e.g. `v2.1-beta`) become pre-releases.

To cut a release:

1. Raise `versionCode` and `versionName` in `app/build.gradle.kts` and merge that to `main`.
2. Tag the commit and push the tag:

   ```sh
   git tag v2.1
   git push origin v2.1
   ```

A manual run (*Actions → Release → Run workflow*) on a branch only builds the artifact; run it on a tag to
re-publish that tag's release (the APK is replaced).

**Signing.** A signed release APK needs these repository secrets (*Settings → Secrets and variables →
Actions*):

| Secret                      | Value                                               |
| --------------------------- | --------------------------------------------------- |
| `ANDROID_KEYSTORE_BASE64`   | The keystore file, base64 encoded (see below)       |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password                                   |
| `ANDROID_KEY_ALIAS`         | Alias of the signing key                            |
| `ANDROID_KEY_PASSWORD`      | Password of the signing key                         |

```sh
keytool -genkeypair -v -keystore release.jks -alias pushy -keyalg RSA -keysize 4096 -validity 10000
base64 -w0 release.jks            # Linux; PowerShell: [Convert]::ToBase64String([IO.File]::ReadAllBytes("release.jks"))
```

Keep the keystore safe and out of the repository: Android only installs updates signed with the same key.
Without the secrets the workflow builds a debug APK instead (marked as a pre-release). Locally the same
environment variables (`ANDROID_KEYSTORE_PATH` pointing at the file, plus the three passwords/alias) sign
`./gradlew :app:assembleRelease`; without them the release build is unsigned.

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

import react from '@vitejs/plugin-react'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitest/config'

const repoRoot = fileURLToPath(new URL('..', import.meta.url))

export default defineConfig({
  // Relative paths so the built editor works from any folder (or GitHub Pages).
  base: './',
  plugins: [react()],
  // The app's assets folder holds the sprites (and campaign levels) – served as /sprites/*.png.
  publicDir: fileURLToPath(new URL('../app/src/main/assets', import.meta.url)),
  server: { fs: { allow: [repoRoot] } },
  test: { environment: 'node' },
})

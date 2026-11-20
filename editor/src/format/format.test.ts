import { readFileSync, writeFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'
import {
  decodeLevel,
  emptyLevel,
  levelFromJson,
  levelProblems,
  levelToJson,
  LevelFormatError,
} from './level.ts'
import { decodeShareCode, encodeShareCode } from './shareCode.ts'
import { ENTITY_TILES, TERRAIN_TILES, TILES } from './tiles.ts'
import { decodeV1Code, looksLikeV1Code } from './v1.ts'

const fixture = (name: string) =>
  fileURLToPath(new URL(`../../../docs/level-format/fixtures/${name}`, import.meta.url))
const sampleJson = readFileSync(fixture('sample-level.json'), 'utf8')

describe('level format v2', () => {
  it('writes byte-identical files to the Kotlin engine', () => {
    const level = levelFromJson(sampleJson)
    expect(levelToJson(level)).toBe(sampleJson.replaceAll('\r\n', '\n'))
  })

  it('round-trips every tile', () => {
    const level = emptyLevel('All tiles', 8, 13)
    TERRAIN_TILES.forEach((tile, i) => (level.terrain[Math.floor(i / 13)]![i % 13] = tile.id))
    ENTITY_TILES.forEach((tile, i) => (level.entities[Math.floor(i / 13) + 3]![i % 13] = tile.id))
    const file = JSON.parse(levelToJson(level))
    expect(Object.keys(file.legend)).toHaveLength(TILES.length)
    expect(decodeLevel(file)).toEqual(level)
  })

  it('rejects broken files', () => {
    expect(() => levelFromJson('{}')).toThrow(LevelFormatError)
    expect(() => levelFromJson(sampleJson.replace('"water"', '"lava"'))).toThrow(LevelFormatError)
    expect(() => levelFromJson('nope')).toThrow(LevelFormatError)
  })

  it('reports problems like the engine', () => {
    expect(levelProblems(emptyLevel('Empty')).map((p) => p.kind)).toEqual([
      'missing-pushy',
      'missing-finish',
    ])
    expect(levelProblems(levelFromJson(sampleJson))).toEqual([])
  })
})

describe('share codes', () => {
  it('round-trips', async () => {
    const level = levelFromJson(sampleJson)
    const code = await encodeShareCode(level)
    expect(code.startsWith('PUSHY2:')).toBe(true)
    expect(await decodeShareCode(`  ${code}\n`)).toEqual(level)
  })

  it('reads codes written by the Android app and writes codes it can read', async () => {
    const level = levelFromJson(sampleJson)
    const kotlinCode = readFileSync(fixture('sample-level.kotlin.code'), 'utf8')
    expect(await decodeShareCode(kotlinCode)).toEqual(level)
    // The engine test decodes this file (run with UPDATE_GENERATED=1 to refresh it).
    if (process.env.UPDATE_GENERATED === '1') {
      writeFileSync(fixture('sample-level.web.code'), `${await encodeShareCode(level)}\n`)
    }
    const webCode = readFileSync(fixture('sample-level.web.code'), 'utf8')
    expect(await decodeShareCode(webCode)).toEqual(level)
  })

  it('rejects garbage', async () => {
    await expect(decodeShareCode('hello')).rejects.toThrow(LevelFormatError)
    await expect(decodeShareCode('PUSHY2:AAAA')).rejects.toThrow(LevelFormatError)
  })
})

describe('v1 codes', () => {
  it('decodes the old share code of the sample level', () => {
    const code = readFileSync(fixture('sample-level.v1.code'), 'utf8')
    expect(looksLikeV1Code(code)).toBe(true)
    // The old editor stored every level code under the name "Levelname".
    expect(decodeV1Code(code)).toEqual({ ...levelFromJson(sampleJson), name: 'Levelname' })
  })
})

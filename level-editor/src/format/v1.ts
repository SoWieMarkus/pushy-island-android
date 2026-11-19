// Converters for levels from the old app (v1 share codes and the old campaign JSON files).
import { DEFAULT_HEIGHT, DEFAULT_WIDTH, LevelFormatError, type LevelData } from './level.ts'
import { LEGACY_NO_ENTITY, TILES, V1_NO_ENTITY, type Tile } from './tiles.ts'

const BITS_PER_TILE = 6

const byV1Code = {
  terrain: new Map(TILES.filter((t) => t.layer === 'terrain').map((t) => [t.v1Code, t])),
  entity: new Map(TILES.filter((t) => t.layer === 'entity').map((t) => [t.v1Code, t])),
}

const byLegacyId = {
  terrain: new Map(TILES.filter((t) => t.layer === 'terrain').map((t) => [t.legacyId, t])),
  entity: new Map(TILES.filter((t) => t.layer === 'entity').map((t) => [t.legacyId, t])),
}

function grid<T>(cell: () => T): T[][] {
  return Array.from({ length: DEFAULT_HEIGHT }, () => Array.from({ length: DEFAULT_WIDTH }, cell))
}

function lookupLegacy(layer: 'terrain' | 'entity', id: string): Tile {
  const tile = byLegacyId[layer].get(id)
  if (!tile) throw new LevelFormatError(`Unknown legacy ${layer} '${id}'`)
  return tile
}

/** Heuristic used by the editor's import box: v1 codes are long hex strings starting with "01". */
export function looksLikeV1Code(text: string): boolean {
  const compact = text.replace(/\s+/g, '')
  return compact.length > 700 && /^01[0-9a-f]+$/i.test(compact)
}

/**
 * Decodes a v1 share code: one version byte, then 12×20 terrain and 12×20 entity tiles with 6 bits
 * each (row-major), then the level name with 8 bits per character.
 */
export function decodeV1Code(code: string): LevelData {
  const hex = code.replace(/\s+/g, '')
  if (!/^[0-9a-f]+$/i.test(hex)) throw new LevelFormatError('A v1 code only contains hex digits')
  const bits = [...hex].map((digit) => parseInt(digit, 16).toString(2).padStart(4, '0')).join('')
  const version = parseInt(bits.slice(0, 8), 2)
  if (version !== 1) throw new LevelFormatError(`Unsupported v1 code version ${version}`)

  const cells = DEFAULT_HEIGHT * DEFAULT_WIDTH
  const tileBits = 2 * cells * BITS_PER_TILE
  if (bits.length < 8 + tileBits) throw new LevelFormatError('v1 code is too short')
  let offset = 8
  const read = () => {
    const value = parseInt(bits.slice(offset, offset + BITS_PER_TILE), 2)
    offset += BITS_PER_TILE
    return value
  }

  const terrain = grid(() => {
    const value = read()
    const tile = byV1Code.terrain.get(value)
    if (!tile) throw new LevelFormatError(`Unknown v1 terrain code ${value}`)
    return tile.id
  })
  const entities = grid(() => {
    const value = read()
    if (value === V1_NO_ENTITY) return null
    const tile = byV1Code.entity.get(value)
    if (!tile) throw new LevelFormatError(`Unknown v1 entity code ${value}`)
    return tile.id
  })

  let name = ''
  for (let i = offset; i + 8 <= bits.length; i += 8) {
    name += String.fromCharCode(parseInt(bits.slice(i, i + 8), 2))
  }
  return { name, width: DEFAULT_WIDTH, height: DEFAULT_HEIGHT, terrain, entities }
}

export interface LegacyLevelJson {
  name: string
  number?: number
  code?: string
  terrain: string[][]
  entities: string[][]
}

/** Converts a level file from the old `app/src/main/assets/level` folder. */
export function convertLegacyJson(legacy: LegacyLevelJson): LevelData {
  return {
    name: legacy.name,
    width: legacy.terrain[0]?.length ?? 0,
    height: legacy.terrain.length,
    terrain: legacy.terrain.map((row) => row.map((id) => lookupLegacy('terrain', id).id)),
    entities: legacy.entities.map((row) =>
      row.map((id) => (id === LEGACY_NO_ENTITY ? null : lookupLegacy('entity', id).id)),
    ),
  }
}

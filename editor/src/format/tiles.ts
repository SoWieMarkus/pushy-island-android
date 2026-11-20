// The tile table is generated from the Kotlin engine (see engine TileTableTest) so both sides agree.
import table from '../../../level-format/tiles.json' with { type: 'json' }

export type Layer = 'terrain' | 'entity'
export type Side = 'north' | 'south' | 'east' | 'west'
export type PortKind = 'input' | 'output'

export interface Tile {
  id: string
  layer: Layer
  sprite: string
  maxInstances: number | null
  isLogic: boolean
  symbol: string | null
  v1Code: number
  legacyId: string
  ports: Partial<Record<Side, PortKind>>
  /** Terrain family used by corner smoothing; `none` for entities. */
  surface: 'sand' | 'grass' | 'water' | 'other' | 'none'
}

export const FORMAT_VERSION: number = table.format
export const EMPTY: string = table.empty
export const SYMBOL_POOL: string = table.symbolPool
export const V1_NO_ENTITY: number = table.v1NoEntity
export const LEGACY_NO_ENTITY: string = table.legacyNoEntity

export const TILES: readonly Tile[] = table.tiles as Tile[]
export const TERRAIN_TILES = TILES.filter((tile) => tile.layer === 'terrain')
export const ENTITY_TILES = TILES.filter((tile) => tile.layer === 'entity')

const byId = new Map(TILES.map((tile) => [tile.id, tile]))

export function tileById(id: string): Tile | undefined {
  return byId.get(id)
}

export function requireTile(id: string): Tile {
  const tile = byId.get(id)
  if (!tile) throw new Error(`Unknown tile '${id}'`)
  return tile
}

export const SIDES: readonly Side[] = ['north', 'south', 'east', 'west']

export const OPPOSITE: Record<Side, Side> = {
  north: 'south',
  south: 'north',
  east: 'west',
  west: 'east',
}

export const STEP: Record<Side, readonly [number, number]> = {
  north: [-1, 0],
  south: [1, 0],
  east: [0, 1],
  west: [0, -1],
}

// What to draw per cell – mirrors `LevelData.previewSprites()` in the engine for the editor preview.
import type { LevelData } from '../format/level.ts'
import { OPPOSITE, SIDES, STEP, requireTile } from '../format/tiles.ts'

export function spriteUrl(sprite: string): string {
  return `${import.meta.env.BASE_URL}sprites/${sprite}.png`
}

function portFacing(level: LevelData, row: number, col: number, side: (typeof SIDES)[number]) {
  const terrain = requireTile(level.terrain[row]![col]!)
  const entityId = level.entities[row]![col]
  const entity = entityId ? requireTile(entityId) : undefined
  return terrain.ports[side] ?? entity?.ports[side]
}

/** Cable arms point at neighbouring cables and at ports facing the cable; no neighbour = a cross. */
function cableSprites(level: LevelData, row: number, col: number): string[] {
  const arms = SIDES.filter((side) => {
    const [dRow, dCol] = STEP[side]
    const r = row + dRow
    const c = col + dCol
    if (r < 0 || r >= level.height || c < 0 || c >= level.width) return false
    return level.terrain[r]![c] === 'cable' || portFacing(level, r, c, OPPOSITE[side]) !== undefined
  })
  return ['sand', ...(arms.length > 0 ? arms : SIDES).map((side) => `cable_${side}`)]
}

export function terrainSprites(level: LevelData, row: number, col: number): string[] {
  const id = level.terrain[row]![col]!
  switch (id) {
    case 'cable':
      return cableSprites(level, row, col)
    case 'ice':
      return ['water', 'ice']
    case 'spike_pressure_plate':
      return ['sand', 'spike_pressure_plate']
    default:
      return [requireTile(id).sprite]
  }
}

export function entitySprites(level: LevelData, row: number, col: number): string[] {
  const id = level.entities[row]![col]
  if (!id) return []
  const tile = requireTile(id)
  // Gates and repeaters show little arrows for their inputs and outputs.
  if (id.startsWith('logic_gate_') || id.startsWith('repeater_')) {
    return [
      tile.sprite,
      ...SIDES.flatMap((side) => (tile.ports[side] ? [`${tile.ports[side]}_${side}`] : [])),
    ]
  }
  return [tile.sprite]
}

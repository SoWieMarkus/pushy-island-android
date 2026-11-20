// Editing operations – a TypeScript port of the engine's LevelEditor (engine/.../editor/LevelEditor.kt).
import type { LevelData } from '../format/level.ts'
import { SIDES, STEP, requireTile, type Tile } from '../format/tiles.ts'

export type EditMode = 'pencil' | 'fill' | 'erase'

export interface Cell {
  row: number
  col: number
}

const HISTORY_LIMIT = 100

/** Cells on the straight line from [from] to [to] (inclusive), in order. */
export function cellsBetween(from: Cell, to: Cell): Cell[] {
  const steps = Math.max(Math.abs(to.row - from.row), Math.abs(to.col - from.col))
  return Array.from({ length: steps + 1 }, (_, i) => ({
    row: Math.round(from.row + ((to.row - from.row) * i) / Math.max(steps, 1)),
    col: Math.round(from.col + ((to.col - from.col) * i) / Math.max(steps, 1)),
  }))
}

export interface EditorState {
  level: LevelData
  undo: LevelData[]
  redo: LevelData[]
}

export function createEditor(level: LevelData): EditorState {
  return { level, undo: [], redo: [] }
}

function cloneLevel(level: LevelData): LevelData {
  return {
    ...level,
    terrain: level.terrain.map((row) => [...row]),
    entities: level.entities.map((row) => [...row]),
  }
}

function sameLevel(a: LevelData, b: LevelData): boolean {
  return (
    a.name === b.name &&
    JSON.stringify([a.terrain, a.entities]) === JSON.stringify([b.terrain, b.entities])
  )
}

/** Records [next] as a new history step, or merges it into the previous one (drag strokes). */
function commit(state: EditorState, next: LevelData, merge = false): EditorState {
  if (sameLevel(next, state.level)) return state
  const history =
    merge && state.undo.length > 0 ? state.undo : [...state.undo, state.level].slice(-HISTORY_LIMIT)
  return { level: next, undo: history, redo: [] }
}

function edit(state: EditorState, block: (level: LevelData) => void, merge = false): EditorState {
  const next = cloneLevel(state.level)
  block(next)
  return commit(state, next, merge)
}

const inside = (level: LevelData, { row, col }: Cell) =>
  row >= 0 && row < level.height && col >= 0 && col < level.width

function put(level: LevelData, cell: Cell, tile: Tile) {
  if (!inside(level, cell)) return
  if (tile.layer === 'terrain') level.terrain[cell.row]![cell.col] = tile.id
  else level.entities[cell.row]![cell.col] = tile.id
}

function read(level: LevelData, cell: Cell, layer: Tile['layer']): string | null {
  return layer === 'terrain'
    ? level.terrain[cell.row]![cell.col]!
    : level.entities[cell.row]![cell.col]!
}

/** Keeps at most `maxInstances - 1` existing copies, then places the new one. */
function placeLimited(level: LevelData, cell: Cell, tile: Tile) {
  let keep = (tile.maxInstances ?? Number.POSITIVE_INFINITY) - 1
  for (let row = 0; row < level.height; row++) {
    for (let col = 0; col < level.width; col++) {
      if (read(level, { row, col }, tile.layer) !== tile.id) continue
      if (keep > 0) {
        keep--
        continue
      }
      if (tile.layer === 'terrain') level.terrain[row]![col] = 'sand'
      else level.entities[row]![col] = null
    }
  }
  put(level, cell, tile)
}

/** The pirate always stands directly north of his hut; placing one places both. */
function placePirate(level: LevelData, cell: Cell, tile: Tile) {
  const north = { row: cell.row - 1, col: cell.col }
  const south = { row: cell.row + 1, col: cell.col }
  let pirate: Cell
  let hut: Cell
  if (tile.id === 'pirate') {
    ;[pirate, hut] = inside(level, south) ? [cell, south] : [north, cell]
  } else {
    ;[pirate, hut] = inside(level, north) ? [north, cell] : [cell, south]
  }
  placeLimited(level, pirate, requireTile('pirate'))
  placeLimited(level, hut, requireTile('pirate_hut'))
}

export function place(state: EditorState, cell: Cell, tile: Tile, merge = false): EditorState {
  return edit(
    state,
    (level) => {
      if (tile.id === 'pirate' || tile.id === 'pirate_hut') placePirate(level, cell, tile)
      else if (tile.maxInstances !== null) placeLimited(level, cell, tile)
      else put(level, cell, tile)
    },
    merge,
  )
}

/** Removes the entity at [cell], or turns the terrain back into water if there is none. */
export function erase(state: EditorState, cell: Cell, merge = false): EditorState {
  return edit(
    state,
    (level) => {
      if (level.entities[cell.row]![cell.col] !== null) level.entities[cell.row]![cell.col] = null
      else level.terrain[cell.row]![cell.col] = 'water'
    },
    merge,
  )
}

/** Flood-fills the connected area of equal tiles on [tile]'s layer. */
export function fill(state: EditorState, start: Cell, tile: Tile): EditorState {
  return edit(state, (level) => {
    const replaced = read(level, start, tile.layer)
    if (replaced === tile.id) return
    const queue = [start]
    const seen = new Set([`${start.row},${start.col}`])
    while (queue.length > 0) {
      const cell = queue.shift()!
      put(level, cell, tile)
      for (const side of SIDES) {
        const [dRow, dCol] = STEP[side]
        const next = { row: cell.row + dRow, col: cell.col + dCol }
        const key = `${next.row},${next.col}`
        if (inside(level, next) && !seen.has(key) && read(level, next, tile.layer) === replaced) {
          seen.add(key)
          queue.push(next)
        }
      }
    }
  })
}

/** Applies [tile] according to [mode]; [merge] continues the current drag stroke. */
export function apply(
  state: EditorState,
  cell: Cell,
  tile: Tile,
  mode: EditMode,
  merge = false,
): EditorState {
  if (mode === 'erase') return erase(state, cell, merge)
  if (mode === 'fill' && tile.maxInstances === null && !tile.isLogic)
    return merge ? state : fill(state, cell, tile)
  return place(state, cell, tile, merge)
}

const SAND = new Set([
  'sand',
  'sand_top_left',
  'sand_top_right',
  'sand_bottom_left',
  'sand_bottom_right',
])
const GRASS = new Set([
  'grass',
  'grass_top_left',
  'grass_top_right',
  'grass_bottom_left',
  'grass_bottom_right',
])
const land = (surface: string | null) => surface === 'sand' || surface === 'grass'
const sandLike = (surface: string | null) =>
  surface === null || surface === 'sand' || surface === 'water'

/** Picks matching corner tiles for every plain sand and grass field (same rules as the engine). */
export function smooth(state: EditorState): EditorState {
  return edit(state, (level) => {
    const original = level.terrain.map((row) => [...row])
    const surface = (row: number, col: number) =>
      row < 0 || row >= level.height || col < 0 || col >= level.width
        ? null
        : requireTile(original[row]![col]!).surface

    for (let row = 0; row < level.height; row++) {
      for (let col = 0; col < level.width; col++) {
        const id = original[row]![col]!
        const top = surface(row - 1, col)
        const down = surface(row + 1, col)
        const left = surface(row, col - 1)
        const right = surface(row, col + 1)
        if (SAND.has(id)) {
          level.terrain[row]![col] =
            !land(down) && !land(right) && land(left) && land(top)
              ? 'sand_bottom_right'
              : !land(down) && !land(left) && land(right) && land(top)
                ? 'sand_bottom_left'
                : !land(top) && !land(right) && land(left) && land(down)
                  ? 'sand_top_right'
                  : !land(top) && !land(left) && land(right) && land(down)
                    ? 'sand_top_left'
                    : 'sand'
        } else if (GRASS.has(id)) {
          level.terrain[row]![col] =
            !sandLike(down) && !sandLike(right) && sandLike(left) && sandLike(top)
              ? 'grass_top_left'
              : !sandLike(down) && !sandLike(left) && sandLike(right) && sandLike(top)
                ? 'grass_top_right'
                : !sandLike(top) && !sandLike(right) && sandLike(left) && sandLike(down)
                  ? 'grass_bottom_left'
                  : !sandLike(top) && !sandLike(left) && sandLike(right) && sandLike(down)
                    ? 'grass_bottom_right'
                    : 'grass'
        }
      }
    }
  })
}

export function reset(state: EditorState): EditorState {
  return edit(state, (level) => {
    level.terrain = level.terrain.map((row) => row.map(() => 'water'))
    level.entities = level.entities.map((row) => row.map(() => null))
  })
}

export function rename(state: EditorState, name: string): EditorState {
  return commit(state, { ...state.level, name })
}

export function undo(state: EditorState): EditorState {
  const previous = state.undo.at(-1)
  if (!previous) return state
  return { level: previous, undo: state.undo.slice(0, -1), redo: [...state.redo, state.level] }
}

export function redo(state: EditorState): EditorState {
  const next = state.redo.at(-1)
  if (!next) return state
  return { level: next, undo: [...state.undo, state.level], redo: state.redo.slice(0, -1) }
}

/** Replaces the level (import) as one undoable step. */
export function load(state: EditorState, level: LevelData): EditorState {
  return commit(state, level)
}

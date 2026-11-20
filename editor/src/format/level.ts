import { EMPTY, FORMAT_VERSION, SYMBOL_POOL, tileById, type Tile } from './tiles.ts'

export const DEFAULT_WIDTH = 20
export const DEFAULT_HEIGHT = 12
export const MAX_NAME_LENGTH = 20

/** A level in memory: tile ids per cell, `null` for an empty entity cell. */
export interface LevelData {
  name: string
  width: number
  height: number
  terrain: string[][]
  entities: (string | null)[][]
}

/** Level file format v2 (see docs/level-format/README.md). Key order matches the Kotlin engine. */
export interface LevelFile {
  format: number
  name: string
  width: number
  height: number
  legend: Record<string, string>
  terrain: string[]
  entities: string[]
}

export class LevelFormatError extends Error {
  override name = 'LevelFormatError'
}

export function emptyLevel(name = '', height = DEFAULT_HEIGHT, width = DEFAULT_WIDTH): LevelData {
  return {
    name,
    width,
    height,
    terrain: Array.from({ length: height }, () => Array.from({ length: width }, () => 'water')),
    entities: Array.from({ length: height }, () => Array.from({ length: width }, () => null)),
  }
}

/** Same algorithm as the Kotlin `LevelFormat.encode`, so both produce identical files. */
export function encodeLevel(level: LevelData): LevelFile {
  const used: string[] = []
  const seen = new Set<string>()
  const remember = (id: string | null) => {
    if (id !== null && !seen.has(id)) {
      seen.add(id)
      used.push(id)
    }
  }
  level.terrain.forEach((row) => row.forEach(remember))
  level.entities.forEach((row) => row.forEach(remember))

  const symbols = new Map<string, string>()
  const taken = new Set<string>([EMPTY])
  let poolIndex = 0
  for (const id of used) {
    const preferred = tileById(id)?.symbol
    let symbol: string
    if (preferred && !taken.has(preferred)) {
      symbol = preferred
    } else {
      do {
        symbol = SYMBOL_POOL[poolIndex++] ?? ''
        if (!symbol) throw new LevelFormatError('Too many different tiles in one level')
      } while (taken.has(symbol))
    }
    taken.add(symbol)
    symbols.set(id, symbol)
  }

  const legend: Record<string, string> = {}
  for (const [id, symbol] of symbols) legend[symbol] = id

  return {
    format: FORMAT_VERSION,
    name: level.name,
    width: level.width,
    height: level.height,
    legend,
    terrain: level.terrain.map((row) => row.map((id) => symbols.get(id)).join('')),
    entities: level.entities.map((row) =>
      row.map((id) => (id === null ? EMPTY : symbols.get(id))).join(''),
    ),
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function stringArray(value: unknown, what: string): string[] {
  if (!Array.isArray(value) || !value.every((row) => typeof row === 'string')) {
    throw new LevelFormatError(`'${what}' must be a list of strings`)
  }
  return value
}

export function decodeLevel(input: unknown): LevelData {
  if (!isRecord(input)) throw new LevelFormatError('Not a level file')
  const { format, name, width, height, legend } = input
  if (format !== FORMAT_VERSION)
    throw new LevelFormatError(`Unsupported level format ${String(format)}`)
  if (typeof name !== 'string') throw new LevelFormatError("'name' must be a string")
  if (
    !Number.isInteger(width) ||
    !Number.isInteger(height) ||
    (width as number) <= 0 ||
    (height as number) <= 0
  ) {
    throw new LevelFormatError('Invalid level size')
  }
  if (!isRecord(legend)) throw new LevelFormatError("'legend' must be an object")
  const w = width as number
  const h = height as number
  const terrainRows = stringArray(input.terrain, 'terrain')
  const entityRows = stringArray(input.entities, 'entities')
  if (terrainRows.length !== h || entityRows.length !== h) {
    throw new LevelFormatError(`Expected ${h} rows per layer`)
  }

  const tiles = new Map<string, Tile>()
  for (const [symbol, id] of Object.entries(legend)) {
    if ([...symbol].length !== 1)
      throw new LevelFormatError(`Legend key '${symbol}' must be one character`)
    const tile = typeof id === 'string' ? tileById(id) : undefined
    if (!tile) throw new LevelFormatError(`Unknown tile '${String(id)}' for symbol '${symbol}'`)
    tiles.set(symbol, tile)
  }

  const readLayer = (rows: string[], layer: 'terrain' | 'entity') =>
    rows.map((line, row) => {
      const cells = [...line]
      if (cells.length !== w) throw new LevelFormatError(`Row ${row} must have ${w} cells`)
      return cells.map((symbol, col) => {
        if (layer === 'entity' && symbol === EMPTY) return null
        const tile = tiles.get(symbol)
        if (!tile || tile.layer !== layer) {
          throw new LevelFormatError(`Symbol '${symbol}' at ${row},${col} is not a ${layer} tile`)
        }
        return tile.id
      })
    })

  return {
    name,
    width: w,
    height: h,
    terrain: readLayer(terrainRows, 'terrain') as string[][],
    entities: readLayer(entityRows, 'entity'),
  }
}

export function levelToJson(level: LevelData, pretty = true): string {
  const file = encodeLevel(level)
  return pretty ? `${JSON.stringify(file, null, 2)}\n` : JSON.stringify(file)
}

export function levelFromJson(json: string): LevelData {
  let parsed: unknown
  try {
    parsed = JSON.parse(json)
  } catch {
    throw new LevelFormatError('Not valid JSON')
  }
  return decodeLevel(parsed)
}

export type LevelProblem =
  | { kind: 'missing-pushy' }
  | { kind: 'missing-finish' }
  | { kind: 'invalid-name' }
  | { kind: 'too-many'; tileId: string; max: number }

/** Mirrors `LevelData.problems()` in the engine. */
export function levelProblems(level: LevelData): LevelProblem[] {
  const counts = new Map<string, number>()
  for (const row of [...level.terrain, ...level.entities]) {
    for (const id of row) if (id !== null) counts.set(id, (counts.get(id) ?? 0) + 1)
  }
  const problems: LevelProblem[] = []
  if (!counts.get('pushy')) problems.push({ kind: 'missing-pushy' })
  if (!counts.get('finish')) problems.push({ kind: 'missing-finish' })
  if (level.name.trim() === '' || level.name.length > MAX_NAME_LENGTH)
    problems.push({ kind: 'invalid-name' })
  for (const [id, count] of counts) {
    const max = tileById(id)?.maxInstances
    if (max != null && count > max) problems.push({ kind: 'too-many', tileId: id, max })
  }
  return problems
}

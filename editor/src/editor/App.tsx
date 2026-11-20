import { useCallback, useEffect, useMemo, useRef, useState, type PointerEvent } from 'react'
import {
  emptyLevel,
  levelFromJson,
  levelProblems,
  levelToJson,
  MAX_NAME_LENGTH,
  type LevelData,
  type LevelProblem,
} from '../format/level.ts'
import { decodeShareCode, encodeShareCode, isShareCode } from '../format/shareCode.ts'
import { ENTITY_TILES, TERRAIN_TILES, requireTile, type Tile } from '../format/tiles.ts'
import { decodeV1Code, looksLikeV1Code } from '../format/v1.ts'
import * as model from './model.ts'
import { entitySprites, markOverhang, spriteUrl, terrainSprites } from './sprites.ts'

const DRAFT_KEY = 'pushy-island-editor-draft'

function loadDraft(): LevelData {
  try {
    const json = localStorage.getItem(DRAFT_KEY)
    if (json) return levelFromJson(json)
  } catch {
    // No storage or a broken draft – start fresh.
  }
  return emptyLevel('My island')
}

function saveDraft(level: LevelData) {
  try {
    localStorage.setItem(DRAFT_KEY, levelToJson(level, false))
  } catch {
    // Storage unavailable (private mode) – the draft just isn't remembered.
  }
}

function describe(problem: LevelProblem): string {
  switch (problem.kind) {
    case 'missing-pushy':
      return 'Place Pushy.'
    case 'missing-finish':
      return 'Place a finish.'
    case 'invalid-name':
      return `Give the level a name (1–${MAX_NAME_LENGTH} characters).`
    case 'too-many':
      return `Too many “${problem.tileId}” tiles (max. ${problem.max}).`
  }
}

function label(tile: Tile): string {
  return tile.id.replaceAll('_', ' ')
}

/** Accepts a PUSHY2 share code, an old v1 code or a level JSON file. */
async function parseImport(text: string): Promise<LevelData> {
  const trimmed = text.trim()
  if (isShareCode(trimmed)) return decodeShareCode(trimmed)
  if (looksLikeV1Code(trimmed)) return decodeV1Code(trimmed)
  return levelFromJson(trimmed)
}

export function App() {
  const [editor, setEditor] = useState(() => model.createEditor(loadDraft()))
  const [selected, setSelected] = useState<Tile>(() => requireTile('sand'))
  const [mode, setMode] = useState<model.EditMode>('pencil')
  const [importText, setImportText] = useState('')
  const [message, setMessage] = useState<{ text: string; error?: boolean } | null>(null)
  const level = editor.level
  const problems = useMemo(() => levelProblems(level), [level])

  useEffect(() => saveDraft(level), [level])

  const update = useCallback((change: (state: model.EditorState) => model.EditorState) => {
    setEditor((state) => change(state))
  }, [])

  // Keyboard shortcuts: Ctrl+Z / Ctrl+Y (or Ctrl+Shift+Z).
  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      if (!(event.ctrlKey || event.metaKey) || event.target instanceof HTMLInputElement) return
      if (event.target instanceof HTMLTextAreaElement) return
      const key = event.key.toLowerCase()
      if (key === 'z' && !event.shiftKey) update(model.undo)
      else if (key === 'y' || (key === 'z' && event.shiftKey)) update(model.redo)
      else return
      event.preventDefault()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [update])

  // ---- painting --------------------------------------------------------------------------
  const lastCell = useRef<model.Cell | null>(null)

  const cellAt = (event: PointerEvent<HTMLDivElement>) => {
    const rect = event.currentTarget.getBoundingClientRect()
    const col = Math.floor(((event.clientX - rect.left) / rect.width) * level.width)
    const row = Math.floor(((event.clientY - rect.top) / rect.height) * level.height)
    if (row < 0 || row >= level.height || col < 0 || col >= level.width) return null
    return { row, col }
  }

  const onPointerDown = (event: PointerEvent<HTMLDivElement>) => {
    const cell = cellAt(event)
    if (!cell) return
    event.currentTarget.setPointerCapture(event.pointerId)
    lastCell.current = cell
    update((state) => model.apply(state, cell, selected, mode))
  }

  const onPointerMove = (event: PointerEvent<HTMLDivElement>) => {
    const last = lastCell.current
    if (last === null) return
    const cell = cellAt(event)
    if (!cell || (cell.row === last.row && cell.col === last.col)) return
    lastCell.current = cell
    // Pointer events can skip cells on fast drags – paint the whole line in between.
    const path = model.cellsBetween(last, cell).slice(1)
    update((state) => path.reduce((s, c) => model.apply(s, c, selected, mode, true), state))
  }

  const endStroke = () => {
    lastCell.current = null
  }

  // ---- import / export -------------------------------------------------------------------
  const flash = (text: string, error = false) => setMessage({ text, error })

  const copyShareCode = async () => {
    const code = await encodeShareCode(level)
    try {
      await navigator.clipboard.writeText(code)
      flash('Share code copied – paste it into “Import code” in the app.')
    } catch {
      setImportText(code)
      flash('Clipboard not available – the code is in the import box.')
    }
  }

  const downloadJson = () => {
    const blob = new Blob([levelToJson(level)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${
      level.name
        .trim()
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, '-') || 'level'
    }.json`
    link.click()
    URL.revokeObjectURL(url)
  }

  const importLevel = async (text: string) => {
    try {
      const imported = await parseImport(text)
      update((state) => model.load(state, imported))
      setImportText('')
      flash(`Imported “${imported.name}”.`)
    } catch (error) {
      flash(`Could not import: ${(error as Error).message}`, true)
    }
  }

  const openFile = async (file: File | undefined) => {
    if (file) await importLevel(await file.text())
  }

  // ---- rendering -------------------------------------------------------------------------
  const cells = []
  for (let row = 0; row < level.height; row++) {
    for (let col = 0; col < level.width; col++) {
      // Sprites are unique within a layer, so layer + sprite makes a stable key.
      const sprites = [
        ...terrainSprites(level, row, col).map((sprite) => `terrain:${sprite}`),
        ...entitySprites(level, row, col).map((sprite) => `entity:${sprite}`),
      ]
      cells.push(
        <div className="cell" key={`${row},${col}`}>
          {sprites.map((key) => (
            <img
              key={key}
              className={key.startsWith('entity:') ? 'entity' : undefined}
              src={spriteUrl(key.split(':')[1]!)}
              alt=""
              draggable={false}
              onLoad={markOverhang}
            />
          ))}
        </div>,
      )
    }
  }

  const modeButton = (value: model.EditMode, text: string, hint: string) => (
    <button className={mode === value ? 'active' : ''} onClick={() => setMode(value)} title={hint}>
      {text}
    </button>
  )

  return (
    <div className="app">
      <header>
        <h1>Pushy Island</h1>
        <input
          className="name"
          value={level.name}
          maxLength={MAX_NAME_LENGTH}
          placeholder="Level name"
          onChange={(event) => update((state) => model.rename(state, event.target.value))}
          aria-label="Level name"
        />
        <div className="toolbar">
          {modeButton('pencil', 'Pencil', 'Paint single fields (drag to paint)')}
          {modeButton('fill', 'Fill', 'Flood-fill an area')}
          {modeButton('erase', 'Erase', 'Remove the entity, then the terrain')}
          <span className="separator" />
          <button
            onClick={() => update(model.undo)}
            disabled={editor.undo.length === 0}
            title="Ctrl+Z"
          >
            Undo
          </button>
          <button
            onClick={() => update(model.redo)}
            disabled={editor.redo.length === 0}
            title="Ctrl+Y"
          >
            Redo
          </button>
          <button onClick={() => update(model.smooth)} title="Round off sand and grass corners">
            Smooth
          </button>
          <button onClick={() => update(model.reset)} title="Clear the whole board">
            Reset
          </button>
        </div>
      </header>

      <main>
        <div
          className="board"
          style={{
            gridTemplateColumns: `repeat(${level.width}, 1fr)`,
            aspectRatio: `${level.width} / ${level.height}`,
          }}
          onPointerDown={onPointerDown}
          onPointerMove={onPointerMove}
          onPointerUp={endStroke}
          onPointerCancel={endStroke}
        >
          {cells}
        </div>

        <aside>
          <section>
            <h2>Terrain</h2>
            <Palette tiles={TERRAIN_TILES} selected={selected} onSelect={setSelected} />
          </section>
          <section>
            <h2>Entities</h2>
            <Palette
              tiles={ENTITY_TILES}
              selected={selected}
              onSelect={(tile) => {
                setSelected(tile)
                if (mode === 'erase') setMode('pencil')
              }}
            />
          </section>
          <p className="selected">
            Selected: <strong>{label(selected)}</strong>
          </p>
        </aside>
      </main>

      <footer>
        <section className="status">
          <h2>Check</h2>
          {problems.length === 0 ? (
            <p className="ok">Ready to share. Solve it in the app before passing it on!</p>
          ) : (
            <ul>
              {problems.map((problem) => (
                <li key={JSON.stringify(problem)}>{describe(problem)}</li>
              ))}
            </ul>
          )}
          <div className="actions">
            <button onClick={copyShareCode} disabled={problems.length > 0}>
              Copy share code
            </button>
            <button onClick={downloadJson}>Download JSON</button>
          </div>
          {message && <p className={message.error ? 'message error' : 'message'}>{message.text}</p>}
        </section>
        <section className="import">
          <h2>Import</h2>
          <textarea
            value={importText}
            onChange={(event) => setImportText(event.target.value)}
            placeholder="Paste a PUSHY2 share code, an old v1 code or level JSON"
            rows={3}
          />
          <div className="actions">
            <button onClick={() => importLevel(importText)} disabled={importText.trim() === ''}>
              Import
            </button>
            <label className="file">
              Open file…
              <input
                type="file"
                accept=".json,application/json"
                onChange={(event) => openFile(event.target.files?.[0])}
              />
            </label>
          </div>
        </section>
      </footer>
    </div>
  )
}

function Palette(props: {
  tiles: readonly Tile[]
  selected: Tile
  onSelect: (tile: Tile) => void
}) {
  return (
    <div className="palette">
      {props.tiles.map((tile) => (
        <button
          key={tile.id}
          className={tile === props.selected ? 'tile active' : 'tile'}
          title={label(tile)}
          aria-label={label(tile)}
          onClick={() => props.onSelect(tile)}
        >
          <img src={spriteUrl(tile.sprite)} alt="" draggable={false} />
        </button>
      ))}
    </div>
  )
}

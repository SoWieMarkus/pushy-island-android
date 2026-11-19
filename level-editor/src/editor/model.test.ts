import { describe, expect, it } from 'vitest'
import { emptyLevel } from '../format/level.ts'
import { requireTile } from '../format/tiles.ts'
import * as model from './model.ts'

const sand = requireTile('sand')
const editor = () => model.createEditor(emptyLevel('Test', 4, 5))

describe('editor model (mirrors LevelEditorTest.kt)', () => {
  it('fills, undoes and redoes', () => {
    let state = model.apply(editor(), { row: 0, col: 0 }, sand, 'fill')
    expect(state.level.terrain.flat().every((id) => id === 'sand')).toBe(true)
    state = model.apply(state, { row: 1, col: 1 }, requireTile('grass'), 'pencil')
    state = model.undo(model.undo(state))
    expect(state.level.terrain.flat().every((id) => id === 'water')).toBe(true)
    state = model.redo(model.redo(state))
    expect(state.level.terrain[1]![1]).toBe('grass')
  })

  it('treats a drag stroke as one undo step', () => {
    let state = model.apply(editor(), { row: 0, col: 0 }, sand, 'pencil')
    state = model.apply(state, { row: 1, col: 0 }, sand, 'pencil')
    state = model.apply(state, { row: 1, col: 1 }, sand, 'pencil', true)
    state = model.apply(state, { row: 1, col: 2 }, sand, 'pencil', true)
    expect(
      model
        .undo(state)
        .level.terrain.flat()
        .filter((id) => id === 'sand'),
    ).toHaveLength(1)
  })

  it('moves singletons instead of duplicating them', () => {
    let state = model.place(editor(), { row: 0, col: 0 }, requireTile('pushy'))
    state = model.place(state, { row: 2, col: 2 }, requireTile('pushy'))
    expect(state.level.entities[0]![0]).toBeNull()
    expect(state.level.entities[2]![2]).toBe('pushy')
  })

  it('places the pirate together with his hut', () => {
    const state = model.place(editor(), { row: 3, col: 1 }, requireTile('pirate'))
    expect(state.level.entities[2]![1]).toBe('pirate')
    expect(state.level.entities[3]![1]).toBe('pirate_hut')
  })

  it('smooths island corners', () => {
    let state = editor()
    for (const row of [1, 2])
      for (const col of [1, 2, 3]) state = model.place(state, { row, col }, sand)
    state = model.smooth(state)
    expect(state.level.terrain[1]!.slice(1, 4)).toEqual(['sand_top_left', 'sand', 'sand_top_right'])
    expect(state.level.terrain[2]!.slice(1, 4)).toEqual([
      'sand_bottom_left',
      'sand',
      'sand_bottom_right',
    ])
  })
})

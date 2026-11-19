// Converts levels from the old app to level format v2.
//
//   node scripts/convert-v1.ts --campaign <old level dir> <output dir>
//   node scripts/convert-v1.ts <v1 share code>
import { mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs'
import { join } from 'node:path'
import { levelProblems, levelToJson, type LevelData } from '../src/format/level.ts'
import { encodeShareCode } from '../src/format/shareCode.ts'
import { convertLegacyJson, decodeV1Code, type LegacyLevelJson } from '../src/format/v1.ts'

export function slug(name: string): string {
  return name
    .toLowerCase()
    .replaceAll('ß', 'ss')
    .replaceAll('ä', 'ae')
    .replaceAll('ö', 'oe')
    .replaceAll('ü', 'ue')
    .normalize('NFKD')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '')
}

// Some old files stored UTF-8 names that were decoded as Windows-1252 (e.g. "GieÃŸi").
const MOJIBAKE: Record<string, string> = {
  ÃŸ: 'ß',
  'Ã¤': 'ä',
  'Ã¶': 'ö',
  'Ã¼': 'ü',
  'Ã„': 'Ä',
  'Ã–': 'Ö',
  Ãœ: 'Ü',
}

export function repairName(name: string): string {
  return Object.entries(MOJIBAKE).reduce(
    (result, [broken, fixed]) => result.replaceAll(broken, fixed),
    name,
  )
}

function sameGrid(a: LevelData, b: LevelData): boolean {
  return JSON.stringify([a.terrain, a.entities]) === JSON.stringify([b.terrain, b.entities])
}

function convertCampaign(source: string, target: string) {
  mkdirSync(target, { recursive: true })
  let checked = 0
  const files = readdirSync(source)
    .filter((file) => file.endsWith('.json'))
    .toSorted()
  for (const file of files) {
    const legacy = JSON.parse(readFileSync(join(source, file), 'utf8')) as LegacyLevelJson
    const number = Number.parseInt(file.slice(0, 3), 10)
    const level = convertLegacyJson({ ...legacy, name: repairName(legacy.name) })
    // Most old files also embed their v1 code: use it to cross-check the v1 decoder.
    if (legacy.code) {
      try {
        if (!sameGrid(level, decodeV1Code(legacy.code))) throw new Error('grid differs')
        checked++
      } catch (error) {
        console.warn(`${file}: embedded v1 code not usable (${(error as Error).message})`)
      }
    }
    const problems = levelProblems(level)
    if (problems.length > 0) console.warn(`${file}: ${JSON.stringify(problems)}`)
    const name = `${String(number).padStart(3, '0')}-${slug(level.name)}.json`
    writeFileSync(join(target, name), levelToJson(level))
  }
  console.log(
    `Converted ${files.length} levels into ${target} (${checked} cross-checked against v1 codes)`,
  )
}

async function convertCode(code: string) {
  const level = decodeV1Code(code)
  process.stdout.write(levelToJson(level))
  console.log(await encodeShareCode(level))
}

const args = process.argv.slice(2)
if (args[0] === '--campaign' && args.length === 3) {
  convertCampaign(args[1]!, args[2]!)
} else if (args.length === 1) {
  await convertCode(args[0]!)
} else {
  console.error('Usage: convert-v1.ts --campaign <old dir> <new dir> | convert-v1.ts <v1 code>')
  process.exit(1)
}

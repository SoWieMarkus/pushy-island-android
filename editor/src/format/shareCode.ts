import { LevelFormatError, levelFromJson, levelToJson, type LevelData } from './level.ts'

/** `PUSHY2:` + base64url(raw-deflate(compact JSON)) – same scheme as the Kotlin `ShareCode`. */
export const SHARE_CODE_PREFIX = 'PUSHY2:'

async function pipe(bytes: Uint8Array, transform: CompressionStream | DecompressionStream) {
  const stream = new Blob([bytes as BlobPart]).stream().pipeThrough(transform)
  return new Uint8Array(await new Response(stream).arrayBuffer())
}

function toBase64Url(bytes: Uint8Array): string {
  let binary = ''
  for (const byte of bytes) binary += String.fromCharCode(byte)
  return btoa(binary).replaceAll('+', '-').replaceAll('/', '_').replace(/=+$/, '')
}

function fromBase64Url(text: string): Uint8Array {
  if (!/^[A-Za-z0-9_-]*$/.test(text))
    throw new LevelFormatError('Share code is not valid base64url')
  const padded =
    text.replaceAll('-', '+').replaceAll('_', '/') + '='.repeat((4 - (text.length % 4)) % 4)
  const binary = atob(padded)
  return Uint8Array.from(binary, (char) => char.charCodeAt(0))
}

export async function encodeShareCode(level: LevelData): Promise<string> {
  const json = new TextEncoder().encode(levelToJson(level, false))
  return SHARE_CODE_PREFIX + toBase64Url(await pipe(json, new CompressionStream('deflate-raw')))
}

export function isShareCode(text: string): boolean {
  return text.trim().toUpperCase().startsWith(SHARE_CODE_PREFIX)
}

export async function decodeShareCode(code: string): Promise<LevelData> {
  const compact = code.replace(/\s+/g, '')
  if (!compact.toUpperCase().startsWith(SHARE_CODE_PREFIX)) {
    throw new LevelFormatError(`Share codes start with ${SHARE_CODE_PREFIX}`)
  }
  let json: string
  try {
    const bytes = await pipe(
      fromBase64Url(compact.slice(SHARE_CODE_PREFIX.length)),
      new DecompressionStream('deflate-raw'),
    )
    json = new TextDecoder().decode(bytes)
  } catch (error) {
    if (error instanceof LevelFormatError) throw error
    throw new LevelFormatError('Share code is corrupted')
  }
  return levelFromJson(json)
}

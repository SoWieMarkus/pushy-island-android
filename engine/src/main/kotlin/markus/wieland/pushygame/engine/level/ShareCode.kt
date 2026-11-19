package markus.wieland.pushygame.engine.level

import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * Compact text form of a level for sharing: `PUSHY2:` + base64url(raw-deflate(compact level JSON)).
 * The web level editor implements the same scheme with `CompressionStream("deflate-raw")`.
 */
object ShareCode {
    const val PREFIX = "PUSHY2:"
    private const val MAX_INFLATED_BYTES = 256 * 1024

    fun encode(level: LevelData): String {
        val json = LevelFormat.toJson(level, pretty = false).toByteArray(Charsets.UTF_8)
        val deflater = Deflater(Deflater.BEST_COMPRESSION, true)
        deflater.setInput(json)
        deflater.finish()
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        while (!deflater.finished()) out.write(buffer, 0, deflater.deflate(buffer))
        deflater.end()
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(out.toByteArray())
    }

    fun decode(code: String): LevelData {
        val compact = code.filterNot { it.isWhitespace() }
        if (!compact.startsWith(PREFIX, ignoreCase = true)) {
            throw LevelFormatException("Share codes start with $PREFIX")
        }
        val bytes = try {
            Base64.getUrlDecoder().decode(compact.substring(PREFIX.length))
        } catch (e: IllegalArgumentException) {
            throw LevelFormatException("Share code is not valid base64url", e)
        }
        val inflater = Inflater(true)
        inflater.setInput(bytes)
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        try {
            while (!inflater.finished()) {
                val n = inflater.inflate(buffer)
                if (n == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
                    throw LevelFormatException("Share code is truncated")
                }
                out.write(buffer, 0, n)
                if (out.size() > MAX_INFLATED_BYTES) throw LevelFormatException("Share code is too large")
            }
        } catch (e: DataFormatException) {
            throw LevelFormatException("Share code is corrupted", e)
        } finally {
            inflater.end()
        }
        return LevelFormat.fromJson(out.toString(Charsets.UTF_8))
    }
}

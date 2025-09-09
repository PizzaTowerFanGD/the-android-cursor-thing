package com.example.cursoroverlay

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Minimal .cur/.ico parser that supports PNG-backed images only.
 * It reads the first image entry and returns a Bitmap.
 * Limitations: does not support XOR/MASK bitmap cursors, animated .ani, or ico icon directories with multiple sizes.
 */
object CursorParser {

    fun parseCUR(input: InputStream): Bitmap? {
        // ICO/CUR header: 2 bytes reserved, 2 bytes type (1=icon,2=cursor), 2 bytes count
        val header = ByteArray(6)
        if (input.read(header) != 6) return null

        val count = (header[4].toInt() and 0xFF) or ((header[5].toInt() and 0xFF) shl 8)
        if (count < 1) return null

        // Each directory entry is 16 bytes
        val entry = ByteArray(16)
        if (input.read(entry) != 16) return null

        // bytes 8-11 of entry = size (little endian), 12-15 = offset
        val size = (entry[8].toInt() and 0xFF) or ((entry[9].toInt() and 0xFF) shl 8) or
                ((entry[10].toInt() and 0xFF) shl 16) or ((entry[11].toInt() and 0xFF) shl 24)

        val offset = (entry[12].toInt() and 0xFF) or ((entry[13].toInt() and 0xFF) shl 8) or
                ((entry[14].toInt() and 0xFF) shl 16) or ((entry[15].toInt() and 0xFF) shl 24)

        if (size <= 0) return null

        // Seek to offset: InputStream may not support skip reliably, so read until offset
        var toSkip = offset - 6 - 16 // we've already consumed header + first entry
        while (toSkip > 0) {
            val skipped = input.skip(toSkip.toLong())
            if (skipped <= 0) break
            toSkip -= skipped.toInt()
        }

        val data = ByteArrayOutputStream()
        val buf = ByteArray(4096)
        var remaining = size
        while (remaining > 0) {
            val r = input.read(buf, 0, minOf(buf.size, remaining))
            if (r <= 0) break
            data.write(buf, 0, r)
            remaining -= r
        }

        val bytes = data.toByteArray()

        // The image data may be PNG or BMP. Try decode as PNG first.
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        return bmp
    }
}

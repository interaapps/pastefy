package de.interaapps.pastefy.service

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PasteThumbnailServiceTest {
    @Test
    fun `renders png for xml-sensitive paste content`() {
        val png = PasteThumbnailService().render("<title>", "first & second\n<xml>")

        assertTrue(png.size > PNG_SIGNATURE.size)
        assertArrayEquals(PNG_SIGNATURE, png.copyOf(PNG_SIGNATURE.size))
    }

    companion object {
        private val PNG_SIGNATURE = byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        )
    }
}

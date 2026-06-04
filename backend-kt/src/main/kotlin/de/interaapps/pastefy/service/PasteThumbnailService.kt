package de.interaapps.pastefy.service

import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.web.util.HtmlUtils
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

@Service
class PasteThumbnailService {
    private val template = ClassPathResource("templates/thumbnail-svg.svg").inputStream.bufferedReader().use { it.readText() }

    init {
        registerFont("fonts/PlusJakartaSans.ttf")
        registerFont("fonts/JetBrainsMono.ttf")
    }

    fun render(title: String?, content: String): ByteArray {
        var svg = template.replace("%title%", escape(title.orEmpty()))
        val lines = content.lines()
        repeat(5) { index ->
            svg = svg.replace("%content_${index + 1}%", escape(lines.getOrElse(index) { "" }))
        }
        return ByteArrayOutputStream().use { output ->
            PNGTranscoder().transcode(
                TranscoderInput(ByteArrayInputStream(svg.toByteArray(StandardCharsets.UTF_8))),
                TranscoderOutput(output),
            )
            output.toByteArray()
        }
    }

    private fun escape(value: String) = HtmlUtils.htmlEscape(value)

    private fun registerFont(path: String) {
        ClassPathResource(path).inputStream.use {
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(Font.createFont(Font.TRUETYPE_FONT, it))
        }
    }
}

package de.interaapps.pastefy.service

import de.interaapps.pastefy.config.PastefyProperties
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SeoRendererTest {
    @Test
    fun `injects escaped metadata and canonical url`() {
        val renderer = SeoRenderer(
            PastefyProperties(metaTagsEnabled = true, serverName = "https://pastefy.example/"),
        )

        val html = renderer.render(
            renderer.page("/tags/kotlin", "Kotlin & JVM | Pastefy", "Public <pastes>")
                .content("<main>content</main>"),
        )

        assertNotNull(html)
        assertTrue(html!!.contains("<title>Kotlin &amp; JVM | Pastefy</title>"))
        assertTrue(html.contains("<link rel=\"canonical\" href=\"https://pastefy.example/tags/kotlin\" />"))
        assertTrue(html.contains("content=\"Public &lt;pastes&gt;\""))
        assertTrue(html.contains("<main>content</main>"))
    }
}

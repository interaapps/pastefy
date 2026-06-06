package de.interaapps.pastefy.util

fun String?.shorten(): String? {
    val lineSequence = this?.lineSequence()

    if (lineSequence?.count() == 1) {
        return if (this!!.length > 303) this.take(300) + "..." else this
    }

    return lineSequence?.take(8)?.joinToString("\n") { line ->
        if (line.length > 70) line.take(67) + "..." else line
    }
        ?: return this
}
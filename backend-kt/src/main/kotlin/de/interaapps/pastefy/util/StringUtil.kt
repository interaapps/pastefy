package de.interaapps.pastefy.util

fun String?.shorten(): String? {
    return this?.lineSequence()?.take(7)?.joinToString("\n") { line ->
        if (line.length > 40) line.take(37) + "..." else line
    }
        ?: return this
}
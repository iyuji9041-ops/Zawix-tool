package com.example.domain.text

import java.text.BreakIterator
import java.util.Locale

data class TextStatistics(
    val characterCount: Int,
    val characterCountNoSpaces: Int,
    val wordCount: Int,
    val lineCount: Int,
    val sentenceCount: Int
)

object TextToolkitEngine {

    fun analyze(text: String): TextStatistics {
        if (text.isEmpty()) {
            return TextStatistics(0, 0, 0, 0, 0)
        }

        val totalChars = text.length
        val charsNoSpaces = text.filter { !it.isWhitespace() }.length

        // Words using BreakIterator for accurate Unicode / non-Latin boundary support
        val wordIterator = BreakIterator.getWordInstance(Locale.getDefault())
        wordIterator.setText(text)
        var words = 0
        var start = wordIterator.first()
        var end = wordIterator.next()
        while (end != BreakIterator.DONE) {
            val token = text.substring(start, end).trim()
            if (token.isNotEmpty() && token.any { Character.isLetterOrDigit(it) }) {
                words++
            }
            start = end
            end = wordIterator.next()
        }

        // Lines: handles \r\n, \r, \n
        val lines = text.lines().size

        // Sentences
        val sentenceIterator = BreakIterator.getSentenceInstance(Locale.getDefault())
        sentenceIterator.setText(text)
        var sentences = 0
        var sStart = sentenceIterator.first()
        var sEnd = sentenceIterator.next()
        while (sEnd != BreakIterator.DONE) {
            val sentence = text.substring(sStart, sEnd).trim()
            if (sentence.isNotEmpty() && sentence.any { Character.isLetterOrDigit(it) }) {
                sentences++
            }
            sStart = sEnd
            sEnd = sentenceIterator.next()
        }

        return TextStatistics(
            characterCount = totalChars,
            characterCountNoSpaces = charsNoSpaces,
            wordCount = words,
            lineCount = lines,
            sentenceCount = sentences
        )
    }

    fun toUpperCase(text: String): String = text.uppercase(Locale.getDefault())

    fun toLowerCase(text: String): String = text.lowercase(Locale.getDefault())

    fun toTitleCase(text: String): String {
        if (text.isEmpty()) return ""
        val boundary = BreakIterator.getWordInstance(Locale.getDefault())
        boundary.setText(text)
        val sb = java.lang.StringBuilder()
        var start = boundary.first()
        var end = boundary.next()
        while (end != BreakIterator.DONE) {
            val word = text.substring(start, end)
            if (word.isNotEmpty() && Character.isLetter(word[0])) {
                sb.append(word.replaceFirstChar { it.titlecase(Locale.getDefault()) })
            } else {
                sb.append(word)
            }
            start = end
            end = boundary.next()
        }
        return sb.toString()
    }

    fun removeExtraSpaces(text: String): String {
        // Collapses consecutive whitespace on each line and trims edges, preserving line breaks
        return text.lines().joinToString("\n") { line ->
            line.trim().replace(Regex("\\s+"), " ")
        }
    }

    fun removeDuplicateLines(text: String): String {
        val seen = linkedSetOf<String>()
        return text.lines().filter { seen.add(it) }.joinToString("\n")
    }

    fun sortLines(text: String, ascending: Boolean = true): String {
        val lines = text.lines()
        val sorted = if (ascending) lines.sorted() else lines.sortedDescending()
        return sorted.joinToString("\n")
    }

    fun reverseText(text: String): String {
        // Reverse characters preserving code points / surrogate pairs correctly
        val codePoints = mutableListOf<Int>()
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            codePoints.add(cp)
            i += Character.charCount(cp)
        }
        codePoints.reverse()
        val sb = java.lang.StringBuilder()
        for (cp in codePoints) {
            sb.appendCodePoint(cp)
        }
        return sb.toString()
    }
}

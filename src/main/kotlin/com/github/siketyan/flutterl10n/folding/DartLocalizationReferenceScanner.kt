package com.github.siketyan.flutterl10n.folding

import com.intellij.openapi.util.TextRange

object DartLocalizationReferenceScanner {
    private val localizationReferenceRegex =
        Regex("""\b([A-Za-z_][A-Za-z0-9_]*)\.of\s*\([^)]*\)\s*[!?]?\s*\.\s*([A-Za-z_][A-Za-z0-9_]*)\b""")

    fun scan(text: String): List<DartLocalizationReference> {
        val codeOffsets = DartCodeOffsets.from(text)

        return localizationReferenceRegex.findAll(text)
            .filter { match -> codeOffsets.contains(match.range.first) }
            .mapNotNull { match ->
                val className = match.groups[1]?.value ?: return@mapNotNull null
                val key = match.groups[2]?.value ?: return@mapNotNull null
                DartLocalizationReference(
                    range = TextRange(match.range.first, match.range.last + 1),
                    localizationClassName = className,
                    key = key,
                )
            }
            .toList()
    }
}

data class DartLocalizationReference(
    val range: TextRange,
    val localizationClassName: String,
    val key: String,
)

private class DartCodeOffsets(private val codeOffsets: BooleanArray) {
    fun contains(offset: Int): Boolean = codeOffsets.getOrNull(offset) == true

    companion object {
        fun from(text: String): DartCodeOffsets {
            val offsets = BooleanArray(text.length) { true }
            var index = 0
            var state = State.CODE

            while (index < text.length) {
                when (state) {
                    State.CODE -> {
                        when {
                            text.startsWith("//", index) -> {
                                offsets[index] = false
                                offsets[index + 1] = false
                                index += 2
                                state = State.LINE_COMMENT
                            }
                            text.startsWith("/*", index) -> {
                                offsets[index] = false
                                offsets[index + 1] = false
                                index += 2
                                state = State.BLOCK_COMMENT
                            }
                            text[index] == '\'' || text[index] == '"' -> {
                                offsets[index] = false
                                state = if (text[index] == '\'') State.SINGLE_QUOTED_STRING else State.DOUBLE_QUOTED_STRING
                                index++
                            }
                            else -> index++
                        }
                    }
                    State.LINE_COMMENT -> {
                        offsets[index] = false
                        if (text[index] == '\n') {
                            state = State.CODE
                        }
                        index++
                    }
                    State.BLOCK_COMMENT -> {
                        offsets[index] = false
                        if (text.startsWith("*/", index)) {
                            offsets[index + 1] = false
                            index += 2
                            state = State.CODE
                        } else {
                            index++
                        }
                    }
                    State.SINGLE_QUOTED_STRING -> {
                        offsets[index] = false
                        val escaped = text[index] == '\\' && index + 1 < text.length
                        if (escaped) {
                            offsets[index + 1] = false
                            index += 2
                        } else {
                            if (text[index] == '\'') {
                                state = State.CODE
                            }
                            index++
                        }
                    }
                    State.DOUBLE_QUOTED_STRING -> {
                        offsets[index] = false
                        val escaped = text[index] == '\\' && index + 1 < text.length
                        if (escaped) {
                            offsets[index + 1] = false
                            index += 2
                        } else {
                            if (text[index] == '"') {
                                state = State.CODE
                            }
                            index++
                        }
                    }
                }
            }

            return DartCodeOffsets(offsets)
        }
    }

    private enum class State {
        CODE,
        LINE_COMMENT,
        BLOCK_COMMENT,
        SINGLE_QUOTED_STRING,
        DOUBLE_QUOTED_STRING,
    }
}

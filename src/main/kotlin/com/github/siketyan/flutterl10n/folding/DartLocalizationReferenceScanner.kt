package com.github.siketyan.flutterl10n.folding

import com.intellij.openapi.util.TextRange

object DartLocalizationReferenceScanner {
    private val localizationReferenceRegex =
        Regex("""\b([A-Za-z_][A-Za-z0-9_]*)\.of\s*\([^)]*\)\s*[!?]?\s*\.\s*([A-Za-z_][A-Za-z0-9_]*)\b""")

    fun scan(text: String): List<DartLocalizationReference> {
        return localizationReferenceRegex.findAll(text)
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

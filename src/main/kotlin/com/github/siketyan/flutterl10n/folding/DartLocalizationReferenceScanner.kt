package com.github.siketyan.flutterl10n.folding

import com.jetbrains.lang.dart.psi.DartExpression
import com.jetbrains.lang.dart.psi.DartFile
import com.jetbrains.lang.dart.psi.DartRecursiveVisitor
import com.intellij.openapi.util.TextRange

object DartLocalizationReferenceScanner {
    private val localizationReferenceRegex =
        Regex("""^([A-Za-z_][A-Za-z0-9_]*)\.of\s*\([^)]*\)\s*[!?]?\s*\.\s*([A-Za-z_][A-Za-z0-9_]*)$""")

    fun scan(file: DartFile): List<DartLocalizationReference> {
        val references = linkedMapOf<TextRange, DartLocalizationReference>()

        file.accept(object : DartRecursiveVisitor() {
            override fun visitExpression(o: DartExpression) {
                collectReference(o)?.let { reference ->
                    references[reference.range] = reference
                }
                super.visitExpression(o)
            }
        })

        return references.values.toList()
    }

    private fun collectReference(expression: DartExpression): DartLocalizationReference? {
        val match = localizationReferenceRegex.matchEntire(expression.text) ?: return null
        val className = match.groups[1]?.value ?: return null
        val key = match.groups[2]?.value ?: return null

        return DartLocalizationReference(
            range = expression.textRange,
            localizationClassName = className,
            key = key,
        )
    }
}

data class DartLocalizationReference(
    val range: TextRange,
    val localizationClassName: String,
    val key: String,
)

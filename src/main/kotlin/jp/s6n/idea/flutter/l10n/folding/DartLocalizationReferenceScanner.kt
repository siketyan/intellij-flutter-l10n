package jp.s6n.idea.flutter.l10n.folding

import com.jetbrains.lang.dart.psi.DartCallExpression
import com.jetbrains.lang.dart.psi.DartExpression
import com.jetbrains.lang.dart.psi.DartFile
import com.jetbrains.lang.dart.psi.DartId
import com.jetbrains.lang.dart.psi.DartRecursiveVisitor
import com.jetbrains.lang.dart.psi.DartReferenceExpression
import com.jetbrains.lang.dart.psi.DartVarDeclarationList
import com.intellij.openapi.util.TextRange

object DartLocalizationReferenceScanner {
    fun scan(file: DartFile): List<DartLocalizationReference> {
        val localizationVariables = collectLocalizationVariables(file)
        val references = linkedMapOf<TextRange, DartLocalizationReference>()

        file.accept(object : DartRecursiveVisitor() {
            override fun visitReferenceExpression(o: DartReferenceExpression) {
                collectReference(o, localizationVariables)?.let { reference ->
                    references[reference.range] = reference
                }
                super.visitReferenceExpression(o)
            }
        })

        return references.values.toList()
    }

    private fun collectLocalizationVariables(file: DartFile): Map<String, String> {
        val variables = mutableMapOf<String, String>()

        file.accept(object : DartRecursiveVisitor() {
            override fun visitVarDeclarationList(o: DartVarDeclarationList) {
                collectVariable(
                    name = o.varAccessDeclaration.componentName.name,
                    initializer = o.varInit?.expression,
                    variables = variables,
                )
                o.varDeclarationListPartList.forEach { part ->
                    collectVariable(
                        name = part.componentName.name,
                        initializer = part.varInit?.expression,
                        variables = variables,
                    )
                }
                super.visitVarDeclarationList(o)
            }
        })

        return variables
    }

    private fun collectVariable(name: String?, initializer: DartExpression?, variables: MutableMap<String, String>) {
        val className = localizationOfCallClass(initializer) ?: return
        if (name != null) {
            variables[name] = className
        }
    }

    private fun collectReference(
        expression: DartReferenceExpression,
        localizationVariables: Map<String, String>,
    ): DartLocalizationReference? {
        val parentCall = expression.parent as? DartCallExpression
        if (parentCall?.expression == expression) {
            return null
        }

        val parts = expression.expressionChildren()
        if (parts.size < 2) {
            return null
        }

        val receiver = parts.first()
        val key = (parts.last() as? DartReferenceExpression)?.simpleName() ?: return null
        val className = localizationOfCallClass(receiver)
            ?: localizationVariables[(receiver as? DartReferenceExpression)?.simpleName()]
            ?: return null

        return DartLocalizationReference(
            range = expression.textRange,
            keyRange = parts.last().textRange,
            localizationClassName = className,
            key = key,
        )
    }

    private fun localizationOfCallClass(expression: DartExpression?): String? {
        return when (expression) {
            is DartCallExpression -> localizationOfCallClass(expression.expression)
            is DartReferenceExpression -> {
                val parts = expression.expressionChildren()
                if (parts.size == 2 && (parts.last() as? DartReferenceExpression)?.simpleName() == LOCALIZATION_OF_METHOD) {
                    (parts.first() as? DartReferenceExpression)?.simpleName()
                } else if (parts.size == 1) {
                    localizationOfCallClass(parts.single())
                } else {
                    null
                }
            }
            else -> expression
                ?.expressionChildren()
                ?.singleOrNull()
                ?.let(::localizationOfCallClass)
        }
    }

    private fun DartExpression.expressionChildren(): List<DartExpression> {
        return children.filterIsInstance<DartExpression>()
    }

    private fun DartReferenceExpression.simpleName(): String? {
        return children.filterIsInstance<DartId>().singleOrNull()?.text
    }

    private const val LOCALIZATION_OF_METHOD = "of"
}

data class DartLocalizationReference(
    val range: TextRange,
    val keyRange: TextRange,
    val localizationClassName: String,
    val key: String,
)

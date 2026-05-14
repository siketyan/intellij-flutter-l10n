package com.github.siketyan.flutterl10n.folding

import com.github.siketyan.flutterl10n.l10n.ArbTranslationService
import com.jetbrains.lang.dart.psi.DartFile
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement

class FlutterL10nFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val dartFile = root.containingFile as? DartFile ?: return FoldingDescriptor.EMPTY_ARRAY
        val virtualFile = dartFile.virtualFile
        val translationService = root.project.service<ArbTranslationService>()

        return DartLocalizationReferenceScanner.scan(dartFile)
            .mapNotNull { reference ->
                val translation = translationService.lookup(
                    contextFile = virtualFile,
                    localizationClassName = reference.localizationClassName,
                    key = reference.key,
                ) ?: return@mapNotNull null

                FoldingDescriptor(
                    root.node,
                    reference.range,
                    null,
                    quotePlaceholder(translation),
                )
            }
            .toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? = null

    override fun isCollapsedByDefault(node: ASTNode): Boolean = true

    private fun quotePlaceholder(value: String): String {
        val escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
        val truncated = if (escaped.length > MAX_PLACEHOLDER_LENGTH) {
            escaped.take(MAX_PLACEHOLDER_LENGTH - 1) + ELLIPSIS
        } else {
            escaped
        }

        return "\"$truncated\""
    }

    companion object {
        private const val MAX_PLACEHOLDER_LENGTH = 80
        private const val ELLIPSIS = "..."
    }
}

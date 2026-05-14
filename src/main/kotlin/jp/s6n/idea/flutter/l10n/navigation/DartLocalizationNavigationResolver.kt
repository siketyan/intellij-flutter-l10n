package jp.s6n.idea.flutter.l10n.navigation

import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.jetbrains.lang.dart.psi.DartFile
import jp.s6n.idea.flutter.l10n.ArbDefinitionService
import jp.s6n.idea.flutter.l10n.folding.DartLocalizationReferenceScanner

object DartLocalizationNavigationResolver {
    fun resolve(sourceElement: PsiElement?, offset: Int): PsiElement? {
        val dartFile = sourceElement?.containingFile as? DartFile ?: return null
        val reference = DartLocalizationReferenceScanner.scan(dartFile)
            .firstOrNull { it.keyRange.containsOffset(offset) }
            ?: return null

        return sourceElement.project.service<ArbDefinitionService>().findDefinition(
            contextFile = dartFile.virtualFile,
            localizationClassName = reference.localizationClassName,
            key = reference.key,
        )
    }
}

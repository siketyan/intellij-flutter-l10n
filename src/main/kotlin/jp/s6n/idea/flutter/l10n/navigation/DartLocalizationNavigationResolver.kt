package jp.s6n.idea.flutter.l10n.navigation

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.DartFile
import jp.s6n.idea.flutter.l10n.ArbDefinitionService
import jp.s6n.idea.flutter.l10n.DartLocalizationUsageService
import jp.s6n.idea.flutter.l10n.folding.DartLocalizationReferenceScanner

object DartLocalizationNavigationResolver {
    fun resolve(sourceElement: PsiElement?, offset: Int): PsiElement? {
        return resolveTargets(sourceElement, offset).firstOrNull()
    }

    fun resolveTargets(sourceElement: PsiElement?, offset: Int): List<PsiElement> {
        return resolveArbTargets(sourceElement, offset)
            ?: resolveDartTargets(sourceElement, offset)
            ?: emptyList()
    }

    private fun resolveDartTargets(sourceElement: PsiElement?, offset: Int): List<PsiElement>? {
        val dartFile = sourceElement?.containingFile as? DartFile ?: return null
        val reference = DartLocalizationReferenceScanner.scan(dartFile)
            .firstOrNull { it.keyRange.containsOffset(offset) }
            ?: return null

        return sourceElement.project.service<ArbDefinitionService>().findDefinitions(
            contextFile = dartFile.virtualFile,
            localizationClassName = reference.localizationClassName,
            key = reference.key,
        )
    }

    private fun resolveArbTargets(sourceElement: PsiElement?, offset: Int): List<PsiElement>? {
        val jsonFile = sourceElement?.containingFile as? JsonFile ?: return null
        if (jsonFile.virtualFile?.extension != ARB_EXTENSION) {
            return null
        }

        val property = PsiTreeUtil.getParentOfType(sourceElement, JsonProperty::class.java, false)
            ?: return null
        val nameElement = property.nameElement
        if (!nameElement.textRange.containsOffset(offset)) {
            return null
        }

        return sourceElement.project.service<DartLocalizationUsageService>().findUsages(
            contextFile = jsonFile.virtualFile,
            key = property.name,
        )
    }

    private const val ARB_EXTENSION = "arb"
}

package jp.s6n.idea.flutter.l10n.navigation

import com.intellij.codeInsight.TargetElementEvaluator
import com.intellij.codeInsight.TargetElementEvaluatorEx2
import com.intellij.lang.Language
import com.intellij.lang.LanguageExtension
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference

class DartLocalizationTargetElementEvaluator : TargetElementEvaluatorEx2() {
    override fun includeSelfInGotoImplementation(element: PsiElement): Boolean {
        if (DartLocalizationNavigationResolver.resolve(element, element.textOffset) != null) {
            return false
        }
        return fallbackEvaluator(element.language)?.includeSelfInGotoImplementation(element)
            ?: super.includeSelfInGotoImplementation(element)
    }

    override fun getTargetCandidates(reference: PsiReference): Collection<PsiElement>? {
        // The Dart plugin suppresses legacy PSI candidates when LSP handles Find Usages.
        // Preserve that behavior while taking precedence for localization implementations.
        val evaluator = fallbackEvaluator(reference.element.language) as? TargetElementEvaluatorEx2
        return evaluator?.getTargetCandidates(reference)
    }

    private fun fallbackEvaluator(language: Language) =
        evaluators.allForLanguage(language)
            .firstOrNull { it !is DartLocalizationTargetElementEvaluator }

    companion object {
        internal val evaluators = LanguageExtension<TargetElementEvaluator>("com.intellij.targetElementEvaluator")
    }
}

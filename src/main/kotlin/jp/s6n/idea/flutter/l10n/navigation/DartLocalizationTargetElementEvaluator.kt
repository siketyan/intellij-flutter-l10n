package jp.s6n.idea.flutter.l10n.navigation

import com.intellij.codeInsight.TargetElementEvaluatorEx2
import com.intellij.psi.PsiElement

class DartLocalizationTargetElementEvaluator : TargetElementEvaluatorEx2() {
    override fun includeSelfInGotoImplementation(element: PsiElement): Boolean {
        return DartLocalizationNavigationResolver.resolve(element, element.textOffset) == null &&
            super.includeSelfInGotoImplementation(element)
    }
}

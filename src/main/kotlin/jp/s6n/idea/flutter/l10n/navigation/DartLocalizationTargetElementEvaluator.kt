package jp.s6n.idea.flutter.l10n.navigation

import com.intellij.codeInsight.TargetElementEvaluatorEx2
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement

class DartLocalizationTargetElementEvaluator : TargetElementEvaluatorEx2() {
    override fun adjustElement(
        editor: Editor,
        flags: Int,
        refElement: PsiElement?,
        namedElement: PsiElement?,
    ): PsiElement? {
        return DartLocalizationNavigationResolver.resolve(refElement, editor.caretModel.offset)
            ?: super.adjustElement(editor, flags, refElement, namedElement)
    }

    override fun adjustTargetElement(editor: Editor, flags: Int, offset: Int, targetElement: PsiElement): PsiElement? {
        return DartLocalizationNavigationResolver.resolve(targetElement, offset)
            ?: super.adjustTargetElement(editor, flags, offset, targetElement)
    }

    override fun getGotoDeclarationTarget(element: PsiElement, navElement: PsiElement?): PsiElement? {
        return DartLocalizationNavigationResolver.resolve(element, element.textOffset)
            ?: super.getGotoDeclarationTarget(element, navElement)
    }

    override fun includeSelfInGotoImplementation(element: PsiElement): Boolean {
        return DartLocalizationNavigationResolver.resolve(element, element.textOffset) == null &&
            super.includeSelfInGotoImplementation(element)
    }
}

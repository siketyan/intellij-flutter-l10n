package jp.s6n.idea.flutter.l10n.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement

class DartLocalizationGotoDeclarationHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor): Array<PsiElement>? {
        val target = DartLocalizationNavigationResolver.resolve(sourceElement, offset) ?: return null
        return arrayOf(target)
    }
}

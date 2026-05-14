package jp.s6n.idea.flutter.l10n.navigation

import com.intellij.navigation.DirectNavigationProvider
import com.intellij.psi.PsiElement

class DartLocalizationDirectNavigationProvider : DirectNavigationProvider {
    override fun getNavigationElement(element: PsiElement): PsiElement? {
        return DartLocalizationNavigationResolver.resolve(element, element.textOffset)
    }
}

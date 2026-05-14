package jp.s6n.idea.flutter.l10n.navigation

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.util.TextRange
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.FakePsiElement
import javax.swing.Icon

class ArbTranslationNavigationElement(
    private val target: PsiElement,
    private val translation: String,
) : FakePsiElement() {
    override fun getName(): String {
        return translation
    }

    override fun getText(): String? {
        return target.text
    }

    override fun getTextRange(): TextRange? {
        return target.textRange
    }

    override fun getTextOffset(): Int {
        return target.textOffset
    }

    override fun getContainingFile(): PsiFile {
        return target.containingFile
    }

    override fun getNavigationElement(): PsiElement {
        return target
    }

    override fun getParent(): PsiElement {
        return target.parent
    }

    override fun getManager(): PsiManager {
        return target.manager
    }

    override fun isValid(): Boolean {
        return target.isValid
    }

    override fun navigate(requestFocus: Boolean) {
        (target as? Navigatable)?.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean {
        return (target as? Navigatable)?.canNavigate() == true
    }

    override fun canNavigateToSource(): Boolean {
        return (target as? Navigatable)?.canNavigateToSource() == true
    }

    override fun getPresentation(): ItemPresentation {
        return object : ItemPresentation {
            override fun getPresentableText(): String {
                return translation
            }

            override fun getLocationString(): String? {
                return target.containingFile.virtualFile?.name
            }

            override fun getIcon(unused: Boolean): Icon? {
                return target.getIcon(0)
            }
        }
    }
}

package jp.s6n.idea.flutter.l10n

import com.intellij.json.JsonFileType
import com.intellij.json.JsonLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class ArbFileType private constructor() : LanguageFileType(JsonLanguage.INSTANCE) {
    override fun getName(): String = "ARB"

    override fun getDescription(): String = "Application Resource Bundle"

    override fun getDefaultExtension(): String = "arb"

    override fun getIcon(): Icon? = JsonFileType.INSTANCE.icon

    companion object {
        @JvmField
        val INSTANCE = ArbFileType()
    }
}

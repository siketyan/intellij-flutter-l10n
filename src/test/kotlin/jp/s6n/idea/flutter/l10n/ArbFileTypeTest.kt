package jp.s6n.idea.flutter.l10n

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ArbFileTypeTest : BasePlatformTestCase() {
    fun testArbFilesAreRecognizedAsJson() {
        val fileType = FileTypeManager.getInstance().getFileTypeByExtension("arb")

        assertEquals("ARB", fileType.name)
        assertEquals("JSON", (fileType as LanguageFileType).language.id)
    }
}

package jp.s6n.idea.flutter.l10n

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.lang.dart.DartFileType
import com.intellij.codeInsight.navigation.GotoImplementationHandler
import jp.s6n.idea.flutter.l10n.navigation.DartLocalizationDirectNavigationProvider
import jp.s6n.idea.flutter.l10n.navigation.DartLocalizationGotoDeclarationHandler

class DartLocalizationNavigationTest : BasePlatformTestCase() {
    fun testGoToDeclarationTargetsArbProperty() {
        addDefaultProjectFiles()
        val file = configureDartFile(
            """
                void build(context) {
                  final text = L10nClass.of(context).fooBar;
                }
            """.trimIndent(),
        )
        val offset = file.text.indexOf("fooBar")
        val sourceElement = file.findElementAt(offset)

        val targets = DartLocalizationGotoDeclarationHandler()
            .getGotoDeclarationTargets(sourceElement, offset, myFixture.editor)

        assertNotNull(targets)
        assertEquals(1, targets!!.size)
        assertEquals("\"fooBar\"", targets.single().text)
        assertEquals("app_en.arb", targets.single().containingFile.virtualFile.name)
    }

    fun testGoToDeclarationTargetsArbPropertyForLocalizationVariable() {
        addDefaultProjectFiles()
        val file = configureDartFile(
            """
                void build(context) {
                  final l10n = L10nClass.of(context);
                  final text = l10n.fooBar;
                }
            """.trimIndent(),
        )
        val offset = file.text.lastIndexOf("fooBar")
        val sourceElement = file.findElementAt(offset)

        val targets = DartLocalizationGotoDeclarationHandler()
            .getGotoDeclarationTargets(sourceElement, offset, myFixture.editor)

        assertNotNull(targets)
        assertEquals("\"fooBar\"", targets!!.single().text)
        assertEquals("app_en.arb", targets.single().containingFile.virtualFile.name)
    }

    fun testDirectNavigationTargetsArbProperty() {
        addDefaultProjectFiles()
        val file = configureDartFile(
            """
                void build(context) {
                  final text = L10nClass.of(context).fooBar;
                }
            """.trimIndent(),
        )
        val sourceElement = file.findElementAt(file.text.indexOf("fooBar"))

        val target = DartLocalizationDirectNavigationProvider().getNavigationElement(sourceElement!!)

        assertNotNull(target)
        assertEquals("\"fooBar\"", target!!.text)
        assertEquals("app_en.arb", target.containingFile.virtualFile.name)
    }

    fun testGoToImplementationTargetsArbProperty() {
        addDefaultProjectFiles()
        val file = configureDartFile(
            """
                void build(context) {
                  final text = L10nClass.of(context).fooBar;
                }
            """.trimIndent(),
        )
        val sourceElement = file.findElementAt(file.text.indexOf("fooBar"))

        val data = GotoImplementationHandler().createDataForSourceForTests(myFixture.editor, sourceElement)

        assertNotNull(data)
        assertEquals("\"fooBar\"", data.targets.single().text)
        assertEquals("app_en.arb", data.targets.single().containingFile.virtualFile.name)
    }

    private fun addDefaultProjectFiles() {
        myFixture.addFileToProject("pubspec.yaml", "name: test_app\n")
        myFixture.addFileToProject(
            "l10n.yaml",
            """
                arb-dir: lib/l10n
                template-arb-file: app_en.arb
                output-class: L10nClass
            """.trimIndent(),
        )
        myFixture.addFileToProject(
            "lib/l10n/app_en.arb",
            """
                {
                  "fooBar": "Hello",
                  "bazQux": "Goodbye"
                }
            """.trimIndent(),
        )
    }

    private fun configureDartFile(text: String) = myFixture.configureByText(DartFileType.INSTANCE, text.trimIndent())
}

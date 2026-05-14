package jp.s6n.idea.flutter.l10n

import com.intellij.codeInsight.navigation.GotoImplementationHandler
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.DefinitionsScopedSearch
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.lang.dart.DartFileType
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
        assertEquals(listOf("app_en.arb", "app_ja.arb"), targets!!.map { it.containingFile.virtualFile.name })
        assertEquals(listOf("\"fooBar\"", "\"fooBar\""), targets.map { it.text })
        assertEquals(listOf("Hello", "こんにちは"), targets.map { it.presentationText() })
        assertEquals(listOf("app_en.arb", "app_ja.arb"), targets.map { it.presentationLocation() })
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
        assertEquals(listOf("app_en.arb", "app_ja.arb"), targets!!.map { it.containingFile.virtualFile.name })
        assertEquals(listOf("\"fooBar\"", "\"fooBar\""), targets.map { it.text })
        assertEquals(listOf("Hello", "こんにちは"), targets.map { it.presentationText() })
        assertEquals(listOf("app_en.arb", "app_ja.arb"), targets.map { it.presentationLocation() })
    }

    fun testDirectNavigationTargetsArbProperty() {
        addDefaultProjectFiles(includeSecondaryLocale = false)
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
        assertEquals("Hello", target.presentationText())
        assertEquals("app_en.arb", target.presentationLocation())
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
        assertEquals("Hello", data.targets.single().presentationText())
        assertEquals("app_en.arb", data.targets.single().presentationLocation())
    }

    fun testImplementationSearchTargetsAllArbProperties() {
        addDefaultProjectFiles()
        val file = configureDartFile(
            """
                void build(context) {
                  final text = L10nClass.of(context).fooBar;
                }
            """.trimIndent(),
        )
        val sourceElement = file.findElementAt(file.text.indexOf("fooBar"))!!

        val targets = DefinitionsScopedSearch.search(
            sourceElement,
            GlobalSearchScope.projectScope(project),
            true,
        ).findAll()
            .sortedBy { it.containingFile.virtualFile.name }

        assertEquals(listOf("app_en.arb", "app_ja.arb"), targets.map { it.containingFile.virtualFile.name })
        assertEquals(listOf("\"fooBar\"", "\"fooBar\""), targets.map { it.text })
        assertEquals(listOf("Hello", "こんにちは"), targets.map { it.presentationText() })
        assertEquals(listOf("app_en.arb", "app_ja.arb"), targets.map { it.presentationLocation() })
    }

    fun testGoToDeclarationTargetsDartUsageFromArbProperty() {
        addDefaultProjectFiles()
        myFixture.addFileToProject(
            "lib/main.dart",
            """
                void build(context) {
                  final l10n = L10nClass.of(context);
                  final text = l10n.fooBar;
                }
            """.trimIndent(),
        )
        val file = myFixture.configureFromTempProjectFile("lib/l10n/app_ja.arb")
        val offset = file.text.indexOf("fooBar")
        val sourceElement = file.findElementAt(offset)

        val targets = DartLocalizationGotoDeclarationHandler()
            .getGotoDeclarationTargets(sourceElement, offset, myFixture.editor)

        assertNotNull(targets)
        assertEquals(listOf("fooBar"), targets!!.map { it.text })
        assertEquals(listOf("main.dart"), targets.map { it.containingFile.virtualFile.name })
    }

    fun testGoToImplementationTargetsDartUsageFromArbProperty() {
        addDefaultProjectFiles()
        myFixture.addFileToProject(
            "lib/main.dart",
            """
                void build(context) {
                  final text = L10nClass.of(context).fooBar;
                }
            """.trimIndent(),
        )
        val file = myFixture.configureFromTempProjectFile("lib/l10n/app_en.arb")
        val sourceElement = file.findElementAt(file.text.indexOf("fooBar"))

        val data = GotoImplementationHandler().createDataForSourceForTests(myFixture.editor, sourceElement)

        assertNotNull(data)
        assertEquals(listOf("fooBar"), data.targets.map { it.text })
        assertEquals(listOf("main.dart"), data.targets.map { it.containingFile.virtualFile.name })
    }

    private fun addDefaultProjectFiles(includeSecondaryLocale: Boolean = true) {
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
        if (includeSecondaryLocale) {
            myFixture.addFileToProject(
                "lib/l10n/app_ja.arb",
                """
                    {
                      "fooBar": "こんにちは",
                      "bazQux": "さようなら"
                    }
                """.trimIndent(),
            )
        }
    }

    private fun configureDartFile(text: String) = myFixture.configureByText(DartFileType.INSTANCE, text.trimIndent())

    private fun Any.presentationText(): String? {
        return (this as? NavigationItem)?.presentation?.presentableText
    }

    private fun Any.presentationLocation(): String? {
        return (this as? NavigationItem)?.presentation?.locationString
    }
}

package jp.s6n.idea.flutter.l10n

import com.jetbrains.lang.dart.DartFileType
import com.jetbrains.lang.dart.psi.DartFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import jp.s6n.idea.flutter.l10n.folding.DartLocalizationReferenceScanner
import jp.s6n.idea.flutter.l10n.folding.FlutterL10nFoldingBuilder

class FlutterL10nFoldingBuilderTest : BasePlatformTestCase() {
    fun testBuildsFoldingForConfiguredOutputClass() {
        addDefaultProjectFiles(
            l10nYaml = """
                arb-dir: lib/l10n
                template-arb-file: app_en.arb
                output-class: L10nClass
            """.trimIndent(),
            arb = """
                {
                  "@@locale": "en",
                  "fooBar": "Hello",
                  "@fooBar": {
                    "description": "A greeting"
                  }
                }
            """.trimIndent(),
        )

        val file = configureDartFile(
            """
                void build(context) {
                  final text = L10nClass.of(context).fooBar;
                }
            """.trimIndent(),
        )

        val descriptors = FlutterL10nFoldingBuilder().buildFoldRegions(file, myFixture.editor.document, false)

        assertEquals(1, descriptors.size)
        assertEquals("\"Hello\"", descriptors.single().placeholderText)
        assertEquals("L10nClass.of(context).fooBar", descriptors.single().range.substring(file.text))
    }

    fun testBuildsFoldingForNullAssertAndNullAwareAccess() {
        addDefaultProjectFiles(
            l10nYaml = """
                output-class: AppLocalizations
            """.trimIndent(),
            arb = """
                {
                  "fooBar": "Hello",
                  "bazQux": "Goodbye"
                }
            """.trimIndent(),
        )

        val file = configureDartFile(
            """
                void build(context) {
                  final first = AppLocalizations.of(context)!.fooBar;
                  final second = AppLocalizations.of(context)?.bazQux;
                }
            """.trimIndent(),
        )

        val descriptors = FlutterL10nFoldingBuilder().buildFoldRegions(file, myFixture.editor.document, false)

        assertEquals(listOf("\"Hello\"", "\"Goodbye\""), descriptors.map { it.placeholderText })
    }

    fun testDoesNotBuildFoldingForMissingTranslation() {
        addDefaultProjectFiles(
            l10nYaml = "output-class: L10nClass",
            arb = """
                {
                  "@fooBar": {
                    "description": "Metadata only"
                  }
                }
            """.trimIndent(),
        )

        val file = configureDartFile(
            """
                void build(context) {
                  final text = L10nClass.of(context).fooBar;
                }
            """.trimIndent(),
        )

        val descriptors = FlutterL10nFoldingBuilder().buildFoldRegions(file, myFixture.editor.document, false)

        assertEmpty(descriptors.toList())
    }

    fun testUsesDefaultConfigWhenL10nYamlDoesNotExist() {
        myFixture.addFileToProject("pubspec.yaml", "name: test_app\n")
        myFixture.addFileToProject(
            "lib/l10n/app_en.arb",
            """
                {
                  "fooBar": "Hello"
                }
            """.trimIndent(),
        )

        val file = configureDartFile(
            """
                void build(context) {
                  final text = AppLocalizations.of(context).fooBar;
                }
            """.trimIndent(),
        )

        val descriptors = FlutterL10nFoldingBuilder().buildFoldRegions(file, myFixture.editor.document, false)

        assertEquals(1, descriptors.size)
        assertEquals("\"Hello\"", descriptors.single().placeholderText)
    }

    fun testBuildsFoldingForLocalizationVariableAccess() {
        addDefaultProjectFiles(
            l10nYaml = "output-class: L10nClass",
            arb = """
                {
                  "fooBar": "Hello",
                  "bazQux": "Goodbye"
                }
            """.trimIndent(),
        )

        val file = configureDartFile(
            """
                void build(context) {
                  final l10n = L10nClass.of(context);
                  final first = l10n.fooBar;
                  final second = l10n.bazQux;
                  final assertedL10n = L10nClass.of(context)!;
                  final third = assertedL10n.fooBar;
                }
            """.trimIndent(),
        )

        val descriptors = FlutterL10nFoldingBuilder().buildFoldRegions(file, myFixture.editor.document, false)

        assertEquals(listOf("\"Hello\"", "\"Goodbye\"", "\"Hello\""), descriptors.map { it.placeholderText })
        assertEquals(
            listOf("l10n.fooBar", "l10n.bazQux", "assertedL10n.fooBar"),
            descriptors.map { it.range.substring(file.text) },
        )
    }

    fun testScannerFindsWholeReferenceRange() {
        val file = configureDartFile("final text = L10nClass.of(context).fooBar;")

        assertInstanceOf(file, DartFile::class.java)
        val reference = DartLocalizationReferenceScanner.scan(file as DartFile).single()

        assertEquals("L10nClass", reference.localizationClassName)
        assertEquals("fooBar", reference.key)
        assertEquals("L10nClass.of(context).fooBar", reference.range.substring(file.text))
    }

    fun testScannerFindsLocalizationVariableReferences() {
        val file = configureDartFile(
            """
                void build(context) {
                  final l10n = L10nClass.of(context);
                  final text = l10n.fooBar;
                }
            """.trimIndent(),
        )

        val reference = DartLocalizationReferenceScanner.scan(file as DartFile).single()

        assertEquals("L10nClass", reference.localizationClassName)
        assertEquals("fooBar", reference.key)
        assertEquals("l10n.fooBar", reference.range.substring(file.text))
    }

    fun testScannerSkipsCommentsAndStrings() {
        val file = configureDartFile(
            """
            // L10nClass.of(context).commented
            final literal = "L10nClass.of(context).literal";
            final text = L10nClass.of(context).fooBar;
            """.trimIndent(),
        )

        val reference = DartLocalizationReferenceScanner.scan(file as DartFile).single()

        assertEquals("fooBar", reference.key)
    }

    private fun addDefaultProjectFiles(l10nYaml: String, arb: String) {
        myFixture.addFileToProject("pubspec.yaml", "name: test_app\n")
        myFixture.addFileToProject("l10n.yaml", l10nYaml)
        myFixture.addFileToProject("lib/l10n/app_en.arb", arb)
    }

    private fun configureDartFile(text: String): com.intellij.psi.PsiFile {
        return myFixture.configureByText(DartFileType.INSTANCE, text.trimIndent())
    }
}

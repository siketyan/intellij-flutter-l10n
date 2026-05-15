package jp.s6n.idea.flutter.l10n

import org.junit.Assert.assertEquals
import org.junit.Test

class L10nYamlParserTest {
    @Test
    fun `parses preferred-supported-locales as scalar string`() {
        val result = L10nYamlParser.parse(
            """
            arb-dir: lib/l10n
            output-class: AppL10n
            preferred-supported-locales: ja
            template-arb-file: app_en.arb
            """.trimIndent(),
        )

        assertEquals(listOf("ja"), result.preferredSupportedLocales)
    }

    @Test
    fun `parses preferred-supported-locales as list`() {
        val result = L10nYamlParser.parse(
            """
            preferred-supported-locales:
              - en
              - ja
            """.trimIndent(),
        )

        assertEquals(listOf("en", "ja"), result.preferredSupportedLocales)
    }

    @Test
    fun `parses all standard l10n yaml fields`() {
        val result = L10nYamlParser.parse(
            """
            arb-dir: lib/src/l10n
            format: true
            nullable-getter: false
            output-class: BizSetupUiL10n
            output-localization-file: bizsetup_ui_l10n.dart
            preferred-supported-locales: en
            template-arb-file: bizsetup_ui_en.arb
            """.trimIndent(),
        )

        assertEquals("lib/src/l10n", result.arbDir)
        assertEquals("BizSetupUiL10n", result.outputClass)
        assertEquals(listOf("en"), result.preferredSupportedLocales)
        assertEquals("bizsetup_ui_en.arb", result.templateArbFile)
    }
}

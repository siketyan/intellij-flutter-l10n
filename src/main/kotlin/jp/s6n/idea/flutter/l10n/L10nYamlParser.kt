package jp.s6n.idea.flutter.l10n

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar

object L10nYamlParser {
    private val yaml = Yaml.default

    fun parse(text: String): ParsedL10nYaml {
        val root = yaml.parseToYamlNode(text) as? YamlMap ?: return ParsedL10nYaml()
        return ParsedL10nYaml(
            arbDir = root.scalar("arb-dir"),
            templateArbFile = root.scalar("template-arb-file"),
            outputClass = root.scalar("output-class"),
            preferredSupportedLocales = root.stringOrList("preferred-supported-locales"),
        )
    }

    private fun YamlMap.scalar(key: String): String? =
        (node(key) as? YamlScalar)?.content

    private fun YamlMap.stringOrList(key: String): List<String> =
        when (val value = node(key)) {
            is YamlScalar -> listOf(value.content)
            is YamlList -> value.items.filterIsInstance<YamlScalar>().map { it.content }
            else -> emptyList()
        }

    private fun YamlMap.node(key: String): YamlNode? =
        entries.entries.firstOrNull { it.key.content == key }?.value
}

data class ParsedL10nYaml(
    val arbDir: String? = null,
    val templateArbFile: String? = null,
    val outputClass: String? = null,
    val preferredSupportedLocales: List<String> = emptyList(),
)

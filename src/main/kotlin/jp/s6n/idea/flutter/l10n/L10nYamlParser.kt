package jp.s6n.idea.flutter.l10n

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlNamingStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

object L10nYamlParser {
    private val yaml = Yaml(
        configuration = YamlConfiguration(
            strictMode = false,
            yamlNamingStrategy = YamlNamingStrategy.Builtins.KebabCase,
        ),
    )

    fun parse(text: String): ParsedL10nYaml = yaml.decodeFromString(text)
}

@Serializable
data class ParsedL10nYaml(
    val arbDir: String? = null,
    val templateArbFile: String? = null,
    val outputClass: String? = null,
    val preferredSupportedLocales: List<String> = emptyList(),
)

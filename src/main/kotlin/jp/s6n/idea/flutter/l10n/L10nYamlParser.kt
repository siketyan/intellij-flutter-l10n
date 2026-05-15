package jp.s6n.idea.flutter.l10n

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlNamingStrategy
import com.charleskorn.kaml.YamlScalar
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object L10nYamlParser {
    private val yaml = Yaml(
        configuration = YamlConfiguration(
            strictMode = false,
            yamlNamingStrategy = YamlNamingStrategy.Builtins.KebabCase,
        ),
    )

    fun parse(text: String): ParsedL10nYaml = yaml.decodeFromString(text)
}

// Flutter's l10n.yaml accepts preferred-supported-locales as either a scalar string or a list.
private object StringOrListSerializer : KSerializer<List<String>> {
    private val listDelegate = ListSerializer(String.serializer())
    override val descriptor: SerialDescriptor = listDelegate.descriptor

    override fun deserialize(decoder: Decoder): List<String> {
        val yamlInput = decoder as? YamlInput
        return if (yamlInput != null && yamlInput.node is YamlScalar) {
            listOf(decoder.decodeString())
        } else {
            listDelegate.deserialize(decoder)
        }
    }

    override fun serialize(encoder: Encoder, value: List<String>) =
        listDelegate.serialize(encoder, value)
}

@Serializable
data class ParsedL10nYaml(
    val arbDir: String? = null,
    val templateArbFile: String? = null,
    val outputClass: String? = null,
    @Serializable(with = StringOrListSerializer::class)
    val preferredSupportedLocales: List<String> = emptyList(),
)

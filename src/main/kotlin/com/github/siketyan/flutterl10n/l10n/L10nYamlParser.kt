package com.github.siketyan.flutterl10n.l10n

object L10nYamlParser {
    fun parse(text: String): ParsedL10nYaml {
        val values = mutableMapOf<String, String>()
        val lists = mutableMapOf<String, List<String>>()
        val lines = text.lines()
        var index = 0

        while (index < lines.size) {
            val rawLine = lines[index]
            val line = rawLine.substringBefore('#').trim()

            if (line.isEmpty() || !line.contains(':')) {
                index++
                continue
            }

            val key = line.substringBefore(':').trim()
            val value = line.substringAfter(':').trim()

            if (value.isEmpty()) {
                val items = mutableListOf<String>()
                var listIndex = index + 1
                while (listIndex < lines.size) {
                    val listLine = lines[listIndex].substringBefore('#')
                    val trimmed = listLine.trim()
                    if (trimmed.isEmpty()) {
                        listIndex++
                        continue
                    }
                    if (!trimmed.startsWith("-")) {
                        break
                    }
                    items += trimmed.removePrefix("-").trim().unquote()
                    listIndex++
                }
                if (items.isNotEmpty()) {
                    lists[key] = items
                    index = listIndex
                    continue
                }
            } else if (value.startsWith("[") && value.endsWith("]")) {
                lists[key] = value
                    .removePrefix("[")
                    .removeSuffix("]")
                    .split(',')
                    .map { it.trim().unquote() }
                    .filter { it.isNotEmpty() }
            } else {
                values[key] = value.unquote()
            }

            index++
        }

        return ParsedL10nYaml(
            arbDir = values["arb-dir"],
            templateArbFile = values["template-arb-file"],
            outputClass = values["output-class"],
            preferredSupportedLocales = lists["preferred-supported-locales"].orEmpty(),
        )
    }

    private fun String.unquote(): String {
        val value = trim()
        return when {
            value.length >= 2 && value.first() == '"' && value.last() == '"' -> value.substring(1, value.length - 1)
            value.length >= 2 && value.first() == '\'' && value.last() == '\'' -> value.substring(1, value.length - 1)
            else -> value
        }
    }
}

data class ParsedL10nYaml(
    val arbDir: String?,
    val templateArbFile: String?,
    val outputClass: String?,
    val preferredSupportedLocales: List<String>,
)

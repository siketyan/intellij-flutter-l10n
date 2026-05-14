package com.github.siketyan.flutterl10n.l10n

import com.google.gson.JsonParser
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

@Service(Service.Level.PROJECT)
class ArbTranslationService(private val project: Project) {
    private var cachedBundle: CachedBundle? = null

    fun lookup(contextFile: VirtualFile?, localizationClassName: String?, key: String): String? {
        val config = project.service<L10nConfigService>().resolveFor(contextFile, localizationClassName)
        val files = findArbFiles(config)
        val signature = CacheSignature(
            configKey = config.cacheKey,
            yamlModificationStamp = config.yamlFile?.modificationStamp,
            arbFiles = files.map { it.path to it.modificationStamp },
        )

        val bundle = cachedBundle
            ?.takeIf { it.signature == signature }
            ?: buildBundle(config, files, signature).also { cachedBundle = it }

        return bundle.translations[key]
    }

    private fun buildBundle(config: L10nConfig, files: List<VirtualFile>, signature: CacheSignature): CachedBundle {
        val parsedFiles = files.mapNotNull(::parseArbFile)
        val selected = selectPrimaryTranslations(config, parsedFiles)
        return CachedBundle(signature, selected)
    }

    private fun parseArbFile(file: VirtualFile): ParsedArbFile? {
        val json = runCatching { JsonParser.parseString(VfsUtilCore.loadText(file)).asJsonObject }.getOrNull()
            ?: return null

        val translations = mutableMapOf<String, String>()
        var declaredLocale: String? = null

        for ((key, value) in json.entrySet()) {
            if (key == "@@locale" && value.isJsonPrimitive && value.asJsonPrimitive.isString) {
                declaredLocale = value.asString
                continue
            }
            if (!key.startsWith("@") && value.isJsonPrimitive && value.asJsonPrimitive.isString) {
                translations[key] = value.asString
            }
        }

        return ParsedArbFile(
            file = file,
            locale = declaredLocale ?: localeFromFileName(file.nameWithoutExtension),
            translations = translations,
        )
    }

    private fun selectPrimaryTranslations(config: L10nConfig, files: List<ParsedArbFile>): Map<String, String> {
        if (files.isEmpty()) {
            return emptyMap()
        }

        val preferredLocales = config.preferredSupportedLocales
        val preferredFile = preferredLocales.firstNotNullOfOrNull { locale ->
            files.find { it.locale == locale || it.locale?.startsWith("${locale}_") == true }
        }

        val templateFile = files.find { it.file.name == config.templateArbFile }
        val firstFile = files.sortedBy { it.file.path }.first()

        return (preferredFile ?: templateFile ?: firstFile).translations
    }

    private fun findArbFiles(config: L10nConfig): List<VirtualFile> {
        val root = config.root
        val arbDir = root?.findFileByRelativePath(config.arbDir)
        val configuredFiles = arbDir
            ?.children
            ?.filter { !it.isDirectory && it.extension == ARB_EXTENSION }
            .orEmpty()

        if (configuredFiles.isNotEmpty()) {
            return configuredFiles.sortedBy { it.path }
        }

        val files = mutableListOf<VirtualFile>()
        ProjectFileIndex.getInstance(project).iterateContent { file ->
            if (!file.isDirectory && file.extension == ARB_EXTENSION) {
                files += file
            }
            true
        }
        return files.sortedBy { it.path }
    }

    private fun localeFromFileName(nameWithoutExtension: String): String? {
        val marker = nameWithoutExtension.indexOf('_')
        return if (marker >= 0 && marker + 1 < nameWithoutExtension.length) {
            nameWithoutExtension.substring(marker + 1)
        } else {
            null
        }
    }

    private data class ParsedArbFile(
        val file: VirtualFile,
        val locale: String?,
        val translations: Map<String, String>,
    )

    private data class CacheSignature(
        val configKey: String,
        val yamlModificationStamp: Long?,
        val arbFiles: List<Pair<String, Long>>,
    )

    private data class CachedBundle(
        val signature: CacheSignature,
        val translations: Map<String, String>,
    )

    companion object {
        private const val ARB_EXTENSION = "arb"
    }
}

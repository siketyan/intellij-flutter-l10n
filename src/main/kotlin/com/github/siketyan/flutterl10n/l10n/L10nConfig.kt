package com.github.siketyan.flutterl10n.l10n

import com.intellij.openapi.vfs.VirtualFile

data class L10nConfig(
    val root: VirtualFile?,
    val yamlFile: VirtualFile?,
    val arbDir: String = DEFAULT_ARB_DIR,
    val templateArbFile: String = DEFAULT_TEMPLATE_ARB_FILE,
    val outputClass: String = DEFAULT_OUTPUT_CLASS,
    val preferredSupportedLocales: List<String> = emptyList(),
) {
    val cacheKey: String
        get() = yamlFile?.path ?: root?.path ?: "<project-default>"

    companion object {
        const val DEFAULT_ARB_DIR = "lib/l10n"
        const val DEFAULT_TEMPLATE_ARB_FILE = "app_en.arb"
        const val DEFAULT_OUTPUT_CLASS = "AppLocalizations"
    }
}

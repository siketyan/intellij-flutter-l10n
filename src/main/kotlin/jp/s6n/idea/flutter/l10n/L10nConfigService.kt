package jp.s6n.idea.flutter.l10n

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

@Service(Service.Level.PROJECT)
class L10nConfigService(private val project: Project) {
    fun resolveFor(contextFile: VirtualFile?, localizationClassName: String?): L10nConfig {
        val configs = findConfigs()
        if (configs.isEmpty()) {
            return defaultConfig(contextFile)
        }

        val matchingClassConfigs = localizationClassName
            ?.let { className -> configs.filter { it.outputClass == className } }
            .orEmpty()

        val candidates = matchingClassConfigs.ifEmpty { configs }
        return candidates.minByOrNull { distanceFrom(contextFile, it.root) } ?: defaultConfig(contextFile)
    }

    private fun findConfigs(): List<L10nConfig> {
        val files = mutableListOf<VirtualFile>()
        ProjectFileIndex.getInstance(project).iterateContent { file ->
            if (!file.isDirectory && file.name == L10N_YAML) {
                files += file
            }
            true
        }

        return files.mapNotNull { file ->
            val parsed = runCatching { L10nYamlParser.parse(VfsUtilCore.loadText(file)) }.getOrNull() ?: return@mapNotNull null
            L10nConfig(
                root = file.parent,
                yamlFile = file,
                arbDir = parsed.arbDir ?: L10nConfig.DEFAULT_ARB_DIR,
                templateArbFile = parsed.templateArbFile ?: L10nConfig.DEFAULT_TEMPLATE_ARB_FILE,
                outputClass = parsed.outputClass ?: L10nConfig.DEFAULT_OUTPUT_CLASS,
                preferredSupportedLocales = parsed.preferredSupportedLocales,
            )
        }
    }

    private fun defaultConfig(contextFile: VirtualFile?): L10nConfig {
        return L10nConfig(root = findNearestPubspecRoot(contextFile), yamlFile = null)
    }

    private fun findNearestPubspecRoot(contextFile: VirtualFile?): VirtualFile? {
        var current = if (contextFile?.isDirectory == true) contextFile else contextFile?.parent
        while (current != null) {
            if (current.findChild(PUBSPEC_YAML) != null) {
                return current
            }
            current = current.parent
        }
        return project.baseDir
    }

    private fun distanceFrom(contextFile: VirtualFile?, root: VirtualFile?): Int {
        val contextPath = contextFile?.path ?: return Int.MAX_VALUE
        val rootPath = root?.path ?: return Int.MAX_VALUE
        return if (FileUtil.isAncestor(rootPath, contextPath, false)) {
            contextPath.removePrefix(rootPath).count { it == '/' }
        } else {
            Int.MAX_VALUE / 2
        }
    }

    companion object {
        private const val L10N_YAML = "l10n.yaml"
        private const val PUBSPEC_YAML = "pubspec.yaml"
    }
}

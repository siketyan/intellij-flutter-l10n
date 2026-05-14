package jp.s6n.idea.flutter.l10n

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import jp.s6n.idea.flutter.l10n.navigation.ArbTranslationNavigationElement

@Service(Service.Level.PROJECT)
class ArbDefinitionService(private val project: Project) {
    fun findDefinition(contextFile: VirtualFile?, localizationClassName: String?, key: String): PsiElement? {
        return findDefinitions(contextFile, localizationClassName, key).firstOrNull()
    }

    fun findDefinitions(contextFile: VirtualFile?, localizationClassName: String?, key: String): List<PsiElement> {
        val config = project.service<L10nConfigService>().resolveFor(contextFile, localizationClassName)
        val files = findArbFiles(config)
        val orderedFiles = files.sortedWith(compareBy<VirtualFile> { it.name != config.templateArbFile }.thenBy { it.path })

        return orderedFiles
            .mapNotNull { file -> findProperty(file, key) }
            .map { property ->
                ArbTranslationNavigationElement(
                    target = property.nameElement,
                    translation = property.translationText(),
                )
            }
    }

    private fun findProperty(file: VirtualFile, key: String): JsonProperty? {
        val psiFile = PsiManager.getInstance(project).findFile(file) as? JsonFile ?: return null
        val jsonObject = psiFile.topLevelValue as? JsonObject ?: return null
        return jsonObject.findProperty(key)
    }

    private fun JsonProperty.translationText(): String {
        return (value as? JsonStringLiteral)?.value ?: value?.text ?: name
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

    companion object {
        private const val ARB_EXTENSION = "arb"
    }
}

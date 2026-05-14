package jp.s6n.idea.flutter.l10n

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.jetbrains.lang.dart.DartFileType
import com.jetbrains.lang.dart.psi.DartFile
import jp.s6n.idea.flutter.l10n.folding.DartLocalizationReferenceScanner

@Service(Service.Level.PROJECT)
class DartLocalizationUsageService(private val project: Project) {
    fun findUsages(contextFile: VirtualFile?, key: String): List<PsiElement> {
        val configService = project.service<L10nConfigService>()
        val config = configService.resolveFor(contextFile, localizationClassName = null)
        val psiManager = PsiManager.getInstance(project)
        val dartFiles = FileTypeIndex.getFiles(DartFileType.INSTANCE, GlobalSearchScope.projectScope(project))
            .filter { file -> belongsToConfig(file, config) }

        return dartFiles
            .sortedBy { it.path }
            .flatMap { file ->
                val dartFile = psiManager.findFile(file) as? DartFile ?: return@flatMap emptyList()
                DartLocalizationReferenceScanner.scan(dartFile)
                    .filter { reference ->
                        reference.key == key &&
                            configService.resolveFor(file, reference.localizationClassName).cacheKey == config.cacheKey
                    }
                    .mapNotNull { reference -> dartFile.findElementAt(reference.keyRange.startOffset) }
            }
    }

    private fun belongsToConfig(file: VirtualFile, config: L10nConfig): Boolean {
        val root = config.root ?: return true
        return FileUtil.isAncestor(root.path, file.path, false)
    }
}

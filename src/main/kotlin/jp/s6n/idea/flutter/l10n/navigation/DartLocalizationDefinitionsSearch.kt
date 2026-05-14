package jp.s6n.idea.flutter.l10n.navigation

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.DefinitionsScopedSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor

class DartLocalizationDefinitionsSearch : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
    override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
        val targets = ApplicationManager.getApplication().runReadAction<List<PsiElement>> {
            DartLocalizationNavigationResolver.resolveTargets(
                sourceElement = queryParameters.element,
                offset = queryParameters.element.textOffset,
            )
        }

        return targets.all { target -> consumer.process(target) }
    }
}

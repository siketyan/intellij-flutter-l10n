package jp.s6n.idea.flutter.l10n

import com.intellij.codeInsight.TargetElementEvaluatorEx2
import com.intellij.codeInsight.TargetElementUtilBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.lang.dart.DartFileType
import jp.s6n.idea.flutter.l10n.navigation.DartLocalizationTargetElementEvaluator

class DartLocalizationTargetElementEvaluatorTest : BasePlatformTestCase() {
    fun testLocalizationEvaluatorTakesPrecedence() {
        val element = configureOrdinaryDartElement()

        assertInstanceOf(
            TargetElementUtilBase.TARGET_ELEMENT_EVALUATOR.forLanguage(element.language),
            DartLocalizationTargetElementEvaluator::class.java,
        )
    }

    fun testOrdinaryDartElementIncludesSelf() {
        val element = configureOrdinaryDartElement()

        assertTrue(DartLocalizationTargetElementEvaluator().includeSelfInGotoImplementation(element))
    }

    fun testOrdinaryDartElementDelegatesIncludeSelf() {
        val element = configureOrdinaryDartElement()
        val fallback = object : TargetElementEvaluatorEx2() {
            override fun includeSelfInGotoImplementation(element: PsiElement) = false
        }

        withFallbackEvaluator(element, fallback) {
            assertFalse(DartLocalizationTargetElementEvaluator().includeSelfInGotoImplementation(element))
        }
    }

    fun testEmptyTargetCandidatesRemainSuppressed() {
        assertTargetCandidatesDelegated { emptyList() }
    }

    fun testNullTargetCandidatesKeepPsiFallback() {
        assertTargetCandidatesDelegated { null }
    }

    fun testNonEmptyTargetCandidatesArePreserved() {
        assertTargetCandidatesDelegated { listOf(it) }
    }

    private fun assertTargetCandidatesDelegated(candidateFactory: (PsiElement) -> Collection<PsiElement>?) {
        val element = configureOrdinaryDartElement()
        val candidates = candidateFactory(element)
        val reference = object : PsiReferenceBase<PsiElement>(element) {
            override fun resolve(): PsiElement? = null
        }
        var called = false
        val fallback = object : TargetElementEvaluatorEx2() {
            override fun getTargetCandidates(reference: PsiReference): Collection<PsiElement>? {
                called = true
                return candidates
            }
        }

        withFallbackEvaluator(element, fallback) {
            assertSame(candidates, DartLocalizationTargetElementEvaluator().getTargetCandidates(reference))
            assertTrue(called)
        }
    }

    private fun withFallbackEvaluator(element: PsiElement, evaluator: TargetElementEvaluatorEx2, test: () -> Unit) {
        val extension = DartLocalizationTargetElementEvaluator.evaluators
        extension.addExplicitExtension(element.language, evaluator)
        try {
            test()
        } finally {
            extension.removeExplicitExtension(element.language, evaluator)
        }
    }

    private fun configureOrdinaryDartElement(): PsiElement {
        val file = myFixture.configureByText(DartFileType.INSTANCE, "void main() {}")
        return file.findElementAt(file.text.indexOf("main"))!!
    }
}

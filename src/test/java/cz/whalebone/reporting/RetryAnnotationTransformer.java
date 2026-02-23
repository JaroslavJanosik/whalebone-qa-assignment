package cz.whalebone.reporting;

import cz.whalebone.config.Config;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Applies a default retry policy without requiring every test to specify @Test(retryAnalyzer=...).
 */
public class RetryAnnotationTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        if (annotation.getRetryAnalyzerClass() != null) return;
        if (testClass == null) return;

        String name = testClass.getName();

        if (name.contains(".tests.ui.") && Config.uiRetryCount() > 0) {
            annotation.setRetryAnalyzer(UiRetryAnalyzer.class);
        }

        if (name.contains(".tests.api.") && Config.apiRetryCount() > 0) {
            annotation.setRetryAnalyzer(ApiRetryAnalyzer.class);
        }
    }
}

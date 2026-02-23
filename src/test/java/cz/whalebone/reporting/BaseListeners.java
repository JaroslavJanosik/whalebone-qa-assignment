package cz.whalebone.reporting;

import org.testng.annotations.Listeners;

@Listeners({
        io.qameta.allure.testng.AllureTestNg.class,
        cz.whalebone.reporting.TimeoutAsFailureListener.class,
        cz.whalebone.reporting.UiFailureArtifactsListener.class,
        cz.whalebone.reporting.RetryAnnotationTransformer.class
})
public abstract class BaseListeners {
}

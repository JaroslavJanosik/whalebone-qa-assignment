package cz.whalebone.reporting;

public class ApiRetryAnalyzer extends RetryAnalyzer {
    public ApiRetryAnalyzer() {
        super(Kind.API);
    }
}

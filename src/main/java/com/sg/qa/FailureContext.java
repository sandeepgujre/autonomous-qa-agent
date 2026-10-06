package com.sg.qa;

import com.sg.ai.TestStep;

/**
 * Contains all locally collected information about
 * a failed test step.
 *
 * This class does not call Gemini.
 * It only stores execution and page-state evidence.
 *
 * Navigation-aware information is also stored so that
 * local self-healing can understand what action happened
 * immediately before a navigation-related failure.
 */
public class FailureContext {

    private TestStep failedStep;

    private String errorMessage;

    private String currentUrl;

    private String pageTitle;

    private PageSnapshot pageSnapshot;

    /*
     * ============================================================
     * PREVIOUS STEP INFORMATION
     * ============================================================
     *
     * Used by local self-healing to understand whether the
     * failed validation was caused by the previous action.
     *
     * Example:
     *
     * CLICK login
     *      ↓
     * VERIFY_URL fails
     *
     * Self-healing can now inspect the previous CLICK action
     * and decide whether retrying it locally makes sense.
     */

    private TestStep previousStep;

    private TestStepExecutor.ExecutionResult previousStepResult;

    public FailureContext() {
    }

    public FailureContext(
            TestStep failedStep,
            String errorMessage,
            String currentUrl,
            String pageTitle,
            PageSnapshot pageSnapshot) {

        this.failedStep = failedStep;
        this.errorMessage = errorMessage;
        this.currentUrl = currentUrl;
        this.pageTitle = pageTitle;
        this.pageSnapshot = pageSnapshot;
    }

    public TestStep getFailedStep() {
        return failedStep;
    }

    public void setFailedStep(TestStep failedStep) {
        this.failedStep = failedStep;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public void setCurrentUrl(String currentUrl) {
        this.currentUrl = currentUrl;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public PageSnapshot getPageSnapshot() {
        return pageSnapshot;
    }

    public void setPageSnapshot(PageSnapshot pageSnapshot) {
        this.pageSnapshot = pageSnapshot;
    }

    /*
     * ============================================================
     * PREVIOUS STEP GETTERS / SETTERS
     * ============================================================
     */

    public TestStep getPreviousStep() {
        return previousStep;
    }

    public void setPreviousStep(TestStep previousStep) {
        this.previousStep = previousStep;
    }

    public TestStepExecutor.ExecutionResult getPreviousStepResult() {
        return previousStepResult;
    }

    public void setPreviousStepResult(
            TestStepExecutor.ExecutionResult previousStepResult) {

        this.previousStepResult =
                previousStepResult;
    }
}
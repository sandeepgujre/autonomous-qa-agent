package com.sg.qa;

/**
 * Stores the execution result of one test step.
 *
 * This class is intentionally independent from
 * Selenium and Gemini so it can later be used
 * by HTML, JSON and Excel reporting.
 */
public class StepExecutionResult {

    private int stepNumber;

    private String action;

    private String target;

    private String value;

    private String expectedResult;

    private String status;

    private String failureCategory;

    private String failureReason;

    private boolean localHealingAttempted;

    private boolean localHealingSuccessful;

    private boolean geminiRecoveryAttempted;

    private boolean geminiRecoverySuccessful;

    private String finalMessage;

    public StepExecutionResult() {
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailureCategory() {
        return failureCategory;
    }

    public void setFailureCategory(String failureCategory) {
        this.failureCategory = failureCategory;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public boolean isLocalHealingAttempted() {
        return localHealingAttempted;
    }

    public void setLocalHealingAttempted(
            boolean localHealingAttempted) {

        this.localHealingAttempted =
                localHealingAttempted;
    }

    public boolean isLocalHealingSuccessful() {
        return localHealingSuccessful;
    }

    public void setLocalHealingSuccessful(
            boolean localHealingSuccessful) {

        this.localHealingSuccessful =
                localHealingSuccessful;
    }

    public boolean isGeminiRecoveryAttempted() {
        return geminiRecoveryAttempted;
    }

    public void setGeminiRecoveryAttempted(
            boolean geminiRecoveryAttempted) {

        this.geminiRecoveryAttempted =
                geminiRecoveryAttempted;
    }

    public boolean isGeminiRecoverySuccessful() {
        return geminiRecoverySuccessful;
    }

    public void setGeminiRecoverySuccessful(
            boolean geminiRecoverySuccessful) {

        this.geminiRecoverySuccessful =
                geminiRecoverySuccessful;
    }

    public String getFinalMessage() {
        return finalMessage;
    }

    public void setFinalMessage(
            String finalMessage) {

        this.finalMessage = finalMessage;
    }

    @Override
    public String toString() {

        return "StepExecutionResult{" +
                "stepNumber=" + stepNumber +
                ", action='" + action + '\'' +
                ", target='" + target + '\'' +
                ", status='" + status + '\'' +
                ", failureCategory='" +
                failureCategory + '\'' +
                ", localHealingAttempted=" +
                localHealingAttempted +
                ", localHealingSuccessful=" +
                localHealingSuccessful +
                ", geminiRecoveryAttempted=" +
                geminiRecoveryAttempted +
                ", geminiRecoverySuccessful=" +
                geminiRecoverySuccessful +
                ", finalMessage='" +
                finalMessage + '\'' +
                '}';
    }
}
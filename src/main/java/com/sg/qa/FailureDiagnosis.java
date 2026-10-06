package com.sg.qa;

/**
 * Represents the result of local deterministic failure diagnosis.
 *
 * This class does NOT call Gemini.
 * It only stores the failure category and explanation
 * determined from locally available execution evidence.
 */
public class FailureDiagnosis {

    private String category;

    private String reason;

    private boolean recoverable;

    public FailureDiagnosis() {
    }

    public FailureDiagnosis(
            String category,
            String reason,
            boolean recoverable) {

        this.category = category;
        this.reason = reason;
        this.recoverable = recoverable;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isRecoverable() {
        return recoverable;
    }

    public void setRecoverable(boolean recoverable) {
        this.recoverable = recoverable;
    }

    @Override
    public String toString() {

        return "FailureDiagnosis{" +
                "category='" + category + '\'' +
                ", reason='" + reason + '\'' +
                ", recoverable=" + recoverable +
                '}';
    }
}
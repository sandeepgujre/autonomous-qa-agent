package com.sg.qa;

import com.sg.ai.TestStep;
import com.sg.ai.TestStepParser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic Autonomous QA execution engine.
 *
 * Execution flow:
 *
 * Requirement
 *      ↓
 * Test Steps
 *      ↓
 * Selenium Execution
 *      ↓
 * PASS
 *      ↓
 * Next Step
 *
 * On failure:
 *
 * FAIL
 *  ↓
 * Failure Context
 *  ↓
 * Local Diagnosis
 *  ↓
 * Local Self-Healing
 *  ↓
 * Retry
 *  ↓
 * Still FAIL
 *  ↓
 * Gemini Failure Recovery
 *  ↓
 * Repaired Step
 *  ↓
 * Retry
 *  ↓
 * PASS / BLOCKED
 *
 * After execution:
 *
 * Generated Steps
 *      ↓
 * Executed Steps
 *      ↓
 * Step Results
 *      ↓
 * ExecutionReport
 *      ↓
 * JSON / HTML / Excel
 */
public class GenericQAEngine {

    private final SeleniumExecutor seleniumExecutor;

    private final TestStepExecutor testStepExecutor;

    private final FailureDiagnosisEngine
            failureDiagnosisEngine;

    private final SelfHealingEngine
            selfHealingEngine;

    private final GeminiFailureRecoveryEngine
            geminiFailureRecoveryEngine;

    /**
     * Stores individual step execution results.
     */
    private final List<StepExecutionResult> executionResults;

    /**
     * Stores the complete report of the latest execution.
     */
    private ExecutionReport lastExecutionReport;

    /**
     * One local healing cycle.
     */
    private static final int MAX_LOCAL_HEALING_ATTEMPTS = 1;

    /**
     * One Gemini recovery cycle.
     */
    private static final int MAX_GEMINI_RECOVERY_ATTEMPTS = 1;

    public GenericQAEngine() {

        this.seleniumExecutor =
                new SeleniumExecutor();

        this.testStepExecutor =
                new TestStepExecutor(
                        seleniumExecutor
                );

        this.failureDiagnosisEngine =
                new FailureDiagnosisEngine();

        this.selfHealingEngine =
                new SelfHealingEngine(
                        seleniumExecutor,
                        testStepExecutor
                );

        this.geminiFailureRecoveryEngine =
                new GeminiFailureRecoveryEngine();

        this.executionResults =
                new ArrayList<>();
    }

    /**
     * Executes structured test steps against a website.
     *
     * @param website website URL
     * @param stepsJson structured test-step JSON
     * @return true when all executable steps ultimately pass
     */
    public boolean execute(
            String website,
            String stepsJson) throws Exception {

        validateInput(
                website,
                stepsJson
        );

        printEngineHeader(
                website
        );

        List<TestStep> steps =
                parseSteps(
                        stepsJson
                );

        executionResults.clear();

        LocalDateTime startTime =
                LocalDateTime.now();

        lastExecutionReport =
                new ExecutionReport();

        lastExecutionReport.setWebsite(
                website
        );

        lastExecutionReport.setStartTime(
                startTime
        );

        /*
         * Store the original number of generated steps.
         */
        lastExecutionReport.setGeneratedSteps(
                steps.size()
        );

        seleniumExecutor.startBrowser();

        boolean overallPassed = true;

        try {

            openWebsite(
                    website
            );

            System.out.println();
            System.out.println(
                    "[4] Executing test steps..."
            );

            System.out.println(
                    "------------------------------------------"
            );

            for (int i = 0;
                 i < steps.size();
                 i++) {

                TestStep step =
                        steps.get(i);

                StepExecutionResult stepResult =
                        executeStepWithRecovery(
                                step,
                                i + 1,
                                steps,
                                i
                        );

                executionResults.add(
                        stepResult
                );

                boolean stepPassed =
                        "PASS".equalsIgnoreCase(
                                stepResult.getStatus()
                        );

                if (!stepPassed) {

                    overallPassed = false;

                    /*
                     * A failed step blocks downstream execution.
                     */
                    stepResult.setStatus(
                            "BLOCKED"
                    );

                    stepResult.setFinalMessage(
                            "Step blocked execution. "
                                    + "Downstream steps were skipped."
                    );

                    System.out.println();
                    System.out.println(
                            "[ENGINE] Step "
                                    + (i + 1)
                                    + " is BLOCKED."
                    );

                    addSkippedSteps(
                            steps,
                            i + 1,
                            "Skipped because Step "
                                    + (i + 1)
                                    + " was BLOCKED."
                    );

                    break;
                }

                System.out.println();
                System.out.println(
                        "[ENGINE] Step "
                                + (i + 1)
                                + " completed successfully."
                );
            }

            completeExecutionReport(
                    website,
                    startTime,
                    overallPassed
            );

            printFinalResult(
                    overallPassed
            );

            return overallPassed;

        } finally {

            /*
             * Preserve report data even if unexpected
             * execution termination occurs.
             */
            if (lastExecutionReport != null
                    && lastExecutionReport.getEndTime() == null) {

                lastExecutionReport.setEndTime(
                        LocalDateTime.now()
                );

                lastExecutionReport.setStepResults(
                        executionResults
                );

                lastExecutionReport.setGeneratedSteps(
                        steps.size()
                );

                lastExecutionReport.calculateSummary();
            }

            System.out.println();
            System.out.println(
                    "[5] Closing browser..."
            );

            seleniumExecutor.closeBrowser();
        }
    }

    /**
     * Adds SKIPPED results for all steps after a blocking step.
     */
    private void addSkippedSteps(
            List<TestStep> allSteps,
            int firstSkippedIndex,
            String reason) {

        if (allSteps == null
                || allSteps.isEmpty()) {

            return;
        }

        for (int i = firstSkippedIndex;
             i < allSteps.size();
             i++) {

            TestStep skippedStep =
                    allSteps.get(i);

            int stepNumber =
                    i + 1;

            StepExecutionResult skippedResult =
                    createSkippedStepResult(
                            skippedStep,
                            stepNumber,
                            reason
                    );

            executionResults.add(
                    skippedResult
            );

            System.out.println(
                    "[ENGINE] Step "
                            + stepNumber
                            + " is SKIPPED."
            );
        }
    }

    /**
     * Creates a report result for a step that was generated
     * but intentionally never executed.
     */
    private StepExecutionResult createSkippedStepResult(
            TestStep step,
            int stepNumber,
            String reason) {

        StepExecutionResult result =
                new StepExecutionResult();

        result.setStepNumber(
                stepNumber
        );

        if (step != null) {

            result.setAction(
                    step.getAction()
            );

            result.setTarget(
                    step.getTarget()
            );

            result.setValue(
                    step.getValue()
            );

            result.setExpectedResult(
                    step.getExpectedResult()
            );
        }

        result.setStatus(
                "SKIPPED"
        );

        result.setFailureCategory(
                "SKIPPED"
        );

        result.setFailureReason(
                reason
        );

        result.setFinalMessage(
                reason
        );

        result.setLocalHealingAttempted(
                false
        );

        result.setLocalHealingSuccessful(
                false
        );

        result.setGeminiRecoveryAttempted(
                false
        );

        result.setGeminiRecoverySuccessful(
                false
        );

        return result;
    }

    /**
     * Returns the step results of the latest execution.
     */
    public List<StepExecutionResult>
    getExecutionResults() {

        return new ArrayList<>(
                executionResults
        );
    }

    /**
     * Returns the complete report of the latest execution.
     */
    public ExecutionReport
    getLastExecutionReport() {

        return lastExecutionReport;
    }

    /**
     * Completes and calculates the final execution report.
     */
    private void completeExecutionReport(
            String website,
            LocalDateTime startTime,
            boolean overallPassed) {

        if (lastExecutionReport == null) {

            lastExecutionReport =
                    new ExecutionReport();
        }

        lastExecutionReport.setWebsite(
                website
        );

        lastExecutionReport.setStartTime(
                startTime
        );

        lastExecutionReport.setEndTime(
                LocalDateTime.now()
        );

        if (lastExecutionReport.getGeneratedSteps() <= 0) {

            lastExecutionReport.setGeneratedSteps(
                    executionResults.size()
            );
        }

        lastExecutionReport.setStepResults(
                executionResults
        );

        lastExecutionReport.calculateSummary();

        /*
         * Engine execution result remains the final authority.
         */
        lastExecutionReport.setOverallStatus(
                overallPassed
                        ? "PASS"
                        : "FAIL"
        );
    }

    /**
     * Executes one step and performs the complete
     * local + Gemini recovery chain when required.
     */
    private StepExecutionResult executeStepWithRecovery(
            TestStep step,
            int stepNumber,
            List<TestStep> allSteps,
            int currentIndex) {

        StepExecutionResult result =
                createInitialStepResult(
                        step,
                        stepNumber
                );

        System.out.println();
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "STEP " + stepNumber
        );

        System.out.println(
                "=========================================="
        );

        printStepInformation(
                step
        );

        /*
         * ========================================================
         * ATTEMPT 1 - NORMAL EXECUTION
         * ========================================================
         */

        TestStepExecutor.ExecutionResult
                executionResult =
                testStepExecutor.executeWithResult(
                        step
                );

        if (executionResult.isPassed()) {

            System.out.println(
                    "Result: PASS"
            );

            result.setStatus(
                    "PASS"
            );

            result.setFinalMessage(
                    "Step executed successfully."
            );

            return result;
        }

        System.out.println(
                "Result: FAIL"
        );

        /*
         * ========================================================
         * FAILURE CONTEXT
         * ========================================================
         */

        TestStep previousStep =
                getPreviousStep(
                        allSteps,
                        currentIndex
                );

        TestStepExecutor.ExecutionResult
                previousStepResult =
                getPreviousStepResult(
                        currentIndex
                );

        FailureContext failureContext =
                collectFailureContext(
                        step,
                        executionResult,
                        previousStep,
                        previousStepResult
                );

        printFailureContext(
                failureContext
        );

        /*
         * ========================================================
         * LOCAL FAILURE DIAGNOSIS
         * ========================================================
         */

        System.out.println();
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "LOCAL FAILURE DIAGNOSIS"
        );

        System.out.println(
                "=========================================="
        );

        FailureDiagnosis diagnosis =
                failureDiagnosisEngine.diagnose(
                        failureContext
                );

        printDiagnosis(
                diagnosis
        );

        if (diagnosis != null) {

            result.setFailureCategory(
                    diagnosis.getCategory()
            );

            result.setFailureReason(
                    diagnosis.getReason()
            );
        }

        /*
         * ========================================================
         * LOCAL SELF-HEALING
         * ========================================================
         */

        if (diagnosis != null
                && diagnosis.isRecoverable()) {

            for (int attempt = 1;
                 attempt <= MAX_LOCAL_HEALING_ATTEMPTS;
                 attempt++) {

                result.setLocalHealingAttempted(
                        true
                );

                System.out.println();
                System.out.println(
                        "------------------------------------------"
                );

                System.out.println(
                        "LOCAL HEALING ATTEMPT "
                                + attempt
                                + " / "
                                + MAX_LOCAL_HEALING_ATTEMPTS
                );

                System.out.println(
                        "------------------------------------------"
                );

                boolean healed =
                        selfHealingEngine.healAndRetry(
                                failureContext,
                                diagnosis
                        );

                if (healed) {

                    result.setLocalHealingSuccessful(
                            true
                    );

                    result.setStatus(
                            "PASS"
                    );

                    result.setFinalMessage(
                            "Step passed after local self-healing."
                    );

                    System.out.println(
                            "[ENGINE] Local self-healing SUCCESS."
                    );

                    return result;
                }

                System.out.println(
                        "[ENGINE] Local self-healing failed."
                );
            }

        } else {

            System.out.println(
                    "[ENGINE] Failure is not locally recoverable."
            );
        }

        /*
         * ========================================================
         * GEMINI FALLBACK
         * ========================================================
         *
         * Gemini is reached ONLY after local recovery
         * has failed.
         */

        System.out.println();
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "GEMINI FALLBACK RECOVERY"
        );

        System.out.println(
                "=========================================="
        );

        for (int attempt = 1;
             attempt <= MAX_GEMINI_RECOVERY_ATTEMPTS;
             attempt++) {

            result.setGeminiRecoveryAttempted(
                    true
            );

            System.out.println(
                    "[ENGINE] Gemini recovery attempt "
                            + attempt
                            + " / "
                            + MAX_GEMINI_RECOVERY_ATTEMPTS
            );

            TestStep repairedStep =
                    geminiFailureRecoveryEngine.recover(
                            failureContext,
                            diagnosis
                    );

            if (repairedStep == null) {

                System.out.println(
                        "[ENGINE] Gemini could not produce "
                                + "a valid repaired step."
                );

                continue;
            }

            System.out.println();
            System.out.println(
                    "[ENGINE] Gemini produced repaired step:"
            );

            printStepInformation(
                    repairedStep
            );

            System.out.println();
            System.out.println(
                    "[ENGINE] Executing Gemini-repaired step..."
            );

            TestStepExecutor.ExecutionResult
                    repairedResult =
                    testStepExecutor.executeWithResult(
                            repairedStep
                    );

            if (repairedResult.isPassed()) {

                result.setGeminiRecoverySuccessful(
                        true
                );

                result.setStatus(
                        "PASS"
                );

                result.setFinalMessage(
                        "Step passed after Gemini recovery."
                );

                if (repairedStep.getTarget() != null) {

                    result.setTarget(
                            repairedStep.getTarget()
                    );
                }

                if (repairedStep.getValue() != null) {

                    result.setValue(
                            repairedStep.getValue()
                    );
                }

                System.out.println();
                System.out.println(
                        "[ENGINE] GEMINI SELF-HEALING SUCCESS."
                );

                return result;
            }

            System.out.println();
            System.out.println(
                    "[ENGINE] Gemini-repaired step FAILED."
            );
        }

        /*
         * ========================================================
         * PERMANENT FAILURE
         * ========================================================
         */

        result.setStatus(
                "FAIL"
        );

        result.setFinalMessage(
                "Local healing + Gemini recovery were unsuccessful."
        );

        System.out.println();
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "STEP RECOVERY FAILED"
        );

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "[ENGINE] Local healing + Gemini recovery "
                        + "were unsuccessful."
        );

        return result;
    }

    /**
     * Returns the immediately previous TestStep.
     */
    private TestStep getPreviousStep(
            List<TestStep> allSteps,
            int currentIndex) {

        if (allSteps == null
                || currentIndex <= 0
                || currentIndex > allSteps.size() - 1) {

            return null;
        }

        return allSteps.get(
                currentIndex - 1
        );
    }

    /**
     * Returns the execution result of the immediately
     * previous step when it is available.
     *
     * The reporting model cannot safely reconstruct
     * the original executor result, so null is returned.
     */
    private TestStepExecutor.ExecutionResult
    getPreviousStepResult(
            int currentIndex) {

        if (currentIndex <= 0) {
            return null;
        }

        if (executionResults.size() < currentIndex) {
            return null;
        }

        StepExecutionResult previousReportResult =
                executionResults.get(
                        currentIndex - 1
                );

        if (previousReportResult == null) {
            return null;
        }

        return null;
    }

    /**
     * Creates initial report data for one step.
     */
    private StepExecutionResult createInitialStepResult(
            TestStep step,
            int stepNumber) {

        StepExecutionResult result =
                new StepExecutionResult();

        result.setStepNumber(
                stepNumber
        );

        if (step != null) {

            result.setAction(
                    step.getAction()
            );

            result.setTarget(
                    step.getTarget()
            );

            result.setValue(
                    step.getValue()
            );

            result.setExpectedResult(
                    step.getExpectedResult()
            );
        }

        result.setStatus(
                "FAIL"
        );

        return result;
    }

    /**
     * Collects all locally available failure evidence.
     *
     * The complete page snapshot is retained internally
     * for diagnosis and self-healing, but is intentionally
     * not printed to the terminal.
     */
    private FailureContext collectFailureContext(
            TestStep failedStep,
            TestStepExecutor.ExecutionResult
                    executionResult,
            TestStep previousStep,
            TestStepExecutor.ExecutionResult
                    previousStepResult) {

        String currentUrl = null;

        String pageTitle = null;

        PageSnapshot pageSnapshot = null;

        String errorMessage =
                executionResult != null
                        && executionResult.getErrorMessage() != null
                        ? executionResult.getErrorMessage()
                        : "Test step returned FAIL.";

        try {

            currentUrl =
                    seleniumExecutor.getCurrentUrl();

        } catch (Exception e) {

            errorMessage +=
                    " Unable to read current URL: "
                            + e.getMessage();
        }

        try {

            pageTitle =
                    seleniumExecutor.getPageTitle();

        } catch (Exception e) {

            errorMessage +=
                    " Unable to read page title: "
                            + e.getMessage();
        }

        try {

            pageSnapshot =
                    seleniumExecutor.createPageSnapshot();

        } catch (Exception e) {

            errorMessage +=
                    " Unable to create page snapshot: "
                            + e.getMessage();
        }

        FailureContext context =
                new FailureContext(
                        failedStep,
                        errorMessage,
                        currentUrl,
                        pageTitle,
                        pageSnapshot
                );

        context.setPreviousStep(
                previousStep
        );

        context.setPreviousStepResult(
                previousStepResult
        );

        return context;
    }

    /**
     * Prints concise failure evidence.
     *
     * Full DOM/Page Snapshot JSON is intentionally not
     * printed to keep terminal output clean.
     */
    private void printFailureContext(
            FailureContext context) {

        System.out.println();
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "FAILURE CONTEXT"
        );

        System.out.println(
                "=========================================="
        );

        if (context == null) {

            System.out.println(
                    "Failure context: NOT AVAILABLE"
            );

            return;
        }

        TestStep failedStep =
                context.getFailedStep();

        if (failedStep != null) {

            System.out.println(
                    "Failed action: "
                            + failedStep.getAction()
            );

            System.out.println(
                    "Failed target: "
                            + failedStep.getTarget()
            );

            System.out.println(
                    "Expected result: "
                            + failedStep.getExpectedResult()
            );
        }

        TestStep previousStep =
                context.getPreviousStep();

        if (previousStep != null) {

            System.out.println();
            System.out.println(
                    "Previous step: "
                            + previousStep.getAction()
                            + " | "
                            + previousStep.getTarget()
            );

        } else {

            System.out.println(
                    "Previous step: NONE"
            );
        }

        System.out.println(
                "Current URL: "
                        + context.getCurrentUrl()
        );

        System.out.println(
                "Page title: "
                        + context.getPageTitle()
        );

        System.out.println(
                "Error: "
                        + context.getErrorMessage()
        );

        /*
         * Keep the snapshot available internally.
         * Only print a concise summary.
         */
        if (context.getPageSnapshot() != null) {

            System.out.println(
                    "Page snapshot: AVAILABLE "
                            + "(used internally for diagnosis/healing)"
            );

        } else {

            System.out.println(
                    "Page snapshot: NOT AVAILABLE"
            );
        }
    }

    /**
     * Prints local diagnosis.
     */
    private void printDiagnosis(
            FailureDiagnosis diagnosis) {

        if (diagnosis == null) {

            System.out.println(
                    "Diagnosis: NOT AVAILABLE"
            );

            return;
        }

        System.out.println(
                "Category: "
                        + diagnosis.getCategory()
        );

        System.out.println(
                "Reason: "
                        + diagnosis.getReason()
        );

        System.out.println(
                "Recoverable: "
                        + diagnosis.isRecoverable()
        );
    }

    /**
     * Validates input.
     */
    private void validateInput(
            String website,
            String stepsJson) {

        if (website == null
                || website.isBlank()) {

            throw new IllegalArgumentException(
                    "Website URL cannot be empty."
            );
        }

        if (stepsJson == null
                || stepsJson.isBlank()) {

            throw new IllegalArgumentException(
                    "Test steps JSON cannot be empty."
            );
        }
    }

    /**
     * Parses structured test steps.
     */
    private List<TestStep> parseSteps(
            String stepsJson) throws Exception {

        System.out.println();
        System.out.println(
                "[1] Parsing test steps..."
        );

        List<TestStep> steps =
                TestStepParser.parse(
                        stepsJson
                );

        if (steps == null
                || steps.isEmpty()) {

            throw new IllegalStateException(
                    "No test steps were found."
            );
        }

        System.out.println(
                "Total generated steps: "
                        + steps.size()
        );

        return steps;
    }

    /**
     * Starts browser and opens website.
     */
    private void openWebsite(
            String website) {

        System.out.println();
        System.out.println(
                "[2] Browser started."
        );

        System.out.println();
        System.out.println(
                "[3] Opening website..."
        );

        seleniumExecutor.openWebsite(
                website
        );

        System.out.println(
                "Website opened successfully."
        );
    }

    /**
     * Prints engine header.
     */
    private void printEngineHeader(
            String website) {

        System.out.println();
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "GENERIC AUTONOMOUS QA ENGINE"
        );

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "Website: "
                        + website
        );

        System.out.println();
        System.out.println(
                "[1] Preparing execution..."
        );
    }

    /**
     * Prints step details.
     */
    private void printStepInformation(
            TestStep step) {

        if (step == null) {

            System.out.println(
                    "Step: NULL"
            );

            return;
        }

        System.out.println(
                "Action: "
                        + step.getAction()
        );

        System.out.println(
                "Target: "
                        + step.getTarget()
        );

        System.out.println(
                "Value: "
                        + step.getValue()
        );

        System.out.println(
                "Expected: "
                        + step.getExpectedResult()
        );
    }

    /**
     * Prints final result.
     */
    private void printFinalResult(
            boolean overallPassed) {

        System.out.println();
        System.out.println(
                "------------------------------------------"
        );

        System.out.println();
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "FINAL RESULT: "
                        + (
                        overallPassed
                                ? "PASS"
                                : "FAIL / BLOCKED"
                )
        );

        System.out.println(
                "=========================================="
        );

        System.out.println();
        System.out.println(
                "Generated steps: "
                        + (
                        lastExecutionReport != null
                                ? lastExecutionReport
                                .getGeneratedSteps()
                                : 0
                )
        );

        System.out.println(
                "Executed steps: "
                        + (
                        lastExecutionReport != null
                                ? lastExecutionReport
                                .getExecutedSteps()
                                : 0
                )
        );

        System.out.println(
                "Passed steps: "
                        + (
                        lastExecutionReport != null
                                ? lastExecutionReport
                                .getPassedSteps()
                                : 0
                )
        );

        System.out.println(
                "Failed steps: "
                        + (
                        lastExecutionReport != null
                                ? lastExecutionReport
                                .getFailedSteps()
                                : 0
                )
        );

        System.out.println(
                "Blocked steps: "
                        + (
                        lastExecutionReport != null
                                ? lastExecutionReport
                                .getBlockedSteps()
                                : 0
                )
        );

        System.out.println(
                "Skipped steps: "
                        + (
                        lastExecutionReport != null
                                ? lastExecutionReport
                                .getSkippedSteps()
                                : 0
                )
        );
    }
}
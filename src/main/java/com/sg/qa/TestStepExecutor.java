package com.sg.qa;

import com.sg.ai.TestStep;

/**
 * Executes structured QA test steps.
 *
 * This class is responsible only for executing an individual
 * test step and preserving useful execution failure information.
 *
 * It does NOT perform diagnosis or self-healing.
 *
 * Architecture:
 *
 * GenericQAEngine
 *       ↓
 * TestStepExecutor
 *       ↓
 * SeleniumExecutor
 *
 * On failure:
 *       ↓
 * ExecutionResult
 *       ↓
 * GenericQAEngine
 *       ↓
 * FailureDiagnosisEngine
 */
public class TestStepExecutor {

    private final SeleniumExecutor seleniumExecutor;

    public TestStepExecutor(
            SeleniumExecutor seleniumExecutor) {

        this.seleniumExecutor =
                seleniumExecutor;
    }

    /**
     * Legacy-compatible execution method.
     *
     * Existing code that expects a boolean can continue
     * using this method.
     */
    public boolean execute(TestStep step) {

        return executeWithResult(step)
                .isPassed();
    }

    /**
     * Executes a test step and returns structured execution
     * information.
     *
     * This is the method used by the autonomous QA engine.
     */
    public ExecutionResult executeWithResult(
            TestStep step) {

        if (step == null) {

            return new ExecutionResult(
                    false,
                    "Test step is null.",
                    null
            );
        }

        if (step.getAction() == null
                || step.getAction().isBlank()) {

            return new ExecutionResult(
                    false,
                    "Test step action is missing.",
                    null
            );
        }

        String action =
                step.getAction()
                        .trim()
                        .toUpperCase();

        System.out.println();
        System.out.println(
                "================================"
        );

        System.out.println(
                "EXECUTING TEST STEP"
        );

        System.out.println(
                "Action: " + action
        );

        System.out.println(
                "Target: " + step.getTarget()
        );

        System.out.println(
                "Value: " + step.getValue()
        );

        System.out.println(
                "Expected: "
                        + step.getExpectedResult()
        );

        System.out.println(
                "================================"
        );

        try {

            switch (action) {

                case "TYPE":

                    seleniumExecutor.typeByPurpose(
                            step.getTarget(),
                            step.getValue()
                    );

                    return success();

                case "CLICK":

                    seleniumExecutor.clickByPurpose(
                            step.getTarget()
                    );

                    return success();

                case "CLEAR":

                    seleniumExecutor.clearByPurpose(
                            step.getTarget()
                    );

                    return success();

                case "GET_TEXT":

                    String actualText =
                            seleniumExecutor.getTextByPurpose(
                                    step.getTarget()
                            );

                    return new ExecutionResult(
                            true,
                            null,
                            actualText
                    );

                case "VERIFY_TEXT":

                    boolean textResult =
                            seleniumExecutor.verifyText(
                                    step.getTarget(),
                                    step.getExpectedResult()
                            );

                    if (textResult) {

                        return new ExecutionResult(
                                true,
                                null,
                                null
                        );
                    }

                    return new ExecutionResult(
                            false,
                            "VERIFY_TEXT validation failed.",
                            null
                    );

                case "VERIFY_URL":

                    boolean urlResult =
                            seleniumExecutor.verifyUrl(
                                    step.getExpectedResult()
                            );

                    if (urlResult) {

                        return success();
                    }

                    return new ExecutionResult(
                            false,
                            "VERIFY_URL validation failed.",
                            null
                    );

                case "VERIFY_TITLE":

                    boolean titleResult =
                            seleniumExecutor.verifyTitle(
                                    step.getExpectedResult()
                            );

                    if (titleResult) {

                        return success();
                    }

                    return new ExecutionResult(
                            false,
                            "VERIFY_TITLE validation failed.",
                            null
                    );

                default:

                    String message =
                            "Unsupported action: "
                                    + action;

                    System.out.println(message);

                    return new ExecutionResult(
                            false,
                            message,
                            null
                    );
            }

        } catch (Exception e) {

            String message =
                    "Step execution failed: "
                            + e.getClass().getSimpleName()
                            + ": "
                            + e.getMessage();

            System.out.println(message);

            return new ExecutionResult(
                    false,
                    message,
                    null
            );
        }
    }

    /**
     * Creates a successful execution result.
     */
    private ExecutionResult success() {

        return new ExecutionResult(
                true,
                null,
                null
        );
    }

    /**
     * Structured result of an individual test step.
     *
     * This class is intentionally simple so the current
     * architecture remains easy to extend.
     *
     * Future phases can add:
     *
     * - execution duration
     * - screenshot path
     * - healed target
     * - retry count
     * - recovery strategy
     */
    public static class ExecutionResult {

        private final boolean passed;

        private final String errorMessage;

        private final String actualValue;

        public ExecutionResult(
                boolean passed,
                String errorMessage,
                String actualValue) {

            this.passed = passed;

            this.errorMessage =
                    errorMessage;

            this.actualValue =
                    actualValue;
        }

        public boolean isPassed() {

            return passed;
        }

        public String getErrorMessage() {

            return errorMessage;
        }

        public String getActualValue() {

            return actualValue;
        }
    }
}
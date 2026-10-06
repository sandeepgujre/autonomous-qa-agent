package com.sg.qa;

import com.sg.ai.TestStep;

/**
 * Performs deterministic local self-healing.
 *
 * IMPORTANT:
 *
 * This class NEVER calls Gemini.
 *
 * Healing priority:
 *
 * Navigation failure:
 *
 * 1. Inspect previous step
 * 2. If previous step was CLICK, retry previous CLICK
 * 3. Retry failed navigation verification
 * 4. Refresh current page if still required
 * 5. Recreate current DOM snapshot
 * 6. Find an alternative target
 * 7. Retry original step
 *
 * Element failure:
 *
 * 1. Refresh current page
 * 2. Recreate current DOM snapshot
 * 3. Find an alternative target
 * 4. Retry original step
 *
 * Gemini fallback is handled by GenericQAEngine.
 */
public class SelfHealingEngine {

    private final SeleniumExecutor seleniumExecutor;

    private final TestStepExecutor testStepExecutor;

    public SelfHealingEngine(
            SeleniumExecutor seleniumExecutor,
            TestStepExecutor testStepExecutor) {

        this.seleniumExecutor =
                seleniumExecutor;

        this.testStepExecutor =
                testStepExecutor;
    }

    /**
     * Attempts deterministic local recovery.
     *
     * @param context failure evidence
     * @param diagnosis locally determined failure diagnosis
     * @return true when recovery succeeds
     */
    public boolean healAndRetry(
            FailureContext context,
            FailureDiagnosis diagnosis) {

        if (context == null) {

            System.out.println(
                    "[SELF-HEALING] Failure context unavailable."
            );

            return false;
        }

        if (diagnosis == null) {

            System.out.println(
                    "[SELF-HEALING] Failure diagnosis unavailable."
            );

            return false;
        }

        TestStep failedStep =
                context.getFailedStep();

        if (failedStep == null) {

            System.out.println(
                    "[SELF-HEALING] Failed step unavailable."
            );

            return false;
        }

        System.out.println();
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "LOCAL SELF-HEALING"
        );

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "Failure category: "
                        + diagnosis.getCategory()
        );

        System.out.println(
                "Reason: "
                        + diagnosis.getReason()
        );

        if (!diagnosis.isRecoverable()) {

            System.out.println(
                    "[SELF-HEALING] Diagnosis is not recoverable locally."
            );

            return false;
        }

        /*
         * ========================================================
         * NAVIGATION FAILURE
         * ========================================================
         *
         * Navigation failures need special treatment.
         *
         * Example:
         *
         * STEP 3 -> CLICK Login
         * STEP 4 -> VERIFY_URL /secure
         *
         * If Step 4 fails because the click did not navigate,
         * refreshing the page is usually not the best first action.
         *
         * Instead, inspect the previous step and retry the action
         * that should have caused the navigation.
         */
        if (isNavigationFailure(diagnosis)) {

            System.out.println();
            System.out.println(
                    "[SELF-HEALING] Navigation failure detected."
            );

            boolean navigationRecovered =
                    retryPreviousNavigationAction(
                            context,
                            failedStep
                    );

            if (navigationRecovered) {

                System.out.println(
                        "[SELF-HEALING] SUCCESS "
                                + "after previous navigation action retry."
                );

                return true;
            }

            System.out.println();
            System.out.println(
                    "[SELF-HEALING] Previous navigation action "
                            + "retry did not recover the step."
            );
        }

        /*
         * ========================================================
         * STRATEGY 1
         * Refresh the current page.
         * ========================================================
         *
         * This remains useful for element failures and as a
         * secondary recovery strategy for navigation failures.
         */

        if (shouldRefresh(diagnosis)) {

            try {

                System.out.println();
                System.out.println(
                        "[SELF-HEALING] Strategy 1: "
                                + "Refreshing current page."
                );

                seleniumExecutor.refreshPage();

                System.out.println(
                        "[SELF-HEALING] Refresh completed."
                );

            } catch (Exception e) {

                System.out.println(
                        "[SELF-HEALING] Refresh failed: "
                                + e.getMessage()
                );
            }

            /*
             * Retry the exact original step.
             */

            System.out.println();
            System.out.println(
                    "[SELF-HEALING] Retrying original step..."
            );

            TestStepExecutor.ExecutionResult
                    retryResult =
                    testStepExecutor.executeWithResult(
                            failedStep
                    );

            if (retryResult.isPassed()) {

                System.out.println(
                        "[SELF-HEALING] SUCCESS "
                                + "after page refresh."
                );

                return true;
            }

            System.out.println(
                    "[SELF-HEALING] Original step still failed."
            );
        }

        /*
         * ========================================================
         * STRATEGY 2
         * Rebuild DOM snapshot.
         *
         * The page may have changed after refresh.
         * ========================================================
         */

        PageSnapshot refreshedSnapshot = null;

        try {

            refreshedSnapshot =
                    seleniumExecutor.createPageSnapshot();

        } catch (Exception e) {

            System.out.println(
                    "[SELF-HEALING] Unable to rebuild DOM snapshot: "
                            + e.getMessage()
            );
        }

        /*
         * ========================================================
         * STRATEGY 3
         * Find alternative target.
         * ========================================================
         */

        if (isElementFailure(diagnosis)
                && refreshedSnapshot != null) {

            String originalTarget =
                    failedStep.getTarget();

            String alternativeTarget =
                    findAlternativeTarget(
                            refreshedSnapshot,
                            originalTarget
                    );

            if (alternativeTarget != null
                    && !alternativeTarget.isBlank()
                    && !alternativeTarget.equalsIgnoreCase(
                            originalTarget
                    )) {

                System.out.println();
                System.out.println(
                        "[SELF-HEALING] Strategy 3: "
                                + "Alternative DOM target found."
                );

                System.out.println(
                        "Original target: "
                                + originalTarget
                );

                System.out.println(
                        "Healed target: "
                                + alternativeTarget
                );

                TestStep healedStep =
                        copyWithTarget(
                                failedStep,
                                alternativeTarget
                        );

                System.out.println();
                System.out.println(
                        "[SELF-HEALING] Retrying healed step..."
                );

                TestStepExecutor.ExecutionResult
                        healedResult =
                        testStepExecutor.executeWithResult(
                                healedStep
                        );

                if (healedResult.isPassed()) {

                    System.out.println(
                            "[SELF-HEALING] SUCCESS "
                                    + "using alternative target."
                    );

                    return true;
                }

                System.out.println(
                        "[SELF-HEALING] Healed target also failed."
                );
            } else {

                System.out.println();
                System.out.println(
                        "[SELF-HEALING] No useful alternative "
                                + "DOM target found."
                );
            }
        }

        System.out.println();
        System.out.println(
                "[SELF-HEALING] Local healing exhausted."
        );

        return false;
    }

    /**
     * Retries the action immediately before a navigation
     * verification failure.
     *
     * This is the key navigation-aware local healing strategy.
     *
     * Example:
     *
     * Previous step:
     * CLICK login
     *
     * Failed step:
     * VERIFY_URL /secure
     *
     * The previous CLICK is retried first because that action
     * is the most likely cause of the missing navigation.
     */
    private boolean retryPreviousNavigationAction(
            FailureContext context,
            TestStep failedStep) {

        TestStep previousStep =
                context.getPreviousStep();

        if (previousStep == null) {

            System.out.println(
                    "[SELF-HEALING] No previous step available "
                            + "for navigation recovery."
            );

            return false;
        }

        String previousAction =
                previousStep.getAction();

        System.out.println();
        System.out.println(
                "[SELF-HEALING] Previous step detected:"
        );

        System.out.println(
                "Previous action: "
                        + previousAction
        );

        System.out.println(
                "Previous target: "
                        + previousStep.getTarget()
        );

        System.out.println(
                "Previous value: "
                        + previousStep.getValue()
        );

        /*
         * A navigation-producing CLICK is currently the safest
         * deterministic action to retry locally.
         */
        if (!"CLICK".equalsIgnoreCase(
                previousAction)) {

            System.out.println(
                    "[SELF-HEALING] Previous step is not CLICK."
            );

            return false;
        }

        try {

            System.out.println();
            System.out.println(
                    "[SELF-HEALING] Retrying previous CLICK "
                            + "to recover navigation..."
            );

            TestStepExecutor.ExecutionResult
                    previousRetryResult =
                    testStepExecutor.executeWithResult(
                            previousStep
                    );

            if (!previousRetryResult.isPassed()) {

                System.out.println(
                        "[SELF-HEALING] Previous CLICK retry failed."
                );

                if (previousRetryResult.getErrorMessage() != null) {

                    System.out.println(
                            "[SELF-HEALING] Previous CLICK error: "
                                    + previousRetryResult.getErrorMessage()
                    );
                }

                return false;
            }

            System.out.println(
                    "[SELF-HEALING] Previous CLICK retry passed."
            );

            /*
             * The previous CLICK may have triggered navigation.
             *
             * SeleniumExecutor's CLICK implementation already
             * waits for page readiness after the click.
             *
             * Now retry the original failed navigation verification.
             */
            System.out.println();
            System.out.println(
                    "[SELF-HEALING] Retrying failed navigation "
                            + "verification..."
            );

            TestStepExecutor.ExecutionResult
                    navigationRetryResult =
                    testStepExecutor.executeWithResult(
                            failedStep
                    );

            if (navigationRetryResult.isPassed()) {

                System.out.println(
                        "[SELF-HEALING] Navigation verification "
                                + "passed after previous CLICK retry."
                );

                return true;
            }

            System.out.println(
                    "[SELF-HEALING] Navigation verification "
                            + "still failed."
            );

            if (navigationRetryResult.getErrorMessage() != null) {

                System.out.println(
                        "[SELF-HEALING] Navigation verification error: "
                                + navigationRetryResult.getErrorMessage()
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "[SELF-HEALING] Navigation retry failed: "
                            + e.getMessage()
            );
        }

        return false;
    }

    /**
     * Determines whether the diagnosis represents a
     * navigation-related failure.
     */
    private boolean isNavigationFailure(
            FailureDiagnosis diagnosis) {

        if (diagnosis == null) {

            return false;
        }

        String category =
                diagnosis.getCategory();

        return "NAVIGATION_FAILURE"
                .equalsIgnoreCase(category)

                || "URL_VALIDATION_FAILURE"
                .equalsIgnoreCase(category);
    }

    /**
     * Determines whether page refresh is a sensible
     * local recovery action.
     */
    private boolean shouldRefresh(
            FailureDiagnosis diagnosis) {

        String category =
                diagnosis.getCategory();

        if (category == null) {

            return true;
        }

        return "ELEMENT_NOT_FOUND"
                .equalsIgnoreCase(category)

                || "ELEMENT_DISABLED"
                .equalsIgnoreCase(category)

                || "EXECUTION_FAILURE"
                .equalsIgnoreCase(category)

                || "NAVIGATION_FAILURE"
                .equalsIgnoreCase(category)

                || "URL_VALIDATION_FAILURE"
                .equalsIgnoreCase(category);
    }

    /**
     * Determines whether the failure may be repaired
     * by changing the element target.
     */
    private boolean isElementFailure(
            FailureDiagnosis diagnosis) {

        String category =
                diagnosis.getCategory();

        if (category == null) {

            return false;
        }

        return "ELEMENT_NOT_FOUND"
                .equalsIgnoreCase(category)

                || "ELEMENT_DISABLED"
                .equalsIgnoreCase(category)

                || "EXECUTION_FAILURE"
                .equalsIgnoreCase(category);
    }

    /**
     * Searches the current DOM snapshot for a useful
     * alternative human-readable target.
     *
     * Priority:
     *
     * ARIA label
     * visible text
     * placeholder
     * name
     * id
     */
    private String findAlternativeTarget(
            PageSnapshot snapshot,
            String originalTarget) {

        if (snapshot == null
                || snapshot.getElements() == null
                || originalTarget == null
                || originalTarget.isBlank()) {

            return null;
        }

        String target =
                originalTarget
                        .trim()
                        .toLowerCase();

        /*
         * First pass:
         * strong exact/partial match.
         */

        for (PageSnapshot.ElementSnapshot element
                : snapshot.getElements()) {

            if (element == null
                    || !element.isVisible()) {

                continue;
            }

            String candidate =
                    getBestCandidate(element);

            if (candidate == null
                    || candidate.isBlank()) {

                continue;
            }

            String normalizedCandidate =
                    candidate
                            .trim()
                            .toLowerCase();

            if (normalizedCandidate.equals(target)
                    || normalizedCandidate.contains(target)
                    || target.contains(normalizedCandidate)) {

                return candidate;
            }
        }

        /*
         * Second pass:
         * token-based matching.
         */

        String[] targetTokens =
                target.split("\\s+");

        int bestScore = 0;

        String bestCandidate = null;

        for (PageSnapshot.ElementSnapshot element
                : snapshot.getElements()) {

            if (element == null
                    || !element.isVisible()) {

                continue;
            }

            String candidate =
                    getBestCandidate(element);

            if (candidate == null
                    || candidate.isBlank()) {

                continue;
            }

            String normalizedCandidate =
                    candidate
                            .trim()
                            .toLowerCase();

            int score = 0;

            for (String token : targetTokens) {

                if (token.length() < 2) {

                    continue;
                }

                if (normalizedCandidate.contains(token)) {

                    score++;
                }
            }

            if (score > bestScore) {

                bestScore = score;

                bestCandidate =
                        candidate;
            }
        }

        return bestScore > 0
                ? bestCandidate
                : null;
    }

    /**
     * Selects the most useful human-readable
     * identifier from an element.
     */
    private String getBestCandidate(
            PageSnapshot.ElementSnapshot element) {

        if (element.getAriaLabel() != null
                && !element.getAriaLabel().isBlank()) {

            return element.getAriaLabel();
        }

        if (element.getText() != null
                && !element.getText().isBlank()) {

            return element.getText();
        }

        if (element.getPlaceholder() != null
                && !element.getPlaceholder().isBlank()) {

            return element.getPlaceholder();
        }

        if (element.getName() != null
                && !element.getName().isBlank()) {

            return element.getName();
        }

        if (element.getId() != null
                && !element.getId().isBlank()) {

            return element.getId();
        }

        return null;
    }

    /**
     * Creates a copy of the failed step with only
     * the target changed.
     */
    private TestStep copyWithTarget(
            TestStep original,
            String newTarget) {

        return new TestStep(
                original.getAction(),
                newTarget,
                original.getValue(),
                original.getExpectedResult()
        );
    }
}
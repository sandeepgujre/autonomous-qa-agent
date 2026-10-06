package com.sg.qa;

import com.sg.ai.TestStep;

/**
 * Performs deterministic local diagnosis of a failed test step.
 *
 * IMPORTANT:
 * This class does NOT call Gemini.
 *
 * It uses:
 * - failed test step
 * - expected result
 * - current URL
 * - page title
 * - page snapshot
 *
 * to determine the most likely failure category.
 */
public class FailureDiagnosisEngine {

    /**
     * Diagnoses a failure using locally available evidence.
     *
     * @param context failure context collected by GenericQAEngine
     * @return local failure diagnosis
     */
    public FailureDiagnosis diagnose(
            FailureContext context) {

        if (context == null) {

            return new FailureDiagnosis(
                    "UNKNOWN",
                    "Failure context is null.",
                    false
            );
        }

        TestStep failedStep =
                context.getFailedStep();

        if (failedStep == null) {

            return new FailureDiagnosis(
                    "UNKNOWN",
                    "Failed test step is not available.",
                    false
            );
        }

        String action =
                failedStep.getAction();

        if (action == null) {

            return new FailureDiagnosis(
                    "UNKNOWN",
                    "Failed step does not contain an action.",
                    false
            );
        }

        /*
         * ------------------------------------------------
         * NAVIGATION FAILURE
         * ------------------------------------------------
         *
         * Example:
         *
         * Expected:
         * https://the-internet.herokuapp.com/secure
         *
         * Actual:
         * https://the-internet.herokuapp.com/login
         */
        if ("VERIFY_URL".equalsIgnoreCase(action)) {

            String expectedUrl =
                    failedStep.getExpectedResult();

            String actualUrl =
                    context.getCurrentUrl();

            if (expectedUrl != null
                    && actualUrl != null
                    && !expectedUrl.equals(actualUrl)) {

                return new FailureDiagnosis(
                        "NAVIGATION_FAILURE",
                        "Expected URL was '"
                                + expectedUrl
                                + "' but actual URL was '"
                                + actualUrl
                                + "'.",
                        true
                );
            }

            return new FailureDiagnosis(
                    "NAVIGATION_FAILURE",
                    "URL verification failed.",
                    true
            );
        }

        /*
         * ------------------------------------------------
         * TEXT VALIDATION FAILURE
         * ------------------------------------------------
         */
        if ("VERIFY_TEXT".equalsIgnoreCase(action)) {

            String expectedText =
                    failedStep.getExpectedResult();

            PageSnapshot snapshot =
                    context.getPageSnapshot();

            if (snapshot != null
                    && expectedText != null) {

                boolean textFound =
                        containsText(
                                snapshot,
                                expectedText
                        );

                if (!textFound) {

                    return new FailureDiagnosis(
                            "TEXT_VALIDATION_FAILURE",
                            "Expected text '"
                                    + expectedText
                                    + "' was not found "
                                    + "in the current page snapshot.",
                            false
                    );
                }
            }

            return new FailureDiagnosis(
                    "TEXT_VALIDATION_FAILURE",
                    "Expected text verification failed.",
                    false
            );
        }

        /*
         * ------------------------------------------------
         * ELEMENT-BASED FAILURE
         * ------------------------------------------------
         *
         * For actions such as:
         * TYPE
         * CLICK
         * CLEAR
         * GET_TEXT
         *
         * inspect the current page snapshot.
         */
        if ("TYPE".equalsIgnoreCase(action)
                || "CLICK".equalsIgnoreCase(action)
                || "CLEAR".equalsIgnoreCase(action)
                || "GET_TEXT".equalsIgnoreCase(action)) {

            String target =
                    failedStep.getTarget();

            PageSnapshot snapshot =
                    context.getPageSnapshot();

            if (snapshot != null
                    && target != null) {

                PageSnapshot.ElementSnapshot
                        matchingElement =
                        findMatchingElement(
                                snapshot,
                                target
                        );

                if (matchingElement == null) {

                    return new FailureDiagnosis(
                            "ELEMENT_NOT_FOUND",
                            "Target element '"
                                    + target
                                    + "' was not found "
                                    + "in the current page snapshot.",
                            true
                    );
                }

                if (!matchingElement.isEnabled()) {

                    return new FailureDiagnosis(
                            "ELEMENT_DISABLED",
                            "Target element '"
                                    + target
                                    + "' was found but "
                                    + "is disabled.",
                            true
                    );
                }
            }

            return new FailureDiagnosis(
                    "EXECUTION_FAILURE",
                    "The "
                            + action
                            + " action failed during execution.",
                    true
            );
        }

        /*
         * ------------------------------------------------
         * UNKNOWN FAILURE
         * ------------------------------------------------
         */
        return new FailureDiagnosis(
                "UNKNOWN",
                "Unable to determine a specific failure "
                        + "category for action: "
                        + action,
                false
        );
    }

    /**
     * Checks whether expected text exists in any
     * relevant text field of the page snapshot.
     */
    private boolean containsText(
            PageSnapshot snapshot,
            String expectedText) {

        if (snapshot.getElements() == null) {
            return false;
        }

        for (PageSnapshot.ElementSnapshot element
                : snapshot.getElements()) {

            if (containsIgnoreCase(
                    element.getText(),
                    expectedText)) {

                return true;
            }

            if (containsIgnoreCase(
                    element.getAriaLabel(),
                    expectedText)) {

                return true;
            }

            if (containsIgnoreCase(
                    element.getPlaceholder(),
                    expectedText)) {

                return true;
            }
        }

        return false;
    }

    /**
     * Finds an element using the same human-readable
     * target information available to the agent.
     */
    private PageSnapshot.ElementSnapshot
    findMatchingElement(
            PageSnapshot snapshot,
            String target) {

        if (snapshot.getElements() == null) {
            return null;
        }

        for (PageSnapshot.ElementSnapshot element
                : snapshot.getElements()) {

            if (matches(
                    element.getId(),
                    target)
                    || matches(
                    element.getName(),
                    target)
                    || matches(
                    element.getPlaceholder(),
                    target)
                    || matches(
                    element.getAriaLabel(),
                    target)
                    || matches(
                    element.getText(),
                    target)) {

                return element;
            }
        }

        return null;
    }

    /**
     * Performs case-insensitive exact or partial matching.
     */
    private boolean matches(
            String actual,
            String target) {

        if (actual == null
                || target == null) {

            return false;
        }

        String actualNormalized =
                actual.trim()
                        .toLowerCase();

        String targetNormalized =
                target.trim()
                        .toLowerCase();

        return actualNormalized.equals(
                targetNormalized
        )
                || actualNormalized.contains(
                targetNormalized
        )
                || targetNormalized.contains(
                actualNormalized
        );
    }

    /**
     * Performs case-insensitive text matching.
     */
    private boolean containsIgnoreCase(
            String actual,
            String expected) {

        if (actual == null
                || expected == null) {

            return false;
        }

        return actual
                .toLowerCase()
                .contains(
                        expected
                                .trim()
                                .toLowerCase()
                );
    }
}
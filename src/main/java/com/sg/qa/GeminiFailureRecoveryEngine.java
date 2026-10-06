package com.sg.qa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sg.ai.TestStep;

import java.util.List;
import java.util.Map;

/**
 * Gemini-based failure recovery engine.
 *
 * IMPORTANT:
 *
 * This class is called ONLY after:
 *
 * 1. Local failure diagnosis
 * 2. Local self-healing
 * 3. Local retry
 *
 * have failed.
 *
 * Gemini receives:
 *
 * - Failed test step
 * - Failure diagnosis
 * - Error information
 * - Current URL
 * - Page title
 * - Current DOM snapshot
 *
 * Gemini returns one repaired TestStep.
 *
 * Gemini does NOT execute Selenium directly.
 *
 * TRANSIENT FAILURE RESILIENCE:
 *
 * Temporary Gemini API failures such as:
 *
 * - HTTP 429
 * - HTTP 500
 * - HTTP 502
 * - HTTP 503
 * - HTTP 504
 *
 * are retried a limited number of times.
 */
public class GeminiFailureRecoveryEngine {

    private static final String MODEL =
            "gemini-3.8-flash";

    /*
     * Maximum number of Gemini API attempts.
     *
     * Example:
     *
     * Attempt 1 -> fail
     * Attempt 2 -> fail
     * Attempt 3 -> fail
     * Recovery stops.
     */
    private static final int MAX_GEMINI_ATTEMPTS = 3;

    /*
     * Delay before retrying Gemini.
     *
     * Attempt 1 -> wait 1 second
     * Attempt 2 -> wait 2 seconds
     */
    private static final long INITIAL_RETRY_DELAY_MS = 1000L;

    private final ObjectMapper objectMapper;

    public GeminiFailureRecoveryEngine() {

        this.objectMapper =
                new ObjectMapper();
    }

    /**
     * Asks Gemini to repair one failed test step.
     *
     * @param context failure context
     * @param diagnosis local failure diagnosis
     * @return repaired TestStep or null when recovery is unavailable
     */
    public TestStep recover(
            FailureContext context,
            FailureDiagnosis diagnosis) {

        if (context == null) {

            System.out.println(
                    "[GEMINI RECOVERY] Failure context is null."
            );

            return null;
        }

        if (diagnosis == null) {

            System.out.println(
                    "[GEMINI RECOVERY] Failure diagnosis is null."
            );

            return null;
        }

        String apiKey =
                System.getenv("GEMINI_API_KEY");

        if (apiKey == null
                || apiKey.isBlank()) {

            System.out.println(
                    "[GEMINI RECOVERY] GEMINI_API_KEY is not configured."
            );

            return null;
        }

        TestStep failedStep =
                context.getFailedStep();

        if (failedStep == null) {

            System.out.println(
                    "[GEMINI RECOVERY] Failed step is unavailable."
            );

            return null;
        }

        try {

            String prompt =
                    buildPrompt(
                            context,
                            diagnosis
                    );

            System.out.println();
            System.out.println(
                    "=========================================="
            );

            System.out.println(
                    "GEMINI FAILURE RECOVERY"
            );

            System.out.println(
                    "=========================================="
            );

            System.out.println(
                    "[GEMINI RECOVERY] Sending failure evidence..."
            );

            /*
             * Gemini API call now contains transient
             * failure retry logic.
             */
            String response =
                    callGeminiWithRetry(
                            apiKey,
                            prompt
                    );

            if (response == null
                    || response.isBlank()) {

                System.out.println(
                        "[GEMINI RECOVERY] Empty response."
                );

                return null;
            }

            System.out.println(
                    "[GEMINI RECOVERY] Response received."
            );

            TestStep repairedStep =
                    parseRepairedStep(
                            response
                    );

            if (!isValidRepairedStep(
                    repairedStep,
                    failedStep
            )) {

                System.out.println(
                        "[GEMINI RECOVERY] Invalid repaired step."
                );

                return null;
            }

            System.out.println(
                    "[GEMINI RECOVERY] Repaired action: "
                            + repairedStep.getAction()
            );

            System.out.println(
                    "[GEMINI RECOVERY] Repaired target: "
                            + repairedStep.getTarget()
            );

            System.out.println(
                    "[GEMINI RECOVERY] Repaired value: "
                            + repairedStep.getValue()
            );

            System.out.println(
                    "[GEMINI RECOVERY] Repaired expected result: "
                            + repairedStep.getExpectedResult()
            );

            return repairedStep;

        } catch (Exception e) {

            System.out.println(
                    "[GEMINI RECOVERY] Recovery failed: "
                            + e.getMessage()
            );

            return null;
        }
    }

    /**
     * Builds a strict repair prompt.
     *
     * Gemini must reason from the supplied DOM evidence.
     */
    private String buildPrompt(
            FailureContext context,
            FailureDiagnosis diagnosis) {

        TestStep failedStep =
                context.getFailedStep();

        String snapshotJson =
                serializeSnapshot(
                        context.getPageSnapshot()
                );

        return """
                You are the failure-recovery engine of an autonomous QA agent.

                Your job is to repair ONE failed Selenium test step.

                IMPORTANT RULES:

                1. Do NOT invent elements that are not present in the DOM.
                2. Use only the supplied current DOM snapshot.
                3. Preserve the original action whenever possible.
                4. Preserve the original value whenever possible.
                5. Preserve the original expected result whenever possible.
                6. Change only what is necessary to recover the step.
                7. Prefer a visible, enabled element.
                8. Prefer aria-label, visible text, placeholder, name, or id.
                9. Return ONLY one JSON object.
                10. Do NOT return markdown.
                11. Do NOT explain your reasoning.

                Supported actions:

                TYPE
                CLICK
                CLEAR
                GET_TEXT
                VERIFY_TEXT
                VERIFY_URL
                VERIFY_TITLE

                Original failed step:
                %s

                Failure category:
                %s

                Failure reason:
                %s

                Error:
                %s

                Current URL:
                %s

                Page title:
                %s

                Current DOM snapshot:
                %s

                Return exactly this JSON structure:

                {
                  "action": "CLICK",
                  "target": "Sign In",
                  "value": "",
                  "expectedResult": ""
                }
                """.formatted(
                failedStep.getAction(),
                diagnosis.getCategory(),
                diagnosis.getReason(),
                context.getErrorMessage(),
                context.getCurrentUrl(),
                context.getPageTitle(),
                snapshotJson
        );
    }

    /**
     * Serializes current DOM evidence.
     */
    private String serializeSnapshot(
            PageSnapshot snapshot) {

        if (snapshot == null) {

            return "{}";
        }

        try {

            return objectMapper
                    .writeValueAsString(snapshot);

        } catch (Exception e) {

            return "{}";
        }
    }

    /**
     * Calls Gemini with controlled retry handling.
     *
     * Only transient HTTP errors are retried.
     *
     * Permanent errors are immediately propagated.
     */
    private String callGeminiWithRetry(
            String apiKey,
            String prompt) throws Exception {

        Exception lastException = null;

        for (int attempt = 1;
             attempt <= MAX_GEMINI_ATTEMPTS;
             attempt++) {

            try {

                System.out.println(
                        "[GEMINI RECOVERY] API attempt "
                                + attempt
                                + "/"
                                + MAX_GEMINI_ATTEMPTS
                );

                return callGemini(
                        apiKey,
                        prompt
                );

            } catch (GeminiApiException e) {

                lastException = e;

                if (!e.isRetryable()) {

                    System.out.println(
                            "[GEMINI RECOVERY] Non-retryable API error: "
                                    + e.getMessage()
                    );

                    throw e;
                }

                if (attempt >= MAX_GEMINI_ATTEMPTS) {

                    System.out.println(
                            "[GEMINI RECOVERY] Maximum retry attempts reached."
                    );

                    break;
                }

                long delay =
                        INITIAL_RETRY_DELAY_MS
                                * attempt;

                System.out.println(
                        "[GEMINI RECOVERY] Transient API error detected."
                );

                System.out.println(
                        "[GEMINI RECOVERY] Retrying in "
                                + delay
                                + " ms..."
                );

                sleepBeforeRetry(
                        delay
                );
            }
        }

        if (lastException != null) {

            throw lastException;
        }

        return null;
    }

    /**
     * Calls Gemini using the existing REST endpoint.
     */
    private String callGemini(
            String apiKey,
            String prompt) throws Exception {

        java.net.http.HttpClient client =
                java.net.http.HttpClient.newBuilder()
                        .connectTimeout(
                                java.time.Duration.ofSeconds(20)
                        )
                        .build();

        Map<String, Object> requestBody =
                Map.of(
                        "contents",
                        List.of(
                                Map.of(
                                        "parts",
                                        List.of(
                                                Map.of(
                                                        "text",
                                                        prompt
                                                )
                                        )
                                )
                        )
                );

        String requestJson =
                objectMapper.writeValueAsString(
                        requestBody
                );

        String endpoint =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                        + MODEL
                        + ":generateContent?key="
                        + apiKey;

        java.net.http.HttpRequest request =
                java.net.http.HttpRequest
                        .newBuilder()
                        .uri(
                                java.net.URI.create(
                                        endpoint
                                )
                        )
                        .timeout(
                                java.time.Duration.ofSeconds(60)
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                java.net.http.HttpRequest.BodyPublishers
                                        .ofString(
                                                requestJson
                                        )
                        )
                        .build();

        java.net.http.HttpResponse<String>
                response =
                client.send(
                        request,
                        java.net.http.HttpResponse.BodyHandlers
                                .ofString()
                );

        int statusCode =
                response.statusCode();

        if (statusCode < 200
                || statusCode >= 300) {

            boolean retryable =
                    isRetryableStatusCode(
                            statusCode
                    );

            throw new GeminiApiException(
                    "Gemini API HTTP "
                            + statusCode
                            + ": "
                            + response.body(),
                    retryable
            );
        }

        return extractGeminiText(
                response.body()
        );
    }

    /**
     * Determines whether an HTTP status represents
     * a temporary condition where retrying is useful.
     */
    private boolean isRetryableStatusCode(
            int statusCode) {

        return statusCode == 429
                || statusCode == 500
                || statusCode == 502
                || statusCode == 503
                || statusCode == 504;
    }

    /**
     * Waits before a retry.
     */
    private void sleepBeforeRetry(
            long delayMs) {

        try {

            Thread.sleep(
                    delayMs
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.out.println(
                    "[GEMINI RECOVERY] Retry wait interrupted."
            );
        }
    }

    /**
     * Extracts generated text from Gemini response.
     */
    private String extractGeminiText(
            String responseJson) throws Exception {

        JsonNode root =
                objectMapper.readTree(
                        responseJson
                );

        JsonNode candidates =
                root.path("candidates");

        if (!candidates.isArray()
                || candidates.isEmpty()) {

            return null;
        }

        JsonNode textNode =
                candidates
                        .get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text");

        if (textNode.isMissingNode()
                || textNode.isNull()) {

            return null;
        }

        return textNode.asText();
    }

    /**
     * Converts Gemini JSON into TestStep.
     */
    private TestStep parseRepairedStep(
            String response) throws Exception {

        String cleaned =
                cleanJsonResponse(
                        response
                );

        JsonNode node =
                objectMapper.readTree(
                        cleaned
                );

        String action =
                getText(
                        node,
                        "action"
                );

        String target =
                getText(
                        node,
                        "target"
                );

        String value =
                getText(
                        node,
                        "value"
                );

        String expectedResult =
                getText(
                        node,
                        "expectedResult"
                );

        return new TestStep(
                action,
                target,
                value,
                expectedResult
        );
    }

    /**
     * Removes accidental markdown code fences.
     */
    private String cleanJsonResponse(
            String response) {

        String cleaned =
                response.trim();

        if (cleaned.startsWith("```")) {

            int firstNewLine =
                    cleaned.indexOf('\n');

            if (firstNewLine >= 0) {

                cleaned =
                        cleaned.substring(
                                firstNewLine + 1
                        );
            }

            if (cleaned.endsWith("```")) {

                cleaned =
                        cleaned.substring(
                                0,
                                cleaned.length() - 3
                        );
            }
        }

        return cleaned.trim();
    }

    /**
     * Safely reads a JSON property.
     */
    private String getText(
            JsonNode node,
            String field) {

        JsonNode value =
                node.get(field);

        if (value == null
                || value.isNull()) {

            return "";
        }

        return value.asText();
    }

    /**
     * Prevents Gemini from changing the test into
     * an unrelated operation.
     */
    private boolean isValidRepairedStep(
            TestStep repairedStep,
            TestStep originalStep) {

        if (repairedStep == null) {

            return false;
        }

        if (repairedStep.getAction() == null
                || repairedStep.getAction().isBlank()) {

            return false;
        }

        if (repairedStep.getTarget() == null
                || repairedStep.getTarget().isBlank()) {

            return false;
        }

        String action =
                repairedStep.getAction()
                        .trim()
                        .toUpperCase();

        boolean supported =
                action.equals("TYPE")
                        || action.equals("CLICK")
                        || action.equals("CLEAR")
                        || action.equals("GET_TEXT")
                        || action.equals("VERIFY_TEXT")
                        || action.equals("VERIFY_URL")
                        || action.equals("VERIFY_TITLE");

        if (!supported) {

            return false;
        }

        /*
         * Do not allow Gemini to turn one action into
         * an entirely different operation.
         */
        if (originalStep != null
                && originalStep.getAction() != null) {

            String originalAction =
                    originalStep.getAction()
                            .trim()
                            .toUpperCase();

            if (!originalAction.equals(action)) {

                return false;
            }
        }

        return true;
    }

    /**
     * Custom exception used to distinguish temporary
     * Gemini API failures from permanent failures.
     */
    private static class GeminiApiException
            extends Exception {

        private final boolean retryable;

        GeminiApiException(
                String message,
                boolean retryable) {

            super(message);

            this.retryable =
                    retryable;
        }

        boolean isRetryable() {

            return retryable;
        }
    }
}
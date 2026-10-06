package com.sg.qa;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GeminiTestStepGenerator {

    public String generateTestSteps(
            String website,
            String requirement) throws Exception {

        if (website == null || website.isBlank()) {

            throw new IllegalArgumentException(
                    "Website URL cannot be empty."
            );
        }

        if (requirement == null || requirement.isBlank()) {

            throw new IllegalArgumentException(
                    "Requirement cannot be empty."
            );
        }

        String prompt = """
                You are an expert autonomous QA automation engineer.

                Your job is to convert a QA requirement into
                executable browser test steps.

                Website:
                %s

                Requirement:
                %s

                Generate ONLY valid JSON.

                Do not generate Markdown.
                Do not use ```json.
                Do not add explanations.

                Each step must contain exactly these fields:

                {
                  "action": "...",
                  "target": "...",
                  "value": "...",
                  "expectedResult": "..."
                }

                Supported actions are:

                TYPE
                CLICK
                CLEAR
                GET_TEXT
                VERIFY_TEXT
                VERIFY_URL
                VERIFY_TITLE

                Rules:

                1. Use TYPE when text must be entered.
                2. Use CLICK when a button, link, checkbox,
                   or other clickable element must be clicked.
                3. Use CLEAR when an input must be cleared.
                4. Use GET_TEXT when text needs to be retrieved.
                5. Use VERIFY_TEXT when visible text must be validated.
                6. Use VERIFY_URL when the URL must be validated.
                7. Use VERIFY_TITLE when the page title must be validated.
                8. The target should describe the element in simple
                   human-readable terms such as:
                   username, password, login, search, submit, menu.
                9. Do not invent unsupported actions.
                10. Return a JSON array only.

                Example:

                [
                  {
                    "action": "TYPE",
                    "target": "username",
                    "value": "tomsmith",
                    "expectedResult": null
                  },
                  {
                    "action": "TYPE",
                    "target": "password",
                    "value": "SuperSecretPassword!",
                    "expectedResult": null
                  },
                  {
                    "action": "CLICK",
                    "target": "login",
                    "value": null,
                    "expectedResult": null
                  },
                  {
                    "action": "VERIFY_TITLE",
                    "target": null,
                    "value": null,
                    "expectedResult": "The Internet"
                  }
                ]
                """.formatted(
                website,
                requirement
        );

        String apiKey =
                System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {

            throw new IllegalStateException(
                    "GEMINI_API_KEY environment variable is not set."
            );
        }

        Client client =
                Client.builder()
                        .apiKey(apiKey)
                        .build();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.8-flash",
                        prompt,
                        null
                );

        String json =
                response.text();

        if (json == null || json.isBlank()) {

            throw new IllegalStateException(
                    "Gemini returned an empty response."
            );
        }

        json = json.trim();

        /*
         * Safety cleanup in case Gemini still returns
         * Markdown code fences.
         */
        if (json.startsWith("```json")) {

            json = json.substring(7);

        } else if (json.startsWith("```")) {

            json = json.substring(3);
        }

        if (json.endsWith("```")) {

            json = json.substring(
                    0,
                    json.length() - 3
            );
        }

        return json.trim();
    }
}
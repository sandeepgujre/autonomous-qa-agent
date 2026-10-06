package com.sg.qa;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.sg.ai.TestCase;
import com.sg.ai.TestCaseParser;
import com.sg.ai.TestCaseResponse;

public class GeminiTestCaseGenerator {

    public TestCaseResponse generateTestCases(
            String website,
            String requirement) throws Exception {

        String prompt = """
                You are an expert QA automation engineer.

                Analyze the following website and requirement.

                Website:
                %s

                Requirement:
                %s

                Generate exactly 5 login test cases.

                Include:
                - Test title
                - Username
                - Password
                - Steps
                - Expected result
                - Expected outcome

                Expected outcome must be either:
                SUCCESS
                or
                FAILURE

                Return ONLY valid JSON.

                Required JSON structure:

                {
                  "testCases": [
                    {
                      "title": "...",
                      "username": "...",
                      "password": "...",
                      "steps": ["...", "..."],
                      "expectedResult": "...",
                      "expectedOutcome": "SUCCESS"
                    }
                  ]
                }
                """.formatted(website, requirement);

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY environment variable is not set."
            );
        }

        Client client = Client.builder()
                .apiKey(apiKey)
                .build();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.8-flash",
                        prompt,
                        null
                );

        String json = response.text();

        System.out.println("\n===== GEMINI RAW RESPONSE =====");
        System.out.println(json);
        System.out.println("================================\n");

        // Remove Markdown JSON fences if Gemini adds them
        json = json.trim();

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

        json = json.trim();

        TestCaseResponse testCaseResponse =
                TestCaseParser.parse(json);

        /*
         * Gemini receives the website as an input,
         * but the JSON structure does not contain the website.
         *
         * Therefore we attach the website to every
         * generated TestCase here.
         */
        if (testCaseResponse.getTestCases() != null) {

            for (TestCase testCase :
                    testCaseResponse.getTestCases()) {

                testCase.setWebsite(website);
            }
        }

        return testCaseResponse;
    }
}
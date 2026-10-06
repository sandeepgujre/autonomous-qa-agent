package com.sg.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GeminiTest {

    public static void main(String[] args) {

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("ERROR: GEMINI_API_KEY is not set.");
            return;
        }

        String requirement = """
                Website: https://www.saucedemo.com/

                Requirement:
                User should be able to login successfully using valid credentials.

                Return ONLY valid JSON.
                Do NOT use Markdown.
                Do NOT use ```json.
                Do NOT add any explanation before or after the JSON.

                Use exactly this JSON structure:

                {
                  "testCases": [
                    {
                      "title": "Test case title",
                      "username": "username",
                      "password": "password",
                      "steps": [
                        "Step 1",
                        "Step 2",
                        "Step 3",
                        "Step 4"
                      ],
                      "expectedResult": "Expected result"
                    }
                  ]
                }

                Generate 3 relevant positive login test cases.
                """;

        try (Client client = Client.builder()
                .apiKey(apiKey)
                .build()) {

            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-3.8-flash",
                            requirement,
                            null
                    );

            System.out.println("========== AI GENERATED JSON ==========");
            System.out.println(response.text());

        } catch (Exception e) {

            System.out.println("Gemini API call failed:");
            System.out.println(e.getMessage());
        }
    }
}
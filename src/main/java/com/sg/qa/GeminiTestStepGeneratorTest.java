package com.sg.qa;

public class GeminiTestStepGeneratorTest {

    public static void main(String[] args) {

        System.out.println("================================");
        System.out.println("GEMINI TEST STEP GENERATOR");
        System.out.println("================================");

        String website =
                "https://the-internet.herokuapp.com/login";

        String requirement = """
                Test the login functionality.

                Verify:
                1. User can enter a valid username.
                2. User can enter a valid password.
                3. User can click the login button.
                4. The page title should be The Internet.
                """;

        try {

            GeminiTestStepGenerator generator =
                    new GeminiTestStepGenerator();

            System.out.println(
                    "\nSending requirement to Gemini..."
            );

            String json =
                    generator.generateTestSteps(
                            website,
                            requirement
                    );

            System.out.println(
                    "\n================================"
            );

            System.out.println(
                    "GEMINI GENERATED JSON"
            );

            System.out.println(
                    "================================"
            );

            System.out.println(json);

            System.out.println(
                    "\n================================"
            );

            System.out.println(
                    "GENERATION SUCCESSFUL"
            );

            System.out.println(
                    "================================"
            );

        } catch (Exception e) {

            System.out.println(
                    "\n================================"
            );

            System.out.println(
                    "GENERATION FAILED"
            );

            System.out.println(
                    "================================"
            );

            e.printStackTrace();
        }
    }
}
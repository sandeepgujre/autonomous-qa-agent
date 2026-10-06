package com.sg.qa;

import com.sg.ai.TestStep;
import com.sg.ai.TestStepParser;

import java.util.List;

public class AutonomousPipelineTest {

    public static void main(String[] args) {

        System.out.println("================================");
        System.out.println("AUTONOMOUS QA PIPELINE");
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

        SeleniumExecutor seleniumExecutor =
                new SeleniumExecutor();

        TestStepExecutor stepExecutor =
                new TestStepExecutor(
                        seleniumExecutor
                );

        try {

            /*
             * ==================================================
             * STEP 1
             * SEND REQUIREMENT TO GEMINI
             * ==================================================
             */

            System.out.println(
                    "\n[1] Sending requirement to Gemini..."
            );

            GeminiTestStepGenerator generator =
                    new GeminiTestStepGenerator();

            String json =
                    generator.generateTestSteps(
                            website,
                            requirement
                    );

            System.out.println(
                    "Gemini generated test steps successfully."
            );

            System.out.println(
                    "\nGenerated JSON:"
            );

            System.out.println(json);


            /*
             * ==================================================
             * STEP 2
             * PARSE GEMINI JSON
             * ==================================================
             */

            System.out.println(
                    "\n[2] Parsing Gemini JSON..."
            );

            List<TestStep> steps =
                    TestStepParser.parse(json);

            System.out.println(
                    "Parsed steps: "
                            + steps.size()
            );


            /*
             * ==================================================
             * STEP 3
             * START BROWSER
             * ==================================================
             */

            System.out.println(
                    "\n[3] Starting Selenium browser..."
            );

            seleniumExecutor.startBrowser();


            /*
             * ==================================================
             * STEP 4
             * OPEN WEBSITE
             * ==================================================
             */

            System.out.println(
                    "\n[4] Opening website..."
            );

            seleniumExecutor.openWebsite(
                    website
            );


            /*
             * ==================================================
             * STEP 5
             * EXECUTE AI GENERATED STEPS
             * ==================================================
             */

            System.out.println(
                    "\n[5] Executing AI generated test steps..."
            );

            boolean overallResult = true;

            int stepNumber = 1;

            for (TestStep step : steps) {

                System.out.println(
                        "\n--------------------------------"
                );

                System.out.println(
                        "AI STEP " + stepNumber
                );

                System.out.println(
                        "--------------------------------"
                );

                boolean result =
                        stepExecutor.execute(step);

                System.out.println(
                        "Step Result: "
                                + (result
                                ? "PASS"
                                : "FAIL")
                );

                if (!result) {

                    overallResult = false;
                }

                stepNumber++;
            }


            /*
             * ==================================================
             * STEP 6
             * FINAL RESULT
             * ==================================================
             */

            System.out.println(
                    "\n================================"
            );

            System.out.println(
                    "AUTONOMOUS QA RESULT"
            );

            System.out.println(
                    "================================"
            );

            if (overallResult) {

                System.out.println(
                        "OVERALL RESULT: PASS"
                );

            } else {

                System.out.println(
                        "OVERALL RESULT: FAIL"
                );
            }

            System.out.println(
                    "================================"
            );

        } catch (Exception e) {

            System.out.println(
                    "\n================================"
            );

            System.out.println(
                    "AUTONOMOUS QA EXECUTION FAILED"
            );

            System.out.println(
                    "================================"
            );

            System.out.println(
                    "Error: "
                            + e.getMessage()
            );

            e.printStackTrace();

        } finally {

            seleniumExecutor.closeBrowser();
        }
    }
}
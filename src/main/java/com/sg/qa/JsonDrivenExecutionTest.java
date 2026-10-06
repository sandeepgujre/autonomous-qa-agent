package com.sg.qa;

import com.sg.ai.TestStep;
import com.sg.ai.TestStepParser;

import java.util.List;

public class JsonDrivenExecutionTest {

    public static void main(String[] args) {

        System.out.println("================================");
        System.out.println("JSON DRIVEN QA EXECUTION");
        System.out.println("================================");

        String website =
                "https://the-internet.herokuapp.com/login";

        /*
         * Test steps are coming from JSON.
         * Selenium code does not know these steps beforehand.
         */
        String json = """
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
                """;

        SeleniumExecutor seleniumExecutor =
                new SeleniumExecutor();

        TestStepExecutor stepExecutor =
                new TestStepExecutor(
                        seleniumExecutor
                );

        try {

            /*
             * STEP 1
             * Parse JSON
             */
            System.out.println(
                    "\n[1] Parsing JSON..."
            );

            List<TestStep> steps =
                    TestStepParser.parse(json);

            System.out.println(
                    "Steps parsed: "
                            + steps.size()
            );

            /*
             * STEP 2
             * Start browser
             */
            System.out.println(
                    "\n[2] Starting browser..."
            );

            seleniumExecutor.startBrowser();

            /*
             * STEP 3
             * Open website
             */
            System.out.println(
                    "\n[3] Opening website..."
            );

            seleniumExecutor.openWebsite(
                    website
            );

            /*
             * STEP 4
             * Execute every JSON step
             */
            System.out.println(
                    "\n[4] Executing JSON steps..."
            );

            boolean overallResult = true;

            int stepNumber = 1;

            for (TestStep step : steps) {

                System.out.println(
                        "\n--------------------------------"
                );

                System.out.println(
                        "STEP " + stepNumber
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
             * STEP 5
             * Final result
             */
            System.out.println(
                    "\n================================"
            );

            System.out.println(
                    "FINAL TEST RESULT"
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
                    "EXECUTION FAILED"
            );

            System.out.println(
                    "================================"
            );

            e.printStackTrace();

        } finally {

            seleniumExecutor.closeBrowser();
        }
    }
}
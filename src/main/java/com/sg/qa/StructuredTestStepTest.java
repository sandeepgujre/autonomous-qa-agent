package com.sg.qa;

import com.sg.ai.TestStep;

public class StructuredTestStepTest {

    public static void main(String[] args) {

        SeleniumExecutor seleniumExecutor =
                new SeleniumExecutor();

        TestStepExecutor stepExecutor =
                new TestStepExecutor(
                        seleniumExecutor
                );

        String website =
                "https://the-internet.herokuapp.com/login";

        try {

            System.out.println("================================");
            System.out.println("STRUCTURED TEST STEP TEST");
            System.out.println("================================");

            // STEP 1: Start browser
            seleniumExecutor.startBrowser();

            // STEP 2: Open website
            seleniumExecutor.openWebsite(
                    website
            );

            /*
             * ----------------------------------------------------
             * TEST STEP 1
             * TYPE USERNAME
             * ----------------------------------------------------
             */

            TestStep usernameStep =
                    new TestStep(
                            "TYPE",
                            "username",
                            "tomsmith",
                            null
                    );

            boolean usernameResult =
                    stepExecutor.execute(
                            usernameStep
                    );

            /*
             * ----------------------------------------------------
             * TEST STEP 2
             * TYPE PASSWORD
             * ----------------------------------------------------
             */

            TestStep passwordStep =
                    new TestStep(
                            "TYPE",
                            "password",
                            "SuperSecretPassword!",
                            null
                    );

            boolean passwordResult =
                    stepExecutor.execute(
                            passwordStep
                    );

            /*
             * ----------------------------------------------------
             * TEST STEP 3
             * CLICK LOGIN
             * ----------------------------------------------------
             */

            TestStep loginStep =
                    new TestStep(
                            "CLICK",
                            "login",
                            null,
                            null
                    );

            boolean loginResult =
                    stepExecutor.execute(
                            loginStep
                    );

            /*
             * ----------------------------------------------------
             * TEST STEP 4
             * VERIFY TITLE
             * ----------------------------------------------------
             */

            TestStep titleStep =
                    new TestStep(
                            "VERIFY_TITLE",
                            null,
                            null,
                            "The Internet"
                    );

            boolean titleResult =
                    stepExecutor.execute(
                            titleStep
                    );

            /*
             * ----------------------------------------------------
             * FINAL RESULT
             * ----------------------------------------------------
             */

            boolean overallResult =
                    usernameResult
                            && passwordResult
                            && loginResult
                            && titleResult;

            System.out.println(
                    "\n================================"
            );

            System.out.println(
                    "TEST STEP RESULTS"
            );

            System.out.println(
                    "Username: "
                            + (usernameResult
                            ? "PASS"
                            : "FAIL")
            );

            System.out.println(
                    "Password: "
                            + (passwordResult
                            ? "PASS"
                            : "FAIL")
            );

            System.out.println(
                    "Login: "
                            + (loginResult
                            ? "PASS"
                            : "FAIL")
            );

            System.out.println(
                    "Title: "
                            + (titleResult
                            ? "PASS"
                            : "FAIL")
            );

            System.out.println(
                    "--------------------------------"
            );

            System.out.println(
                    "OVERALL RESULT: "
                            + (overallResult
                            ? "PASS"
                            : "FAIL")
            );

            System.out.println(
                    "================================"
            );

        } catch (Exception e) {

            System.out.println(
                    "\nSTRUCTURED TEST FAILED"
            );

            e.printStackTrace();

        } finally {

            seleniumExecutor.closeBrowser();
        }
    }
}
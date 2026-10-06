package com.sg.qa;

public class DOMInspectionTest {

    public static void main(String[] args) {

        SeleniumExecutor executor =
                new SeleniumExecutor();

        String website =
                "https://the-internet.herokuapp.com/login";

        String username =
                "tomsmith";

        String password =
                "SuperSecretPassword!";

        try {

            System.out.println("================================");
            System.out.println("GENERIC ACTION ENGINE TEST");
            System.out.println("================================");

            // STEP 1: Start browser
            System.out.println("\n[1] Starting browser...");

            executor.startBrowser();

            // STEP 2: Open website
            System.out.println("\n[2] Opening website...");

            executor.openWebsite(website);

            // STEP 3: Enter username
            System.out.println(
                    "\n[3] Executing TYPE action for username..."
            );

            executor.typeByPurpose(
                    "username",
                    username
            );

            // STEP 4: Enter password
            System.out.println(
                    "\n[4] Executing TYPE action for password..."
            );

            executor.typeByPurpose(
                    "password",
                    password
            );

            // STEP 5: Click login
            System.out.println(
                    "\n[5] Executing CLICK action..."
            );

            executor.clickByPurpose("login");

            // STEP 6: Validate page title
            System.out.println(
                    "\n[6] Validating page title..."
            );

            boolean titlePassed =
                    executor.verifyTitle("The Internet");

            // STEP 7: Validate URL
            System.out.println(
                    "\n[7] Validating URL..."
            );

            boolean urlPassed =
                    executor.verifyUrl(
                            "https://the-internet.herokuapp.com/login"
                    );

            // STEP 8: Final result
            System.out.println(
                    "\n================================"
            );

            if (titlePassed && urlPassed) {

                System.out.println(
                        "GENERIC ACTION ENGINE TEST PASSED"
                );

            } else {

                System.out.println(
                        "GENERIC ACTION ENGINE TEST FAILED"
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
                    "GENERIC ACTION ENGINE TEST FAILED"
            );

            System.out.println(
                    "================================"
            );

            e.printStackTrace();

        } finally {

            executor.closeBrowser();
        }
    }
}
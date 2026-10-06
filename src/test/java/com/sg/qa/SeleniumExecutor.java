package com.sg.qa;

import com.sg.ai.TestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SeleniumExecutor {

    private WebDriver driver;

    public void startBrowser() {

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
    }

    public void executeLoginTest(TestCase testCase) {

        System.out.println("\n================================");
        System.out.println("Executing: " + testCase.getTitle());
        System.out.println("Expected Outcome: " + testCase.getExpectedOutcome());
        System.out.println("================================");

        try {

            // Step 1: Open website
            driver.get("https://www.saucedemo.com/");

            System.out.println("Step 1: Open login page");

            // Step 2: Username
            WebElement username =
                    driver.findElement(By.id("user-name"));

            if (testCase.getUsername() != null
                    && !testCase.getUsername().isBlank()) {

                username.sendKeys(testCase.getUsername());
            }

            System.out.println("Step 2: Username entered");

            // Step 3: Password
            WebElement password =
                    driver.findElement(By.id("password"));

            if (testCase.getPassword() != null
                    && !testCase.getPassword().isBlank()) {

                password.sendKeys(testCase.getPassword());
            }

            System.out.println("Step 3: Password entered");

            // Step 4: Login
            WebElement loginButton =
                    driver.findElement(By.id("login-button"));

            loginButton.click();

            System.out.println("Step 4: Login clicked");

            // ==========================================
            // ACTUAL RESULT DETECTION
            // ==========================================

            boolean loginSuccessful =
                    driver.getCurrentUrl().contains("inventory.html");

            String actualOutcome =
                    loginSuccessful ? "SUCCESS" : "FAILURE";

            System.out.println(
                    "Actual Outcome: " + actualOutcome
            );

            System.out.println(
                    "Current URL: " + driver.getCurrentUrl()
            );

            // ==========================================
            // EXPECTED RESULT VALIDATION
            // ==========================================

            boolean testPassed =
                    actualOutcome.equals(
                            testCase.getExpectedOutcome()
                    );

            // ==========================================
            // ERROR MESSAGE VALIDATION
            // ==========================================

            if ("FAILURE".equals(testCase.getExpectedOutcome())) {

                try {

                    WebElement errorMessage =
                            driver.findElement(
                                    By.cssSelector(
                                            "[data-test='error']"
                                    )
                            );

                    String actualError =
                            errorMessage.getText();

                    System.out.println(
                            "Actual Error Message: "
                                    + actualError
                    );

                    String expectedResult =
                            testCase.getExpectedResult();

                    if (expectedResult != null
                            && !expectedResult.isBlank()) {

                        /*
                         * We don't require the entire expectedResult
                         * to exactly match because AI may include
                         * additional explanation around the message.
                         */

                        if (actualError.contains(
                                extractExpectedError(expectedResult))) {

                            System.out.println(
                                    "Error Message Validation: PASS"
                            );

                        } else {

                            System.out.println(
                                    "Error Message Validation: FAIL"
                            );

                            testPassed = false;
                        }
                    }

                } catch (Exception e) {

                    System.out.println(
                            "Error message element not found."
                    );

                    testPassed = false;
                }
            }

            // ==========================================
            // FINAL TEST RESULT
            // ==========================================

            if (testPassed) {

                System.out.println("RESULT: PASS");

            } else {

                System.out.println("RESULT: FAIL");
            }

        } catch (Exception e) {

            System.out.println(
                    "Execution Error: "
                            + e.getClass().getSimpleName()
            );

            System.out.println(
                    "Error Message: "
                            + e.getMessage()
            );

            System.out.println("RESULT: FAIL");
        }
    }

    private String extractExpectedError(String expectedResult) {

        if (expectedResult.contains(":")) {

            String[] parts =
                    expectedResult.split(":", 2);

            return parts[1]
                    .replace("'", "")
                    .replace("\"", "")
                    .trim();
        }

        return expectedResult.trim();
    }

    public void closeBrowser() {

        if (driver != null) {
            driver.quit();
        }
    }
}
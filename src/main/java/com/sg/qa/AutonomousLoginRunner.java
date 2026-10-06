package com.sg.qa;

import com.sg.ai.TestCase;
import com.sg.ai.TestCaseParser;
import com.sg.ai.TestCaseResponse;

public class AutonomousLoginRunner {

    public static void main(String[] args) {

        String json = """
                {
                  "testCases": [
                    {
                      "title": "Successful Login",
                      "username": "standard_user",
                      "password": "secret_sauce",
                      "steps": [
                        "Open login page",
                        "Enter username",
                        "Enter password",
                        "Click Login"
                      ],
                      "expectedResult": "Products page should be displayed"
                    }
                  ]
                }
                """;

        SeleniumExecutor executor =
                new SeleniumExecutor();

        try {

            TestCaseResponse response =
                    TestCaseParser.parse(json);

            executor.startBrowser();

            for (TestCase testCase : response.getTestCases()) {

                executor.executeLoginTest(testCase);
            }

        } catch (Exception e) {

            System.out.println("Execution failed:");
            e.printStackTrace();

        } finally {

            executor.closeBrowser();
        }
    }
}

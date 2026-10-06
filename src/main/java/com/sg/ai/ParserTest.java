package com.sg.ai;

public class ParserTest {

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

        try {

            TestCaseResponse response =
                    TestCaseParser.parse(json);

            for (TestCase testCase : response.getTestCases()) {

                System.out.println("================================");
                System.out.println("Title: " + testCase.getTitle());
                System.out.println("Username: " + testCase.getUsername());
                System.out.println("Password: " + testCase.getPassword());

                System.out.println("Steps:");

                for (String step : testCase.getSteps()) {
                    System.out.println(" - " + step);
                }

                System.out.println(
                        "Expected: " + testCase.getExpectedResult()
                );
            }

        } catch (Exception e) {

            System.out.println("JSON parsing failed:");
            e.printStackTrace();
        }
    }
}
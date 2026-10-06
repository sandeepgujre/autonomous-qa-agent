package com.sg.qa;

import com.sg.ai.TestStep;
import com.sg.ai.TestStepParser;

import java.util.List;

public class TestStepParserTest {

    public static void main(String[] args) {

        System.out.println("================================");
        System.out.println("JSON → TEST STEP PARSER TEST");
        System.out.println("================================");

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

        try {

            List<TestStep> steps =
                    TestStepParser.parse(json);

            System.out.println(
                    "\nTotal steps parsed: "
                            + steps.size()
            );

            int stepNumber = 1;

            for (TestStep step : steps) {

                System.out.println(
                        "\nStep " + stepNumber++
                );

                System.out.println(
                        "Action: "
                                + step.getAction()
                );

                System.out.println(
                        "Target: "
                                + step.getTarget()
                );

                System.out.println(
                        "Value: "
                                + step.getValue()
                );

                System.out.println(
                        "Expected: "
                                + step.getExpectedResult()
                );
            }

            System.out.println(
                    "\n================================"
            );

            System.out.println(
                    "JSON PARSING SUCCESSFUL"
            );

            System.out.println(
                    "================================"
            );

        } catch (Exception e) {

            System.out.println(
                    "\n================================"
            );

            System.out.println(
                    "JSON PARSING FAILED"
            );

            System.out.println(
                    "================================"
            );

            e.printStackTrace();
        }
    }
}
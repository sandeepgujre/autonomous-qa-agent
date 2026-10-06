package com.sg.qa;

public class GenericQAEngineTest {

    public static void main(String[] args) {

        /*
         * IMPORTANT:
         *
         * Ye URL aur test steps sirf testing fixture hain.
         * Ye GenericQAEngine ka hardcoded logic nahi hai.
         *
         * Final agent me ye data:
         *
         * Requirement + Website
         *          ↓
         *       Gemini
         *          ↓
         *     Test Steps JSON
         *
         * se aayega.
         */

        String website =
                "https://the-internet.herokuapp.com/login";

        String stepsJson = """
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

            System.out.println();
            System.out.println(
                    "=========================================="
            );

            System.out.println(
                    "GENERIC QA ENGINE TEST"
            );

            System.out.println(
                    "=========================================="
            );

            GenericQAEngine engine =
                    new GenericQAEngine();

            boolean result =
                    engine.execute(
                            website,
                            stepsJson
                    );

            System.out.println();
            System.out.println(
                    "=========================================="
            );

            System.out.println(
                    "TEST RESULT: "
                            + (
                            result
                                    ? "PASS"
                                    : "FAIL"
                    )
            );

            System.out.println(
                    "=========================================="
            );

            if (!result) {

                throw new RuntimeException(
                        "Generic QA Engine test failed."
                );
            }

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "GENERIC QA ENGINE TEST FAILED"
            );

            e.printStackTrace();

            System.exit(1);
        }
    }
}
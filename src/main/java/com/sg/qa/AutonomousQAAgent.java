package com.sg.qa;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Main entry point of the Autonomous QA Agent.
 *
 * User provides:
 * 1. Website URL
 * 2. QA requirement
 *
 * Complete flow:
 *
 * Website + Requirement
 *        ↓
 * AI Test Generation
 *        ↓
 * Generic QA Engine
 *        ↓
 * Selenium Execution
 *        ↓
 * Local Diagnosis
 *        ↓
 * Local Self-Healing
 *        ↓
 * Gemini Fallback
 *        ↓
 * Execution Report
 *        ↓
 * JSON + HTML + Excel Reports
 */
public class AutonomousQAAgent {

    private static final String REPORTS_DIRECTORY =
            "reports";

    private static final String REQUIREMENT_END_MARKER =
            "END";

    private static final DateTimeFormatter
            RUN_DIRECTORY_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd_HH-mm-ss"
            );

    public static void main(String[] args) {

        System.out.println();
        System.out.println(
                "================================================"
        );

        System.out.println(
                "          AUTONOMOUS QA AGENT"
        );

        System.out.println(
                "================================================"
        );

        Scanner scanner =
                new Scanner(System.in);

        try {

            /*
             * ------------------------------------------------
             * STEP 1
             * ------------------------------------------------
             */
            System.out.println();
            System.out.println(
                    "Enter Website URL:"
            );

            System.out.print("> ");

            String website =
                    scanner.nextLine().trim();

            if (website.isBlank()) {

                throw new IllegalArgumentException(
                        "Website URL cannot be empty."
                );
            }

            /*
             * ------------------------------------------------
             * STEP 2
             * ------------------------------------------------
             *
             * QA Requirement supports:
             *
             * - Multiple lines
             * - Blank lines
             * - Natural language formatting
             *
             * Requirement ends only when the user enters:
             *
             * END
             */
            System.out.println();
            System.out.println(
                    "Enter QA Requirement:"
            );

            System.out.println(
                    "(You can describe the complete test scenario "
                            + "using multiple lines.)"
            );

            System.out.println(
                    "(Type END on a separate line when finished.)"
            );

            System.out.print("> ");

            String requirement =
                    readMultiLineRequirement(scanner);

            if (requirement.isBlank()) {

                throw new IllegalArgumentException(
                        "QA requirement cannot be empty."
                );
            }

            /*
             * Display exact test input.
             */
            System.out.println();
            System.out.println(
                    "------------------------------------------------"
            );

            System.out.println(
                    "TEST URL:"
            );

            System.out.println(
                    website
            );

            System.out.println();
            System.out.println(
                    "QA REQUIREMENT:"
            );

            System.out.println(
                    requirement
            );

            System.out.println(
                    "------------------------------------------------"
            );

            /*
             * ------------------------------------------------
             * STEP 3
             * ------------------------------------------------
             */
            System.out.println();
            System.out.println(
                    "[1] Generating test steps using AI..."
            );

            GeminiTestStepGenerator generator =
                    new GeminiTestStepGenerator();

            String stepsJson =
                    generator.generateTestSteps(
                            website,
                            requirement
                    );

            System.out.println();
            System.out.println(
                    "AI generated test steps received."
            );

            /*
             * Intentionally do not print the complete
             * generated JSON to keep terminal output concise.
             */

            /*
             * ------------------------------------------------
             * STEP 4
             * ------------------------------------------------
             */
            System.out.println();
            System.out.println(
                    "[2] Executing generated test..."
            );

            GenericQAEngine engine =
                    new GenericQAEngine();

            boolean result =
                    engine.execute(
                            website,
                            stepsJson
                    );

            /*
             * ------------------------------------------------
             * STEP 5
             * ------------------------------------------------
             */
            System.out.println();
            System.out.println(
                    "[3] Preparing execution reports..."
            );

            ExecutionReport executionReport =
                    engine.getLastExecutionReport();

            /*
             * ------------------------------------------------
             * STEP 6
             * ------------------------------------------------
             *
             * Create one unique folder for this complete run.
             */
            if (executionReport != null) {

                File runDirectory =
                        createRunReportDirectory();

                System.out.println();
                System.out.println(
                        "[REPORT] Run report directory:"
                );

                System.out.println(
                        runDirectory.getAbsolutePath()
                );

                /*
                 * JSON REPORT
                 */
                JsonReportGenerator
                        jsonReportGenerator =
                        new JsonReportGenerator();

                String jsonReportPath =
                        jsonReportGenerator.generate(
                                executionReport,
                                runDirectory
                        );

                /*
                 * HTML REPORT
                 */
                HtmlReportGenerator
                        htmlReportGenerator =
                        new HtmlReportGenerator();

                String htmlReportPath =
                        htmlReportGenerator.generate(
                                executionReport,
                                runDirectory
                        );

                /*
                 * EXCEL REPORT
                 */
                ExcelReportGenerator
                        excelReportGenerator =
                        new ExcelReportGenerator();

                String excelReportPath =
                        excelReportGenerator.generate(
                                executionReport,
                                runDirectory
                        );

                /*
                 * Display generated reports.
                 */
                System.out.println();
                System.out.println(
                        "[4] Reports generated successfully:"
                );

                System.out.println();
                System.out.println(
                        "JSON  : "
                                + jsonReportPath
                );

                System.out.println(
                        "HTML  : "
                                + htmlReportPath
                );

                System.out.println(
                        "Excel : "
                                + excelReportPath
                );

            } else {

                System.out.println();
                System.out.println(
                        "[4] Reports skipped because "
                                + "execution report is unavailable."
                );
            }

            /*
             * ------------------------------------------------
             * FINAL RESULT
             * ------------------------------------------------
             */
            System.out.println();
            System.out.println(
                    "================================================"
            );

            System.out.println(
                    "AUTONOMOUS QA AGENT RESULT: "
                            + (
                            result
                                    ? "PASS"
                                    : "FAIL"
                    )
            );

            System.out.println(
                    "================================================"
            );

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "================================================"
            );

            System.out.println(
                    "AUTONOMOUS QA AGENT ERROR"
            );

            System.out.println(
                    "================================================"
            );

            e.printStackTrace();

        } finally {

            scanner.close();
        }

        System.out.println();
        System.out.println(
                "Autonomous QA Agent execution completed."
        );
    }

    /**
     * Reads a multi-line QA requirement.
     *
     * Blank lines are preserved.
     *
     * The requirement ends only when the user enters
     * the dedicated END marker on its own line.
     *
     * Example:
     *
     * Perform this QA test:
     *
     * 1. Open login page.
     *
     * 2. Enter username.
     * 3. Enter password.
     *
     * END
     *
     * The END marker is not included in the requirement.
     */
    private static String readMultiLineRequirement(
            Scanner scanner) {

        StringBuilder requirement =
                new StringBuilder();

        while (scanner.hasNextLine()) {

            String line =
                    scanner.nextLine();

            /*
             * END marker terminates the requirement.
             *
             * Comparison is case-insensitive and ignores
             * surrounding spaces.
             */
            if (REQUIREMENT_END_MARKER.equalsIgnoreCase(
                    line.trim())) {

                break;
            }

            /*
             * Preserve the user's original line structure,
             * including blank lines.
             */
            if (requirement.length() > 0) {

                requirement.append(
                        System.lineSeparator()
                );
            }

            requirement.append(line);
        }

        return requirement
                .toString()
                .trim();
    }

    /**
     * Creates a unique timestamped directory for one
     * complete agent execution.
     *
     * Example:
     *
     * reports/
     *     2026-10-05_10-30-15/
     */
    private static File createRunReportDirectory() {

        File reportsDirectory =
                new File(
                        REPORTS_DIRECTORY
                );

        if (!reportsDirectory.exists()) {

            boolean created =
                    reportsDirectory.mkdirs();

            if (!created) {

                throw new IllegalStateException(
                        "Unable to create reports directory: "
                                + reportsDirectory
                                .getAbsolutePath()
                );
            }
        }

        String baseDirectoryName =
                LocalDateTime.now()
                        .format(
                                RUN_DIRECTORY_FORMATTER
                        );

        File runDirectory =
                new File(
                        reportsDirectory,
                        baseDirectoryName
                );

        /*
         * Protect against two runs starting in the same
         * second.
         */
        int suffix = 1;

        while (runDirectory.exists()) {

            runDirectory =
                    new File(
                            reportsDirectory,
                            baseDirectoryName
                                    + "-"
                                    + suffix
                    );

            suffix++;
        }

        boolean created =
                runDirectory.mkdirs();

        if (!created) {

            throw new IllegalStateException(
                    "Unable to create run report directory: "
                            + runDirectory
                            .getAbsolutePath()
            );
        }

        return runDirectory;
    }
}
package com.sg.qa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates a machine-readable JSON report
 * from the final ExecutionReport.
 *
 * This class does not execute Selenium
 * and does not call Gemini.
 */
public class JsonReportGenerator {

    private static final String REPORT_FILE =
            "execution-report.json";

    /**
     * Generates the JSON report inside the supplied
     * run-specific directory.
     *
     * @param report complete execution report
     * @param reportDirectory unique directory for this run
     * @return absolute path of generated JSON file
     */
    public String generate(
            ExecutionReport report,
            File reportDirectory) throws Exception {

        validateInputs(
                report,
                reportDirectory
        );

        File outputFile =
                new File(
                        reportDirectory,
                        REPORT_FILE
                );

        ObjectMapper objectMapper =
                new ObjectMapper();

        objectMapper.enable(
                SerializationFeature.INDENT_OUTPUT
        );

        Map<String, Object> reportData =
                new LinkedHashMap<>();

        reportData.put(
                "website",
                report.getWebsite()
        );

        reportData.put(
                "overallStatus",
                report.getOverallStatus()
        );

        reportData.put(
                "startTime",
                report.getStartTime() != null
                        ? report.getStartTime().toString()
                        : null
        );

        reportData.put(
                "endTime",
                report.getEndTime() != null
                        ? report.getEndTime().toString()
                        : null
        );

        reportData.put(
                "generatedSteps",
                report.getGeneratedSteps()
        );

        reportData.put(
                "executedSteps",
                report.getExecutedSteps()
        );

        reportData.put(
                "passedSteps",
                report.getPassedSteps()
        );

        reportData.put(
                "failedSteps",
                report.getFailedSteps()
        );

        reportData.put(
                "blockedSteps",
                report.getBlockedSteps()
        );

        reportData.put(
                "skippedSteps",
                report.getSkippedSteps()
        );

        /*
         * Backward compatibility.
         */
        reportData.put(
                "totalSteps",
                report.getTotalSteps()
        );

        /*
         * Detailed results include SKIPPED steps.
         */
        reportData.put(
                "stepResults",
                report.getStepResults()
        );

        objectMapper.writeValue(
                outputFile,
                reportData
        );

        System.out.println(
                "[REPORT] JSON report generated."
        );

        return outputFile.getAbsolutePath();
    }

    /**
     * Backward-compatible generate method.
     *
     * Creates a standalone timestamped report directory
     * when this generator is used directly.
     */
    public String generate(
            ExecutionReport report) throws Exception {

        File reportDirectory =
                createStandaloneReportDirectory();

        return generate(
                report,
                reportDirectory
        );
    }

    /**
     * Validates generator input.
     */
    private void validateInputs(
            ExecutionReport report,
            File reportDirectory) {

        if (report == null) {

            throw new IllegalArgumentException(
                    "ExecutionReport cannot be null."
            );
        }

        if (reportDirectory == null) {

            throw new IllegalArgumentException(
                    "Report directory cannot be null."
            );
        }

        if (!reportDirectory.exists()
                && !reportDirectory.mkdirs()) {

            throw new IllegalStateException(
                    "Unable to create report directory: "
                            + reportDirectory
                            .getAbsolutePath()
            );
        }

        if (!reportDirectory.isDirectory()) {

            throw new IllegalArgumentException(
                    "Report path is not a directory: "
                            + reportDirectory
                            .getAbsolutePath()
            );
        }
    }

    /**
     * Creates a timestamped directory for direct usage.
     */
    private File createStandaloneReportDirectory() {

        File reportsDirectory =
                new File("reports");

        if (!reportsDirectory.exists()
                && !reportsDirectory.mkdirs()) {

            throw new IllegalStateException(
                    "Unable to create reports directory."
            );
        }

        String directoryName =
                java.time.LocalDateTime.now()
                        .format(
                                java.time.format.DateTimeFormatter
                                        .ofPattern(
                                                "yyyy-MM-dd_HH-mm-ss"
                                        )
                        );

        File directory =
                new File(
                        reportsDirectory,
                        directoryName
                );

        int suffix = 1;

        while (directory.exists()) {

            directory =
                    new File(
                            reportsDirectory,
                            directoryName
                                    + "-"
                                    + suffix
                    );

            suffix++;
        }

        if (!directory.mkdirs()) {

            throw new IllegalStateException(
                    "Unable to create report directory: "
                            + directory.getAbsolutePath()
            );
        }

        return directory;
    }
}
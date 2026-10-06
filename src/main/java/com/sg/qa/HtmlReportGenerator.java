package com.sg.qa;

import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;

/**
 * Generates a human-readable HTML report
 * from the final ExecutionReport.
 *
 * This class does not execute Selenium
 * and does not call Gemini.
 *
 * Reports are written into the run-specific
 * directory supplied by the execution layer.
 */
public class HtmlReportGenerator {

    private static final String REPORT_ROOT_DIRECTORY =
            "reports";

    private static final String REPORT_FILE =
            "execution-report.html";

    private static final DateTimeFormatter
            DATE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            );

    /**
     * Generates the HTML execution report
     * inside the supplied run directory.
     *
     * @param report complete execution report
     * @param reportDirectory timestamped run directory
     * @return absolute path of generated HTML file
     */
    public String generate(
            ExecutionReport report,
            File reportDirectory) throws Exception {

        validateReport(report);
        validateReportDirectory(reportDirectory);

        File outputFile =
                new File(
                        reportDirectory,
                        REPORT_FILE
                );

        String html =
                buildHtml(report);

        try (FileWriter writer =
                     new FileWriter(outputFile)) {

            writer.write(html);
        }

        System.out.println();
        System.out.println(
                "[REPORT] HTML report generated:"
        );

        System.out.println(
                outputFile.getAbsolutePath()
        );

        return outputFile.getAbsolutePath();
    }

    /**
     * Backward-compatible report generation.
     *
     * Creates a standalone timestamped directory
     * when the caller does not supply a run directory.
     *
     * @param report complete execution report
     * @return absolute path of generated HTML file
     */
    public String generate(
            ExecutionReport report) throws Exception {

        validateReport(report);

        File runDirectory =
                createStandaloneRunDirectory();

        return generate(
                report,
                runDirectory
        );
    }

    /**
     * Validates the execution report.
     */
    private void validateReport(
            ExecutionReport report) {

        if (report == null) {

            throw new IllegalArgumentException(
                    "ExecutionReport cannot be null."
            );
        }
    }

    /**
     * Validates and creates the supplied
     * timestamped report directory if required.
     */
    private void validateReportDirectory(
            File reportDirectory) {

        if (reportDirectory == null) {

            throw new IllegalArgumentException(
                    "Report directory cannot be null."
            );
        }

        if (reportDirectory.exists()) {

            if (!reportDirectory.isDirectory()) {

                throw new IllegalStateException(
                        "Report path is not a directory: "
                                + reportDirectory
                                        .getAbsolutePath()
                );
            }

            return;
        }

        boolean created =
                reportDirectory.mkdirs();

        if (!created
                && !reportDirectory.isDirectory()) {

            throw new IllegalStateException(
                    "Unable to create report directory: "
                            + reportDirectory
                                    .getAbsolutePath()
            );
        }
    }

    /**
     * Creates a timestamped directory for
     * backward-compatible standalone generation.
     */
    private File createStandaloneRunDirectory() {

        File rootDirectory =
                new File(
                        REPORT_ROOT_DIRECTORY
                );

        if (!rootDirectory.exists()) {

            boolean created =
                    rootDirectory.mkdirs();

            if (!created
                    && !rootDirectory.isDirectory()) {

                throw new IllegalStateException(
                        "Unable to create reports directory: "
                                + rootDirectory
                                        .getAbsolutePath()
                );
            }
        }

        String timestamp =
                java.time.LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "yyyy-MM-dd_HH-mm-ss"
                                )
                        );

        File runDirectory =
                new File(
                        rootDirectory,
                        timestamp
                );

        int suffix = 1;

        while (runDirectory.exists()) {

            runDirectory =
                    new File(
                            rootDirectory,
                            timestamp
                                    + "_"
                                    + suffix
                    );

            suffix++;
        }

        boolean created =
                runDirectory.mkdirs();

        if (!created
                && !runDirectory.isDirectory()) {

            throw new IllegalStateException(
                    "Unable to create timestamped report directory: "
                            + runDirectory
                                    .getAbsolutePath()
            );
        }

        return runDirectory;
    }

    /**
     * Builds the complete HTML document.
     */
    private String buildHtml(
            ExecutionReport report) {

        StringBuilder html =
                new StringBuilder();

        String status =
                safe(report.getOverallStatus());

        String statusClass =
                "PASS".equalsIgnoreCase(status)
                        ? "pass"
                        : "fail";

        html.append("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                         content="width=device-width,
                         initial-scale=1.0">

                    <title>Autonomous QA Test Report</title>

                    <style>

                        body {
                            font-family: Arial, sans-serif;
                            margin: 0;
                            padding: 30px;
                            background: #f4f6f8;
                        }

                        .container {
                            max-width: 1200px;
                            margin: auto;
                        }

                        .header {
                            background: #1f2937;
                            color: white;
                            padding: 25px;
                            border-radius: 10px;
                            margin-bottom: 20px;
                        }

                        .header h1 {
                            margin: 0 0 10px 0;
                        }

                        .website {
                            word-break: break-all;
                            opacity: 0.9;
                        }

                        .status {
                            display: inline-block;
                            margin-top: 15px;
                            padding: 8px 18px;
                            border-radius: 20px;
                            font-weight: bold;
                        }

                        .pass {
                            background: #d1fae5;
                            color: #065f46;
                        }

                        .fail {
                            background: #fee2e2;
                            color: #991b1b;
                        }

                        .summary {
                            display: grid;
                            grid-template-columns:
                                repeat(
                                    auto-fit,
                                    minmax(150px, 1fr)
                                );
                            gap: 15px;
                            margin-bottom: 20px;
                        }

                        .card {
                            background: white;
                            padding: 20px;
                            border-radius: 10px;
                            box-shadow:
                                0 2px 8px
                                rgba(0,0,0,0.08);
                        }

                        .card h3 {
                            margin-top: 0;
                            color: #6b7280;
                            font-size: 14px;
                        }

                        .card .number {
                            font-size: 30px;
                            font-weight: bold;
                            color: #111827;
                        }

                        .section {
                            background: white;
                            padding: 20px;
                            border-radius: 10px;
                            margin-bottom: 20px;
                            box-shadow:
                                0 2px 8px
                                rgba(0,0,0,0.08);
                        }

                        table {
                            width: 100%;
                            border-collapse:
                                collapse;
                        }

                        th {
                            background: #f3f4f6;
                            text-align: left;
                            padding: 12px;
                            border-bottom:
                                2px solid #d1d5db;
                        }

                        td {
                            padding: 12px;
                            border-bottom:
                                1px solid #e5e7eb;
                            vertical-align: top;
                        }

                        .badge {
                            display: inline-block;
                            padding: 5px 10px;
                            border-radius: 15px;
                            font-size: 12px;
                            font-weight: bold;
                        }

                        .badge-pass {
                            background: #d1fae5;
                            color: #065f46;
                        }

                        .badge-fail {
                            background: #fee2e2;
                            color: #991b1b;
                        }

                        .badge-blocked {
                            background: #fef3c7;
                            color: #92400e;
                        }

                        .badge-skipped {
                            background: #e0e7ff;
                            color: #3730a3;
                        }

                        .badge-na {
                            background: #e5e7eb;
                            color: #374151;
                        }

                        .small {
                            font-size: 13px;
                            color: #6b7280;
                        }

                        .error {
                            color: #991b1b;
                            font-size: 13px;
                        }

                        .footer {
                            text-align: center;
                            color: #6b7280;
                            font-size: 13px;
                            margin-top: 25px;
                        }

                        @media (max-width: 700px) {

                            body {
                                padding: 15px;
                            }

                            table {
                                font-size: 13px;
                            }

                            th,
                            td {
                                padding: 8px;
                            }
                        }

                    </style>
                </head>

                <body>

                <div class="container">

                <div class="header">

                    <h1>
                        Autonomous QA Test Report
                    </h1>

                    <div class="website">
                        Website:
                """);

        html.append(
                escapeHtml(
                        safe(report.getWebsite())
                )
        );

        html.append("""
                    </div>

                    <div class="status
                """);

        html.append(statusClass);

        html.append("\">");

        html.append(
                escapeHtml(status)
        );

        html.append("""
                    </div>

                </div>

                <!-- Execution Summary -->

                <div class="summary">

                    <div class="card">
                        <h3>Generated</h3>
                        <div class="number">
                """);

        html.append(report.getGeneratedSteps());

        html.append("""
                        </div>
                    </div>

                    <div class="card">
                        <h3>Executed</h3>
                        <div class="number">
                """);

        html.append(report.getExecutedSteps());

        html.append("""
                        </div>
                    </div>

                    <div class="card">
                        <h3>Passed</h3>
                        <div class="number">
                """);

        html.append(report.getPassedSteps());

        html.append("""
                        </div>
                    </div>

                    <div class="card">
                        <h3>Failed</h3>
                        <div class="number">
                """);

        html.append(report.getFailedSteps());

        html.append("""
                        </div>
                    </div>

                    <div class="card">
                        <h3>Blocked</h3>
                        <div class="number">
                """);

        html.append(report.getBlockedSteps());

        html.append("""
                        </div>
                    </div>

                    <div class="card">
                        <h3>Skipped</h3>
                        <div class="number">
                """);

        html.append(report.getSkippedSteps());

        html.append("""
                        </div>
                    </div>

                </div>

                <div class="section">

                    <h2>Execution Details</h2>

                    <p>
                        <strong>Generated Steps:</strong>
                """);

        html.append(report.getGeneratedSteps());

        html.append("""
                    </p>

                    <p>
                        <strong>Executed Steps:</strong>
                """);

        html.append(report.getExecutedSteps());

        html.append("""
                    </p>

                    <p>
                        <strong>Skipped Steps:</strong>
                """);

        html.append(report.getSkippedSteps());

        html.append("""
                    </p>

                    <p>
                        <strong>Start Time:</strong>
                        """);

        html.append(
                formatDateTime(
                        report.getStartTime()
                )
        );

        html.append("""
                    </p>

                    <p>
                        <strong>End Time:</strong>
                        """);

        html.append(
                formatDateTime(
                        report.getEndTime()
                )
        );

        html.append("""
                    </p>

                </div>

                <div class="section">

                    <h2>Step Execution Details</h2>

                    <table>

                        <thead>

                            <tr>
                                <th>Step</th>
                                <th>Action</th>
                                <th>Target</th>
                                <th>Value</th>
                                <th>Status</th>
                                <th>Recovery</th>
                                <th>Message</th>
                            </tr>

                        </thead>

                        <tbody>
                """);

        if (report.getStepResults() != null
                && !report.getStepResults().isEmpty()) {

            for (StepExecutionResult result :
                    report.getStepResults()) {

                if (result == null) {
                    continue;
                }

                html.append("<tr>");

                html.append("<td>");
                html.append(result.getStepNumber());
                html.append("</td>");

                html.append("<td>");
                html.append(
                        escapeHtml(
                                safe(result.getAction())
                        )
                );
                html.append("</td>");

                html.append("<td>");
                html.append(
                        escapeHtml(
                                safe(result.getTarget())
                        )
                );
                html.append("</td>");

                html.append("<td>");
                html.append(
                        escapeHtml(
                                safe(result.getValue())
                        )
                );
                html.append("</td>");

                html.append("<td>");
                html.append(
                        createStatusBadge(
                                result.getStatus()
                        )
                );
                html.append("</td>");

                html.append("<td>");

                boolean localHealing =
                        result.isLocalHealingSuccessful();

                boolean geminiRecovery =
                        result.isGeminiRecoverySuccessful();

                if (localHealing) {

                    html.append(
                            "<span class=\"badge badge-pass\">"
                                    + "Local Healing SUCCESS"
                                    + "</span>"
                    );

                } else if (geminiRecovery) {

                    html.append(
                            "<span class=\"badge badge-pass\">"
                                    + "Gemini Recovery SUCCESS"
                                    + "</span>"
                    );

                } else if (
                        result.isLocalHealingAttempted()
                                || result
                                .isGeminiRecoveryAttempted()) {

                    html.append(
                            "<span class=\"badge badge-fail\">"
                                    + "Recovery Failed"
                                    + "</span>"
                    );

                } else if (
                        "SKIPPED".equalsIgnoreCase(
                                safe(result.getStatus())
                        )) {

                    html.append(
                            "<span class=\"badge badge-skipped\">"
                                    + "Skipped"
                                    + "</span>"
                    );

                } else {

                    html.append(
                            "<span class=\"badge badge-na\">"
                                    + "Not Needed"
                                    + "</span>"
                    );
                }

                html.append("</td>");

                html.append("<td>");

                String message =
                        safe(
                                result.getFinalMessage()
                        );

                if (!message.isEmpty()) {

                    html.append(
                            escapeHtml(message)
                    );

                } else {

                    html.append(
                            "<span class=\"small\">"
                                    + "No message"
                                    + "</span>"
                    );
                }

                if (result.getFailureCategory()
                        != null
                        && !result
                        .getFailureCategory()
                        .isBlank()) {

                    html.append("<br><br>");

                    html.append(
                            "<span class=\"error\">"
                    );

                    html.append(
                            "<strong>Failure Category:</strong> "
                    );

                    html.append(
                            escapeHtml(
                                    result
                                            .getFailureCategory()
                            )
                    );

                    html.append("</span>");
                }

                if (result.getFailureReason()
                        != null
                        && !result
                        .getFailureReason()
                        .isBlank()) {

                    html.append("<br>");

                    html.append(
                            "<span class=\"error\">"
                    );

                    html.append(
                            "<strong>Reason:</strong> "
                    );

                    html.append(
                            escapeHtml(
                                    result
                                            .getFailureReason()
                            )
                    );

                    html.append("</span>");
                }

                html.append("</td>");

                html.append("</tr>");
            }

        } else {

            html.append("""
                        <tr>
                            <td colspan="7"
                                class="small">
                                No step results available.
                            </td>
                        </tr>
                    """);
        }

        html.append("""
                        </tbody>

                    </table>

                </div>

                <div class="section">

                    <h2>Self-Healing Summary</h2>

                    <table>

                        <thead>

                            <tr>
                                <th>Step</th>
                                <th>Local Healing</th>
                                <th>Gemini Recovery</th>
                            </tr>

                        </thead>

                        <tbody>
                """);

        if (report.getStepResults() != null
                && !report.getStepResults().isEmpty()) {

            for (StepExecutionResult result :
                    report.getStepResults()) {

                if (result == null) {
                    continue;
                }

                html.append("<tr>");

                html.append("<td>");
                html.append(result.getStepNumber());
                html.append("</td>");

                html.append("<td>");
                html.append(
                        createRecoveryStatus(
                                result
                                        .isLocalHealingAttempted(),
                                result
                                        .isLocalHealingSuccessful()
                        )
                );
                html.append("</td>");

                html.append("<td>");
                html.append(
                        createRecoveryStatus(
                                result
                                        .isGeminiRecoveryAttempted(),
                                result
                                        .isGeminiRecoverySuccessful()
                        )
                );
                html.append("</td>");

                html.append("</tr>");
            }

        } else {

            html.append("""
                        <tr>
                            <td colspan="3"
                                class="small">
                                No recovery data available.
                            </td>
                        </tr>
                    """);
        }

        html.append("""
                        </tbody>

                    </table>

                </div>

                <div class="footer">

                    Generated by
                    <strong>Autonomous QA Agent</strong>

                </div>

                </div>

                </body>
                </html>
                """);

        return html.toString();
    }

    /**
     * Creates an HTML status badge.
     */
    private String createStatusBadge(
            String status) {

        String safeStatus =
                safe(status);

        String cssClass;

        if ("PASS".equalsIgnoreCase(
                safeStatus)) {

            cssClass = "badge-pass";

        } else if ("BLOCKED".equalsIgnoreCase(
                safeStatus)) {

            cssClass = "badge-blocked";

        } else if ("FAIL".equalsIgnoreCase(
                safeStatus)) {

            cssClass = "badge-fail";

        } else if ("SKIPPED".equalsIgnoreCase(
                safeStatus)) {

            cssClass = "badge-skipped";

        } else {

            cssClass = "badge-na";
        }

        return
                "<span class=\"badge "
                        + cssClass
                        + "\">"
                        + escapeHtml(safeStatus)
                        + "</span>";
    }

    /**
     * Creates recovery status text.
     */
    private String createRecoveryStatus(
            boolean attempted,
            boolean successful) {

        if (!attempted) {

            return
                    "<span class=\"badge badge-na\">"
                            + "Not Needed"
                            + "</span>";
        }

        if (successful) {

            return
                    "<span class=\"badge badge-pass\">"
                            + "SUCCESS"
                            + "</span>";
        }

        return
                "<span class=\"badge badge-fail\">"
                        + "FAILED"
                        + "</span>";
    }

    /**
     * Safely formats LocalDateTime.
     */
    private String formatDateTime(
            java.time.LocalDateTime dateTime) {

        if (dateTime == null) {
            return "N/A";
        }

        return dateTime.format(
                DATE_FORMATTER
        );
    }

    /**
     * Prevents null values from appearing
     * as the literal string "null".
     */
    private String safe(String value) {

        return value == null
                ? ""
                : value;
    }

    /**
     * Basic HTML escaping to prevent
     * report corruption from special characters.
     */
    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
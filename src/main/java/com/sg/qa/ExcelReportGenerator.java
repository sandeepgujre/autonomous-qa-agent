package com.sg.qa;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generates an Excel execution report
 * from the final ExecutionReport.
 *
 * This class does not execute Selenium
 * and does not call Gemini.
 *
 * Reports are written into the run-specific
 * directory supplied by the execution layer.
 */
public class ExcelReportGenerator {

    private static final String REPORT_ROOT_DIRECTORY =
            "reports";

    private static final String REPORT_FILE =
            "execution-report.xlsx";

    private static final DateTimeFormatter
            RUN_DIRECTORY_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd_HH-mm-ss"
            );

    /**
     * Generates the Excel execution report
     * inside the supplied run directory.
     *
     * @param report complete execution report
     * @param reportDirectory timestamped run directory
     * @return absolute path of generated Excel file
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

        try (Workbook workbook =
                     new XSSFWorkbook()) {

            /*
             * ------------------------------------------------
             * SUMMARY SHEET
             * ------------------------------------------------
             */
            Sheet summarySheet =
                    workbook.createSheet(
                            "Summary"
                    );

            createSummarySheet(
                    summarySheet,
                    report
            );

            /*
             * ------------------------------------------------
             * STEP RESULTS SHEET
             * ------------------------------------------------
             */
            Sheet stepsSheet =
                    workbook.createSheet(
                            "Step Results"
                    );

            createStepResultsSheet(
                    stepsSheet,
                    report
            );

            /*
             * ------------------------------------------------
             * RECOVERY SHEET
             * ------------------------------------------------
             */
            Sheet recoverySheet =
                    workbook.createSheet(
                            "Self Healing"
                    );

            createRecoverySheet(
                    recoverySheet,
                    report
            );

            /*
             * Write workbook to disk.
             */
            try (FileOutputStream outputStream =
                         new FileOutputStream(
                                 outputFile
                         )) {

                workbook.write(outputStream);
            }
        }

        System.out.println();
        System.out.println(
                "[REPORT] Excel report generated:"
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
     * @return absolute path of generated Excel file
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
                LocalDateTime.now()
                        .format(
                                RUN_DIRECTORY_FORMATTER
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
     * Creates the Summary worksheet.
     */
    private void createSummarySheet(
            Sheet sheet,
            ExecutionReport report) {

        CellStyle headerStyle =
                createHeaderStyle(
                        sheet.getWorkbook()
                );

        CellStyle valueStyle =
                createValueStyle(
                        sheet.getWorkbook()
                );

        int rowNumber = 0;

        Row titleRow =
                sheet.createRow(rowNumber++);

        Cell titleCell =
                titleRow.createCell(0);

        titleCell.setCellValue(
                "Autonomous QA Execution Report"
        );

        titleCell.setCellStyle(
                headerStyle
        );

        rowNumber++;

        addSummaryRow(
                sheet,
                rowNumber++,
                "Website",
                safe(report.getWebsite()),
                valueStyle
        );

        addSummaryRow(
                sheet,
                rowNumber++,
                "Overall Status",
                safe(report.getOverallStatus()),
                valueStyle
        );

        addSummaryRow(
                sheet,
                rowNumber++,
                "Start Time",
                report.getStartTime() != null
                        ? report.getStartTime().toString()
                        : "N/A",
                valueStyle
        );

        addSummaryRow(
                sheet,
                rowNumber++,
                "End Time",
                report.getEndTime() != null
                        ? report.getEndTime().toString()
                        : "N/A",
                valueStyle
        );

        rowNumber++;

        /*
         * ------------------------------------------------
         * EXECUTION SUMMARY
         * ------------------------------------------------
         */

        addSummaryRow(
                sheet,
                rowNumber++,
                "Generated Steps",
                String.valueOf(
                        report.getGeneratedSteps()
                ),
                valueStyle
        );

        addSummaryRow(
                sheet,
                rowNumber++,
                "Executed Steps",
                String.valueOf(
                        report.getExecutedSteps()
                ),
                valueStyle
        );

        addSummaryRow(
                sheet,
                rowNumber++,
                "Passed Steps",
                String.valueOf(
                        report.getPassedSteps()
                ),
                valueStyle
        );

        addSummaryRow(
                sheet,
                rowNumber++,
                "Failed Steps",
                String.valueOf(
                        report.getFailedSteps()
                ),
                valueStyle
        );

        addSummaryRow(
                sheet,
                rowNumber++,
                "Blocked Steps",
                String.valueOf(
                        report.getBlockedSteps()
                ),
                valueStyle
        );

        addSummaryRow(
                sheet,
                rowNumber++,
                "Skipped Steps",
                String.valueOf(
                        report.getSkippedSteps()
                ),
                valueStyle
        );

        /*
         * Keep Total Steps for backward compatibility.
         * It represents generated steps.
         */
        addSummaryRow(
                sheet,
                rowNumber++,
                "Total Steps",
                String.valueOf(
                        report.getTotalSteps()
                ),
                valueStyle
        );

        sheet.setColumnWidth(
                0,
                30 * 256
        );

        sheet.setColumnWidth(
                1,
                70 * 256
        );
    }

    /**
     * Creates the detailed Step Results worksheet.
     */
    private void createStepResultsSheet(
            Sheet sheet,
            ExecutionReport report) {

        CellStyle headerStyle =
                createHeaderStyle(
                        sheet.getWorkbook()
                );

        Row headerRow =
                sheet.createRow(0);

        String[] headers = {
                "Step",
                "Action",
                "Target",
                "Value",
                "Expected Result",
                "Status",
                "Failure Category",
                "Failure Reason",
                "Final Message"
        };

        for (int i = 0;
             i < headers.length;
             i++) {

            Cell cell =
                    headerRow.createCell(i);

            cell.setCellValue(
                    headers[i]
            );

            cell.setCellStyle(
                    headerStyle
            );
        }

        int rowNumber = 1;

        if (report.getStepResults() != null) {

            for (StepExecutionResult result :
                    report.getStepResults()) {

                if (result == null) {
                    continue;
                }

                Row row =
                        sheet.createRow(
                                rowNumber++
                        );

                createCell(
                        row,
                        0,
                        String.valueOf(
                                result
                                        .getStepNumber()
                        )
                );

                createCell(
                        row,
                        1,
                        safe(
                                result.getAction()
                        )
                );

                createCell(
                        row,
                        2,
                        safe(
                                result.getTarget()
                        )
                );

                createCell(
                        row,
                        3,
                        safe(
                                result.getValue()
                        )
                );

                createCell(
                        row,
                        4,
                        safe(
                                result
                                        .getExpectedResult()
                        )
                );

                createCell(
                        row,
                        5,
                        safe(
                                result.getStatus()
                        )
                );

                createCell(
                        row,
                        6,
                        safe(
                                result
                                        .getFailureCategory()
                        )
                );

                createCell(
                        row,
                        7,
                        safe(
                                result
                                        .getFailureReason()
                        )
                );

                createCell(
                        row,
                        8,
                        safe(
                                result
                                        .getFinalMessage()
                        )
                );
            }
        }

        /*
         * Make columns readable.
         */
        int[] widths = {
                10,
                18,
                30,
                30,
                35,
                15,
                25,
                45,
                50
        };

        for (int i = 0;
             i < widths.length;
             i++) {

            sheet.setColumnWidth(
                    i,
                    widths[i] * 256
            );
        }

        /*
         * Freeze the header row.
         */
        sheet.createFreezePane(
                0,
                1
        );
    }

    /**
     * Creates the Self Healing worksheet.
     */
    private void createRecoverySheet(
            Sheet sheet,
            ExecutionReport report) {

        CellStyle headerStyle =
                createHeaderStyle(
                        sheet.getWorkbook()
                );

        Row headerRow =
                sheet.createRow(0);

        String[] headers = {
                "Step",
                "Action",
                "Status",
                "Local Healing Attempted",
                "Local Healing Successful",
                "Gemini Recovery Attempted",
                "Gemini Recovery Successful"
        };

        for (int i = 0;
             i < headers.length;
             i++) {

            Cell cell =
                    headerRow.createCell(i);

            cell.setCellValue(
                    headers[i]
            );

            cell.setCellStyle(
                    headerStyle
            );
        }

        int rowNumber = 1;

        if (report.getStepResults() != null) {

            for (StepExecutionResult result :
                    report.getStepResults()) {

                if (result == null) {
                    continue;
                }

                Row row =
                        sheet.createRow(
                                rowNumber++
                        );

                createCell(
                        row,
                        0,
                        String.valueOf(
                                result
                                        .getStepNumber()
                        )
                );

                createCell(
                        row,
                        1,
                        safe(
                                result.getAction()
                        )
                );

                createCell(
                        row,
                        2,
                        safe(
                                result.getStatus()
                        )
                );

                createCell(
                        row,
                        3,
                        String.valueOf(
                                result
                                        .isLocalHealingAttempted()
                        )
                );

                createCell(
                        row,
                        4,
                        String.valueOf(
                                result
                                        .isLocalHealingSuccessful()
                        )
                );

                createCell(
                        row,
                        5,
                        String.valueOf(
                                result
                                        .isGeminiRecoveryAttempted()
                        )
                );

                createCell(
                        row,
                        6,
                        String.valueOf(
                                result
                                        .isGeminiRecoverySuccessful()
                        )
                );
            }
        }

        int[] widths = {
                10,
                20,
                15,
                25,
                30,
                30,
                35
        };

        for (int i = 0;
             i < widths.length;
             i++) {

            sheet.setColumnWidth(
                    i,
                    widths[i] * 256
            );
        }

        sheet.createFreezePane(
                0,
                1
        );
    }

    /**
     * Adds one row to the summary sheet.
     */
    private void addSummaryRow(
            Sheet sheet,
            int rowNumber,
            String label,
            String value,
            CellStyle style) {

        Row row =
                sheet.createRow(
                        rowNumber
                );

        Cell labelCell =
                row.createCell(0);

        labelCell.setCellValue(
                label
        );

        labelCell.setCellStyle(
                style
        );

        Cell valueCell =
                row.createCell(1);

        valueCell.setCellValue(
                value
        );

        valueCell.setCellStyle(
                style
        );
    }

    /**
     * Creates a normal cell.
     */
    private void createCell(
            Row row,
            int column,
            String value) {

        Cell cell =
                row.createCell(column);

        cell.setCellValue(
                value
        );
    }

    /**
     * Creates header styling.
     */
    private CellStyle createHeaderStyle(
            Workbook workbook) {

        CellStyle style =
                workbook.createCellStyle();

        Font font =
                workbook.createFont();

        font.setBold(true);

        style.setFont(font);

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        return style;
    }

    /**
     * Creates summary/value styling.
     */
    private CellStyle createValueStyle(
            Workbook workbook) {

        CellStyle style =
                workbook.createCellStyle();

        style.setAlignment(
                HorizontalAlignment.LEFT
        );

        return style;
    }

    /**
     * Prevents null values.
     */
    private String safe(String value) {

        return value == null
                ? ""
                : value;
    }
}
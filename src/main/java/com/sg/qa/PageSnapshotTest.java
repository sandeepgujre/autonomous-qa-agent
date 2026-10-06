package com.sg.qa;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PageSnapshotTest {

    public static void main(String[] args) {

        System.out.println("================================");
        System.out.println("PAGE SNAPSHOT TEST");
        System.out.println("================================");

        String website =
                "https://the-internet.herokuapp.com/login";

        SeleniumExecutor seleniumExecutor =
                new SeleniumExecutor();

        try {

            System.out.println(
                    "\n[1] Starting browser..."
            );

            seleniumExecutor.startBrowser();

            System.out.println(
                    "\n[2] Opening website..."
            );

            seleniumExecutor.openWebsite(
                    website
            );

            System.out.println(
                    "\n[3] Creating page snapshot..."
            );

            PageSnapshot snapshot =
                    seleniumExecutor.createPageSnapshot();

            System.out.println(
                    "\n[4] Converting snapshot to JSON..."
            );

            ObjectMapper objectMapper =
                    new ObjectMapper();

            String json =
                    objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(snapshot);

            System.out.println(
                    "\n================================"
            );

            System.out.println(
                    "PAGE SNAPSHOT JSON"
            );

            System.out.println(
                    "================================"
            );

            System.out.println(json);

            System.out.println(
                    "\n================================"
            );

            System.out.println(
                    "PAGE SNAPSHOT TEST SUCCESSFUL"
            );

            System.out.println(
                    "================================"
            );

        } catch (Exception e) {

            System.out.println(
                    "\n================================"
            );

            System.out.println(
                    "PAGE SNAPSHOT TEST FAILED"
            );

            System.out.println(
                    "================================"
            );

            System.out.println(
                    "Error: " + e.getMessage()
            );

            e.printStackTrace();

        } finally {

            seleniumExecutor.closeBrowser();
        }
    }
}
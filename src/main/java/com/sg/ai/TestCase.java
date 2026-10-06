package com.sg.ai;

import java.util.List;

public class TestCase {

    private String title;
    private String website;
    private String username;
    private String password;
    private List<String> steps;
    private String expectedResult;
    private String expectedOutcome;

    public TestCase() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public String getExpectedOutcome() {
        return expectedOutcome;
    }

    public void setExpectedOutcome(String expectedOutcome) {
        this.expectedOutcome = expectedOutcome;
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "title='" + title + '\'' +
                ", website='" + website + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", steps=" + steps +
                ", expectedResult='" + expectedResult + '\'' +
                ", expectedOutcome='" + expectedOutcome + '\'' +
                '}';
    }
}
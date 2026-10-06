package com.sg.ai;

public class TestStep {

    private String action;
    private String target;
    private String value;
    private String expectedResult;

    public TestStep() {
    }

    public TestStep(
            String action,
            String target,
            String value,
            String expectedResult) {

        this.action = action;
        this.target = target;
        this.value = value;
        this.expectedResult = expectedResult;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    @Override
    public String toString() {

        return "TestStep{" +
                "action='" + action + '\'' +
                ", target='" + target + '\'' +
                ", value='" + value + '\'' +
                ", expectedResult='" +
                expectedResult + '\'' +
                '}';
    }
}
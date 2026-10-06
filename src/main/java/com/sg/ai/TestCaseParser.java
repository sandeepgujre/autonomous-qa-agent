package com.sg.ai;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestCaseParser {

    public static TestCaseResponse parse(String json) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(json, TestCaseResponse.class);
    }
}
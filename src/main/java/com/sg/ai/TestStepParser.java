package com.sg.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class TestStepParser {

    public static List<TestStep> parse(String json)
            throws Exception {

        ObjectMapper objectMapper =
                new ObjectMapper();

        return objectMapper.readValue(
                json,
                new TypeReference<List<TestStep>>() {}
        );
    }
}
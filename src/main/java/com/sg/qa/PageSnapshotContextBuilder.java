package com.sg.qa;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PageSnapshotContextBuilder {

    public String build(PageSnapshot snapshot)
            throws Exception {

        if (snapshot == null) {

            throw new IllegalArgumentException(
                    "Page snapshot cannot be null."
            );
        }

        ObjectMapper objectMapper =
                new ObjectMapper();

        return objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(snapshot);
    }
}
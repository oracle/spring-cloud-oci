/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.core.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class OCIObjectMapperTests {

    @Test
    void testObjectMapper() throws JSONException {
        TestJsonObject inputJson = new TestJsonObject(12, "fee", "foo");
        String jsonToStr = OCIObjectMapper.toPrintableString(inputJson);
        JSONObject strToJson = new JSONObject(jsonToStr);

        assertEquals(strToJson.get("id"), inputJson.getId());
        assertEquals(strToJson.getString("firstName"), inputJson.getFirstName());
        assertEquals(strToJson.getString("lastName"), inputJson.getLastName());
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    static final class TestJsonObject {
        private int id;
        private String firstName;
        private String lastName;
    }
}

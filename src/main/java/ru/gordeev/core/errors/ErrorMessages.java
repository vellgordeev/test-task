package ru.gordeev.core.errors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorMessages {

    // --- Static Error Constants (for messages that do not change) ---
    public static final String NULL_NOT_A_STRING = "Request body deserialize error: invalid type: null, expected a string";
    public static final String INVALID_QUERY_STRING = "Invalid query string";


    // --- Template-based Error Methods ---
    public static String missingField(String fieldName) {
        return String.format("Request body deserialize error: missing field `%s`", fieldName);
    }

    public static String invalidType(String actualValue, String expectedType) {
        return String.format("Request body deserialize error: invalid type: %s, expected %s", actualValue, expectedType);
    }

}

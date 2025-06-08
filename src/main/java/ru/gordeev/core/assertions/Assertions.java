package ru.gordeev.core.assertions;

import io.restassured.response.Response;

/**
 * Entry point for all assertions.
 * This class will be extended by AssertJ generator.
 */
public class Assertions extends org.assertj.core.api.Assertions {

    /**
     * Creates assertion for Response object.
     */
    public static ResponseAssert assertThatResponse(Response response) {
        return new ResponseAssert(response);
    }
}

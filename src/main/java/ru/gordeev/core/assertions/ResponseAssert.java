package ru.gordeev.core.assertions;

import io.restassured.response.Response;
import org.assertj.core.api.AbstractAssert;

/**
 * Assertions for Response objects.
 * Used primarily for negative test scenarios.
 */
public class ResponseAssert extends AbstractAssert<ResponseAssert, Response> {

    public ResponseAssert(Response actual) {
        super(actual, ResponseAssert.class);
    }

    /**
     * Verifies response has expected status code.
     */
    public ResponseAssert hasStatusCode(int expected) {
        isNotNull();

        int actualStatus = actual.statusCode();
        if (actualStatus != expected) {
            failWithMessage("Expected status code <%d> but was <%d>", expected, actualStatus);
        }

        return this;
    }

    /**
     * Verifies response body contains expected text.
     */
    public ResponseAssert hasBodyContaining(String expected) {
        isNotNull();

        String body = actual.getBody().asString();
        if (!body.contains(expected)) {
            failWithMessage("Expected body to contain <%s> but was <%s>", expected, body);
        }

        return this;
    }

    /**
     * Verifies response has expected content type.
     */
    public ResponseAssert hasContentType(String expected) {
        isNotNull();

        String contentType = actual.contentType();
        if (!contentType.contains(expected)) {
            failWithMessage("Expected content type <%s> but was <%s>", expected, contentType);
        }

        return this;
    }

    /**
     * Verifies response has header.
     */
    public ResponseAssert hasHeader(String name, String value) {
        isNotNull();

        String actualValue = actual.header(name);
        if (!value.equals(actualValue)) {
            failWithMessage("Expected header <%s> to be <%s> but was <%s>", name, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies response is success (2xx).
     */
    public ResponseAssert isSuccess() {
        isNotNull();

        int status = actual.statusCode();
        if (status < 200 || status >= 300) {
            failWithMessage("Expected success status (2xx) but was <%d>", status);
        }

        return this;
    }

    /**
     * Verifies response is client error (4xx).
     */
    public ResponseAssert isClientError() {
        isNotNull();

        int status = actual.statusCode();
        if (status < 400 || status >= 500) {
            failWithMessage("Expected client error status (4xx) but was <%d>", status);
        }

        return this;
    }
}

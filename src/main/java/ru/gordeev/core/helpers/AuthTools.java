package ru.gordeev.core.helpers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Base64;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthTools {

    public static String encodeBasicAuth(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}

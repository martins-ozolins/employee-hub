package com.employeehub.employeehub.shared.util;

import com.employeehub.employeehub.shared.exception.InvalidCredentialsException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

public class CookieUtils {

    private CookieUtils() {}

    public static String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        String token = Arrays.stream(cookies != null ? cookies : new Cookie[0])
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(InvalidCredentialsException::new);

        return token;
    }

    public static void addCookie(HttpServletResponse response, String name, String value) {
        response.addHeader(
                "Set-Cookie",
                name + "=" + value + "; Path=/; HttpOnly; SameSite=Lax"
        );
        // For production HTTPS add: "; Secure"
    }
}

package com.ats.shared.web;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Envelope for every JSON response: {@code result} is "OK" or "NG", {@code code} is the HTTP status. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(String result, int code, String message, T data) {

    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>("OK", code, message, data);
    }

    public static <T> ApiResponse<T> success(int code, T data) {
        return new ApiResponse<>("OK", code, null, data);
    }

    public static ApiResponse<Void> error(int code, String message) {
        return new ApiResponse<>("NG", code, message, null);
    }

    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>("NG", code, message, data);
    }
}

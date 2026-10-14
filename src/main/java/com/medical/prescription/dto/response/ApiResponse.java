package com.medical.prescription.dto.response;

import java.util.List;

public record ApiResponse<T>(boolean success, T data, String message, List<String> errors) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "OK", null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, null);
    }

    public static <T> ApiResponse<T> validationError(List<String> errors) {
        return new ApiResponse<>(false, null, "Validation failed", errors);
    }
}

package com.iyunxin.jxkh.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;
    private long timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(500, message);
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        // 将业务错误码映射为 HTTP 状态码
        int httpCode = mapErrorCodeToHttpStatus(errorCode);
        return ApiResponse.<T>builder()
                .code(httpCode)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 将业务错误码映射为 HTTP 状态码
     */
    private static int mapErrorCodeToHttpStatus(String errorCode) {
        if (errorCode == null) {
            return 500;
        }
        return switch (errorCode) {
            case "VALIDATION_ERROR", "MISSING_PARAMETER", "TYPE_MISMATCH", "INVALID_REQUEST_BODY" -> 400;
            case "AUTHENTICATION_FAILED", "INVALID_CREDENTIALS", "UNAUTHORIZED" -> 401;
            case "ACCESS_DENIED", "PERMISSION_DENIED" -> 403;
            case "RESOURCE_NOT_FOUND", "NOT_FOUND" -> 404;
            case "DATA_CONFLICT", "DUPLICATE_ENTRY" -> 409;
            case "FILE_TOO_LARGE" -> 413;
            case "METHOD_NOT_ALLOWED" -> 405;
            case "RATE_LIMIT_EXCEEDED" -> 429;
            case "INTERNAL_ERROR", "DATABASE_ERROR" -> 500;
            case "SERVICE_UNAVAILABLE" -> 503;
            default -> 400; // 默认为业务错误
        };
    }
}

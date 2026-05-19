package com.example.demo.exception;

import com.example.demo.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return new ErrorResponse("リクエストの解析に失敗しました。Content-Type: application/json; charset=UTF-8 でリクエストを送信してください。", HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("入力値が不正です");
        return new ErrorResponse(message, HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(NonTransientAiException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse handleNonTransientAi(NonTransientAiException ex) {
        log.error("AIプロバイダーで非リトライエラー", ex);
        return new ErrorResponse(
                "AIプロバイダーへのリクエストが失敗しました: " + ex.getMessage(),
                HttpStatus.BAD_GATEWAY.value());
    }

    @ExceptionHandler(TransientAiException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleTransientAi(TransientAiException ex) {
        log.error("AIプロバイダーで一時的エラー", ex);
        return new ErrorResponse(
                "AIプロバイダーが一時的に利用できません。時間をおいて再試行してください: " + ex.getMessage(),
                HttpStatus.SERVICE_UNAVAILABLE.value());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleIllegalState(IllegalStateException ex) {
        log.error("内部処理エラー", ex);
        return new ErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        return new ErrorResponse("サーバーエラーが発生しました。", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}

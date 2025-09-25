package com.swp.pizzashop.controller;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    public Map<String, Object> handleNotFound(NoHandlerFoundException ex) {
        return Map.of(
            "errorTitle", "404 Not Found",
            "errorCode", 404,
            "errorMessage", "The page you are looking for does not exist."
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String, Object> handleException(Exception ex) {
        return Map.of(
            "errorTitle", "Unexpected Error",
            "errorCode", 500,
            "errorMessage", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred."
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Map<String, Object> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .toList();
        return Map.of(
            "success", false,
            "errors", errors
        );
    }
}

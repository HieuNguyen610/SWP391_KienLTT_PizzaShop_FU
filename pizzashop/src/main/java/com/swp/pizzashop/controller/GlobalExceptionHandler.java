package com.swp.pizzashop.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private boolean isApiRequest(HttpServletRequest request) {
        if (request == null) return false;
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        String xhr = request.getHeader("X-Requested-With");
        return (uri != null && uri.startsWith("/api/"))
                || (accept != null && accept.contains("application/json"))
                || ("XMLHttpRequest".equalsIgnoreCase(xhr));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return Map.of(
                "errorTitle", "Access Denied",
                "errorCode", 403,
                "errorMessage", "You do not have permission to access this resource.",
                "timestamp", Instant.now().toString(),
                "path", request != null ? request.getRequestURI() : null
            );
        }
        ModelAndView mav = new ModelAndView("error/403");
        mav.setStatus(HttpStatus.FORBIDDEN);
        mav.addObject("message", "You don’t have permission to access this page.");
        return mav;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return Map.of(
                "errorTitle", "404 Not Found",
                "errorCode", 404,
                "errorMessage", "The page you are looking for does not exist.",
                "timestamp", Instant.now().toString(),
                "path", request != null ? request.getRequestURI() : null
            );
        }
        ModelAndView mav = new ModelAndView("error/404");
        mav.setStatus(HttpStatus.NOT_FOUND);
        mav.addObject("message", "The page you are looking for does not exist.");
        return mav;
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

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return Map.of(
                "errorTitle", "Unexpected Error",
                "errorCode", 500,
                "errorMessage", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred.",
                "timestamp", Instant.now().toString(),
                "path", request != null ? request.getRequestURI() : null
            );
        }
        ModelAndView mav = new ModelAndView("error/500");
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        mav.addObject("message", "We’re sorry, but something went wrong.");
        return mav;
    }
}

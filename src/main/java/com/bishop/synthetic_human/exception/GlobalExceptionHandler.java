package com.bishop.synthetic_human.exception;

import com.bishop.synthetic_human.dto.ErrorDto;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity
                .badRequest()
                .body(new ErrorDto("VALIDATION_FAILED", msg));
    }

    @ExceptionHandler(QueueOverflowException.class)
    public ResponseEntity<ErrorDto> handleQueue(QueueOverflowException ex) {
        return ResponseEntity
                .status(507)
                .body(new ErrorDto("QUEUE_OVERFLOW", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleJsonError(HttpMessageNotReadableException ex) {
        String msg;
        if (ex.getCause() instanceof InvalidFormatException ife) {
            String field = ife.getPathReference();
            Object[] enums = ife.getTargetType().getEnumConstants();
            msg = String.format(
                    "Неверное значение для приоритета, ожидалось одно из %s",
                     java.util.Arrays.toString(enums)
            );
        } else {
            msg = ex.getMessage();
        }
        return ResponseEntity
                .badRequest()
                .body(new ErrorDto("JSON_PARSE_ERROR", msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleOther(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto("INTERNAL_ERROR", ex.getMessage()));
    }
}



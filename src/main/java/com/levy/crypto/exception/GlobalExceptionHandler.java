package com.levy.crypto.exception;

import com.levy.crypto.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CoinNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCoinNotFound(CoinNotFoundException ex) {
        ErrorResponseDto response = new ErrorResponseDto(
                ex.getMessage(),
                404
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
}
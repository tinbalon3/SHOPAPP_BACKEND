package com.project.shopapp.exceptions;

import com.project.shopapp.response.ResponseObject;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ResponseObject> handlerGeneralException(Exception exception) {
        return ResponseEntity.internalServerError().body(
                ResponseObject.builder()
                        .message(exception.getMessage())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .build()
        );
    }
    @ExceptionHandler(ProductOutOfStockException.class)
    @ResponseStatus(HttpStatus.LOCKED)
    public ResponseEntity<ResponseObject> handlerProductOutOfStockException(ProductOutOfStockException exception) {
        Long userId = Long.parseLong(exception.getMessage().split(":")[1].toString());
        return ResponseEntity.internalServerError().body(
                ResponseObject.builder()
                        .message(exception.getMessage())
                        .status(HttpStatus.LOCKED.value())
                        .data(userId)
                        .build()
        );
    }
    @ExceptionHandler(UserErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ResponseObject> handlerUserErrorException(UserErrorException exception) {
        return ResponseEntity.internalServerError().body(
                ResponseObject.builder()
                        .message(exception.getMessage())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .build()
        );
    }
    @ExceptionHandler(OrderException.class)
    @ResponseStatus(HttpStatus.LOCKED)
    public ResponseEntity<ResponseObject> handlerOrderServiceException(OrderException exception) {
        return ResponseEntity.internalServerError().body(
                ResponseObject.builder()
                        .message(exception.getMessage())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .build()
        );
    }
    @ExceptionHandler(value = InvalidDataRegisterException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ResponseObject> handleInvalidDataRegisterException(InvalidDataRegisterException ex) {
        return ResponseEntity.internalServerError().body(
                ResponseObject.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .build()
        );

    }
    @ExceptionHandler(value = ExpiredTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ResponseObject> handleTokenRefreshException(ExpiredTokenException ex) {
        return ResponseEntity.badRequest().body(
                ResponseObject.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .build()
        );

    }
    @ExceptionHandler(value = EmailNotRegisterException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ResponseObject> handleEmailNotRegisterException(EmailNotRegisterException ex) {
        return ResponseEntity.badRequest().body(
                ResponseObject.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .build()
        );

    }
    @ExceptionHandler(value = InvalidPasswordException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ResponseObject> handleInvalidPasswordLoginException(InvalidPasswordException ex) {
        return ResponseEntity.badRequest().body(
                ResponseObject.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .build()
        );

    }
    @ExceptionHandler(value = OTPExpiredException.class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    public ResponseEntity<ResponseObject> handleOTPExpiredException(OTPExpiredException ex) {
        return ResponseEntity.badRequest().body(
                ResponseObject.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.REQUEST_TIMEOUT.value())
                        .build()
        );

    }
    @ExceptionHandler(DataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleResourceNotFoundException(DataNotFoundException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                .message(exception.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .build());
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleImagesNotFoundException(DataNotFoundException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                .message(exception.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .build());
    }
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> handleSecurityException(SecurityException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                .message(exception.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build());
    }
    @ExceptionHandler(MessagingException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleMessageException(DataNotFoundException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                .message(exception.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .build());
    }

}

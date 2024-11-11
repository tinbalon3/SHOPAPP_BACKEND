package com.project.shopapp.exceptions;

import com.project.shopapp.response.ResponseObject;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ResponseObject> handlerGeneralException(Exception exception) {
        return ResponseEntity.internalServerError().body(
                ResponseObject.builder()
                        .message(exception.getMessage())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build()
        );
    }
    @ExceptionHandler(DataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleResourceNotFoundException(DataNotFoundException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                .message(exception.getMessage())
                .status(HttpStatus.NOT_FOUND)
                .build());
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleImagesNotFoundException(DataNotFoundException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                .message(exception.getMessage())
                .status(HttpStatus.NOT_FOUND)
                .build());
    }

    @ExceptionHandler(MessagingException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleMessageException(DataNotFoundException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                .message("Email không tồn tại hoặc sai. Vui lòng thử lại.")
                .status(HttpStatus.NOT_FOUND)
                .build());
    }
}

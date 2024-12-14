package com.project.shopapp.response;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseObject {
    private String message;
    private int status;
    private Object data;
}

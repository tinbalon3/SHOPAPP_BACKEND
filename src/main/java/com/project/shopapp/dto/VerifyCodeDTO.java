package com.project.shopapp.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerifyCodeDTO {
    private String code;
    private String email;
}

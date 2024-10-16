package com.project.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordDTO {
    private String password;
    @JsonProperty("retype_password")
    private String retypePassword;
}

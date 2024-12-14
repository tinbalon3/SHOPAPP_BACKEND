package com.project.shopapp.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordRequest {
    private String email;
    private String password;
    @JsonProperty("retype_password")
    private String retypePassword;
}

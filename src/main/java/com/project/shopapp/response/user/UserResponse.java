package com.project.shopapp.response.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserResponse {

    private Long id;

    @JsonProperty("fullname")
    private String fullName;

    @JsonProperty("phone_number")

    private String phoneNumber;

    @JsonProperty("address")
    private String address;

    @JsonProperty("email")
    private String email;



    private boolean active;

    @JsonProperty("role_id")
    private Role role;
}

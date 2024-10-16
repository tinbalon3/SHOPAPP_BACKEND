package com.project.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO {
    @JsonProperty("user_id")
    @Min(value=1,message = "User's ID must be > 0")
    private Long userId;

    @JsonProperty("fullname")
    private String fullName;

    private String email;

    @JsonProperty("phone_number")
    @Min(value=5,message = "Phone number must be at least 5 characters")
    @NotBlank(message = "Phone number is required")
    private String phone_number;

    private String address;
}

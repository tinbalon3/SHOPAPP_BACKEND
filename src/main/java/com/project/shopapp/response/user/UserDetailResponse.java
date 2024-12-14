package com.project.shopapp.response.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.Role;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserDetailResponse {


    @JsonProperty("fullname")
    private String fullName;

    @JsonProperty("phone_number")

    private String phoneNumber;

    @JsonProperty("address")
    private String address;

    @JsonProperty("email")
    private String email;

    @JsonProperty("date_of_birth")
    private Date dateOfBirth;

    private boolean active;


}

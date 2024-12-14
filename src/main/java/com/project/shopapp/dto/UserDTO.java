package com.project.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    @JsonProperty("fullname")
    @Size(min = 3, max = 50, message = "Họ và tên tối thiếu 3 kí tự và tối đa 50 kí tự")
    private String fullName;

    @JsonProperty("phone_number")
    @NotBlank(message = "Số điện thoại là bắt buộc")
    private String phoneNumber;

    @NotBlank(message = "Mật khẩu không thể bỏ trống")
    @Length(min = 6, max = 50, message = "Mật khẩu phải có độ dài từ 6 đến 50 kí tự")
    private String password;

    @JsonProperty("email")
    @NotBlank(message = "Email là bắt buộc")
    private String email;

    @NotBlank(message = "Mật khẩu nhập lại không thể bỏ trống")
    @JsonProperty("retype_password")
    private String retypePassword;

    @JsonProperty("address")
    private String address;


    @JsonProperty("date_of_birth")
    private Date dateOfBirth;


    @NotNull(message = "Role ID is required")
    @JsonProperty("role_id")
    private Long role;

    @JsonProperty("auth_provider")
    private String authProvider;

    @JsonProperty("isAccepted")
    private boolean isAccepted;

}

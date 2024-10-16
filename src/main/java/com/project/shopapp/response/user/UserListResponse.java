package com.project.shopapp.response.user;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.response.user.UserResponse;
import lombok.*;

import java.util.List;
@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class UserListResponse {
    @JsonProperty("user")
    private List<UserResponse> userResponses;
    private int totalPages;
}

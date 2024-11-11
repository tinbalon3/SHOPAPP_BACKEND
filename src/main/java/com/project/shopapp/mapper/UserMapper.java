package com.project.shopapp.mapper;

import com.project.shopapp.dto.UserDTO;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.response.user.UserDetailResponse;
import com.project.shopapp.response.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper MAPPER = Mappers.getMapper(UserMapper.class);
    UserResponse mapToUserResponse(User user);
    @Mapping(source = "role", target = "role")
    UserDTO mapToUserDTO(User user);
    UserDetailResponse mapToUserDetailResponse(User user);
    default Long map(Role role) {
        return role != null ? role.getId() : null; // Giả sử Role có phương thức getId()
    }
}

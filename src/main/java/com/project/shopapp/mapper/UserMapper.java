package com.project.shopapp.mapper;

import com.project.shopapp.models.User;
import com.project.shopapp.response.user.UserDetailResponse;
import com.project.shopapp.response.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper MAPPER = Mappers.getMapper(UserMapper.class);
    UserResponse mapToUserResponse(User user);
    UserDetailResponse mapToUserDetailResponse(User user);
}

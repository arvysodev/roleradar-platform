package com.roleradar.auth.mapper;

import com.roleradar.auth.domain.User;
import com.roleradar.auth.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    @Mapping(target = "emailVerified", expression = "java(user.isEmailVerified())")
    UserResponse toUserResponse(User user);
}
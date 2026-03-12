package com.roleradar.auth.mapper;

import com.roleradar.auth.domain.User;
import com.roleradar.auth.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    UserResponse toUserResponse(User user);
}
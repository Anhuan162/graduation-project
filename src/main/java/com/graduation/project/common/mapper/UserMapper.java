package com.graduation.project.common.mapper;

import com.graduation.project.common.dto.UserSummaryDto;
import com.graduation.project.common.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @org.mapstruct.Mapping(target = "fullName", expression = "java(user.getFullName() != null && !user.getFullName().isEmpty() ? user.getFullName() : user.getEmail())")
    @org.mapstruct.Mapping(target = "email", source = "email")
    UserSummaryDto toUserSummaryDto(User user);
}

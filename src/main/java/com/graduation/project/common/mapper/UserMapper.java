package com.graduation.project.common.mapper;

import com.graduation.project.common.dto.UserSummaryDto;
import com.graduation.project.common.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserSummaryDto toUserSummaryDto(User user);
}

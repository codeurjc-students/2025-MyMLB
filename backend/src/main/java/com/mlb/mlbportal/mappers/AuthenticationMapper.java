package com.mlb.mlbportal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mlb.mlbportal.dto.authentication.RegisterRequest;
import com.mlb.mlbportal.dto.user.UserRole;
import com.mlb.mlbportal.models.UserEntity;

@Mapper(componentModel= "spring")
public interface AuthenticationMapper {
    RegisterRequest toRegisterRequest(UserEntity user);

    @Mapping(target= "id", ignore=true)
    @Mapping(target= "email", ignore=true)
    @Mapping(target= "name", ignore=true)
    @Mapping(target= "roles", ignore=true)
    @Mapping(target= "resetToken", ignore=true)
    UserEntity toDomain(RegisterRequest registerRequest);

    UserRole toUserRole(UserEntity user);
}
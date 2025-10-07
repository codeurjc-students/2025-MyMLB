package com.mlb.mlbportal.mappers;

import java.util.Collection;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mlb.mlbportal.dto.User.ShowUser;
import com.mlb.mlbportal.models.UserEntity;

@Mapper(componentModel= "spring")
public interface UserMapper {
    ShowUser toShowUser(UserEntity user);
    
    List<ShowUser> toShowUsers(Collection<UserEntity> users);

    @Mapping(target= "id", ignore=true)
    @Mapping(target= "name", ignore=true)
    @Mapping(target= "password", ignore=true)
    @Mapping(target= "roles", ignore=true)
    UserEntity toDomain(ShowUser registerRequest);
}
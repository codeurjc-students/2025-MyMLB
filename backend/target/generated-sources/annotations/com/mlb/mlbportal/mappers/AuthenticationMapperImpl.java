package com.mlb.mlbportal.mappers;

import com.mlb.mlbportal.dto.authentication.RegisterRequest;
import com.mlb.mlbportal.dto.user.UserRole;
import com.mlb.mlbportal.models.UserEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-23T20:25:19+0200",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.44.0.v20251001-1143, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class AuthenticationMapperImpl implements AuthenticationMapper {

    @Override
    public RegisterRequest toRegisterRequest(UserEntity user) {
        if ( user == null ) {
            return null;
        }

        String email = null;
        String username = null;
        String password = null;

        email = user.getEmail();
        username = user.getUsername();
        password = user.getPassword();

        RegisterRequest registerRequest = new RegisterRequest( email, username, password );

        return registerRequest;
    }

    @Override
    public UserEntity toDomain(RegisterRequest registerRequest) {
        if ( registerRequest == null ) {
            return null;
        }

        UserEntity userEntity = new UserEntity();

        userEntity.setPassword( registerRequest.password() );
        userEntity.setUsername( registerRequest.username() );

        return userEntity;
    }

    @Override
    public UserRole toUserRole(UserEntity user) {
        if ( user == null ) {
            return null;
        }

        String username = null;
        List<String> roles = null;

        username = user.getUsername();
        List<String> list = user.getRoles();
        if ( list != null ) {
            roles = new ArrayList<String>( list );
        }

        UserRole userRole = new UserRole( username, roles );

        return userRole;
    }
}

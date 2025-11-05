package com.mlb.mlbportal.mappers;

import com.mlb.mlbportal.dto.user.ShowUser;
import com.mlb.mlbportal.models.UserEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-05T20:11:43+0100",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.44.0.v20251023-0518, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public ShowUser toShowUser(UserEntity user) {
        if ( user == null ) {
            return null;
        }

        String username = null;
        String email = null;

        username = user.getUsername();
        email = user.getEmail();

        ShowUser showUser = new ShowUser( username, email );

        return showUser;
    }

    @Override
    public List<ShowUser> toShowUsers(Collection<UserEntity> users) {
        if ( users == null ) {
            return null;
        }

        List<ShowUser> list = new ArrayList<ShowUser>( users.size() );
        for ( UserEntity userEntity : users ) {
            list.add( toShowUser( userEntity ) );
        }

        return list;
    }

    @Override
    public UserEntity toDomain(ShowUser registerRequest) {
        if ( registerRequest == null ) {
            return null;
        }

        UserEntity userEntity = new UserEntity();

        userEntity.setEmail( registerRequest.email() );
        userEntity.setUsername( registerRequest.username() );

        return userEntity;
    }
}

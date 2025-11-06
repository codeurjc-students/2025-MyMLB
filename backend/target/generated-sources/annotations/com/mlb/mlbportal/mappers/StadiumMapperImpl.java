package com.mlb.mlbportal.mappers;

import com.mlb.mlbportal.dto.stadium.StadiumDTO;
import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-06T09:14:20+0100",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.44.0.v20251023-0518, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class StadiumMapperImpl implements StadiumMapper {

    @Override
    public StadiumDTO toStadiumDTO(Stadium stadium) {
        if ( stadium == null ) {
            return null;
        }

        String name = null;
        int openingDate = 0;

        name = stadium.getName();
        openingDate = stadium.getOpeningDate();

        StadiumDTO stadiumDTO = new StadiumDTO( name, openingDate );

        return stadiumDTO;
    }

    @Override
    public List<StadiumDTO> toListStadiumDTO(List<Stadium> stadiumList) {
        if ( stadiumList == null ) {
            return null;
        }

        List<StadiumDTO> list = new ArrayList<StadiumDTO>( stadiumList.size() );
        for ( Stadium stadium : stadiumList ) {
            list.add( toStadiumDTO( stadium ) );
        }

        return list;
    }

    @Override
    public StadiumInitDTO toStadiumInitDTO(Stadium stadium) {
        if ( stadium == null ) {
            return null;
        }

        String teamName = null;
        String name = null;
        int openingDate = 0;

        teamName = stadiumTeamName( stadium );
        name = stadium.getName();
        openingDate = stadium.getOpeningDate();

        StadiumInitDTO stadiumInitDTO = new StadiumInitDTO( name, openingDate, teamName );

        return stadiumInitDTO;
    }

    @Override
    public List<StadiumInitDTO> toListStadiumInitDTO(List<Stadium> stadiumList) {
        if ( stadiumList == null ) {
            return null;
        }

        List<StadiumInitDTO> list = new ArrayList<StadiumInitDTO>( stadiumList.size() );
        for ( Stadium stadium : stadiumList ) {
            list.add( toStadiumInitDTO( stadium ) );
        }

        return list;
    }

    private String stadiumTeamName(Stadium stadium) {
        Team team = stadium.getTeam();
        if ( team == null ) {
            return null;
        }
        return team.getName();
    }
}

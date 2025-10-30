package com.mlb.mlbportal.integration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.handler.notFound.StadiumNotFoundException;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.StadiumService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_YEAR;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_TEAM;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StadiumServiceIntegrationTest {
    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private StadiumService stadiumService;

    @Autowired
    private TeamRepository teamRepository;

    private List<Stadium> stadiums;

    private List<StadiumInitDTO> stadiumDtos;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.stadiumRepository.deleteAll();
        this.teamRepository.deleteAll();

        List<Team> teams = BuildMocksFactory.setUpTeamMocks();
        this.teamRepository.saveAll(teams);

        this.stadiums = BuildMocksFactory.setUpStadiums(teams);
        this.stadiumRepository.saveAll(this.stadiums);

        this.stadiumDtos = BuildMocksFactory.buildStadiumInitDTOMocks(teams);
    }

    @Test
    @DisplayName("Should return all of the stadiums")
    void testGetAllStadiums() {
        List<StadiumInitDTO> result = this.stadiumService.getAllStadiums();
        assertThat(result).hasSize(3).containsExactlyElementsOf(this.stadiumDtos);
    }

    @Test
    @DisplayName("Should return an empty list if there are no registered stadiums")
    void testGetEmptyStadiums() {
        this.stadiumRepository.deleteAll();
        this.teamRepository.deleteAll();
        List<StadiumInitDTO> result = this.stadiumService.getAllStadiums();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return the specified stadium with all of its attributes")
    void testFindStadiumByName() {
        assertThatNoException().isThrownBy(() -> this.stadiumService.findStadiumByName(STADIUM1_NAME));

        StadiumInitDTO result = this.stadiumService.findStadiumByName(STADIUM1_NAME);
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(STADIUM1_NAME);
        assertThat(result.openingDate()).isEqualTo(STADIUM1_YEAR);
        assertThat(result.teamName()).isEqualTo(stadiumDtos.get(0).teamName());
    }

    @Test
    @DisplayName("Should throw StadiumNotFoundException for a non existent stadium")
    void testNonExistentStadium() {
        assertThatThrownBy(() -> this.stadiumService.findStadiumByName(UNKNOWN_TEAM))
            .isInstanceOf(StadiumNotFoundException.class)
            .hasMessageContaining("Stadium Not Found");
    }

    @Test
    @DisplayName("Should persist Stadium with its associated Team")
    void testStadiumEntityHasTeam() {
        Stadium stadium = this.stadiumRepository.findByName(STADIUM1_NAME).orElseThrow(StadiumNotFoundException::new);
        Team team = stadium.getTeam();

        assertThat(team).isNotNull();
        assertThat(team.getStadium()).isEqualTo(stadium);
        assertThat(team.getName()).isEqualTo(TEST_TEAM1_NAME);
    }
}
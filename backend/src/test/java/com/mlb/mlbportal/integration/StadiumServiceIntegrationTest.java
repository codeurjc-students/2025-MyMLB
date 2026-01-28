package com.mlb.mlbportal.integration;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.dto.stadium.CreateStadiumRequest;
import com.mlb.mlbportal.dto.stadium.StadiumDTO;
import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.handler.notFound.StadiumNotFoundException;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.others.PictureInfo;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.StadiumService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import static com.mlb.mlbportal.utils.TestConstants.NEW_STADIUM;
import static com.mlb.mlbportal.utils.TestConstants.NEW_STADIUM_YEAR;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;

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

    private List<StadiumInitDTO> stadiumDtos;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.stadiumRepository.deleteAll();
        this.teamRepository.deleteAll();

        List<Team> teams = BuildMocksFactory.setUpTeamMocks();
        this.teamRepository.saveAll(teams);

        List<Stadium> stadiums = BuildMocksFactory.setUpStadiums(teams);
        this.stadiumRepository.saveAll(stadiums);

        this.stadiumDtos = BuildMocksFactory.buildStadiumInitDTOMocks(teams);
    }

    @Test
    @DisplayName("Should return all of the stadiums")
    void testGetAllStadiums() {
        Page<StadiumInitDTO> result = this.stadiumService.getAllStadiums(0, 10);

        assertThat(result.getContent()).hasSize(3).containsExactlyElementsOf(this.stadiumDtos);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Should return all available stadiums")
    void testGetAvailableStadiums() {
        Stadium stadium = new Stadium(NEW_STADIUM, NEW_STADIUM_YEAR, null);
        this.stadiumRepository.save(stadium);
        StadiumInitDTO dto = new StadiumInitDTO(NEW_STADIUM, NEW_STADIUM_YEAR, null, Collections.emptyList(), null);
        Page<StadiumInitDTO> result = this.stadiumService.getAllAvailableStadiums(0, 10);

        assertThat(result.getContent()).hasSize(1).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.hasNext()).isFalse();
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

    @Test
    @DisplayName("Should upload picture and persist URL + publicId")
    void testAddPicture() throws Exception {
        Stadium stadium = this.stadiumRepository.findByName(STADIUM1_NAME).orElseThrow(StadiumNotFoundException::new);

        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "fake".getBytes());
        PictureInfo dto = this.stadiumService.addPicture(STADIUM1_NAME, file);

        assertThat(dto.getUrl()).contains("http://fake.cloudinary.com");
        assertThat(dto.getPublicId()).isEqualTo("fake123");
        assertThat(stadium.getPictures()).anySatisfy(p -> {
            assertThat(p.getUrl()).contains("http://fake.cloudinary.com");
            assertThat(p.getPublicId()).isEqualTo("fake123");
        });
    }

    @Test
    @DisplayName("Should delete picture by publicId")
    void testDeletePicture() {
        Stadium stadium = this.stadiumRepository.findByName(STADIUM1_NAME).orElseThrow(StadiumNotFoundException::new);
        stadium.getPictures().add(new PictureInfo("http://fake.cloudinary.com/fake123.jpg", "fake123"));
        stadium.getPictures().add(new PictureInfo("http://fake.cloudinary.com/fake124.jpg", "fake124"));
        this.stadiumRepository.save(stadium);

        this.stadiumService.deletePicture(STADIUM1_NAME, "fake123");

        Stadium updated = this.stadiumRepository.findByName(STADIUM1_NAME).orElseThrow(StadiumNotFoundException::new);
        assertThat(updated.getPictures()).hasSize(1);
    }

    @Test
    @DisplayName("Should create a new stadium")
    void testCreateStadium() {
        CreateStadiumRequest request = new CreateStadiumRequest(NEW_STADIUM, NEW_STADIUM_YEAR);
        StadiumDTO result = this.stadiumService.createStadium(request);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(NEW_STADIUM);
        assertThat(result.openingDate()).isEqualTo(NEW_STADIUM_YEAR);
    }
}
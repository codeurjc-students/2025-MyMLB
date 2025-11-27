package com.mlb.mlbportal.integration;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.mlb.mlbportal.handler.conflict.LastPictureDeletionException;
import com.mlb.mlbportal.handler.conflict.StadiumAlreadyExistsException;
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
        StadiumInitDTO dto = new StadiumInitDTO(NEW_STADIUM, NEW_STADIUM_YEAR, null, Collections.emptyList());
        Page<StadiumInitDTO> result = this.stadiumService.getAllAvailableStadiums(0, 10);

        assertThat(result.getContent()).hasSize(1).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Should return an empty list if there are no registered stadiums")
    void testGetEmptyStadiums() {
        this.stadiumRepository.deleteAll();
        this.teamRepository.deleteAll();

        Page<StadiumInitDTO> result = this.stadiumService.getAllStadiums(0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Should return the specified stadium with all of its attributes")
    void testFindStadiumByName() {
        assertThatNoException().isThrownBy(() -> this.stadiumService.findStadiumByName(STADIUM1_NAME));

        StadiumInitDTO result = this.stadiumService.findStadiumByName(STADIUM1_NAME);
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(STADIUM1_NAME);
        assertThat(result.openingDate()).isEqualTo(STADIUM1_YEAR);
        assertThat(result.teamName()).isEqualTo(stadiumDtos.getFirst().teamName());
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

    @Test
    @DisplayName("Should return the stadium's pictures")
    void testGetStadiumPictures() {
        Stadium stadium = this.stadiums.getFirst();
        PictureInfo picture = new PictureInfo("http://cloudinary.com/test123.jpg", "test123");
        stadium.getPictures().add(picture);
        this.stadiumRepository.save(stadium);

        List<PictureInfo> result = this.stadiumService.getStadiumPictures(STADIUM1_NAME);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getPublicId()).isEqualTo(picture.getPublicId());
    }

    @Test
    @DisplayName("Should upload picture and persist URL + publicId")
    void testAddPicture() throws Exception {
        Stadium stadium = stadiumRepository.findByName(STADIUM1_NAME).orElseThrow(StadiumNotFoundException::new);

        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "fake".getBytes());
        PictureInfo dto = stadiumService.addPicture(STADIUM1_NAME, file);

        assertThat(dto.getUrl()).contains("http://fake.cloudinary.com");
        assertThat(dto.getPublicId()).isEqualTo("fake123");
        assertThat(stadium.getPictures()).anySatisfy(p -> {
            assertThat(p.getUrl()).contains("http://fake.cloudinary.com");
            assertThat(p.getPublicId()).isEqualTo("fake123");
        });
    }

    @Test
    @DisplayName("Should throw exception when stadium already has 5 pictures")
    void testAddPictureLimitExceededIntegration() throws Exception {
        Stadium stadium = stadiumRepository.findByName(STADIUM1_NAME).orElseThrow(StadiumNotFoundException::new);
        stadium.getPictures().addAll(List.of(
            new PictureInfo("", ""),
            new PictureInfo("", ""),
            new PictureInfo("", ""),
            new PictureInfo("", ""),
            new PictureInfo("", "")
        ));
        stadiumRepository.save(stadium);

        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "fake".getBytes());

        assertThatThrownBy(() -> stadiumService.addPicture(STADIUM1_NAME, file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Maximum amount of pictures reached");
    }

    /**
     * Initializes a stadium with one or two pictures for deletion tests.
     * <p>
     * This helper method centralizes test setup logic to avoid duplicating
     * picture initialization across multiple test cases, thereby adhering
     * to the DRY (Don't Repeat Yourself) principle.
     * </p>
     *
     * @param success true to allow a valid deletion scenario (two pictures),
     *                false to enforce the "cannot delete last picture" rule (one picture).
     */
    private void deletePictureTestSetUp(boolean success) {
        Stadium stadium = this.stadiumRepository.findByName(STADIUM1_NAME).orElseThrow(StadiumNotFoundException::new);
        if (success) {
            stadium.getPictures().add(new PictureInfo("http://fake.cloudinary.com/fake123.jpg", "fake123"));
            stadium.getPictures().add(new PictureInfo("http://fake.cloudinary.com/fake124.jpg", "fake124"));
        }
        else {
            stadium.getPictures().add(new PictureInfo("http://fake.cloudinary.com/fake123.jpg", "fake123"));
        }
        this.stadiumRepository.save(stadium);
    }

    @Test
    @DisplayName("Should delete picture by publicId")
    void testDeletePicture() {
        this.deletePictureTestSetUp(true);

        this.stadiumService.deletePicture(STADIUM1_NAME, "fake123");

        Stadium updated = this.stadiumRepository.findByName(STADIUM1_NAME).orElseThrow(StadiumNotFoundException::new);
        assertThat(updated.getPictures()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw LastPictureDeletionException when trying to delete the last picture")
    void testDeleteLastPicture() {
        this.deletePictureTestSetUp(false);

        assertThatThrownBy(() -> this.stadiumService.deletePicture(STADIUM1_NAME, "test123"))
                .isInstanceOf(LastPictureDeletionException.class)
                .hasMessageContaining("Cannot delete the last picture of a stadium");
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

    @Test
    @DisplayName("Should throw StadiumAlreadyExists when trying to create a stadium already created")
    void testInvalidStadiumCreation() {
        CreateStadiumRequest request = new CreateStadiumRequest(STADIUM1_NAME, STADIUM1_YEAR);

        assertThatThrownBy(() -> this.stadiumService.createStadium(request))
                .isInstanceOf(StadiumAlreadyExistsException.class)
                .hasMessageContaining("Stadium Already Exists");
    }
}
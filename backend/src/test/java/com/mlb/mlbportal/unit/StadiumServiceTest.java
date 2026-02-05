package com.mlb.mlbportal.unit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import com.mlb.mlbportal.dto.stadium.CreateStadiumRequest;
import com.mlb.mlbportal.dto.stadium.StadiumDTO;
import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.handler.conflict.StadiumAlreadyExistsException;
import com.mlb.mlbportal.handler.notFound.StadiumNotFoundException;
import com.mlb.mlbportal.mappers.StadiumMapper;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.others.PictureInfo;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.services.StadiumService;
import com.mlb.mlbportal.services.uploader.PictureService;
import com.mlb.mlbportal.services.utilities.PaginationHandlerService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import static com.mlb.mlbportal.utils.TestConstants.NEW_STADIUM;
import static com.mlb.mlbportal.utils.TestConstants.NEW_STADIUM_YEAR;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_YEAR;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_TEAM;

@ExtendWith(MockitoExtension.class)
class StadiumServiceTest {
    @Mock
    private StadiumRepository stadiumRepository;

    @Mock
    private StadiumMapper stadiumMapper;

    @Mock
    private PictureService pictureService;

    @Mock
    private PaginationHandlerService paginationHandlerService;

    @InjectMocks
    private StadiumService stadiumService;

    private List<Stadium> stadiums;
    private List<StadiumInitDTO> stadiumDtos;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.stadiums = BuildMocksFactory.setUpStadiums();
        this.stadiumDtos = BuildMocksFactory.buildStadiumInitDTOMocks();
    }

    @Test
    @DisplayName("Should return all of the stadiums")
    void testGetAllStadiums() {
        Page<StadiumInitDTO> mockPage = new PageImpl<>(this.stadiumDtos, PageRequest.of(0, 10), this.stadiumDtos.size());

        when(this.stadiumRepository.findAll()).thenReturn(this.stadiums);
        doReturn(mockPage).when(this.paginationHandlerService).paginateAndMap(eq(this.stadiums), eq(0), eq(10), any());

        Page<StadiumInitDTO> result = this.stadiumService.getAllStadiums(0, 10);
        List<StadiumInitDTO> content = result.getContent();

        assertThat(content).hasSize(3).containsExactlyElementsOf(this.stadiumDtos);
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
        List<Stadium> availableStadiums = List.of(stadium);
        StadiumInitDTO dto = new StadiumInitDTO(NEW_STADIUM, NEW_STADIUM_YEAR, null, Collections.emptyList(), null);
        Page<StadiumInitDTO> mockPage = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        when(this.stadiumRepository.findByTeamIsNull()).thenReturn(availableStadiums);
        doReturn(mockPage).when(this.paginationHandlerService).paginateAndMap(eq(availableStadiums), eq(0), eq(10), any());

        Page<StadiumInitDTO> result = this.stadiumService.getAllAvailableStadiums(0, 10);

        assertThat(result.getContent()).hasSize(1).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Should return the specified stadium with all of its attributes")
    void testFindStadiumByName() {
        Stadium stadium = this.stadiums.getFirst();
        StadiumInitDTO dto = this.stadiumDtos.getFirst();

        when(this.stadiumRepository.findByNameOrThrow(STADIUM1_NAME)).thenReturn(stadium);
        when(this.stadiumMapper.toStadiumInitDTO(stadium)).thenReturn(dto);
        
        assertThatNoException().isThrownBy(() -> {
            StadiumInitDTO result = this.stadiumService.findStadiumByName(STADIUM1_NAME);
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo(dto.name());
            assertThat(result.openingDate()).isEqualTo(dto.openingDate());
            assertThat(result.teamName()).isEqualTo(dto.teamName());
        });
    }

    @Test
    @DisplayName("Should throw StadiumNotFoundException for a non existent stadium")
    void testNonExistentStadium() {
        when(this.stadiumRepository.findByNameOrThrow(any())).thenCallRealMethod();
        assertThatThrownBy(() -> this.stadiumService.findStadiumByName(UNKNOWN_TEAM))
            .isInstanceOf(StadiumNotFoundException.class)
            .hasMessageContaining("Stadium Not Found");
    }

    @Test
    @DisplayName("Should return the stadium's pictures")
    void testGetStadiumPictures() {
        Stadium stadium = this.stadiums.getFirst();
        PictureInfo picture = new PictureInfo("http://cloudinary.com/test123.jpg", "test123");
        stadium.getPictures().add(picture);

        when(this.stadiumRepository.findByNameOrThrow(STADIUM1_NAME)).thenReturn(stadium);
        List<PictureInfo> result = this.stadiumService.getStadiumPictures(STADIUM1_NAME);

        assertThat(result).hasSize(1).containsExactlyElementsOf(List.of(picture));
    }

    @Test
    @DisplayName("Should upload picture and return PictureDTO")
    void testAddPicture() throws Exception {
        Stadium stadium = this.stadiums.getFirst();
        MultipartFile mockFile = mock(MultipartFile.class);
        PictureInfo pictureInfo = new PictureInfo("http://cloudinary.com/test123.jpg", "test123");

        when(this.stadiumRepository.findByNameOrThrow(stadium.getName())).thenReturn(stadium);
        when(this.pictureService.uploadPicture(mockFile)).thenReturn(pictureInfo);

        PictureInfo result = this.stadiumService.addPicture(stadium.getName(), mockFile);
        assertThat(result).isNotNull();
        assertThat(result.getUrl()).isEqualTo(pictureInfo.getUrl());
        assertThat(result.getPublicId()).isEqualTo(pictureInfo.getPublicId());

        verify(this.stadiumRepository, times(1)).save(any(Stadium.class));
    }

    @Test
    @DisplayName("Should throw exception when stadium already has 5 pictures")
    void testAddPictureLimitExceeded() {
        Stadium stadium = this.stadiums.getFirst();
        stadium.getPictures().addAll(List.of(
            new PictureInfo("", ""),
            new PictureInfo("", ""),
            new PictureInfo("", ""),
            new PictureInfo("", ""),
            new PictureInfo("", "")
        ));

        MultipartFile mockFile = mock(MultipartFile.class);
        when(this.stadiumRepository.findByNameOrThrow(STADIUM1_NAME)).thenReturn(stadium);

        assertThatThrownBy(() -> this.stadiumService.addPicture(STADIUM1_NAME, mockFile))
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
    private Stadium deletePictureTestSetUp(boolean success) {
        Stadium stadium = this.stadiums.getFirst();
        if (success) {
            stadium.getPictures().add(new PictureInfo("http://fake.cloudinary.com/fake123.jpg", "fake123"));
            stadium.getPictures().add(new PictureInfo("http://fake.cloudinary.com/fake124.jpg", "fake124"));
        }
        else {
            stadium.getPictures().add(new PictureInfo("http://fake.cloudinary.com/fake123.jpg", "fake123"));
        }
        return stadium;
    }

    @Test
    @DisplayName("Should delete picture by publicId")
    void testDeletePicture() {
        Stadium stadium = this.deletePictureTestSetUp(true);

        when(this.stadiumRepository.findByNameOrThrow(STADIUM1_NAME)).thenReturn(stadium);

        this.stadiumService.deletePicture(STADIUM1_NAME, "fake123");

        verify(this.stadiumRepository).save(stadium);
        assertThat(stadium.getPictures()).hasSize(1);
    }

    @Test
    @DisplayName("Should create a new stadium")
    void testCreateStadium() {
        CreateStadiumRequest request = new CreateStadiumRequest(NEW_STADIUM, NEW_STADIUM_YEAR);
        StadiumDTO newStadium = new StadiumDTO(NEW_STADIUM, NEW_STADIUM_YEAR, Collections.emptyList());

        when(this.stadiumRepository.findByName(NEW_STADIUM)).thenReturn(Optional.empty());
        when(this.stadiumMapper.toDomainFromStadiumDTO(newStadium)).thenReturn(this.stadiums.getLast());

        StadiumDTO result = this.stadiumService.createStadium(request);
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(newStadium.name());
        verify(this.stadiumRepository, times(1)).save(this.stadiums.getLast());
    }

    @Test
    @DisplayName("Should throw StadiumAlreadyExists when trying to create a stadium already created")
    void testInvalidStadiumCreation() {
        CreateStadiumRequest request = new CreateStadiumRequest(STADIUM1_NAME, STADIUM1_YEAR);

        when(this.stadiumRepository.findByName(STADIUM1_NAME)).thenReturn(Optional.of(this.stadiums.getFirst()));
        assertThatThrownBy(() -> this.stadiumService.createStadium(request))
                .isInstanceOf(StadiumAlreadyExistsException.class)
                .hasMessageContaining("Stadium Already Exists");

        verify(this.stadiumRepository, never()).save(any());
    }
}
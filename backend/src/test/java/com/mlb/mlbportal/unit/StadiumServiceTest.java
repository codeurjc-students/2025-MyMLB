package com.mlb.mlbportal.unit;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mlb.mlbportal.handler.conflict.LastPictureDeletionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.handler.notFound.StadiumNotFoundException;
import com.mlb.mlbportal.mappers.StadiumMapper;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.others.PictureInfo;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.services.StadiumService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_TEAM;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StadiumServiceTest {
    @Mock
    private StadiumRepository stadiumRepository;

    @Mock
    private StadiumMapper stadiumMapper;

    @Mock
    private Cloudinary cloudinary;

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
        when(this.stadiumRepository.findAll()).thenReturn(this.stadiums);
        when(this.stadiumMapper.toListStadiumInitDTO(this.stadiums)).thenReturn(this.stadiumDtos);

        List<StadiumInitDTO> result = this.stadiumService.getAllStadiums();
        assertThat(result).hasSize(3).containsExactlyElementsOf(this.stadiumDtos);
    }

    @Test
    @DisplayName("Should return an empty list if there are no registered stadiums")
    void testGetEmptyStadiums() {
        when(this.stadiumRepository.findAll()).thenReturn(Collections.emptyList());
        List<StadiumInitDTO> result = this.stadiumService.getAllStadiums();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return the specified stadium with all of its attributes")
    void testFindStadiumByName() {
        Stadium stadium = this.stadiums.get(0);
        StadiumInitDTO dto = this.stadiumDtos.get(0);

        when(this.stadiumRepository.findByName(STADIUM1_NAME)).thenReturn(Optional.of(stadium));
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
        when(this.stadiumRepository.findByName(any())).thenReturn(Optional.empty());
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

        when(this.stadiumRepository.findByName(STADIUM1_NAME)).thenReturn(Optional.of(stadium));
        List<PictureInfo> result = this.stadiumService.getStadiumPictures(STADIUM1_NAME);

        assertThat(result).hasSize(1);
        assertThat(result).containsExactlyElementsOf(List.of(picture));
    }

    @Test
    @DisplayName("Should upload picture and return PictureDTO")
    void testAddPicture() throws Exception {
        Stadium stadium = this.stadiums.get(0);
        stadium.getPictures().clear();

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenReturn("fake-image".getBytes());

        Uploader uploader = mock(Uploader.class);
        when(this.cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
            .thenReturn(Map.of("secure_url", "http://cloudinary.com/test.jpg", "public_id", "test123"));

        when(this.stadiumRepository.findByName(STADIUM1_NAME)).thenReturn(Optional.of(stadium));

        PictureInfo result = this.stadiumService.addPicture(STADIUM1_NAME, mockFile);

        assertThat(result.getUrl()).isEqualTo("http://cloudinary.com/test.jpg");
        assertThat(result.getPublicId()).isEqualTo("test123");
        verify(this.stadiumRepository).save(stadium);
    }

    @Test
    @DisplayName("Should throw exception when stadium already has 5 pictures")
    void testAddPictureLimitExceeded() throws Exception {
        Stadium stadium = this.stadiums.get(0);
        stadium.getPictures().addAll(List.of(
            new PictureInfo("", ""),
            new PictureInfo("", ""),
            new PictureInfo("", ""),
            new PictureInfo("", ""),
            new PictureInfo("", "")
        ));

        MultipartFile mockFile = mock(MultipartFile.class);
        when(this.stadiumRepository.findByName(STADIUM1_NAME)).thenReturn(Optional.of(stadium));

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

        when(this.stadiumRepository.findByName(STADIUM1_NAME)).thenReturn(Optional.of(stadium));

        this.stadiumService.deletePicture(STADIUM1_NAME, "fake123");

        verify(this.stadiumRepository).save(stadium);
        assertThat(stadium.getPictures()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw LastPictureDeletionException when trying to delete the last picture")
    void testDeleteLastPicture() {
        Stadium stadium = this.deletePictureTestSetUp(false);

        when(this.stadiumRepository.findByName(STADIUM1_NAME)).thenReturn(Optional.of(stadium));

        assertThatThrownBy(() -> this.stadiumService.deletePicture(STADIUM1_NAME, "fake123"))
                .isInstanceOf(LastPictureDeletionException.class)
                .hasMessageContaining("Cannot delete the last picture of a stadium");

        verify(this.stadiumRepository, never()).save(stadium);
    }
}
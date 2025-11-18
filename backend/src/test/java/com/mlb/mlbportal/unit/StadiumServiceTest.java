package com.mlb.mlbportal.unit;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.mlb.mlbportal.dto.picture.PictureDTO;
import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.handler.notFound.StadiumNotFoundException;
import com.mlb.mlbportal.mappers.StadiumMapper;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.services.StadiumService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_TEAM;

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

        PictureDTO result = this.stadiumService.addPicture(STADIUM1_NAME, mockFile);

        assertThat(result.url()).isEqualTo("http://cloudinary.com/test.jpg");
        assertThat(result.publicId()).isEqualTo("test123");
        verify(this.stadiumRepository).save(stadium);
    }

    @Test
    @DisplayName("Should throw exception when stadium already has 5 pictures")
    void testAddPictureLimitExceeded() throws Exception {
        Stadium stadium = this.stadiums.get(0);
        stadium.getPictures().addAll(List.of("1","2","3","4","5"));

        MultipartFile mockFile = mock(MultipartFile.class);
        when(this.stadiumRepository.findByName(STADIUM1_NAME)).thenReturn(Optional.of(stadium));

        assertThatThrownBy(() -> this.stadiumService.addPicture(STADIUM1_NAME, mockFile))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Maximum amount of pictures reached");
    }

    @Test
    @DisplayName("Should delete picture by publicId")
    void testDeletePicture() throws Exception {
        Stadium stadium = this.stadiums.get(0);
        stadium.getPictures().add("http://cloudinary.com/test123.jpg");

        Uploader uploader = mock(Uploader.class);
        when(this.cloudinary.uploader()).thenReturn(uploader);
        when(this.stadiumRepository.findByName(STADIUM1_NAME)).thenReturn(Optional.of(stadium));

        this.stadiumService.deletePicture(STADIUM1_NAME, "test123");

        verify(uploader).destroy(eq("test123"), any(Map.class));
        verify(this.stadiumRepository).save(stadium);
        assertThat(stadium.getPictures()).isEmpty();
    }
}
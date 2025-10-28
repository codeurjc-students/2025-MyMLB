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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
package com.mlb.mlbportal.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.mappers.StadiumMapper;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.player.PlayerRepository;
import com.mlb.mlbportal.services.SearchService;
import com.mlb.mlbportal.utils.BuildMocksFactory;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {
    @Mock
    private StadiumRepository stadiumRepository;
    
    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private StadiumMapper stadiumMapper;

    @InjectMocks
    private SearchService searchService;

    private List<Stadium> stadiums;
    private List<StadiumInitDTO> stadiumsDtos;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.stadiums = BuildMocksFactory.setUpStadiums();
        this.stadiumsDtos = BuildMocksFactory.buildStadiumInitDTOMocks();
    }

    @Test
    @DisplayName("Should return the stadiums that matches with the input search")
    void testSearchStadium() {
        when(this.stadiumRepository.findByNameContainingIgnoreCase("St")).thenReturn(this.stadiums);
        when(this.stadiumMapper.toListStadiumInitDTO(this.stadiums)).thenReturn(this.stadiumsDtos);

        List<StadiumInitDTO> result = this.searchService.searchStadiums("St");
        assertThat(result).hasSize(3).containsExactlyElementsOf(this.stadiumsDtos);
    }
}
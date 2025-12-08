package com.mlb.mlbportal.services;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mlb.mlbportal.dto.stadium.CreateStadiumRequest;
import com.mlb.mlbportal.dto.stadium.StadiumDTO;
import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.handler.conflict.LastPictureDeletionException;
import com.mlb.mlbportal.handler.conflict.StadiumAlreadyExistsException;
import com.mlb.mlbportal.handler.notFound.StadiumNotFoundException;
import com.mlb.mlbportal.mappers.StadiumMapper;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.others.PictureInfo;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.services.uploader.PictureService;
import com.mlb.mlbportal.services.utilities.PaginationHandlerService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class StadiumService {
    private final StadiumRepository stadiumRepository;
    private final StadiumMapper stadiumMapper;
    private final PictureService pictureService;
    private final PaginationHandlerService paginationHandlerService;

    @Transactional(readOnly = true)
    public Page<StadiumInitDTO> getAllStadiums(int page, int size) {
        List<Stadium> stadiums = this.stadiumRepository.findAll();
        return this.paginationHandlerService.paginateAndMap(stadiums, page, size, this.stadiumMapper::toStadiumInitDTO);
    }

    @Transactional(readOnly = true)
    public Page<StadiumInitDTO> getAllAvailableStadiums(int page, int size) {
        List<Stadium> stadiums = this.stadiumRepository.findByTeamIsNull();
        return this.paginationHandlerService.paginateAndMap(stadiums, page, size, this.stadiumMapper::toStadiumInitDTO);
    }

    @Transactional(readOnly = true)
    public StadiumInitDTO findStadiumByName(String name) {
        Stadium stadium = this.stadiumRepository.findByName(name).orElseThrow(StadiumNotFoundException::new);
        return this.stadiumMapper.toStadiumInitDTO(stadium);
    }

    @Transactional(readOnly = true)
    public List<PictureInfo> getStadiumPictures(String stadiumName) {
        Stadium stadium = this.stadiumRepository.findByName(stadiumName).orElseThrow(StadiumNotFoundException::new);
        return stadium.getPictures();
    }

    @Transactional
    public PictureInfo addPicture(String stadiumName, MultipartFile file) throws IOException {
        Stadium stadium = this.stadiumRepository.findByName(stadiumName).orElseThrow(StadiumNotFoundException::new);

        if (stadium.getPictures().size() >= 5) {
            throw new IllegalArgumentException("Maximum amount of pictures reached");
        }
        PictureInfo pictureInfo = this.pictureService.uploadPicture(file);

        stadium.getPictures().add(pictureInfo);
        this.stadiumRepository.save(stadium);

        return pictureInfo;
    }

    @Transactional
    public void deletePicture(String stadiumName, String publicId) {
        Stadium stadium = this.stadiumRepository.findByName(stadiumName).orElseThrow(StadiumNotFoundException::new);
        if (stadium.getPictures().size() <= 1) {
            throw new LastPictureDeletionException("Cannot delete the last picture of a stadium");
        }
        stadium.getPictures().removeIf(p -> publicId.equals(p.getPublicId()));
        this.stadiumRepository.save(stadium);
    }

    @Transactional
    public StadiumDTO createStadium(CreateStadiumRequest request) {
        if (this.stadiumRepository.findByName(request.name()).isPresent()) {
            throw new StadiumAlreadyExistsException();
        }
        StadiumDTO newStadium = new StadiumDTO(request.name(), request.openingDate(), Collections.emptyList());
        Stadium stadium = this.stadiumMapper.toDomainFromStadiumDTO(newStadium);
        this.stadiumRepository.save(stadium);
        return newStadium;
    }
}
package com.mlb.mlbportal.services;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
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

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class StadiumService {
    private final StadiumRepository stadiumRepository;
    private final StadiumMapper stadiumMapper;
    private final Cloudinary cloudinary;

    private Page<StadiumInitDTO> generatePagination(int page, int size, boolean allAvailable) {
        List<Stadium> stadiums;
        if (!allAvailable) {
            stadiums = this.stadiumRepository.findAll();
        }
        else {
            stadiums = this.stadiumRepository.findByTeamIsNull();
        }
        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), stadiums.size());
        int end = Math.min(start + pageable.getPageSize(), stadiums.size());

        List<StadiumInitDTO> result = stadiums.subList(start, end).stream()
                .map(this.stadiumMapper::toStadiumInitDTO).toList();

        return new PageImpl<>(result, pageable, stadiums.size());
    }

    @Transactional(readOnly = true)
    public Page<StadiumInitDTO> getAllStadiums(int page, int size) {
        return this.generatePagination(page, size, false);
    }

    @Transactional(readOnly = true)
    public Page<StadiumInitDTO> getAllAvailableStadiums(int page, int size) {
        return this.generatePagination(page, size, true);
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

    @SuppressWarnings("unchecked")
    @Transactional
    public PictureInfo addPicture(String stadiumName, MultipartFile file) throws IOException {
        Stadium stadium = this.stadiumRepository.findByName(stadiumName).orElseThrow(StadiumNotFoundException::new);

        if (stadium.getPictures().size() >= 5) {
            throw new IllegalArgumentException("Maximum amount of pictures reached");
        }

        Map<String, Object> uploadResult = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
        String publicUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");

        PictureInfo pictureInfo = new PictureInfo(publicUrl, publicId);

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
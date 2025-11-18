package com.mlb.mlbportal.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.mlb.mlbportal.dto.picture.PictureDTO;
import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.handler.notFound.StadiumNotFoundException;
import com.mlb.mlbportal.mappers.StadiumMapper;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.repositories.StadiumRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class StadiumService {
    private final StadiumRepository stadiumRepository;
    private final StadiumMapper stadiumMapper;
    private final Cloudinary cloudinary;

    public List<StadiumInitDTO> getAllStadiums() {
        return this.stadiumMapper.toListStadiumInitDTO(this.stadiumRepository.findAll());
    }

    public StadiumInitDTO findStadiumByName(String name) {
        Stadium stadium = this.stadiumRepository.findByName(name).orElseThrow(StadiumNotFoundException::new);
        return this.stadiumMapper.toStadiumInitDTO(stadium);
    }

    @SuppressWarnings("unchecked")
    public PictureDTO addPicture(String stadiumName, MultipartFile file) throws IOException {
        Stadium stadium = this.stadiumRepository.findByName(stadiumName).orElseThrow(StadiumNotFoundException::new);

        if (stadium.getPictures().size() >= 5) {
            throw new IllegalArgumentException("Maximum amount of pictures reached");
        }

        Map<String, Object> uploadResult = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
        String publicUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");

        stadium.getPictures().add(publicUrl);
        this.stadiumRepository.save(stadium);

        return new PictureDTO(publicUrl, publicId);
    }

    public void deletePicture(String stadiumName, String publicId) throws IOException {
        Stadium stadium = this.stadiumRepository.findByName(stadiumName).orElseThrow(StadiumNotFoundException::new);

        this.cloudinary.uploader().destroy(publicId, Map.of());

        stadium.getPictures().removeIf(url -> url.contains(publicId));
        this.stadiumRepository.save(stadium);
    }
}
package com.mlb.mlbportal.services.uploader;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.mlb.mlbportal.models.others.PictureInfo;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PictureService {
    private final Cloudinary cloudinary;

    @SuppressWarnings("unchecked")
    public PictureInfo uploadPicture(MultipartFile file) throws IOException {
        Map<String, Object> uploadResult = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
        String publicUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");

        return new PictureInfo(publicUrl, publicId);
    }
}
package com.mlb.mlbportal.unit.uploader;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.mlb.mlbportal.models.others.PictureInfo;
import com.mlb.mlbportal.services.uploader.PictureService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PictureServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private PictureService pictureService;

    @Test
    @DisplayName("Should upload picture and return PictureInfo")
    void testUploadPictureSuccess() throws IOException {
        byte[] fileBytes = "fake-image".getBytes();
        when(this.file.getBytes()).thenReturn(fileBytes);
        when(this.cloudinary.uploader()).thenReturn(this.uploader);

        Map<String, Object> uploadResult = Map.of(
                "secure_url", "http://cloudinary.com/test123.jpg",
                "public_id", "test123"
        );
        when(this.uploader.upload(fileBytes, Map.of())).thenReturn(uploadResult);

        PictureInfo result = this.pictureService.uploadPicture(file);

        assertThat(result).isNotNull();
        assertThat(result.getUrl()).isEqualTo("http://cloudinary.com/test123.jpg");
        assertThat(result.getPublicId()).isEqualTo("test123");

        verify(this.file, times(1)).getBytes();
        verify(this.cloudinary, times(1)).uploader();
        verify(this.uploader, times(1)).upload(fileBytes, Map.of());
    }

    @Test
    @DisplayName("Should throw IOException when file.getBytes() fails")
    void testUploadPictureIOException() throws IOException {
        when(this.file.getBytes()).thenThrow(new IOException("File read error"));

        assertThatThrownBy(() -> this.pictureService.uploadPicture(file))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("File read error");

        verify(this.file, times(1)).getBytes();
    }
}
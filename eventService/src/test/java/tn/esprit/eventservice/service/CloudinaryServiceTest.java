package tn.esprit.eventservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @Test
    @DisplayName("uploadImage_shouldReturnSecureUrl_whenUploadSucceeds")
    void uploadImage_shouldReturnSecureUrl_whenUploadSucceeds() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        given(cloudinary.uploader()).willReturn(uploader);
        given(uploader.upload(any(byte[].class), any(Map.class)))
                .willReturn(Map.of("secure_url", "https://res.cloudinary.com/test/image.jpg"));

        String result = cloudinaryService.uploadImage(file, "events");

        assertThat(result).isEqualTo("https://res.cloudinary.com/test/image.jpg");
    }

    @Test
    @DisplayName("uploadImage_shouldThrowIOException_whenCloudinaryFails")
    void uploadImage_shouldThrowIOException_whenCloudinaryFails() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "image", "bad.jpg", "image/jpeg", "bytes".getBytes());

        given(cloudinary.uploader()).willReturn(uploader);
        given(uploader.upload(any(byte[].class), any(Map.class)))
                .willThrow(new IOException("Cloudinary upload failed"));

        assertThatThrownBy(() -> cloudinaryService.uploadImage(file, "events"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Cloudinary upload failed");
    }

    @Test
    @DisplayName("deleteImage_shouldCallDestroy_withPublicId")
    void deleteImage_shouldCallDestroy_withPublicId() throws IOException {
        given(cloudinary.uploader()).willReturn(uploader);
        given(uploader.destroy(any(String.class), any(Map.class)))
                .willReturn(Map.of("result", "ok"));

        cloudinaryService.deleteImage("events/my-image-id");

        verify(uploader).destroy(any(String.class), any(Map.class));
    }

    @Test
    @DisplayName("deleteImage_shouldThrowIOException_whenCloudinaryFails")
    void deleteImage_shouldThrowIOException_whenCloudinaryFails() throws IOException {
        given(cloudinary.uploader()).willReturn(uploader);
        given(uploader.destroy(any(String.class), any(Map.class)))
                .willThrow(new IOException("destroy failed"));

        assertThatThrownBy(() -> cloudinaryService.deleteImage("bad/id"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("destroy failed");
    }
}
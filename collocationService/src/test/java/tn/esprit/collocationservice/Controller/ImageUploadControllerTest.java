package tn.esprit.collocationservice.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.collocationservice.Service.FileStorageService;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageUploadController Unit Tests")
class ImageUploadControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FileStorageService storageService;

    @InjectMocks
    private ImageUploadController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("POST /imagesColloc/upload should return 200 with filenames")
    void upload_shouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "test.png", "image/png", "data".getBytes());
        when(storageService.store(any())).thenReturn("stored_test.png");

        mockMvc.perform(multipart("/imagesColloc/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("stored_test.png"));
    }

    @Test
    @DisplayName("POST /imagesColloc/upload should return 500 on IOException")
    void upload_onIOException_shouldReturn500() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "test.png", "image/png", "data".getBytes());
        when(storageService.store(any())).thenThrow(new IOException("Storage failed"));

        mockMvc.perform(multipart("/imagesColloc/upload").file(file))
                .andExpect(status().isInternalServerError());
    }
}

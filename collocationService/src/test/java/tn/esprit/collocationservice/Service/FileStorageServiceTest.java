package tn.esprit.collocationservice.Service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FileStorageService Unit Tests")
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("store() should save file to disk and return filename")
    void store_shouldSaveFile() throws IOException {
        // Arrange
        FileStorageService service = new FileStorageService();
        // Override the root path to use tempDir
        ReflectionTestUtils.setField(service, "root", tempDir);

        MockMultipartFile file = new MockMultipartFile("file", "test file.png", "image/png", "content".getBytes());

        // Act
        String filename = service.store(file);

        // Assert
        assertThat(filename).contains("test_file.png");
        assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
        assertThat(Files.readAllBytes(tempDir.resolve(filename))).isEqualTo("content".getBytes());
    }
}

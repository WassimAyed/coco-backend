package tn.esprit.collocationservice.Service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MultipartInputStreamFileResource Unit Tests")
class MultipartInputStreamFileResourceTest {

    @Test
    @DisplayName("should return correct filename and length")
    void resource_shouldReturnCorrectMetadata() throws IOException {
        byte[] data = "test content".getBytes();
        InputStream is = new ByteArrayInputStream(data);
        MultipartInputStreamFileResource resource = new MultipartInputStreamFileResource(is, "test.txt");

        assertThat(resource.getFilename()).isEqualTo("test.txt");
        assertThat(resource.contentLength()).isEqualTo(-1);
        assertThat(resource.getInputStream()).isEqualTo(is);
    }

    @Test
    @DisplayName("should compare resources by filename")
    void equalsAndHashCode_shouldUseFilename() {
        MultipartInputStreamFileResource resource = new MultipartInputStreamFileResource(
                new ByteArrayInputStream("a".getBytes()), "same.txt");
        MultipartInputStreamFileResource sameFilename = new MultipartInputStreamFileResource(
                new ByteArrayInputStream("b".getBytes()), "same.txt");
        MultipartInputStreamFileResource differentFilename = new MultipartInputStreamFileResource(
                new ByteArrayInputStream("c".getBytes()), "other.txt");

        assertThat(resource)
                .isEqualTo(resource)
                .isEqualTo(sameFilename)
                .isNotEqualTo(differentFilename)
                .isNotEqualTo("same.txt")
                .hasSameHashCodeAs(sameFilename);
    }
}

package tn.esprit.collocationservice.Service;

import org.springframework.core.io.InputStreamResource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class MultipartInputStreamFileResource extends InputStreamResource {

    private final String filename;

    public MultipartInputStreamFileResource(InputStream inputStream, String filename) {
        super(inputStream);
        this.filename = filename;
    }

    @Override
    public String getFilename() {
        return this.filename;
    }

    @Override
    public long contentLength() throws IOException {
        return -1; // unknown length
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MultipartInputStreamFileResource that)) {
            return false;
        }
        return Objects.equals(filename, that.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename);
    }
}

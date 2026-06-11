package tn.esprit.collocationservice.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class FileStorageService {

    // Save inside collocationService module
    private final Path root = Paths.get("collocationService", "imagesColloc");

    public FileStorageService() throws IOException {
        Files.createDirectories(root); // create folder if not exists
    }

    public String store(MultipartFile file) throws IOException {
        String filename = System.currentTimeMillis()
                + "_" + file.getOriginalFilename().replace(" ", "_");

        log.info("Saving to: {}", root.toAbsolutePath());

        Files.copy(file.getInputStream(), root.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }
}

package tn.esprit.covoiturageservice.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final Path root = Paths.get("covoiturageService", "imagesVehicules");

    public FileStorageService() throws IOException {
        Files.createDirectories(root);
    }

    public String store(MultipartFile file) throws IOException {
        String filename = System.currentTimeMillis()
                + "_" + file.getOriginalFilename().replace(" ", "_");
        Files.copy(file.getInputStream(), root.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    public void delete(String filename) {
        try {
            Files.deleteIfExists(root.resolve(filename));
        } catch (IOException e) {
            // ignore
        }
    }
}

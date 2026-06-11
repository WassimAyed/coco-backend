package tn.esprit.collocationservice.Controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.collocationservice.Service.FileStorageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/imagesColloc")
@RequiredArgsConstructor
public class ImageUploadController {

    private final FileStorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<List<String>> upload(
            @RequestParam("files") List<MultipartFile> files) {

        List<String> names = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                names.add(storageService.store(file));
            } catch (IOException e) {
                log.error("Failed to store file {}", file.getOriginalFilename(), e);
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build();
            }
        }

        return ResponseEntity.ok(names);
    }
}

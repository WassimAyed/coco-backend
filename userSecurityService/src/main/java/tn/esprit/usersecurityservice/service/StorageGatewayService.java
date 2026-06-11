package tn.esprit.usersecurityservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.usersecurityservice.dto.ImageUploadResponse;

import java.io.IOException;
import java.util.Map;

@Service
public class StorageGatewayService {
    private final RestClient restClient;

    @Value("${app.storage.base-url}")
    private String storageBaseUrl;

    @Value("${app.storage.upload-path:/api/minio/upload}")
    private String uploadPath;

    @Value("${app.storage.profile-folder:profile-images}")
    private String profileFolder;

    @Value("${app.storage.signal-folder:signal-images}")
    private String signalFolder;

    public StorageGatewayService() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(15000);
        requestFactory.setReadTimeout(30000);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public ImageUploadResponse uploadProfileImage(MultipartFile file, Long userId) {
        return upload(file, buildUserScopedFolder(userId, profileFolder));
    }

    public ImageUploadResponse uploadSignalImage(MultipartFile file, Long userId) {
        return upload(file, buildUserScopedFolder(userId, signalFolder));
    }

    private ImageUploadResponse upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        try {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);
            body.add("folder", folder);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(joinUrl(storageBaseUrl, uploadPath))
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            Map<String, Object> data = asMap(response.get("data"));
            String objectKey = readString(data, "objectKey");
            if (objectKey == null || objectKey.isBlank()) {
                throw new IllegalStateException("Storage server did not return objectKey");
            }

            return new ImageUploadResponse(buildContentUrl(objectKey), objectKey);
        } catch (IOException exception) {
            throw new RuntimeException("Unable to read image file", exception);
        }
    }

    private String buildContentUrl(String objectKey) {
        return joinUrl(storageBaseUrl, "/api/minio/" + objectKey + "/content");
    }

    private String buildUserScopedFolder(Long userId, String leafFolder) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required for storage uploads");
        }

        String normalizedLeafFolder = leafFolder == null ? "" : leafFolder.trim().replaceAll("^/+", "").replaceAll("/+$", "");
        return normalizedLeafFolder.isBlank()
                ? String.valueOf(userId)
                : userId + "/" + normalizedLeafFolder;
    }

    private String joinUrl(String baseUrl, String path) {
        String normalizedBase = baseUrl.replaceAll("/+$", "");
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalStateException("Unexpected storage server response");
    }

    private String readString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value instanceof String stringValue ? stringValue : null;
    }
}

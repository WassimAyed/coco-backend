package tn.esprit.serviceetudiant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.serviceetudiant.dto.CoverUploadResponse;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StorageGatewayService {

    private final RestTemplate restTemplate;

    @Value("${storage.server.base-url}")
    private String storageServerBaseUrl;

    @Value("${storage.server.upload-path}")
    private String storageUploadPath;

    @Value("${storage.server.banner-folder}")
    private String bannerFolder;

    @Value("${storage.server.chat-folder}")
    private String chatFolder;

    public CoverUploadResponse uploadBanner(MultipartFile file, Long ownerId) {
        return uploadFile(file, resolveFolder(bannerFolder, ownerId), "Banner image file is required.");
    }

    public CoverUploadResponse uploadChatImage(MultipartFile file, Long ownerId) {
        return uploadFile(file, resolveFolder(chatFolder, ownerId), "Chat image file is required.");
    }

    private CoverUploadResponse uploadFile(MultipartFile file, String folder, String emptyFileMessage) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(emptyFileMessage);
        }

        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
            });
            body.add("folder", folder);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    joinUrl(storageServerBaseUrl, storageUploadPath),
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    new ParameterizedTypeReference<>() {
                    }
            );

            Map<String, Object> root = response.getBody();
            Map<String, Object> data = asMap(root != null ? root.get("data") : null);
            if (data == null) {
                throw new IllegalArgumentException("Storage server returned an invalid upload response.");
            }

            return new CoverUploadResponse(
                    readString(data.get("objectKey")),
                    firstNonBlank(readString(data.get("presignedDownloadUrl")), readString(data.get("imageUrl"))),
                    readString(data.get("fileName")),
                    readLong(data.get("size")),
                    readString(data.get("mimeType"))
            );
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read the banner image before upload.", ex);
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Unable to upload the file to the storage service.", ex);
        }
    }

    private String joinUrl(String baseUrl, String path) {
        String normalizedBase = baseUrl.replaceAll("/+$", "");
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> ? (Map<String, Object>) value : null;
    }

    private String readString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private long readLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }

    private String firstNonBlank(String first, String fallback) {
        return first != null && !first.isBlank() ? first : fallback;
    }

    private String resolveFolder(String configuredFolder, Long ownerId) {
        String normalizedFolder = configuredFolder == null ? "" : configuredFolder.trim().replaceAll("^/+", "").replaceAll("/+$", "");
        if (ownerId == null || ownerId <= 0) {
            return normalizedFolder;
        }
        return ownerId + "/" + normalizedFolder;
    }
}

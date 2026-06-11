package tn.esprit.lostfoundservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiSimilarityClient {

    @Value("${ai.similarity.url:http://localhost:8000}")
    private String aiApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public void indexItem(Long postId, String postType, String title, String description, String category, String imagePath) {
        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                log.warn("Image file not found at path: {}. Skipping AI indexing.", imagePath);
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("post_id", postId);
            body.add("post_type", postType);
            body.add("title", title);
            body.add("description", description);
            body.add("category", category);
            body.add("image", new FileSystemResource(file));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    aiApiUrl + "/v1/object-similarity/index",
                    requestEntity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            log.info("AI Indexing response for post {}: {}", postId, response.getBody());
        } catch (Exception e) {
            log.error("Failed to index item in AI service", e);
        }
    }

    @SuppressWarnings("unchecked")
        public List<Map<String, Object>> proposeSimilarItems(
            Long postId,
            String postType,
            String category,
            String title,
            String description,
            String imagePath,
            int topK
        ) {
        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                log.warn("Image file not found at path: {}. Skipping AI proposal.", imagePath);
                return Collections.emptyList();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("post_id", postId);
            body.add("post_type", postType);
            if (category != null) {
                body.add("category", category);
            }
            if (title != null) {
                body.add("title", title);
            }
            if (description != null) {
                body.add("description", description);
            }
            body.add("top_k", topK);
            body.add("image", new FileSystemResource(file));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    aiApiUrl + "/v1/object-similarity/propose",
                    requestEntity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            if (response.getBody() != null && response.getBody().containsKey("candidates")) {
                Object candidatesObject = response.getBody().get("candidates");
                if (candidatesObject instanceof List<?>) {
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) candidatesObject;
                    log.info("AI Proposed {} candidates for post {}", candidates.size(), postId);
                    return candidates;
                }
            }
        } catch (Exception e) {
            log.error("Failed to get proposals from AI service", e);
        }
        return Collections.emptyList();
    }
}

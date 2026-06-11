package tn.esprit.eventservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * Service de géocodage utilisant l'API Nominatim d'OpenStreetMap.
 * Convertit une adresse textuelle en coordonnées GPS (lat, lon) + adresse complète.
 *
 * Endpoint Nominatim utilisé :
 *   GET https://nominatim.openstreetmap.org/search?q={adresse}&format=json&limit=1
 */
@Service
public class GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Géocode une adresse libre et retourne lat, lon, display_name.
     *
     * @param address adresse à géocoder (ex: "Ariana, Tunisie")
     * @return map avec clés "lat", "lon", "display_name", ou null si non trouvé
     */
    public Map<String, String> getCoordinates(String address) {
        if (address == null || address.isBlank()) return Collections.emptyMap();

        String encodedAddress = address.replace(" ", "+");
        String url = String.format(NOMINATIM_URL, encodedAddress);

        // Nominatim exige un User-Agent valide (RFC obligatoire)
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "SpringBootEventService/1.0");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());

            if (root == null || root.isEmpty()) {
                return Collections.emptyMap(); // adresse introuvable
            }

            JsonNode node = root.get(0);

            Map<String, String> result = new HashMap<>();
            result.put("lat", node.get("lat").asText());
            result.put("lon", node.get("lon").asText());
            result.put("display_name", node.get("display_name").asText());

            return result;

        } catch (Exception e) {
            // Log l'erreur sans faire planter la création d'événement
            log.warn("Nominatim lookup failed for address: {}", address, e);
            return Collections.emptyMap();
        }
    }
}

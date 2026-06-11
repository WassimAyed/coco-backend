package tn.esprit.collocationservice.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.collocationservice.Entity.UserActivityLog;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Entity.collocOffreRequest;
import tn.esprit.collocationservice.Repository.UserActivityLogRepository;
import tn.esprit.collocationservice.Repository.collocOffreRepo;
import tn.esprit.collocationservice.Repository.collocOffreRequestRepo;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartFeatureService {
    private final RestTemplate restTemplate;
    private final UserActivityLogRepository activityLogRepository;
    private final collocOffreRepo offreRepository;
    private final collocOffreRequestRepo requestRepository;

    private static final String AI_SERVICE_URL = "http://localhost:5001";
    private static final String DEFAULT_CITY = "Tunis";

    public List<Long> getRecommendations(Long userId) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("age", 22);
        user.put("budget", 600.0);
        user.put("city", DEFAULT_CITY);
        user.put("smoker", false);
        user.put("pets", false);
        user.put("cleanliness", 3);
        user.put("sleepSchedule", "night_owl");
        user.put("studyLevel", "master");
        user.put("interests", Arrays.asList("gaming", "music"));

        List<collocOffre> allOffers = offreRepository.findAll();
        List<Map<String, Object>> offers = allOffers.stream().map(o -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", o.getId());
            map.put("prixLoc", o.getPrixLoc() != null ? o.getPrixLoc() : 500.0);
            map.put("ville", o.getVille() != null ? o.getVille() : DEFAULT_CITY);
            map.put("chambres", o.getChambres() != null ? o.getChambres() : 1);
            map.put("meublee", o.getMeublee() == null || o.getMeublee());
            map.put("latitude", o.getLatitude() != null ? o.getLatitude() : 0.0);
            map.put("longitude", o.getLongitude() != null ? o.getLongitude() : 0.0);
            return map;
        }).toList();

        List<UserActivityLog> logs = activityLogRepository.findByUserId(userId);
        List<Long> activityIds = logs.stream().map(UserActivityLog::getOfferId).toList();

        Map<String, Object> payload = new HashMap<>();
        payload.put("user", user);
        payload.put("offers", offers);
        payload.put("user_activity_offer_ids", activityIds);

        try {
            Long[] recommended = restTemplate.postForObject(AI_SERVICE_URL + "/recommend/" + userId, payload, Long[].class);
            return recommended != null ? Arrays.asList(recommended) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch recommendations for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public Object rankApplicants(Long offerId) {
        Optional<collocOffre> offerOpt = offreRepository.findById(offerId);
        if (offerOpt.isEmpty()) return Collections.emptyList();
        collocOffre offer = offerOpt.get();

        List<collocOffreRequest> requests = requestRepository.findByOfferId(offerId);
        List<Map<String, Object>> applicants = new ArrayList<>();

        for(collocOffreRequest req : requests) {
            Map<String, Object> app = new HashMap<>();
            app.put("studentId", req.getStudentId());
            double profileCompleteness = 0.7 + (req.getStudentId() % 10) * 0.03;
            double responseRate = 0.6 + (req.getStudentId() % 5) * 0.08;
            double budget = 400.0 + (req.getStudentId() % 15) * 50.0;
            
            app.put("profileCompleteness", profileCompleteness);
            app.put("responseRate", responseRate);
            app.put("budget", budget);
            
            long accepted = requestRepository.countByStudentIdAndStatus(req.getStudentId(), collocOffreRequest.Status.ACCEPTEE);
            long total = requestRepository.countByStudentId(req.getStudentId());
            app.put("acceptedRatio", total > 0 ? (double) accepted / total : 0.0);

            applicants.add(app);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("offerPrice", offer.getPrixLoc() != null ? offer.getPrixLoc() : 500.0);
        payload.put("offerCity", offer.getVille() != null ? offer.getVille() : DEFAULT_CITY);
        payload.put("applicants", applicants);

        try {
            return restTemplate.postForObject(AI_SERVICE_URL + "/rank-applicants/" + offerId, payload, Object.class);
        } catch (Exception e) {
            log.error("Failed to fetch ranking for offer {}: {}", offerId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public Object getTrustScore(Long userId) {
        long accepted = requestRepository.countByStudentIdAndStatus(userId, collocOffreRequest.Status.ACCEPTEE);
        long total = requestRepository.countByStudentId(userId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("profileCompleteness", 0.95);
        payload.put("isVerified", true);
        payload.put("acceptedRatio", total > 0 ? (double) accepted / total : 0.5);
        payload.put("responseRate", 0.9);

        try {
            return restTemplate.postForObject(AI_SERVICE_URL + "/trust-score/" + userId, payload, Object.class);
        } catch (Exception e) {
            log.error("Failed to fetch trust score for user {}: {}", userId, e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("userId", userId);
            fallback.put("score", 50.0);
            fallback.put("category", "New User");
            fallback.put("color", "yellow");
            return fallback;
        }
    }

    public Object predictPrice(Object req) {
        try {
            return restTemplate.postForObject(AI_SERVICE_URL + "/predict-price", req, Object.class);
        } catch (Exception e) {
            log.error("Failed to predict price: {}", e.getMessage());
            return null;
        }
    }
}

package tn.esprit.collocationservice.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.collocationservice.Service.SmartFeatureService;

import java.util.List;

@RestController
@RequestMapping("/collocation/smart")
@RequiredArgsConstructor

public class SmartFeatureController {

    private final SmartFeatureService smartFeatureService;

    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<List<Long>> getRecommendations(@PathVariable Long userId) {
        return ResponseEntity.ok(smartFeatureService.getRecommendations(userId));
    }

    @GetMapping("/ranking/{offerId}")
    public ResponseEntity<Object> getApplicantRanking(@PathVariable Long offerId) {
        return ResponseEntity.ok(smartFeatureService.rankApplicants(offerId));
    }

    @GetMapping("/trust-score/{userId}")
    public ResponseEntity<Object> getTrustScore(@PathVariable Long userId) {
        return ResponseEntity.ok(smartFeatureService.getTrustScore(userId));
    }

    @PostMapping("/predict-price")
    public ResponseEntity<Object> predictPrice(@RequestBody Object requestParams) {
        return ResponseEntity.ok(smartFeatureService.predictPrice(requestParams));
    }
}

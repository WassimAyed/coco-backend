package tn.esprit.serviceetudiant.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.serviceetudiant.dto.ServiceRecommendationResponse;
import tn.esprit.serviceetudiant.service.MatchingService;

import java.util.List;

@RestController
@RequestMapping("/student-services/recommendations")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @GetMapping
    public ResponseEntity<List<ServiceRecommendationResponse>> getRecommendations(
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(matchingService.getRecommendations(userId));
    }
}
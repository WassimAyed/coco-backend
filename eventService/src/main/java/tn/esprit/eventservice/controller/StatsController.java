package tn.esprit.eventservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.eventservice.dto.StatsDTO;
import tn.esprit.eventservice.service.IStatsService;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final IStatsService statsService;

    public StatsController(IStatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public ResponseEntity<StatsDTO> getGlobalStats() {
        return ResponseEntity.ok(statsService.getGlobalStats());
    }
}
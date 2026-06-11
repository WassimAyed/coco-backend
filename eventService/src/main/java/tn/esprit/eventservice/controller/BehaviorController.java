package tn.esprit.eventservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventservice.dto.BehaviorDTO;
import tn.esprit.eventservice.service.BehaviorService;

@RestController
@RequestMapping("/api/behavior")
public class BehaviorController {

    private final BehaviorService behaviorService;

    public BehaviorController(BehaviorService behaviorService) {
        this.behaviorService = behaviorService;
    }

    @PostMapping
    public ResponseEntity<Void> recordBehavior(@RequestBody BehaviorDTO dto) {
        behaviorService.save(dto);
        return ResponseEntity.ok().build();
    }
}

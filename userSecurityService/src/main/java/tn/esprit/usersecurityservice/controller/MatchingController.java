package tn.esprit.usersecurityservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.usersecurityservice.dto.MatchCollocRespDTO;
import tn.esprit.usersecurityservice.dto.ProfileRequestDTO;
import tn.esprit.usersecurityservice.service.MatchingCollocService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matchingColloc")
public class MatchingController {

    @Autowired
    private MatchingCollocService matchingService;

    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping
    public MatchCollocRespDTO[] match(@RequestBody Map<String, Object> body) {

        // Convert USER
        ProfileRequestDTO user =
                mapper.convertValue(
                        body.get("user"),
                        ProfileRequestDTO.class
                );

        // Convert CANDIDATES
        List<ProfileRequestDTO> candidates =
                mapper.convertValue(
                        body.get("candidates"),
                        new TypeReference<List<ProfileRequestDTO>>() {}
                );

        return matchingService.callMatching(user, candidates);
    }
}
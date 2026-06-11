package tn.esprit.usersecurityservice.utils;

import tn.esprit.usersecurityservice.dto.MatchRequestDTOColl;
import tn.esprit.usersecurityservice.dto.ProfileRequestDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MatchingMapper {

    public static MatchRequestDTOColl toMatchRequest(
            ProfileRequestDTO user,
            List<ProfileRequestDTO> candidates) {

        MatchRequestDTOColl request = new MatchRequestDTOColl();

        request.setUser(convertProfile(user));

        request.setCandidates(
                candidates.stream()
                        .map(MatchingMapper::convertProfile)
                        .collect(Collectors.toList())
        );

        return request;
    }

    private static Map<String, Object> convertProfile(ProfileRequestDTO p) {

        Map<String, Object> map = new HashMap<>();

        map.put("id", p.getUserId());
        map.put("user_id", p.getUserId());
        map.put("age", p.getAge());
        map.put("gender", p.getGender());
        map.put("budget", p.getBudget());
        map.put("city", p.getCity());
        map.put("cleanliness", p.getCleanliness());
        map.put("sleep_schedule", p.getSleepSchedule());
        map.put("study_level", p.getStudyLevel());
        map.put("social_level", p.getSocialLevel());
        map.put("accepts_guests", p.getAcceptsGuests() ? 1 : 0);
        map.put("noise_tolerance", p.getNoiseTolerance());
        map.put("smoker", p.getSmoker() ? 1 : 0);
        map.put("pets", p.getPets() ? 1 : 0);
        map.put("latitude", p.getLatitude());
        map.put("longitude", p.getLongitude());

        return map;
    }
}
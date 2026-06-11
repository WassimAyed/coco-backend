package tn.esprit.usersecurityservice.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class MatchRequestDTOColl {

    private Map<String, Object> user;
    private List<Map<String, Object>> candidates;
}
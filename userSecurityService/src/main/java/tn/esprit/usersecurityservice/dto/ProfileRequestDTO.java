package tn.esprit.usersecurityservice.dto;

import lombok.Data;
import tn.esprit.usersecurityservice.entity.User;

import java.util.List;

@Data
public class ProfileRequestDTO {
 private Long id;

        private Long userId;
        private User user;

        private Integer age;
        private String gender;
        private Double budget;
        private String city;
        private Boolean smoker;
        private Boolean pets;
        private Integer cleanliness;
        private String sleepSchedule;
        private String studyLevel;
        private Integer socialLevel;
        private Boolean acceptsGuests;
        private Integer noiseTolerance;

        private List<String> interests;

        private Double latitude;
        private Double longitude;
}
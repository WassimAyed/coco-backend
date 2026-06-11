package tn.esprit.usersecurityservice.dto;

import lombok.Data;

@Data
public class MatchCollocRespDTO {


        private Long id;
        private Double score;

        private Integer age;
        private String gender;
        private String city;
        private Double budget;
    }




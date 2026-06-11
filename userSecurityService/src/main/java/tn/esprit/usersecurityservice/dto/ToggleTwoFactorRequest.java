package tn.esprit.usersecurityservice.dto;

import lombok.Data;

@Data
public class ToggleTwoFactorRequest {
    private boolean enabled;
}

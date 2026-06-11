package tn.esprit.usersecurityservice.dto;

public record SignalResponse(
        Long id,
        String description,
        String imageUrl,
        Long userId,
        String username,
        String createdAt
) {
}

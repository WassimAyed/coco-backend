package tn.esprit.usersecurityservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.usersecurityservice.dto.ProfileRequestDTO;
import tn.esprit.usersecurityservice.entity.User;
import tn.esprit.usersecurityservice.entity.UserProfile;
import tn.esprit.usersecurityservice.repository.UserProfileRepository;
import tn.esprit.usersecurityservice.repository.UserRepository;
import tn.esprit.usersecurityservice.validation.Validators;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository repository;
    private final UserRepository userRepository;

    // ✅ GET profile
    public UserProfile getByUserId(Long userId) {
        Validators.requirePositive(userId, "userId");
        return repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    public List<UserProfile> getAllProfiles() {
        return repository.findAll();
    }

    public UserProfile save(ProfileRequestDTO dto) {
        Validators.requireNonNull(dto, "request");
        Validators.requirePositive(dto.getUserId(), "userId");

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = UserProfile.builder()
                .user(user)
                .age(dto.getAge())
                .gender(dto.getGender())
                .budget(dto.getBudget())
                .city(dto.getCity())
                .smoker(dto.getSmoker())
                .pets(dto.getPets())
                .cleanliness(dto.getCleanliness())
                .sleepSchedule(dto.getSleepSchedule())
                .studyLevel(dto.getStudyLevel())
                .socialLevel(dto.getSocialLevel())
                .acceptsGuests(dto.getAcceptsGuests())
                .noiseTolerance(dto.getNoiseTolerance())
                .interests(dto.getInterests())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();

        return repository.save(profile);
    }

    public boolean existsByUserId(Long userId) {
        Validators.requirePositive(userId, "userId");
        return repository.findByUserId(userId).isPresent();
    }


    // ✅ UPDATE profile
    public UserProfile updateProfile(Long userId, ProfileRequestDTO dto) {
        Validators.requirePositive(userId, "userId");
        Validators.requireNonNull(dto, "request");

        // find existing profile
        UserProfile profile = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        // update fields
        profile.setAge(dto.getAge());
        profile.setGender(dto.getGender());
        profile.setBudget(dto.getBudget());
        profile.setCity(dto.getCity());
        profile.setSmoker(dto.getSmoker());
        profile.setPets(dto.getPets());
        profile.setCleanliness(dto.getCleanliness());
        profile.setSleepSchedule(dto.getSleepSchedule());
        profile.setStudyLevel(dto.getStudyLevel());
        profile.setSocialLevel(dto.getSocialLevel());
        profile.setAcceptsGuests(dto.getAcceptsGuests());
        profile.setNoiseTolerance(dto.getNoiseTolerance());
        profile.setInterests(dto.getInterests());
        profile.setLatitude(dto.getLatitude());
        profile.setLongitude(dto.getLongitude());

        return repository.save(profile);
    }


}
package tn.esprit.usersecurityservice.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.esprit.usersecurityservice.Enum.Role;
import tn.esprit.usersecurityservice.dto.UserResponse;
import tn.esprit.usersecurityservice.entity.User;
import tn.esprit.usersecurityservice.mapper.UserMapper;
import tn.esprit.usersecurityservice.repository.EmailVerificationTokenRepository;
import tn.esprit.usersecurityservice.repository.TwoFactorCodeRepository;
import tn.esprit.usersecurityservice.repository.UserRepository;
import tn.esprit.usersecurityservice.validation.Validators;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final TwoFactorCodeRepository twoFactorCodeRepository;
    private final UserMapper userMapper;

    public List<UserResponse> getAllUsers() {
        return userMapper.toResponseList(userRepository.findAll());
    }

    @Transactional
    public void setEnabled(Long userId, boolean enabled) {
        Validators.requirePositive(userId, "userId");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Cannot disable another admin");
        }

        userRepository.updateEnabledById(userId, enabled);
    }

    @Transactional
    public void deleteUser(Long userId) {
        Validators.requirePositive(userId, "userId");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Cannot delete admin account");
        }
        twoFactorCodeRepository.deleteByUser(user);
        emailVerificationTokenRepository.deleteByUser(user);
        userRepository.delete(user);
    }
}

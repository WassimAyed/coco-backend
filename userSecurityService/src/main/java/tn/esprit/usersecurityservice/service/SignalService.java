package tn.esprit.usersecurityservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.usersecurityservice.Enum.Role;
import tn.esprit.usersecurityservice.dto.SignalResponse;
import tn.esprit.usersecurityservice.entity.Signal;
import tn.esprit.usersecurityservice.entity.User;
import tn.esprit.usersecurityservice.mapper.SignalMapper;
import tn.esprit.usersecurityservice.repository.SignalRepository;
import tn.esprit.usersecurityservice.validation.Validators;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SignalService {
    private final SignalRepository signalRepository;
    private final UserService userService;
    private final StorageGatewayService storageGatewayService;
    private final SignalMapper signalMapper;

    @Transactional
    public SignalResponse create(String description, MultipartFile image) {
        Validators.requireNonBlank(description, "description");
        Validators.requireMaxLength(description, Validators.MAX_DESCRIPTION_LENGTH, "description");
        if (image != null && !image.isEmpty()) {
            Validators.requireImage(image, Validators.MAX_IMAGE_BYTES);
        }

        User current = userService.getCurrentUser();
        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            imageUrl = storageGatewayService.uploadSignalImage(image, current.getId()).imageUrl();
        }

        Signal signal = Signal.builder()
                .description(description)
                .imageUrl(imageUrl)
                .user(current)
                .build();

        Signal saved = signalRepository.save(signal);

        return signalMapper.toResponse(saved);
    }


    public List<SignalResponse> getMine() {
        User current = userService.getCurrentUser();
        return signalRepository.findAll().stream()
                .filter(signal -> signal.getUser().getId().equals(current.getId()))
                .map(signalMapper::toResponse)
                .toList();
    }

    public List<SignalResponse> getAll() {
        return signalRepository.findAll().stream().map(signalMapper::toResponse).toList();
    }

    @Transactional
    public SignalResponse update(Long signalId, String description, MultipartFile image) {
        Validators.requirePositive(signalId, "signalId");
        if (description != null) {
            Validators.requireMaxLength(description, Validators.MAX_DESCRIPTION_LENGTH, "description");
        }
        if (image != null && !image.isEmpty()) {
            Validators.requireImage(image, Validators.MAX_IMAGE_BYTES);
        }

        User current = userService.getCurrentUser();
        Signal signal = getAuthorizedSignal(signalId, current);

        if (description != null) {
            signal.setDescription(description);
        }

        if (image != null && !image.isEmpty()) {
            signal.setImageUrl(storageGatewayService.uploadSignalImage(image, current.getId()).imageUrl());
        }

        Signal saved = signalRepository.save(signal);
        return signalMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long signalId) {
        Validators.requirePositive(signalId, "signalId");
        User current = userService.getCurrentUser();
        Signal signal = getAuthorizedSignal(signalId, current);

        signalRepository.delete(signal);
    }

    private Signal getAuthorizedSignal(Long signalId, User current) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new RuntimeException("Signal not found"));

        boolean isOwner = signal.getUser().getId().equals(current.getId());
        boolean isAdmin = current.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("You are not allowed to modify this signal");
        }

        return signal;
    }

}

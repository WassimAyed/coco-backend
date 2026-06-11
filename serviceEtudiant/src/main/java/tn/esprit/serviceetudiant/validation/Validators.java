package tn.esprit.serviceetudiant.validation;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.serviceetudiant.exception.ValidationException;

import java.util.List;
import java.util.Locale;

/**
 * Defensive guards used by the service layer (defense-in-depth alongside @Valid in controllers).
 * Each method throws {@link ValidationException} (HTTP 400) with a field-named message.
 */
public final class Validators {

    public static final int MAX_TAG_LENGTH = 40;
    public static final int MAX_TAG_COUNT = 20;
    public static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    public static final int MAX_PREFERRED_DATE_LENGTH = 100;

    private Validators() {
    }

    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " must not be null.");
        }
        return value;
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " must not be blank.");
        }
        return value;
    }

    public static Long requirePositive(Long value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " must not be null.");
        }
        if (value <= 0L) {
            throw new ValidationException(fieldName + " must be a positive number.");
        }
        return value;
    }

    public static Long requirePositiveOrNull(Long value, String fieldName) {
        if (value != null && value <= 0L) {
            throw new ValidationException(fieldName + " must be a positive number when provided.");
        }
        return value;
    }

    public static String requireMaxLength(String value, int max, String fieldName) {
        if (value != null && value.length() > max) {
            throw new ValidationException(fieldName + " must not exceed " + max + " characters.");
        }
        return value;
    }

    public static <E> List<E> requireListSize(List<E> list, int max, String fieldName) {
        if (list != null && list.size() > max) {
            throw new ValidationException(fieldName + " must contain at most " + max + " items.");
        }
        return list;
    }

    public static MultipartFile requireImage(MultipartFile file, long maxBytes) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("file must not be empty.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new ValidationException("file must be an image (received content-type: " + contentType + ").");
        }
        if (file.getSize() > maxBytes) {
            throw new ValidationException("file must not exceed " + (maxBytes / (1024 * 1024)) + " MB.");
        }
        return file;
    }
}
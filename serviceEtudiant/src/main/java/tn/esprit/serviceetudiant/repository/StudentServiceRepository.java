package tn.esprit.serviceetudiant.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.serviceetudiant.entity.StudentService;

import java.util.List;
import java.util.Optional;

public interface StudentServiceRepository extends JpaRepository<StudentService, Long> {
    Optional<StudentService> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
    List<StudentService> findByProviderIdOrderByUpdatedAtDesc(Long providerId);
    List<StudentService> findAll(Sort sort);
}
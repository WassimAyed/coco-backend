package tn.esprit.usersecurityservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.usersecurityservice.entity.Signal;

import java.util.List;
import java.util.Optional;

@Repository
public interface SignalRepository extends JpaRepository<Signal, Long> {
    List<Signal> findByUserId(Long userId);

    Optional<Signal> findById(Long id);

    List<Signal> id(Long id);
}

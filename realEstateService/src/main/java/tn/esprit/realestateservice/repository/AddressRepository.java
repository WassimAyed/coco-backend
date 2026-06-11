package tn.esprit.realestateservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.realestateservice.entity.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}

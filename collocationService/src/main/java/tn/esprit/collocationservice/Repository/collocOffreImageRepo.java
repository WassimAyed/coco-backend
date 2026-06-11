package tn.esprit.collocationservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tn.esprit.collocationservice.Entity.*;

public interface collocOffreImageRepo  extends JpaRepository<collocOffreImage, Long>,
        JpaSpecificationExecutor<collocOffreImage> {
}

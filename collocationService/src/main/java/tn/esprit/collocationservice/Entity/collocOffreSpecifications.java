package tn.esprit.collocationservice.Entity;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class collocOffreSpecifications {

    private collocOffreSpecifications() {
    }

    public static Specification<collocOffre> filter(collocOffreFilter f) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if(f.getMinPrixLoc()!=null)
                predicates.add(cb.ge(root.get("prixLoc"), f.getMinPrixLoc()));

            if(f.getMaxPrixLoc()!=null)
                predicates.add(cb.le(root.get("prixLoc"), f.getMaxPrixLoc()));

            if(f.getVille()!=null)
                predicates.add(cb.equal(root.get("ville"), f.getVille()));

            if(f.getMeublee()!=null)
                predicates.add(cb.equal(root.get("meublee"), f.getMeublee()));

            if(f.getMinChambres()!=null)
                predicates.add(cb.ge(root.get("chambres"), f.getMinChambres()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

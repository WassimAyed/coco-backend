package tn.esprit.collocationservice.Entity;

import lombok.Data;

@Data
public class collocOffreFilter {
    private Double minPrixLoc;
    private Double maxPrixLoc;
    private String ville;
    private Boolean meublee;
    private Integer minChambres;

}


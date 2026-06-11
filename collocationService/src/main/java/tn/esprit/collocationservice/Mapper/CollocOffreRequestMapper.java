package tn.esprit.collocationservice.Mapper;

import tn.esprit.collocationservice.Dto.CollocOffreRequestDTO;
import tn.esprit.collocationservice.Entity.collocOffreRequest;

public class CollocOffreRequestMapper {

    private CollocOffreRequestMapper() {
    }

    public static CollocOffreRequestDTO toDTO(collocOffreRequest entity) {
        if (entity == null) return null;
        return CollocOffreRequestDTO.builder()
                .id(entity.getId())
                .studentId(entity.getStudentId())
                .offerId(entity.getOffer() != null ? entity.getOffer().getId() : null)
                .offerTitle(entity.getOffer() != null ? entity.getOffer().getTitre() : null)
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static collocOffreRequest toEntity(CollocOffreRequestDTO dto) {
        if (dto == null) return null;
        collocOffreRequest entity = new collocOffreRequest();
        entity.setId(dto.getId());
        entity.setStudentId(dto.getStudentId());
        entity.setStatus(dto.getStatus());
        entity.setCreatedAt(dto.getCreatedAt());
        // Offer should be handled by service
        return entity;
    }
}

package tn.esprit.collocationservice.Mapper;

import tn.esprit.collocationservice.Dto.CollocOffreDTO;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Entity.collocOffreImage;

import java.util.Collections;
import java.util.List;

public class CollocOffreMapper {

    private CollocOffreMapper() {
    }

    public static collocOffre toEntity(CollocOffreDTO dto) {
        if (dto == null) return null;
        collocOffre entity = new collocOffre();
        entity.setId(dto.getId());
        entity.setTitre(dto.getTitre());
        entity.setDescription(dto.getDescription());
        entity.setPrixLoc(dto.getPrixLoc());
        entity.setVille(dto.getVille());
        entity.setChambres(dto.getChambres());
        entity.setMeublee(dto.getMeublee());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setExpiryDate(dto.getExpiryDate());
        entity.setOwnerId(dto.getOwnerId());
        entity.setNotified(dto.getNotified());
        return entity;
    }

    public static CollocOffreDTO toDTO(collocOffre entity) {
        if (entity == null) return null;
        return CollocOffreDTO.builder()
                .id(entity.getId())
                .titre(entity.getTitre())
                .description(entity.getDescription())
                .prixLoc(entity.getPrixLoc())
                .ville(entity.getVille())
                .chambres(entity.getChambres())
                .meublee(entity.getMeublee())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .createdAt(entity.getCreatedAt())
                .expiryDate(entity.getExpiryDate())
                .ownerId(entity.getOwnerId())
                .notified(entity.getNotified())
                .imagesColoc(mapImages(entity.getImagesColoc()))
                .build();
    }

    private static List<CollocOffreDTO.CollocOffreImageDTO> mapImages(List<collocOffreImage> images) {
        if (images == null) return Collections.emptyList();
        return images.stream()
                .map(img -> CollocOffreDTO.CollocOffreImageDTO.builder()
                        .id(img.getId())
                        .filename(img.getFilename())
                        .url(img.getUrl())
                        .build())
                .toList();
    }
}

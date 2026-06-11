package tn.esprit.collocationservice.Mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tn.esprit.collocationservice.Dto.CollocOffreDTO;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Entity.collocOffreImage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CollocOffreMapper Unit Tests")
class CollocOffreMapperTest {

    @Test
    @DisplayName("should return null when source is null")
    void mappingNullValues_shouldReturnNull() {
        assertThat(CollocOffreMapper.toEntity(null)).isNull();
        assertThat(CollocOffreMapper.toDTO(null)).isNull();
    }

    @Test
    @DisplayName("should map DTO fields to entity")
    void toEntity_shouldMapFields() {
        LocalDate createdAt = LocalDate.now();
        LocalDate expiryDate = createdAt.plusDays(10);
        CollocOffreDTO dto = CollocOffreDTO.builder()
                .id(1L)
                .titre("Studio")
                .description("Bright studio")
                .prixLoc(700.0)
                .ville("Tunis")
                .chambres(2)
                .meublee(true)
                .latitude(36.8)
                .longitude(10.2)
                .createdAt(createdAt)
                .expiryDate(expiryDate)
                .ownerId(100L)
                .notified(false)
                .build();

        collocOffre entity = CollocOffreMapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getTitre()).isEqualTo("Studio");
        assertThat(entity.getDescription()).isEqualTo("Bright studio");
        assertThat(entity.getPrixLoc()).isEqualTo(700.0);
        assertThat(entity.getVille()).isEqualTo("Tunis");
        assertThat(entity.getChambres()).isEqualTo(2);
        assertThat(entity.getMeublee()).isTrue();
        assertThat(entity.getLatitude()).isEqualTo(36.8);
        assertThat(entity.getLongitude()).isEqualTo(10.2);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getExpiryDate()).isEqualTo(expiryDate);
        assertThat(entity.getOwnerId()).isEqualTo(100L);
        assertThat(entity.getNotified()).isFalse();
    }

    @Test
    @DisplayName("should map entity images to DTO images")
    void toDTO_shouldMapImages() {
        collocOffreImage image = new collocOffreImage();
        image.setId(7L);
        image.setFilename("room.jpg");
        image.setUrl("/uploads/room.jpg");

        collocOffre entity = new collocOffre();
        entity.setId(1L);
        entity.setTitre("Studio");
        entity.setImagesColoc(List.of(image));

        CollocOffreDTO dto = CollocOffreMapper.toDTO(entity);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitre()).isEqualTo("Studio");
        assertThat(dto.getImagesColoc()).hasSize(1);
        assertThat(dto.getImagesColoc().get(0).getId()).isEqualTo(7L);
        assertThat(dto.getImagesColoc().get(0).getFilename()).isEqualTo("room.jpg");
        assertThat(dto.getImagesColoc().get(0).getUrl()).isEqualTo("/uploads/room.jpg");
    }

    @Test
    @DisplayName("should use empty image list when entity images are null")
    void toDTO_whenImagesAreNull_shouldUseEmptyList() {
        collocOffre entity = new collocOffre();

        CollocOffreDTO dto = CollocOffreMapper.toDTO(entity);

        assertThat(dto.getImagesColoc()).isEmpty();
    }
}

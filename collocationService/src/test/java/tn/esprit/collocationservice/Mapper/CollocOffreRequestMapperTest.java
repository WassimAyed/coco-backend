package tn.esprit.collocationservice.Mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tn.esprit.collocationservice.Dto.CollocOffreRequestDTO;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Entity.collocOffreRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CollocOffreRequestMapper Unit Tests")
class CollocOffreRequestMapperTest {

    @Test
    @DisplayName("should return null when source is null")
    void mappingNullValues_shouldReturnNull() {
        assertThat(CollocOffreRequestMapper.toDTO(null)).isNull();
        assertThat(CollocOffreRequestMapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("should map entity with offer to DTO")
    void toDTO_withOffer_shouldMapOfferFields() {
        collocOffre offer = new collocOffre();
        offer.setId(3L);
        offer.setTitre("Studio Lac");

        collocOffreRequest request = new collocOffreRequest();
        request.setId(9L);
        request.setStudentId(20L);
        request.setOffer(offer);
        request.setStatus(collocOffreRequest.Status.ACCEPTEE);
        request.setCreatedAt(LocalDateTime.of(2026, 4, 24, 10, 0));

        CollocOffreRequestDTO dto = CollocOffreRequestMapper.toDTO(request);

        assertThat(dto.getId()).isEqualTo(9L);
        assertThat(dto.getStudentId()).isEqualTo(20L);
        assertThat(dto.getOfferId()).isEqualTo(3L);
        assertThat(dto.getOfferTitle()).isEqualTo("Studio Lac");
        assertThat(dto.getStatus()).isEqualTo(collocOffreRequest.Status.ACCEPTEE);
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 4, 24, 10, 0));
    }

    @Test
    @DisplayName("should map entity without offer to DTO with null offer fields")
    void toDTO_withoutOffer_shouldUseNullOfferFields() {
        collocOffreRequest request = new collocOffreRequest();

        CollocOffreRequestDTO dto = CollocOffreRequestMapper.toDTO(request);

        assertThat(dto.getOfferId()).isNull();
        assertThat(dto.getOfferTitle()).isNull();
    }

    @Test
    @DisplayName("should map DTO fields to entity")
    void toEntity_shouldMapFields() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 24, 11, 0);
        CollocOffreRequestDTO dto = CollocOffreRequestDTO.builder()
                .id(15L)
                .studentId(30L)
                .status(collocOffreRequest.Status.REJETEE)
                .createdAt(createdAt)
                .build();

        collocOffreRequest entity = CollocOffreRequestMapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(15L);
        assertThat(entity.getStudentId()).isEqualTo(30L);
        assertThat(entity.getStatus()).isEqualTo(collocOffreRequest.Status.REJETEE);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }
}

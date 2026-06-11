package tn.esprit.eventservice.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryDtoTest {

    @Test
    @DisplayName("builder_shouldSetAllFields_whenValidInput")
    void builder_shouldSetAllFields_whenValidInput() {
        // Given
        Long expectedId = 1L;
        String expectedName = "Sport";
        String expectedDescription = "Sports events";

        // When
        CategoryDTO dto = CategoryDTO.builder()
                .id(expectedId)
                .name(expectedName)
                .description(expectedDescription)
                .build();

        // Then
        assertThat(dto)
                .extracting(CategoryDTO::getId, CategoryDTO::getName, CategoryDTO::getDescription)
                .containsExactly(expectedId, expectedName, expectedDescription);
    }

    @Test
    @DisplayName("setters_shouldUpdateFields_whenUsingNoArgsConstructor")
    void setters_shouldUpdateFields_whenUsingNoArgsConstructor() {
        // Given
        CategoryDTO dto = new CategoryDTO();

        // When
        dto.setId(2L);
        dto.setName("Music");
        dto.setDescription("Music events");

        // Then
        assertThat(dto.getName()).isEqualTo("Music");
    }
}

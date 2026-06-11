package tn.esprit.eventservice.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tn.esprit.eventservice.dto.CategoryDTO;
import tn.esprit.eventservice.service.ICategoryService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private ICategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    @DisplayName("create_shouldReturn201_whenPayloadIsValid")
    void create_shouldReturn201_whenPayloadIsValid() {
        // Given
        CategoryDTO request = CategoryDTO.builder().name("Sport").description("Sports events").build();
        CategoryDTO response = CategoryDTO.builder().id(1L).name("Sport").description("Sports events").build();
        given(categoryService.createCategory(request)).willReturn(response);

        // When
        ResponseEntity<CategoryDTO> result = categoryController.create(request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getById_shouldReturn200_whenCategoryExists")
    void getById_shouldReturn200_whenCategoryExists() {
        // Given
        CategoryDTO response = CategoryDTO.builder().id(2L).name("Music").description("Music events").build();
        given(categoryService.getCategoryById(2L)).willReturn(response);

        // When
        ResponseEntity<CategoryDTO> result = categoryController.getById(2L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getName()).isEqualTo("Music");
    }

    @Test
    @DisplayName("getAll_shouldReturn200_whenCategoriesAvailable")
    void getAll_shouldReturn200_whenCategoriesAvailable() {
        // Given
        given(categoryService.getAllCategories()).willReturn(List.of(
                CategoryDTO.builder().id(1L).name("Sport").description("Sports events").build()
        ));

        // When
        ResponseEntity<List<CategoryDTO>> result = categoryController.getAll();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("delete_shouldReturn204_whenCategoryExists")
    void delete_shouldReturn204_whenCategoryExists() {
        // Given + When
        ResponseEntity<Void> result = categoryController.delete(5L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(categoryService).deleteCategory(5L);
    }
}

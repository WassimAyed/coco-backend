package tn.esprit.eventservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventservice.dto.*;
import tn.esprit.eventservice.entity.*;
import tn.esprit.eventservice.exception.BusinessException;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock CategoryRepository categoryRepository;
    @InjectMocks CategoryServiceImpl categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Sport");
        category.setDescription("Sports events");
    }

    @Test
    void shouldCreateCategory() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Sport");
        dto.setDescription("Sports events");

        when(categoryRepository.existsByNameIgnoreCase("Sport")).thenReturn(false);
        when(categoryRepository.save(any())).thenReturn(category);

        CategoryDTO result = categoryService.createCategory(dto);

        assertNotNull(result);
        assertEquals("Sport", result.getName());
        verify(categoryRepository).save(any());
    }

    @Test
    void shouldThrowWhenCategoryNameExists() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Sport");

        when(categoryRepository.existsByNameIgnoreCase("Sport")).thenReturn(true);

        assertThrows(BusinessException.class, () -> categoryService.createCategory(dto));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldUpdateCategory() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Music");
        dto.setDescription("Music events");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenReturn(category);

        CategoryDTO result = categoryService.updateCategory(1L, dto);

        assertNotNull(result);
        verify(categoryRepository).save(any());
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentCategory() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        CategoryDTO emptyDto = new CategoryDTO();
        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(99L, emptyDto));
    }

    @Test
    void shouldDeleteCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentCategory() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(99L));
    }

    @Test
    void shouldGetCategoryById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryDTO result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldGetAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryDTO> result = categoryService.getAllCategories();

        assertEquals(1, result.size());
    }
}
package tn.esprit.eventservice.service;

import tn.esprit.eventservice.dto.CategoryDTO;
import java.util.List;

public interface ICategoryService {
    CategoryDTO createCategory(CategoryDTO dto);
    CategoryDTO updateCategory(Long id, CategoryDTO dto);
    void deleteCategory(Long id);
    CategoryDTO getCategoryById(Long id);
    List<CategoryDTO> getAllCategories();
}
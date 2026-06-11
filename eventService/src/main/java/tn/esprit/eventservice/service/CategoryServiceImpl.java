package tn.esprit.eventservice.service;

import org.springframework.stereotype.Service;
import tn.esprit.eventservice.dto.CategoryDTO;
import tn.esprit.eventservice.entity.Category;
import tn.esprit.eventservice.exception.BusinessException;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.CategoryRepository;

import java.util.List;

@Service
public class CategoryServiceImpl implements ICategoryService {

    private static final String CATEGORY_NOT_FOUND_MSG = "Catégorie introuvable : ";

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    private CategoryDTO toDTO(Category c) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setDescription(c.getDescription());
        return dto;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName()))
            throw new BusinessException("Une catégorie avec ce nom existe déjà");
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return toDTO(categoryRepository.save(category));
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND_MSG + id));
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        return toDTO(categoryRepository.save(existing));
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND_MSG + id));
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        return toDTO(categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND_MSG + id)));
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::toDTO).toList();
    }
}
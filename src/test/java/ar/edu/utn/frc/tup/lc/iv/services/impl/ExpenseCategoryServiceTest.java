package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoCategory;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseDeleteExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseCategoryDTO;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoCategory;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseDeleteExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseCategoryDTO;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseCategoryEntity;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseCategoryModel;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseCategoryServiceTest {

    @InjectMocks
    private ExpenseCategoryService expenseCategoryService;

    @Mock
    private ExpenseCategoryRepository expenseCategoryRepository;
    @Mock
    private ModelMapper modelMapper;

    private ExpenseCategoryEntity categoryEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        categoryEntity = new ExpenseCategoryEntity();
        categoryEntity.setId(1);
        categoryEntity.setDescription("Category 1");
        categoryEntity.setEnabled(true);
        categoryEntity.setLastUpdatedDatetime(LocalDateTime.now());
    }

    @Test
    void getCategoryModel() {

        when(expenseCategoryRepository.findById(1)).thenReturn(Optional.of(categoryEntity));


        ExpenseCategoryModel categoryModel = new ExpenseCategoryModel();
        categoryModel.setId(1);
        categoryModel.setDescription("Category 1");
        when(modelMapper.map(categoryEntity, ExpenseCategoryModel.class)).thenReturn(categoryModel);


        ExpenseCategoryModel result = expenseCategoryService.getCategoryModel(1);


        assertNotNull(result);
        assertEquals("Category 1", result.getDescription());


        verify(expenseCategoryRepository, times(1)).findById(1);
        verify(modelMapper, times(1)).map(categoryEntity, ExpenseCategoryModel.class);
    }

    @Test
    void deleteCategory() {
        // Arrange
        when(expenseCategoryRepository.findById(1)).thenReturn(Optional.of(categoryEntity));

        // Act
        DtoResponseDeleteExpense response = expenseCategoryService.deteleCategory(1);

        // Assert
        assertEquals("Category delete logic successfully", response.getDescriptionResponse());
        assertEquals("Category 1", response.getExpense());


        verify(expenseCategoryRepository, times(1)).save(categoryEntity);
        assertFalse(categoryEntity.getEnabled());
    }

    @Test
    void deleteCategory_NotFound() {
        // Arrange
        when(expenseCategoryRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseCategoryService.deteleCategory(999);
        });

        assertEquals("The category does not exist", exception.getMessage());
        verify(expenseCategoryRepository, times(1)).findById(999);
    }

    @Test
    void putCategory() {
        // Arrange
        when(expenseCategoryRepository.findById(1)).thenReturn(Optional.of(categoryEntity));

        // Act
        ExpenseCategoryDTO updatedCategory = expenseCategoryService.putCategory(1, "Updated Category");

        // Assert
        assertNotNull(updatedCategory);
        assertEquals("Updated Category", updatedCategory.getDescription());


        verify(expenseCategoryRepository, times(1)).save(categoryEntity);
        assertEquals("Updated Category", categoryEntity.getDescription());
    }

    @Test
    void putCategory_AlreadyExists() {
        // Creamos una segunda categoría
        ExpenseCategoryEntity anotherCategory = new ExpenseCategoryEntity();
        anotherCategory.setId(2);
        anotherCategory.setDescription("Existing Category");
        anotherCategory.setEnabled(true);

        // Arrange
        when(expenseCategoryRepository.findByDescription("Existing Category")).thenReturn(Optional.of(anotherCategory));
        when(expenseCategoryRepository.findById(1)).thenReturn(Optional.of(categoryEntity));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseCategoryService.putCategory(1, "Existing Category");
        });

        assertEquals("A category with this description already exists", exception.getMessage());
        verify(expenseCategoryRepository, times(1)).findByDescription("Existing Category");

    }
    @Test
    void testGetCategoryModel() {
        // Arrange
        Integer id = 1;
        ExpenseCategoryEntity entity = new ExpenseCategoryEntity();
        when(expenseCategoryRepository.findById(id)).thenReturn(Optional.of(entity));
        when(modelMapper.map(entity, ExpenseCategoryModel.class)).thenReturn(new ExpenseCategoryModel());

        // Act
        ExpenseCategoryModel result = expenseCategoryService.getCategoryModel(id);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllCategories() {
        // Arrange
        ExpenseCategoryEntity entity = new ExpenseCategoryEntity();
        when(expenseCategoryRepository.findAllEnabled()).thenReturn(Collections.singletonList(entity));
        when(modelMapper.map(entity, DtoCategory.class)).thenReturn(new DtoCategory());

        // Act
        List<DtoCategory> result = expenseCategoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllCategories_NoCategoriesFound() {
        // Arrange
        when(expenseCategoryRepository.findAllEnabled()).thenReturn(Collections.emptyList());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseCategoryService.getAllCategories();
        });

        assertEquals("No categories found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}

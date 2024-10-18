package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
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

import java.time.LocalDateTime;
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
}
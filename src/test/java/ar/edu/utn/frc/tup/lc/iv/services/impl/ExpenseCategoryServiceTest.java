package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoCategory;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseCategoryEntity;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseCategoryModel;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class ExpenseCategoryServiceTest {
    @Mock
    private ExpenseCategoryRepository expenseCategoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ExpenseCategoryService expenseCategoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoCategory;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
public class ExpenseCategoryControllerTest {
    @Mock
    private IExpenseCategoryService expenseCategoryService;

    @InjectMocks
    private ExpenseCategoryController expenseCategoryController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllCategories() {
        List<DtoCategory> mockCategories = Arrays.asList(
                new DtoCategory(1, "Category1", LocalDateTime.now(), "Activo"),
                new DtoCategory(2, "Category2", LocalDateTime.now(), "Activo")
        );
        when(expenseCategoryService.getAllCategories()).thenReturn(mockCategories);

        ResponseEntity<List<DtoCategory>> response = expenseCategoryController.getAllCategories();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockCategories, response.getBody());
    }
}

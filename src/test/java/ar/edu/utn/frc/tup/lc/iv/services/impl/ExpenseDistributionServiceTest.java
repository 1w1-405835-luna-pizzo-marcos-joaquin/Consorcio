package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseCategoryDTO;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseOwnerVisualizerDTO;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseCategoryEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseDistributionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseDistributionServiceTest {

    @InjectMocks
    private ExpenseDistributionService service;

    @Mock
    private ExpenseDistributionRepository repository;

    private ExpenseDistributionEntity entity;
    private ExpenseEntity expense;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock data
        expense = new ExpenseEntity();
        expense.setExpenseDate(LocalDate.now());
        expense.setExpenseType(ExpenseType.INDIVIDUAL);
        expense.setAmount(new BigDecimal("500"));
        expense.setDescription("Test expense");

        entity = new ExpenseDistributionEntity();
        entity.setExpense(expense);
        entity.setOwnerId(1);
    }

    @Test
    void testFindAll() {
        // Given
        when(repository.findAllDistinct()).thenReturn(Arrays.asList(entity));

        // When
        List<ExpenseOwnerVisualizerDTO> result = service.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findAllDistinct();
    }

    @Test
    void testFilterExpenses_AllFilters() {
        // Given
        when(repository.findAllDistinct()).thenReturn(Arrays.asList(entity));

        // When
        List<ExpenseOwnerVisualizerDTO> result = service.filterExpenses(
                1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1),
                ExpenseType.INDIVIDUAL, null, "Test", new BigDecimal("400"), new BigDecimal("600"));

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findAllDistinct();
    }

    @Test
    void testFilterExpenses_InvalidOwnerId() {
        // When/Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            service.filterExpenses(-1, LocalDate.now(), LocalDate.now(), ExpenseType.INDIVIDUAL, null, null, null, null);
        });
        assertEquals("El ID del propietario debe ser mayor que cero.", thrown.getMessage());
    }

    @Test
    void testFilterExpenses_StartDateAfterEndDate() {
        // When/Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            service.filterExpenses(1, LocalDate.now().plusDays(1), LocalDate.now(), null, null, null, null, null);
        });
        assertEquals("La fecha de inicio no puede ser posterior a la fecha de finalización.", thrown.getMessage());
    }

    @Test
    void testFilterExpenses_NegativeAmountFrom() {
        // When/Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            service.filterExpenses(1, LocalDate.now(), LocalDate.now(), null, null, null, new BigDecimal("-100"), null);
        });
        assertEquals("El monto 'Desde' no puede ser negativo.", thrown.getMessage());
    }

    @Test
    void testFilterExpenses_AmountFromGreaterThanAmountTo() {
        // When/Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            service.filterExpenses(1, LocalDate.now(), LocalDate.now(), null, null, null, new BigDecimal("600"), new BigDecimal("500"));
        });
        assertEquals("El monto 'Desde' no puede ser mayor que el monto 'Hasta'.", thrown.getMessage());
    }

    @Test
    void testFilterExpenses_ValidAmountRange() {
        // Given
        when(repository.findAllDistinct()).thenReturn(Arrays.asList(entity));

        // When
        List<ExpenseOwnerVisualizerDTO> result = service.filterExpenses(1, LocalDate.now(), LocalDate.now(), null, null, null, new BigDecimal("400"), new BigDecimal("600"));

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testToDto_NullEntity() {
        // When
        ExpenseCategoryDTO result = ExpenseDistributionService.toDto(null);

        // Then
        assertNull(result);
    }

    @Test
    void testToDto_ValidEntity() {
        // Given
        ExpenseCategoryEntity category = new ExpenseCategoryEntity();
        category.setId(1);
        category.setDescription("Category");

        // When
        ExpenseCategoryDTO result = ExpenseDistributionService.toDto(category);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Category", result.getDescription());
    }

    @Test
    void testEntityDistributionToDto_NullEntity() {
        // When
        ExpenseOwnerVisualizerDTO result = service.entityDistributiontoDto(null);

        // Then
        assertNull(result);
    }

    @Test
    void testEntityDistributionToDto_ValidEntity() {
        // When
        ExpenseOwnerVisualizerDTO result = service.entityDistributiontoDto(entity);

        // Then
        assertNotNull(result);
        assertEquals("Test expense", result.getDescription());
        assertEquals(ExpenseType.INDIVIDUAL, result.getExpenseType());
    }

    @Test
    void testFilterExpenses_EmptyResults() {
        // Given
        when(repository.findAllDistinct()).thenReturn(Arrays.asList());

        // When
        List<ExpenseOwnerVisualizerDTO> result = service.filterExpenses(
                1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1),
                ExpenseType.INDIVIDUAL, null, "Test", new BigDecimal("400"), new BigDecimal("600"));

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAllDistinct();
    }

    @Test
    void testFilterExpenses_AmountToNegative() {
        // When/Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            service.filterExpenses(1, LocalDate.now(), LocalDate.now(), null, null, null, null, new BigDecimal("-100"));
        });
        assertEquals("El monto 'Hasta' no puede ser negativo.", thrown.getMessage());
    }
//    @Test
//    void testFindByOwnerId_ValidOwnerId() {
//        // Given
//        ExpenseEntity expense1 = new ExpenseEntity();
//        expense1.setExpenseDate(LocalDate.now());
//        expense1.setExpenseType(ExpenseType.INDIVIDUAL);
//        expense1.setAmount(new BigDecimal("500"));
//        expense1.setDescription("Test expense 1");
//
//        ExpenseDistributionEntity entity1 = new ExpenseDistributionEntity();
//        entity1.setOwnerId(1);
//        entity1.setExpense(expense1);
//        entity1.setProportion(new BigDecimal("0.5"));
//
//        ExpenseEntity expense2 = new ExpenseEntity();
//        expense2.setExpenseDate(LocalDate.now());
//        expense2.setExpenseType(ExpenseType.INDIVIDUAL);
//        expense2.setAmount(new BigDecimal("300"));
//        expense2.setDescription("Test expense 2");
//
//        ExpenseDistributionEntity entity2 = new ExpenseDistributionEntity();
//        entity2.setOwnerId(1);
//        entity2.setExpense(expense2);
//        entity2.setProportion(new BigDecimal("0.75"));
//
//        when(repository.findAllByOwnerId(1)).thenReturn(Arrays.asList(entity1, entity2));
//
//        // When
//        List<ExpenseOwnerVisualizerDTO> result = service.findByOwnerId(1);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(2, result.size());
//
//
//        assertEquals(new BigDecimal("250.00").setScale(2), result.get(0).getAmount().setScale(2)); // 500 * 0.5
//        assertEquals(new BigDecimal("225.00").setScale(2), result.get(1).getAmount().setScale(2)); // 300 * 0.75
//
//        verify(repository, times(1)).findAllByOwnerId(1);
//    }



//    @Test
//    void testFindByOwnerId_EmptyResult() {
//        // Given
//        when(repository.findAllByOwnerId(2)).thenReturn(Arrays.asList());
//
//        // When
//        List<ExpenseOwnerVisualizerDTO> result = service.findByOwnerId(2);
//
//        // Then
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//
//        verify(repository, times(1)).findAllByOwnerId(2);
//    }

}

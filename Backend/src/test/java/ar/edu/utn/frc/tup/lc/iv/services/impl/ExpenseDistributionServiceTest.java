package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseOwnerVisualizerDTO;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseDistributionRepository;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ExpenseDistributionServiceTest {

    @Mock
    private ExpenseDistributionRepository expenseDistributionRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseDistributionService expenseDistributionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        // Arrange
        ExpenseDistributionEntity entity1 = new ExpenseDistributionEntity();
        ExpenseDistributionEntity entity2 = new ExpenseDistributionEntity();
        when(expenseDistributionRepository.findAllDistinct()).thenReturn(Arrays.asList(entity1, entity2));

        // Act
        List<ExpenseOwnerVisualizerDTO> result = expenseDistributionService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testFindByOwnerId() {
        // Arrange
        Integer ownerId = 1;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        ExpenseEntity expenseEntity = new ExpenseEntity();
        expenseEntity.setAmount(BigDecimal.valueOf(1000));
        ExpenseDistributionEntity distributionEntity = new ExpenseDistributionEntity();
        distributionEntity.setOwnerId(ownerId);
        distributionEntity.setProportion(BigDecimal.valueOf(0.5));
        expenseEntity.setDistributions(Arrays.asList(distributionEntity));
        when(expenseRepository.findAllByDate(startDate, endDate)).thenReturn(Arrays.asList(expenseEntity));

        // Act
        List<ExpenseOwnerVisualizerDTO> result = expenseDistributionService.findByOwnerId(ownerId, "2024-01-01", "2024-12-31");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ownerId, result.get(0).getId());
        assertEquals(BigDecimal.valueOf(500.00), result.get(0).getAmount());  // 1000 * 0.5 = 500
    }

    @Test
    void testFilterExpenses() {
        // Arrange
        Integer ownerId = 1;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        ExpenseDistributionEntity entity = new ExpenseDistributionEntity();
        ExpenseEntity expense = new ExpenseEntity();
        expense.setAmount(BigDecimal.valueOf(500));
        expense.setExpenseDate(LocalDate.of(2024, 6, 1));
        expense.setExpenseType(ExpenseType.INDIVIDUAL);
        entity.setOwnerId(ownerId);
        entity.setExpense(expense);
        when(expenseDistributionRepository.findAllDistinct()).thenReturn(Arrays.asList(entity));

        // Act
        List<ExpenseOwnerVisualizerDTO> result = expenseDistributionService.filterExpenses(ownerId, startDate, endDate, ExpenseType.INDIVIDUAL, null, null, BigDecimal.valueOf(100), BigDecimal.valueOf(1000));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ownerId, result.get(0).getId());
        assertEquals(BigDecimal.valueOf(500), result.get(0).getAmount());
    }
}

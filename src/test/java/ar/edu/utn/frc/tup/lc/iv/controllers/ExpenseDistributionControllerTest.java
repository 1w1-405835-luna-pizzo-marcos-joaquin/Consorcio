package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseOwnerVisualizerDTO;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.services.impl.ExpenseDistributionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ExpenseDistributionControllerTest {

    @Mock
    private ExpenseDistributionService expenseDistributionService;

    @InjectMocks
    private ExpenseDistributionController expenseDistributionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllExpenses() {
        // Datos de prueba
        ExpenseDistributionEntity entity1 = new ExpenseDistributionEntity();
        ExpenseDistributionEntity entity2 = new ExpenseDistributionEntity();
        List<ExpenseDistributionEntity> mockExpenseList = Arrays.asList(entity1, entity2);

        // Simulamos el comportamiento del servicio
        when(expenseDistributionService.findAll()).thenReturn(mockExpenseList);

        // Llamamos al controlador
        ResponseEntity<List<ExpenseDistributionEntity>> response = expenseDistributionController.getAllExpenses();

        // Verificamos que el servicio fue llamado
        verify(expenseDistributionService, times(1)).findAll();

        // Verificamos que la respuesta sea correcta
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockExpenseList, response.getBody());
    }

    @Test
    void testFilterExpenseDistributions() {
        // Datos de prueba
        ExpenseOwnerVisualizerDTO dto1 = new ExpenseOwnerVisualizerDTO();
        ExpenseOwnerVisualizerDTO dto2 = new ExpenseOwnerVisualizerDTO();
        List<ExpenseOwnerVisualizerDTO> mockDtoList = Arrays.asList(dto1, dto2);

        Integer ownerId = 1;
        String description = "Electricity";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        ExpenseType expenseType = ExpenseType.INDIVIDUAL;
        Integer categoryId = 2;
        BigDecimal amountFrom = BigDecimal.valueOf(100);
        BigDecimal amountTo = BigDecimal.valueOf(500);

        // Simulamos el comportamiento del servicio
        when(expenseDistributionService.filterExpenses(ownerId, startDate, endDate, expenseType, categoryId, description, amountFrom, amountTo))
                .thenReturn(mockDtoList);

        // Llamamos al controlador
        ResponseEntity<List<ExpenseOwnerVisualizerDTO>> response = expenseDistributionController.filterExpenseDistributions(
                ownerId, description, startDate, endDate, expenseType, categoryId, amountFrom, amountTo);

        // Verificamos que el servicio fue llamado
        verify(expenseDistributionService, times(1)).filterExpenses(ownerId, startDate, endDate, expenseType, categoryId, description, amountFrom, amountTo);

        // Verificamos que la respuesta sea correcta
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockDtoList, response.getBody());
    }
}

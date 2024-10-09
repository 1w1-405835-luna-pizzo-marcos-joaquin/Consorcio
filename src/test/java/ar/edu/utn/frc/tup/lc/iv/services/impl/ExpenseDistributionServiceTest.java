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
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


class ExpenseDistributionServiceTest {

    @InjectMocks
    private ExpenseDistributionService service;

    @Mock
    private ExpenseDistributionRepository repository;

    private ExpenseDistributionEntity entity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa los mocks

        // Crea un ExpenseDistributionEntity de prueba
        ExpenseEntity expense = new ExpenseEntity();
        expense.setDescription("Descripción de prueba");
        expense.setProviderId(1);
        expense.setExpenseDate(LocalDate.now());
        expense.setFileId(UUID.fromString("02f5b2cb-2721-4152-b71f-39b8752f0abe"));
        expense.setInvoiceNumber(Integer.valueOf("957"));
        expense.setExpenseType(ExpenseType.TODOS);
        expense.setAmount(BigDecimal.valueOf(100));
        expense.setInstallments(1);

        entity = new ExpenseDistributionEntity();
        entity.setId(1);
        entity.setExpense(expense);
        entity.setProportion(BigDecimal.valueOf(1));
        entity.setEnabled(true);
    }

    @Test
    void findVisualizersByOwnerAndFilters_ValidParams() {
        List<ExpenseDistributionEntity> entities = new ArrayList<>();
        entities.add(entity);

        when(repository.findByOwnerAndFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(entities);

        List<ExpenseOwnerVisualizerDTO> result = service.findVisualizersByOwnerAndFilters(
                1, LocalDate.now(), LocalDate.now(), ExpenseType.TODOS, null, null, null, null
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        ExpenseOwnerVisualizerDTO dto = result.get(0);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getExpense().getDescription(), dto.getDescription());
    }

    @Test
    void findVisualizersByOwnerAndFilters_OwnerIdInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.findVisualizersByOwnerAndFilters(-1, LocalDate.now(), LocalDate.now(), ExpenseType.TODOS, null, null, null, null);
        });

        assertEquals("El ID debe ser mayor que cero.", exception.getMessage());
    }



    @Test
    void toDto_ExpenseCategoryEntity() {
        ExpenseCategoryDTO dto = ExpenseDistributionService.toDto((ExpenseCategoryEntity) null);
        assertNull(dto);

        ExpenseCategoryEntity entity = new ExpenseCategoryEntity();
        entity.setId(1);
        entity.setDescription("Categoría de prueba");

        dto = ExpenseDistributionService.toDto(entity);
        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals("Categoría de prueba", dto.getDescription());
    }

    @Test
    void testToDto_NullEntity() {
        ExpenseOwnerVisualizerDTO dto = ExpenseDistributionService.toDto((ExpenseDistributionEntity) null);
        assertNull(dto);
    }

    @Test
    void testToDto_ValidEntity() {
        ExpenseEntity expense = new ExpenseEntity();
        expense.setDescription("Descripción de prueba");
        expense.setProviderId(1);
        expense.setExpenseDate(LocalDate.now());
        expense.setFileId(UUID.fromString("02f5b2cb-2721-4152-b71f-39b8752f0abe"));
        expense.setInvoiceNumber(Integer.valueOf("123456"));
        expense.setExpenseType(ExpenseType.TODOS);
        expense.setAmount(BigDecimal.valueOf(100));
        expense.setInstallments(1);

        ExpenseDistributionEntity entity = new ExpenseDistributionEntity();
        entity.setId(1);
        entity.setExpense(expense);
        entity.setProportion(BigDecimal.valueOf(1));
        entity.setEnabled(true);

        ExpenseOwnerVisualizerDTO dto = ExpenseDistributionService.toDto(entity);
        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(expense.getDescription(), dto.getDescription());
        assertEquals(expense.getProviderId(), dto.getProviderId());
        assertEquals(expense.getExpenseDate(), dto.getExpenseDate());
        assertEquals(expense.getFileId(), dto.getFileId());
        assertEquals(expense.getInvoiceNumber(), dto.getInvoiceNumber());
        assertEquals(expense.getExpenseType(), dto.getExpenseType());
        assertEquals(expense.getAmount(), dto.getAmount());
        assertEquals(entity.getProportion(), dto.getProportion());
        assertEquals(expense.getInstallments(), dto.getInstallments());
        assertTrue(dto.getEnabled());
    }
}
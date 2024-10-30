package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.dtos.ReportChart.ExpenseCategoryPeriodDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.ReportChart.ExpenseYearDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.PeriodDto;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseRepository;
import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ExpenseReportServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseReportService expenseReportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetExpenseCategoriesPeriod_Success() {
        PeriodDto periodDto = new PeriodDto();
        periodDto.setStartDate(LocalDate.of(2023, 1, 1));
        periodDto.setEndDate(LocalDate.of(2023, 12, 31));

        Object[] result1 = {"Category1", BigDecimal.valueOf(100)};
        Object[] result2 = {"Category2", BigDecimal.valueOf(200)};
        List<Object[]> repoResults = Arrays.asList(result1, result2);

        when(expenseRepository.findAllByPeriodGroupByCategory(periodDto.getStartDate(), periodDto.getEndDate()))
                .thenReturn(repoResults);

        List<ExpenseCategoryPeriodDto> result = expenseReportService.getExpenseCategoriesPeriod(periodDto);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Category1", result.get(0).getCategory());
        assertEquals(BigDecimal.valueOf(100), result.get(0).getAmount());
        assertEquals("Category2", result.get(1).getCategory());
        assertEquals(BigDecimal.valueOf(200), result.get(1).getAmount());
    }

    @Test
    void testGetExpenseCategoriesPeriod_InvalidPeriod() {
        PeriodDto periodDto = new PeriodDto();
        periodDto.setStartDate(LocalDate.of(2023, 12, 31));
        periodDto.setEndDate(LocalDate.of(2023, 1, 1));

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseReportService.getExpenseCategoriesPeriod(periodDto);
        });

        assertEquals("The start date must be earlier than the end date.", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testGetExpenseCategoriesPeriod_Exception() {
        PeriodDto periodDto = new PeriodDto();
        periodDto.setStartDate(LocalDate.of(2023, 1, 1));
        periodDto.setEndDate(LocalDate.of(2023, 12, 31));

        when(expenseRepository.findAllByPeriodGroupByCategory(periodDto.getStartDate(), periodDto.getEndDate()))
                .thenThrow(new RuntimeException("Database error"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseReportService.getExpenseCategoriesPeriod(periodDto);
        });

        assertEquals("Ocurrio un error al obtener los datos", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }

    @Test
    void testGetExpenseYears_Success() {
        PeriodDto periodDto = new PeriodDto();
        periodDto.setStartDate(LocalDate.of(2023, 1, 1));
        periodDto.setEndDate(LocalDate.of(2023, 12, 31));

        Object[] result1 = {2023, 1, BigDecimal.valueOf(100)};
        Object[] result2 = {2023, 2, BigDecimal.valueOf(200)};
        List<Object[]> repoResults = Arrays.asList(result1, result2);

        when(expenseRepository.findAllByPeriodGroupByYearMonth(2022, 2023))
                .thenReturn(repoResults);

        List<ExpenseYearDto> result = expenseReportService.getExpenseYears(periodDto);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2023, result.get(0).getYear());
        assertEquals(1, result.get(0).getMonth());
        assertEquals(BigDecimal.valueOf(100), result.get(0).getAmount());
        assertEquals(2023, result.get(1).getYear());
        assertEquals(2, result.get(1).getMonth());
        assertEquals(BigDecimal.valueOf(200), result.get(1).getAmount());
    }

    @Test
    void testGetExpenseYears_InvalidPeriod() {
        PeriodDto periodDto = new PeriodDto();
        periodDto.setStartDate(LocalDate.of(2023, 12, 31));
        periodDto.setEndDate(LocalDate.of(2023, 1, 1));

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseReportService.getExpenseYears(periodDto);
        });

        assertEquals("The start date must be earlier than the end date.", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testGetExpenseYears_Exception() {
        PeriodDto periodDto = new PeriodDto();
        periodDto.setStartDate(LocalDate.of(2023, 1, 1));
        periodDto.setEndDate(LocalDate.of(2023, 12, 31));

        when(expenseRepository.findAllByPeriodGroupByYearMonth(2022, 2023))
                .thenThrow(new RuntimeException("Database error"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseReportService.getExpenseYears(periodDto);
        });

        assertEquals("Ocurrio un error al obtener los datos", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }
}
package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.ReportChart.ExpenseYearDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.PeriodDto;
import ar.edu.utn.frc.tup.lc.iv.services.impl.ExpenseReportService;
import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ExpenseReportChartControllerTest {

    @Mock
    private ExpenseReportService expenseReportService;

    @InjectMocks
    private ExpenseReportChartController expenseReportChartController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetYearMonth_Success() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        PeriodDto periodDto = new PeriodDto(startDate, endDate);

        ExpenseYearDto dto1 = new ExpenseYearDto(2023, 1, BigDecimal.valueOf(100));
        ExpenseYearDto dto2 = new ExpenseYearDto(2023, 2, BigDecimal.valueOf(200));
        List<ExpenseYearDto> serviceResult = Arrays.asList(dto1, dto2);

        when(expenseReportService.getExpenseYears(periodDto)).thenReturn(serviceResult);

        ResponseEntity<List<ExpenseYearDto>> response = expenseReportChartController.getYearMonth(startDate, endDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResult, response.getBody());
    }

    @Test
    void testGetYearMonth_InvalidPeriod() {
        LocalDate startDate = LocalDate.of(2023, 12, 31);
        LocalDate endDate = LocalDate.of(2023, 1, 1);
        PeriodDto periodDto = new PeriodDto(startDate, endDate);

        when(expenseReportService.getExpenseYears(periodDto)).thenThrow(new CustomException("The start date must be earlier than the end date.", HttpStatus.BAD_REQUEST));

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseReportChartController.getYearMonth(startDate, endDate);
        });

        assertEquals("The start date must be earlier than the end date.", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testGetYearMonth_Exception() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        PeriodDto periodDto = new PeriodDto(startDate, endDate);

        when(expenseReportService.getExpenseYears(periodDto)).thenThrow(new CustomException("Ocurrio un error al obtener los datos", HttpStatus.INTERNAL_SERVER_ERROR));

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseReportChartController.getYearMonth(startDate, endDate);
        });

        assertEquals("Ocurrio un error al obtener los datos", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }

    @Test
    void testGetCategoriesPeriod_InvalidPeriod() {
        LocalDate startDate = LocalDate.of(2023, 12, 31);
        LocalDate endDate = LocalDate.of(2023, 1, 1);
        PeriodDto periodDto = new PeriodDto(startDate, endDate);

        when(expenseReportService.getExpenseCategoriesPeriod(periodDto)).thenThrow(new CustomException("The start date must be earlier than the end date.", HttpStatus.BAD_REQUEST));

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseReportChartController.getCategoriesPeriod(startDate, endDate);
        });

        assertEquals("The start date must be earlier than the end date.", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testGetCategoriesPeriod_Exception() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        PeriodDto periodDto = new PeriodDto(startDate, endDate);

        when(expenseReportService.getExpenseCategoriesPeriod(periodDto)).thenThrow(new CustomException("Ocurrio un error al obtener los datos", HttpStatus.INTERNAL_SERVER_ERROR));

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseReportChartController.getCategoriesPeriod(startDate, endDate);
        });

        assertEquals("Ocurrio un error al obtener los datos", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }
}
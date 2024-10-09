package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseOwnerVisualizerDTO;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.services.impl.ExpenseDistributionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseDistributionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ExpenseDistributionService expenseDistributionService;

    @Spy
    @InjectMocks
    private ExpenseDistributionController expenseDistributionController;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

    }

    @Test
    void filterExpenseDistributions_ValidParams_ReturnsList() throws Exception {
        // Arrange
        Integer ownerId = 1;
        String description = "Electricity Bill";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        ExpenseType expenseType = ExpenseType.COMUN;
        Integer categoryId = 2;
        BigDecimal amountFrom = new BigDecimal("100");
        BigDecimal amountTo = new BigDecimal("1000");

        List<ExpenseOwnerVisualizerDTO> mockResponse = new ArrayList<>();
        mockResponse.add(new ExpenseOwnerVisualizerDTO());

        when(expenseDistributionService.findVisualizersByOwnerAndFilters(
                ownerId, startDate, endDate, expenseType, categoryId, description, amountFrom, amountTo))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/expenses/distributions/filter")
                        .header("ownerId", ownerId)
                        .param("description", description)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("expenseType", expenseType.toString())
                        .param("categoryId", categoryId.toString())
                        .param("amountFrom", amountFrom.toString())
                        .param("amountTo", amountTo.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void filterExpenseDistributions_NoParams_ReturnsEmptyList() throws Exception {
        // Arrange
        List<ExpenseOwnerVisualizerDTO> emptyResponse = new ArrayList<>();

        when(expenseDistributionService.findVisualizersByOwnerAndFilters(
                null, null, null, null, null, null, null, null))
                .thenReturn(emptyResponse);

        // Act & Assert
        mockMvc.perform(get("/api/expenses/distributions/filter")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Verificar que la respuesta sea 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0)); // Verificar que la lista esté vacía
    }
}

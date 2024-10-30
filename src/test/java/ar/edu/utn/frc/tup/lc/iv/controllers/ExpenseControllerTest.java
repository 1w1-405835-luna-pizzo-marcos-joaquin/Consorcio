package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.*;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExpenseControllerTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private IExpenseService expenseService;

    @InjectMocks
    private ExpenseController expenseController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(expenseController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testPostExpense() throws Exception {
        DtoRequestExpense requestExpense = new DtoRequestExpense();

        MockMultipartFile file = new MockMultipartFile("file", "test.txt",
                MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

        DtoResponseExpense responseExpense = new DtoResponseExpense();
        responseExpense.setDescription("Test Expense");
        responseExpense.setProviderId(1);
        responseExpense.setExpenseDate(LocalDate.of(2024, 10, 9));
        responseExpense.setFileId(UUID.randomUUID());
        responseExpense.setInvoiceNumber("12345");
        responseExpense.setExpenseType(ExpenseType.COMUN);
        responseExpense.setDtoCategory(new DtoCategory());
        responseExpense.setDtoDistributionList(Arrays.asList(new DtoDistribution()));
        responseExpense.setDtoInstallmentList(Arrays.asList(new DtoInstallment()));

        when(expenseService.postExpense(any(DtoRequestExpense.class), any(MultipartFile.class)))
                .thenReturn(ResponseEntity.ok(responseExpense));

        mockMvc.perform(multipart("/expenses")
                        .file(file)
                        .file(new MockMultipartFile("expense", "",
                                MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsString(requestExpense).getBytes()))
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Expense"))
                .andExpect(jsonPath("$.providerId").value(1))
                .andExpect(jsonPath("$.expenseDate").value("2024-10-09"))
                .andExpect(jsonPath("$.fileId").isNotEmpty())
                .andExpect(jsonPath("$.invoiceNumber").value(12345))
                .andExpect(jsonPath("$.expenseType").value("COMUN"))
                .andExpect(jsonPath("$.dtoCategory").isNotEmpty())
                .andExpect(jsonPath("$.dtoDistributionList").isArray())
                .andExpect(jsonPath("$.dtoInstallmentList").isArray());


        verify(expenseService, times(1)).postExpense(any(DtoRequestExpense.class), any(MultipartFile.class));
    }

    @Test
    void testPostExpenseWithoutFile() throws Exception {
        DtoRequestExpense requestExpense = new DtoRequestExpense();

        DtoResponseExpense responseExpense = getDtoResponseExpense();

        when(expenseService.postExpense(any(DtoRequestExpense.class), isNull()))
                .thenReturn(ResponseEntity.ok(responseExpense));

        mockMvc.perform(multipart("/expenses")
                        .file(new MockMultipartFile("expense", "",
                                MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsString(requestExpense).getBytes()))
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Expense Without File"))
                .andExpect(jsonPath("$.providerId").value(2))
                .andExpect(jsonPath("$.expenseDate").value("2024-10-10"))
                .andExpect(jsonPath("$.fileId").doesNotExist())
                .andExpect(jsonPath("$.invoiceNumber").value(54321))
                .andExpect(jsonPath("$.expenseType").value("EXTRAORDINARIO"))
                .andExpect(jsonPath("$.dtoCategory").isNotEmpty())
                .andExpect(jsonPath("$.dtoDistributionList").isArray())
                .andExpect(jsonPath("$.dtoInstallmentList").isArray());

        verify(expenseService, times(1)).postExpense(any(DtoRequestExpense.class), isNull());
    }

    private static DtoResponseExpense getDtoResponseExpense() {
        DtoResponseExpense responseExpense = new DtoResponseExpense();
        responseExpense.setDescription("Test Expense Without File");
        responseExpense.setProviderId(2);
        responseExpense.setExpenseDate(LocalDate.of(2024, 10, 10));
        responseExpense.setInvoiceNumber("54321");
        responseExpense.setExpenseType(ExpenseType.EXTRAORDINARIO);
        responseExpense.setDtoCategory(new DtoCategory());
        responseExpense.setDtoDistributionList(Arrays.asList(new DtoDistribution()));
        responseExpense.setDtoInstallmentList(Arrays.asList(new DtoInstallment()));
        return responseExpense;
    }
@Test
void testGetExpenseById() throws Exception {
    int expenseId = 1;
    DtoExpenseQuery expenseQuery = new DtoExpenseQuery();

    List<DtoExpenseDistributionQuery> expenseDistribution = new ArrayList<>();
    DtoExpenseDistributionQuery distribution = new DtoExpenseDistributionQuery();
    distribution.setAmount(new BigDecimal("100.00"));
    distribution.setOwnerId(1);
    distribution.setOwnerFullName("Owner");
    expenseDistribution.add(distribution);

    expenseQuery.setCategory("Category");
    expenseQuery.setId(1);
    expenseQuery.setAmount(new BigDecimal("100.00"));
    expenseQuery.setExpenseType(ExpenseType.COMUN.toString());
    expenseQuery.setProvider("Provider");
    expenseQuery.setExpenseDate(LocalDate.parse("2024-01-01"));
    expenseQuery.setDistributionList(expenseDistribution);

    // Ensure the mock is configured correctly
    when(expenseService.getExpenseById(anyInt())).thenReturn(expenseQuery);

    mockMvc.perform(get("/expenses/getById")
                    .param("expenseId", String.valueOf(expenseId))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.category").value("Category"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.amount").value("100.0"))
            .andExpect(jsonPath("$.expenseType").value("COMUN"))
            .andExpect(jsonPath("$.provider").value("Provider"))
            .andExpect(jsonPath("$.expenseDate").value("2024-01-01"))
            .andExpect(jsonPath("$.distributionList[0].amount").value("100.0"))
            .andExpect(jsonPath("$.distributionList[0].ownerId").value(1))
            .andExpect(jsonPath("$.distributionList[0].ownerFullName").value("Owner"));

    verify(expenseService, times(1)).getExpenseById(expenseId);
}

@Test
void testGetExpenses() throws Exception {
    String expenseType = "COMUN";
    String category = "Category";
    String provider = "Provider";
    String dateFrom = "2024-01-01";
    String dateTo = "2024-12-31";

    List<DtoExpenseQuery> expenseQueryList = new ArrayList<>();
    DtoExpenseQuery expenseQuery = new DtoExpenseQuery();

    List<DtoExpenseDistributionQuery> expenseDistribution = new ArrayList<>();
    DtoExpenseDistributionQuery distribution = new DtoExpenseDistributionQuery();
    distribution.setAmount(new BigDecimal("100.00"));
    distribution.setOwnerId(1);
    distribution.setOwnerFullName("Owner");
    expenseDistribution.add(distribution);

    expenseQuery.setCategory("Category");
    expenseQuery.setId(1);
    expenseQuery.setAmount(new BigDecimal("100.00"));
    expenseQuery.setExpenseType(ExpenseType.COMUN.toString());
    expenseQuery.setProvider("Provider");
    expenseQuery.setExpenseDate(LocalDate.parse("2024-01-01"));
    expenseQuery.setDistributionList(expenseDistribution);

    expenseQueryList.add(expenseQuery);

    // Ensure the mock is configured correctly
    when(expenseService.getExpenses(expenseType, category, provider, dateFrom, dateTo))
            .thenReturn(expenseQueryList);

    mockMvc.perform(get("/expenses/getByFilters")
                    .param("expenseType", expenseType)
                    .param("category", category)
                    .param("provider", provider)
                    .param("dateFrom", dateFrom)
                    .param("dateTo", dateTo)
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].category").value("Category"))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].amount").value("100.0"))
            .andExpect(jsonPath("$[0].expenseType").value("COMUN"))
            .andExpect(jsonPath("$[0].provider").value("Provider"))
            .andExpect(jsonPath("$[0].expenseDate").value("2024-01-01"))
            .andExpect(jsonPath("$[0].distributionList[0].amount").value("100.0"))
            .andExpect(jsonPath("$[0].distributionList[0].ownerId").value(1))
            .andExpect(jsonPath("$[0].distributionList[0].ownerFullName").value("Owner"));

    verify(expenseService, times(1)).getExpenses(expenseType, category, provider, dateFrom, dateTo);
}
}
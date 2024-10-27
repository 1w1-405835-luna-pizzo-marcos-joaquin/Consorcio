package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.client.OwnerRestClient;
import ar.edu.utn.frc.tup.lc.iv.client.ProviderRestClient;
import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoDistribution;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoExpenseQuery;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoRequestExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseExpense;
import ar.edu.utn.frc.tup.lc.iv.entities.*;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseCategoryModel;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseDistributionModel;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseModel;
import ar.edu.utn.frc.tup.lc.iv.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExpenseServiceTest {
    @InjectMocks
    private ExpenseService expenseService;

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private ExpenseDistributionRepository expenseDistributionRepository;
    @Mock
    private ExpenseInstallmentRepository expenseInstallmentRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ExpenseCategoryService expenseCategoryService;
    @Mock
    private BillExpenseInstallmentsRepository billExpenseInstallmentsRepository;
    @Mock
    private ProviderRestClient providerRestClient;
    @Mock
    private OwnerRestClient ownerRestClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void postExpense_ValidRequest_ReturnsOkResponse() {
        DtoRequestExpense request = createValidDtoRequestExpense();
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/jpeg");

        ExpenseCategoryModel categoryModel = new ExpenseCategoryModel();
        categoryModel.setId(1);
        when(expenseCategoryService.getCategoryModel(anyInt())).thenReturn(categoryModel);

        when(expenseRepository.findFirstByInvoiceNumberAndProviderId(anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        ExpenseEntity validExpenseEntity = new ExpenseEntity();
        validExpenseEntity.setAmount(new BigDecimal("100.00"));
        validExpenseEntity.setCategory(new ExpenseCategoryEntity());
        validExpenseEntity.setDescription("Test Expense");
        validExpenseEntity.setExpenseDate(LocalDate.now());
        validExpenseEntity.setExpenseType(ExpenseType.COMUN);
        validExpenseEntity.setInvoiceNumber("909090");
        validExpenseEntity.setProviderId(1);

        when(modelMapper.map(any(ExpenseModel.class), eq(ExpenseEntity.class))).thenReturn(validExpenseEntity);
        when(modelMapper.map(any(), eq(ExpenseDistributionEntity.class))).thenReturn(new ExpenseDistributionEntity());
        when(modelMapper.map(any(), eq(ExpenseInstallmentEntity.class))).thenReturn(new ExpenseInstallmentEntity());

        when(expenseRepository.save(any())).thenReturn(validExpenseEntity);

        ResponseEntity<DtoResponseExpense> response = expenseService.postExpense(request, file);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(expenseRepository, times(1)).save(any());
        verify(expenseDistributionRepository, times(1)).save(any());
        verify(expenseInstallmentRepository, times(1)).save(any());
    }

    @Test
    void postExpense_DuplicateExpense_ThrowsCustomException() {
        DtoRequestExpense request = createValidDtoRequestExpense();
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/jpeg");

        when(expenseRepository.findFirstByInvoiceNumberAndProviderId(anyInt(), anyInt()))
                .thenReturn(Optional.of(new ExpenseEntity()));

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseService.postExpense(request, file);
        });

        assertEquals("The expense have already exist", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void postExpense_InvalidCategory_ThrowsCustomException() {
        DtoRequestExpense request = createValidDtoRequestExpense();
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/jpeg");

        when(expenseRepository.findFirstByInvoiceNumberAndProviderId(anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        when(expenseCategoryService.getCategoryModel(anyInt())).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseService.postExpense(request, file);
        });

        assertEquals("The category does not exist", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void postExpense_InvalidFileType_ThrowsCustomException() {
        DtoRequestExpense request = createValidDtoRequestExpense();
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("application/octet-stream");

        when(expenseRepository.findFirstByInvoiceNumberAndProviderId(anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        ExpenseCategoryModel categoryModel = new ExpenseCategoryModel();
        categoryModel.setId(1);
        when(expenseCategoryService.getCategoryModel(anyInt())).thenReturn(categoryModel);

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseService.postExpense(request, file);
        });

        assertEquals("the file must be an image or pdf", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void postExpense_InvalidDistributions_ThrowsIllegalArgumentException() {

        DtoRequestExpense request = createValidDtoRequestExpense();
        request.getDistributions().get(0).setProportion(new BigDecimal("11.00"));
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/jpeg");

        when(expenseRepository.findFirstByInvoiceNumberAndProviderId(anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        ExpenseCategoryModel categoryModel = new ExpenseCategoryModel();
        categoryModel.setId(1);
        when(expenseCategoryService.getCategoryModel(anyInt())).thenReturn(categoryModel);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            expenseService.postExpense(request, file);
        });

        assertEquals("the sum of distributions can't be less or more than 10.00", exception.getMessage());
    }

    private DtoRequestExpense createValidDtoRequestExpense() {
        DtoRequestExpense request = new DtoRequestExpense();
        request.setDescription("Test Expense");
        request.setProviderId(1);
        request.setExpenseDate(LocalDate.now());
        request.setInvoiceNumber("9243");
        request.setTypeExpense("COMUN");
        request.setCategoryId(1);
        request.setAmount(new BigDecimal("100.00"));
        request.setInstallments(1);

        DtoDistribution distribution = new DtoDistribution();
        distribution.setOwnerId(1);
        distribution.setProportion(new BigDecimal("10.00"));
        request.setDistributions(Collections.singletonList(distribution));

        return request;
    }
    @Test
    void deleteExpense_ExpenseDoesNotExist_ThrowsCustomException() {
        Integer expenseId = 1;
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseService.deteleExpense(expenseId);
        });

        assertEquals("The expense does not exist", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void deleteExpense_ExpenseHasRelatedInstallments_ThrowsCustomException() {
        Integer expenseId = 1;
        ExpenseEntity expenseEntity = new ExpenseEntity();
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));
        when(billExpenseInstallmentsRepository.findByExpenseId(expenseId))
                .thenReturn(Optional.of(Collections.singletonList(new BillExpenseInstallmentsEntity())));

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseService.deteleExpense(expenseId);
        });

        assertEquals("Expense has related bill installments", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void deleteExpense_NoRelatedInstallments_PerformsLogicalDeletion() {
        Integer expenseId = 1;
        ExpenseEntity expenseEntity = new ExpenseEntity();
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));
        when(billExpenseInstallmentsRepository.findByExpenseId(expenseId)).thenReturn(Optional.empty());

        expenseService.deteleExpense(expenseId);

        verify(expenseRepository, times(1)).save(expenseEntity);
        assertFalse(expenseEntity.getEnabled());
    }
    @Test
    void createCreditNoteForExpense_ExpenseAlreadyHasCreditNote_ThrowsCustomException() {
        Integer expenseId = 1;
        ExpenseEntity expenseEntity = new ExpenseEntity();
        expenseEntity.setNoteCredit(true);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseService.createCreditNoteForExpense(expenseId);
        });

        assertEquals("The expense have a note of credit", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void createCreditNoteForExpense_Successful() {
        Integer expenseId = 1;
        ExpenseEntity expenseEntity = new ExpenseEntity();
        expenseEntity.setAmount(new BigDecimal("100.00"));
        expenseEntity.setNoteCredit(false);
        expenseEntity.setDistributions(new ArrayList<>());
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));
        when(expenseInstallmentRepository.save(any())).thenReturn(new ExpenseInstallmentEntity());
        BillExpenseInstallmentsEntity installmentEntity = new BillExpenseInstallmentsEntity();
        when(billExpenseInstallmentsRepository.findByExpenseId(expenseId))
                .thenReturn(Optional.of(Collections.singletonList(installmentEntity)));

        expenseService.createCreditNoteForExpense(expenseId);
        verify(expenseRepository, times(1)).save(any(ExpenseEntity.class));

    }

    @Test
void setExpenseDistributionModels_ValidDistributions_ReturnsExpenseDistributionModels() throws Exception {
    DtoRequestExpense request = new DtoRequestExpense();
    List<DtoDistribution> distributions = new ArrayList<>();
    DtoDistribution distribution1 = new DtoDistribution();
    distribution1.setOwnerId(1);
    distribution1.setProportion(new BigDecimal("5.00"));
    distributions.add(distribution1);

    DtoDistribution distribution2 = new DtoDistribution();
    distribution2.setOwnerId(2);
    distribution2.setProportion(new BigDecimal("5.00"));
    distributions.add(distribution2);

    request.setDistributions(distributions);

    // Use reflection to access the private method
    Method method = ExpenseService.class.getDeclaredMethod("setExpenseDistributionModels", DtoRequestExpense.class);
    method.setAccessible(true);
    List<ExpenseDistributionModel> result = (List<ExpenseDistributionModel>) method.invoke(expenseService, request);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(1, result.get(0).getOwnerId());
    assertEquals(new BigDecimal("5.00"), result.get(0).getProportion());
    assertEquals(2, result.get(1).getOwnerId());
    assertEquals(new BigDecimal("5.00"), result.get(1).getProportion());
}

    @Test
void setExpenseDistributionModels_InvalidProportion_ThrowsIllegalArgumentException() throws Exception {
    DtoRequestExpense request = new DtoRequestExpense();
    List<DtoDistribution> distributions = new ArrayList<>();
    DtoDistribution distribution1 = new DtoDistribution();
    distribution1.setOwnerId(1);
    distribution1.setProportion(new BigDecimal("6.00"));
    distributions.add(distribution1);

    DtoDistribution distribution2 = new DtoDistribution();
    distribution2.setOwnerId(2);
    distribution2.setProportion(new BigDecimal("5.00"));
    distributions.add(distribution2);

    request.setDistributions(distributions);

    Method method = ExpenseService.class.getDeclaredMethod("setExpenseDistributionModels", DtoRequestExpense.class);
    method.setAccessible(true);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        method.invoke(expenseService, request);
    });

    assertEquals("the sum of distributions can't be less or more than 10.00", exception.getMessage());
}


   /////////////////////EXPENSES BY ID///////////////////////////////
    @Test
void getExpenseById_ValidId_ReturnsDtoExpenseQuery() {
    Integer expenseId = 1;
    ExpenseEntity expenseEntity = new ExpenseEntity();
    expenseEntity.setEnabled(true);
    expenseEntity.setId(expenseId);
    expenseEntity.setAmount(new BigDecimal("100.00"));
    expenseEntity.setCategory(new ExpenseCategoryEntity());
    expenseEntity.setExpenseDate(LocalDate.now());
    expenseEntity.setExpenseType(ExpenseType.COMUN);
    expenseEntity.setProviderId(1);
    expenseEntity.setDistributions(new ArrayList<>());
    expenseEntity.setInstallmentsList(new ArrayList<>());

    when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));
    when(providerRestClient.getProvider(1)).thenReturn("Provider Name");
    when(modelMapper.map(expenseEntity, DtoExpenseQuery.class)).thenReturn(new DtoExpenseQuery());

    DtoExpenseQuery result = expenseService.getExpenseById(expenseId);

    assertNotNull(result);
    verify(expenseRepository, times(1)).findById(expenseId);
}
@Test
void getExpenseById_ExpenseNotEnabled_ThrowsCustomException() {
    Integer expenseId = 1;
    ExpenseEntity expenseEntity = new ExpenseEntity();
    expenseEntity.setEnabled(false);

    when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));

    CustomException exception = assertThrows(CustomException.class, () -> {
        expenseService.getExpenseById(expenseId);
    });

    assertEquals("The expense does not exist", exception.getMessage());
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
}
@Test
void getExpenseById_ExpenseDoesNotExist_ThrowsCustomException() {
    Integer expenseId = 1;

    when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
        expenseService.getExpenseById(expenseId);
    });

    assertEquals("The expense does not exist", exception.getMessage());
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
}


////////////////////EXPENSES BY DATE///////////////////////////////
    @Test
    void getExpenses_ValidDateRange_ReturnsDtoExpenseQueryList() {
        String dateFrom = "2023-01-01";
        String dateTo = "2023-12-31";
        ExpenseEntity expenseEntity = new ExpenseEntity();
        expenseEntity.setEnabled(true);
        expenseEntity.setCategory(new ExpenseCategoryEntity());
        expenseEntity.setDistributions(new ArrayList<>());
        expenseEntity.setInstallmentsList(new ArrayList<>());
        expenseEntity.setExpenseDate(LocalDate.parse("2023-06-01"));
        List<ExpenseEntity> expenseEntityList = Collections.singletonList(expenseEntity);
        DtoExpenseQuery dtoExpenseQuery = new DtoExpenseQuery();

        when(expenseRepository.findAllByDate(any(LocalDate.class), any(LocalDate.class))).thenReturn(expenseEntityList);
        when(modelMapper.map(expenseEntity, DtoExpenseQuery.class)).thenReturn(dtoExpenseQuery);

        List<DtoExpenseQuery> result = expenseService.getExpenses(null, null, null, dateFrom, dateTo);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(expenseRepository, times(1)).findAllByDate(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getExpenses_InvalidDateRange_ThrowsCustomException() {
        String dateFrom = "2023-12-31";
        String dateTo = "2023-01-01";

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseService.getExpenses(null, null, null, dateFrom, dateTo);
        });

        assertEquals("The date range is not correct", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void getExpenses_NullDateRange_ThrowsCustomException() {
        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseService.getExpenses(null, null, null, null, null);
        });

        assertEquals("The date range is required", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void getExpenses_ExpenseNotEnabled_IsNotIncludedInResult() {
        String dateFrom = "2023-01-01";
        String dateTo = "2023-12-31";
        ExpenseEntity expenseEntity = new ExpenseEntity();
        expenseEntity.setEnabled(false);
        List<ExpenseEntity> expenseEntityList = Collections.singletonList(expenseEntity);

        when(expenseRepository.findAllByDate(any(LocalDate.class), any(LocalDate.class))).thenReturn(expenseEntityList);

        List<DtoExpenseQuery> result = expenseService.getExpenses(null, null, null, dateFrom, dateTo);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(expenseRepository, times(1)).findAllByDate(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getExpenses_InvalidDateFormat_ThrowsCustomException() {
        String dateFrom = "invalid-date";
        String dateTo = "2023-12-31";

        CustomException exception = assertThrows(CustomException.class, () -> {
            expenseService.getExpenses(null, null, null, dateFrom, dateTo);
        });

        assertEquals("The date format is not correct", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    //////////////////// MAP ENTITY TO DTO EXPENSE /////////////////////


    @Test
    void mapEntityToDtoExpense_ValidExpenseEntity_ReturnsDtoExpenseQuery() throws Exception {
        ExpenseEntity expenseEntity = new ExpenseEntity();
        expenseEntity.setProviderId(1);
        expenseEntity.setExpenseDate(LocalDate.now());
        expenseEntity.setFileId(UUID.randomUUID());
        ExpenseCategoryEntity category = new ExpenseCategoryEntity();
        category.setDescription("Category Description");
        expenseEntity.setCategory(category);
        expenseEntity.setDistributions(new ArrayList<>());
        expenseEntity.setInstallmentsList(new ArrayList<>());

        DtoExpenseQuery dtoExpenseQuery = new DtoExpenseQuery();
        when(modelMapper.map(expenseEntity, DtoExpenseQuery.class)).thenReturn(dtoExpenseQuery);
        when(providerRestClient.getProvider(1)).thenReturn("Provider Name");

        // Use reflection to access the private method
        Method method = ExpenseService.class.getDeclaredMethod("mapEntityToDtoExpense", ExpenseEntity.class);
        method.setAccessible(true);
        DtoExpenseQuery result = (DtoExpenseQuery) method.invoke(expenseService, expenseEntity);

        assertNotNull(result);
        verify(modelMapper, times(1)).map(expenseEntity, DtoExpenseQuery.class);
        verify(providerRestClient, times(1)).getProvider(1);
    }

  @Test
void mapEntityToDtoExpense_ExpenseEntityWithDistributionsAndInstallments_ReturnsDtoExpenseQuery() throws Exception {
    ExpenseEntity expenseEntity = new ExpenseEntity();
    expenseEntity.setProviderId(1);
    expenseEntity.setExpenseDate(LocalDate.now());
    expenseEntity.setFileId(UUID.fromString("00000000-0000-0000-0000-000000000123"));
    expenseEntity.setAmount(new BigDecimal("100.00"));
    ExpenseCategoryEntity category = new ExpenseCategoryEntity();
    category.setDescription("Category Description");
    expenseEntity.setCategory(category);

    ExpenseDistributionEntity distributionEntity = new ExpenseDistributionEntity();
    distributionEntity.setOwnerId(1);
    distributionEntity.setProportion(new BigDecimal("0.5"));
    expenseEntity.setDistributions(List.of(distributionEntity));

    ExpenseInstallmentEntity installmentEntity = new ExpenseInstallmentEntity();
    installmentEntity.setInstallmentNumber(1);
    installmentEntity.setPaymentDate(LocalDate.now());
    expenseEntity.setInstallmentsList(List.of(installmentEntity));

    DtoExpenseQuery dtoExpenseQuery = new DtoExpenseQuery();
    when(modelMapper.map(expenseEntity, DtoExpenseQuery.class)).thenReturn(dtoExpenseQuery);
    when(providerRestClient.getProvider(1)).thenReturn("Provider Name");
    when(ownerRestClient.getOwnerFullName(1)).thenReturn("Owner Name");

    // Use reflection to access the private method
    Method method = ExpenseService.class.getDeclaredMethod("mapEntityToDtoExpense", ExpenseEntity.class);
    method.setAccessible(true);
    DtoExpenseQuery result = (DtoExpenseQuery) method.invoke(expenseService, expenseEntity);

    assertNotNull(result);
    assertEquals("Provider Name", result.getProvider());
    assertEquals(expenseEntity.getExpenseDate(), result.getExpenseDate());
    assertEquals("00000000-0000-0000-0000-000000000123", result.getFileId());
    assertEquals("Category Description", result.getCategory());
    assertEquals(1, result.getDistributionList().size());
    assertEquals("Owner Name", result.getDistributionList().get(0).getOwnerFullName());
    assertEquals(new BigDecimal("0.5").multiply(expenseEntity.getAmount()), result.getDistributionList().get(0).getAmount());
    assertEquals(1, result.getInstallmentList().size());
    assertEquals(1, result.getInstallmentList().get(0).getInstallmentNumber());
    assertEquals(expenseEntity.getInstallmentsList().get(0).getPaymentDate(), result.getInstallmentList().get(0).getPaymentDate());
}

}
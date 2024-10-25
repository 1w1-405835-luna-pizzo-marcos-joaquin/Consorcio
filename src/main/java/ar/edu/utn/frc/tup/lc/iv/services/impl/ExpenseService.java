package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.client.OwnerRestClient;
import ar.edu.utn.frc.tup.lc.iv.client.ProviderRestClient;
import ar.edu.utn.frc.tup.lc.iv.comunication.FileServerRestClient;
import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.*;
import ar.edu.utn.frc.tup.lc.iv.dtos.owner.OwnerDto;
import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseInstallmentsEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseInstallmentEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseCategoryModel;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseDistributionModel;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseInstallmentModel;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseModel;
import ar.edu.utn.frc.tup.lc.iv.repositories.*;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseService;
import jakarta.transaction.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ExpenseService implements IExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private ExpenseDistributionRepository expenseDistributionRepository;
    @Autowired
    private ExpenseInstallmentRepository expenseInstallmentRepository;
    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ExpenseCategoryService expenseCategoryService;
    @Autowired
    private FileServerRestClient fileServerRestClient;
    @Autowired
    private BillExpenseInstallmentsRepository billExpenseInstallmentsRepository;
    @Autowired
    private OwnerRestClient ownerRestClient;
    @Autowired
    private ProviderRestClient providerRestClient;


    /**
     * Creates a new expense based on the provided request and file.
     *
     * @param request The DTO containing expense details.
     * @param file The file associated with the expense (can be null).
     * @return ResponseEntity containing the created expense details.
     * @throws CustomException if the expense is not valid or already exists.
     */
    @Transactional
    public ResponseEntity<DtoResponseExpense> postExpense(DtoRequestExpense request, MultipartFile file) {
        Boolean expenseValid = fetchValidExpenseModel(request, file);
        if (expenseValid) {
            ExpenseModel expenseModel = setDataToExpenseModel(request);
            List<ExpenseInstallmentModel> expenseInstallmentModels = setExpenseInstallmentModels(request, expenseModel);
            List<ExpenseDistributionModel> expenseDistributionModels = new ArrayList<>();
            if(ExpenseType.valueOf(request.getTypeExpense()).equals(ExpenseType.INDIVIDUAL)) {
                expenseDistributionModels = setExpenseDistributionModels(request);
            }
            expenseModel.setInstallmentsList(expenseInstallmentModels);
            expenseModel.setDistributions(expenseDistributionModels);
            UUID fileId = UUID.randomUUID();//TODO FILE SERVER
            expenseModel.setFileId(fileId);
            DtoResponseExpense dtoResponseExpense = setDtoResponseExpense(expenseModel);
            saveExpenseEntity(expenseModel, expenseInstallmentModels, expenseDistributionModels);
            return ResponseEntity.ok(dtoResponseExpense);
        } else {
            throw new CustomException("The expense is not valid", HttpStatus.CONFLICT);
        }

    }

    @Override
    public List<ExpenseModel> getExpenseByPaymentDateRange(LocalDate from, LocalDate to) {
        List<ExpenseEntity> expenseEntities = expenseRepository.findAllByPaymentDate(from,to);
        List<ExpenseModel> result = new ArrayList<>();
        for (ExpenseEntity expenseEntity : expenseEntities) {
            result.add(modelMapper.map(expenseEntity, ExpenseModel.class));
        }
        return result;
    }

    private void saveExpenseEntity(ExpenseModel expenseModel, List<ExpenseInstallmentModel> expenseInstallmentModels, List<ExpenseDistributionModel> expenseDistributionModels) {
        ExpenseEntity expenseEntity = modelMapper.map(expenseModel, ExpenseEntity.class);
        expenseEntity.setDistributions(new ArrayList<>());
        expenseEntity.setInstallmentsList(new ArrayList<>());
        expenseEntity.setNoteCredit(false);
        if (expenseEntity.getAmount() == null || expenseEntity.getCategory() == null) {
            throw new CustomException("Missing required fields in ExpenseEntity", HttpStatus.BAD_REQUEST);
        }
        expenseEntity = expenseRepository.save(expenseEntity);

        for (ExpenseDistributionModel distributionModel : expenseDistributionModels) {
            ExpenseDistributionEntity distributionEntity = modelMapper.map(distributionModel, ExpenseDistributionEntity.class);
            distributionEntity.setExpense(expenseEntity);
            expenseDistributionRepository.save(distributionEntity);
            expenseEntity.getDistributions().add(distributionEntity);
        }

        for (ExpenseInstallmentModel installmentModel : expenseInstallmentModels) {
            ExpenseInstallmentEntity expenseInstallmentEntity = modelMapper.map(installmentModel, ExpenseInstallmentEntity.class);
            expenseInstallmentEntity.setExpense(expenseEntity);
            expenseInstallmentRepository.save(expenseInstallmentEntity);
            expenseEntity.getInstallmentsList().add(expenseInstallmentEntity);
        }

    }
    /**
     * Creates a DTO response from an expense model.
     *
     * @param expenseModel The expense model to convert.
     * @return A DtoResponseExpense object containing the expense details.
     */
    private DtoResponseExpense setDtoResponseExpense(ExpenseModel expenseModel) {
        DtoResponseExpense dtoResponseExpense = new DtoResponseExpense();
        dtoResponseExpense.setExpenseDate(expenseModel.getExpenseDate());
        dtoResponseExpense.setExpenseType(expenseModel.getExpenseType());
        dtoResponseExpense.setDescription(expenseModel.getDescription());
        dtoResponseExpense.setFileId(expenseModel.getFileId());
        DtoCategory dtoCategory = new DtoCategory();
        dtoCategory.setId(expenseModel.getCategory().getId());
        dtoCategory.setDescription(expenseModel.getCategory().getDescription());
        dtoResponseExpense.setDtoCategory(dtoCategory);
        dtoResponseExpense.setInvoiceNumber(expenseModel.getInvoiceNumber());
        dtoResponseExpense.setProviderId(expenseModel.getProviderId());
        List<DtoInstallment> dtoInstallments = new ArrayList<>();
        for(ExpenseInstallmentModel expenseInstallmentModel : expenseModel.getInstallmentsList())
        {
            DtoInstallment dtoInstallment = new DtoInstallment();
            dtoInstallment.setPaymentDate(expenseInstallmentModel.getPaymentDate());
            dtoInstallment.setInstallmentNumber(expenseInstallmentModel.getInstallmentNumber());
            dtoInstallments.add(dtoInstallment);
        }
        dtoResponseExpense.setDtoInstallmentList(dtoInstallments);
        List<DtoDistribution> dtoDistributions = new ArrayList<>();
        for(ExpenseDistributionModel expenseDistributionModel : expenseModel.getDistributions()){
            DtoDistribution dtoDistribution = new DtoDistribution();
            dtoDistribution.setProportion(expenseDistributionModel.getProportion());
            dtoDistribution.setOwnerId(expenseDistributionModel.getOwnerId());
            dtoDistributions.add(dtoDistribution);
        }
        dtoResponseExpense.setDtoDistributionList(dtoDistributions);
        return dtoResponseExpense;

    }
    /**
     * Creates a list of expense installment models based on the request and expense model.
     *
     * @param request The DTO containing expense details.
     * @param expenseModel The expense model.
     * @return A list of ExpenseInstallmentModel objects.
     */
    private List<ExpenseInstallmentModel> setExpenseInstallmentModels(DtoRequestExpense request, ExpenseModel expenseModel) {
        List<ExpenseInstallmentModel> expenseInstallmentModels = new ArrayList<>();
        Integer installments = 1;
        do {
            ExpenseInstallmentModel expenseInstallmentModel = new ExpenseInstallmentModel();
            expenseInstallmentModel.setInstallmentNumber(installments);
            expenseInstallmentModel.setEnabled(Boolean.TRUE);
            expenseInstallmentModel.setCreatedDatetime(LocalDateTime.now());
            expenseInstallmentModel.setCreatedUser(1);
            if (installments.equals(1)) {
                expenseInstallmentModel.setPaymentDate(LocalDate.now());
            } else {
                expenseInstallmentModel.setPaymentDate(LocalDate.now().plusMonths(installments - 1));
            }

            expenseInstallmentModel.setLastUpdatedDatetime(LocalDateTime.now());
            expenseInstallmentModel.setLastUpdatedUser(1);
            expenseInstallmentModels.add(expenseInstallmentModel);
            installments++;
        } while (installments <= request.getInstallments());

        return expenseInstallmentModels;

    }

    /**
     * Creates a list of expense distribution models based on the request.
     *
     * @param request The DTO containing expense details.
     * @return A list of ExpenseDistributionModel objects.
     * @throws IllegalArgumentException if the sum of distributions is not valid.
     */
    private List<ExpenseDistributionModel> setExpenseDistributionModels(DtoRequestExpense request) {
        List<ExpenseDistributionModel> expenseDistributionModels = new ArrayList<>();
        BigDecimal totalProportion = BigDecimal.ZERO;

        for (DtoDistribution dtoDistribution : request.getDistributions()) {
            totalProportion = totalProportion.add(dtoDistribution.getProportion());
            if (totalProportion.compareTo(new BigDecimal("10.00")) > 0 || totalProportion.compareTo(new BigDecimal("-10.00")) < 0) {
                throw new IllegalArgumentException("the sum of distributions can't be less or more than 10.00");
            }
            ExpenseDistributionModel expenseDistributionModel = new ExpenseDistributionModel();
            expenseDistributionModel.setEnabled(Boolean.TRUE);
            expenseDistributionModel.setCreatedUser(1);
            expenseDistributionModel.setLastUpdatedDatetime(LocalDateTime.now());
            expenseDistributionModel.setCreatedDatetime(LocalDateTime.now());
            expenseDistributionModel.setLastUpdatedUser(1);
            expenseDistributionModel.setOwnerId(dtoDistribution.getOwnerId());
            expenseDistributionModel.setProportion(dtoDistribution.getProportion());
            expenseDistributionModels.add(expenseDistributionModel);
        }

        return expenseDistributionModels;
    }
    /**
     * Creates an expense model from the provided request.
     *
     * @param request The DTO containing expense details.
     * @return An ExpenseModel object.
     */
    private ExpenseModel setDataToExpenseModel(DtoRequestExpense request) {
        ExpenseModel expenseModel = new ExpenseModel();
        expenseModel.setDescription(request.getDescription());
        expenseModel.setProviderId(request.getProviderId());
        expenseModel.setExpenseDate(request.getExpenseDate());
        expenseModel.setInvoiceNumber(request.getInvoiceNumber());
        expenseModel.setExpenseType(ExpenseType.valueOf(request.getTypeExpense()));
        ExpenseCategoryModel expenseCategoryModel = expenseCategoryService.getCategoryModel(request.getCategoryId());
        expenseModel.setCategory(expenseCategoryModel);
        expenseModel.setAmount(request.getAmount());
        expenseModel.setInstallments(request.getInstallments());
        expenseModel.setCreatedDatetime(LocalDateTime.now());
        expenseModel.setLastUpdatedDatetime(LocalDateTime.now());
        expenseModel.setCreatedUser(1);//the user must be receipted from the frontend
        expenseModel.setLastUpdatedUser(1);
        expenseModel.setEnabled(Boolean.TRUE);
        return expenseModel;

    }
    /**
     * Validates the expense request and associated file.
     *
     * @param request The DTO containing expense details.
     * @param file The file associated with the expense (can be null).
     * @return true if the expense is valid, false otherwise.
     * @throws CustomException for various validation errors.
     */
    private Boolean fetchValidExpenseModel(DtoRequestExpense request,MultipartFile file) {
        Optional<ExpenseEntity> expenseEntityValidateExist =  expenseRepository.findExpenseEntitiesByInvoiceNumberAndProviderId(request.getInvoiceNumber(), request.getProviderId());
        if (expenseEntityValidateExist.isPresent()) {
            throw new CustomException("The expense have already exist", HttpStatus.BAD_REQUEST);
        }
        ExpenseCategoryModel expenseCategoryModel = expenseCategoryService.getCategoryModel(request.getCategoryId());
        if (expenseCategoryModel == null) {
            throw new CustomException("The category does not exist", HttpStatus.BAD_REQUEST);
        }
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            throw new CustomException("Description cannot be empty", HttpStatus.BAD_REQUEST);
        }

        if (request.getProviderId() == null) {
            throw new CustomException("Provider ID cannot be null", HttpStatus.BAD_REQUEST);
        }

        if (request.getExpenseDate() == null) {
            throw new CustomException("Expense date cannot be null", HttpStatus.BAD_REQUEST);
        }

        if (request.getInvoiceNumber() == null) {
            throw new CustomException("Invoice number cannot be null", HttpStatus.BAD_REQUEST);
        }

        if (request.getTypeExpense() == null || request.getTypeExpense().isEmpty()) {
            throw new CustomException("Type of expense cannot be empty", HttpStatus.BAD_REQUEST);
        }
        List<String> validExpenseTypes = Arrays.asList("COMUN", "EXTRAORDINARIO", "INDIVIDUAL");
        if (!validExpenseTypes.contains(request.getTypeExpense().toUpperCase())) {
            throw new CustomException("Type of expense must be one of: COMUN, EXTRAORDINARIO, INDIVIDUAL", HttpStatus.BAD_REQUEST);
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Amount must be greater than zero", HttpStatus.BAD_REQUEST);
        }

        if (request.getInstallments() == null || request.getInstallments() <= 0) {
            throw new CustomException("Installments must be greater than zero", HttpStatus.BAD_REQUEST);
        }

        if (request.getDistributions().isEmpty() && request.getTypeExpense().equals(ExpenseType.INDIVIDUAL.toString())) {
            throw new CustomException("Distributions cannot be empty", HttpStatus.BAD_REQUEST);
        }
        if (file != null) {
            if (file.getContentType() == null ||
                    (!file.getContentType().startsWith("image/") && !"application/pdf".equals(file.getContentType()))) {
                throw new CustomException("the file must be an image or pdf", HttpStatus.BAD_REQUEST);
            }
        }
        return true;
    }

    /**
 * Retrieves an expense by its ID.
 *
 * @param expenseId the ID of the expense to retrieve
 * @return a DtoExpenseQuery object containing the details of the expense
 * @throws CustomException if the expense does not exist or is not enabled
 */
public DtoExpenseQuery getExpenseById(Integer expenseId) {
    // Initialize the DTO object
    DtoExpenseQuery dtoExpenseQuery = new DtoExpenseQuery();

    // Retrieve the expense entity from the repository
    ExpenseEntity expenseEntity = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new CustomException("The expense does not exist", HttpStatus.NOT_FOUND));

    // Check if the expense is enabled
    if (!expenseEntity.getEnabled()) {
        throw new CustomException("The expense does not exist", HttpStatus.NOT_FOUND);
    }

    // Map the entity to the DTO
    dtoExpenseQuery = mapEntityToDtoExpense(expenseEntity);

    return dtoExpenseQuery;
}


   /**
 * Retrieves a list of expenses filtered by the given parameters.
 *
 * @param expenseType the type of expense to filter by (optional)
 * @param category the category of the expense to filter by (optional)
 * @param provider the provider of the expense to filter by (optional)
 * @param dateFrom the start date for filtering expenses (required, format: YYYY-MM-DD)
 * @param dateTo the end date for filtering expenses (required, format: YYYY-MM-DD)
 * @return a list of DtoExpenseQuery objects that match the filters
 * @throws CustomException if the date range is invalid or the date format is incorrect
 */
public List<DtoExpenseQuery> getExpenses(String expenseType, String category, String provider, String dateFrom, String dateTo) {
    DtoExpenseQuery dtoExpenseQuery = new DtoExpenseQuery();
    List<DtoExpenseQuery> dtoExpenseQueryList = new ArrayList<>();
    if (dateFrom == null || dateTo == null) {
        throw new CustomException("The date range is required", HttpStatus.BAD_REQUEST);
    }

    DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate from1;
    LocalDate to1;
    try {
        from1 = LocalDate.parse(dateFrom, formatter1);
        to1 = LocalDate.parse(dateTo, formatter1);
    } catch (DateTimeParseException e) {
        throw new CustomException("The date format is not correct", HttpStatus.BAD_REQUEST);
    }

    if (from1.isAfter(to1)) {
        throw new CustomException("The date range is not correct", HttpStatus.BAD_REQUEST);
    }

    // Retrieve all expenses from the database within the date range
    List<ExpenseEntity> expenseEntityList = expenseRepository.findAllByDate(from1, to1);
    List<OwnerDto> ownerDtos = getOwners();
    List<ExpenseModel> expenseModelList = new ArrayList<>();
    for (ExpenseEntity expenseEntity : expenseEntityList) {
        expenseModelList.add(modelMapper.map(expenseEntity, ExpenseModel.class));
    }

    // Add only active expenses and expenses that matches to the list
    for (ExpenseModel expenseModel : expenseModelList) {
        if (!expenseModel.getEnabled()) {
            continue;
        }
        dtoExpenseQuery = mapModelToDtoExpense(expenseModel,ownerDtos);
        if (expenseType != null && !dtoExpenseQuery.getExpenseType().equalsIgnoreCase(expenseType)) {
            continue;
        }
        if (category != null && !dtoExpenseQuery.getCategory().equalsIgnoreCase(category)) {
            continue;
        }
        if (provider != null && !dtoExpenseQuery.getProvider().equalsIgnoreCase(provider)) {
            continue;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate from = LocalDate.parse(dateFrom, formatter);
            LocalDate expenseDate = dtoExpenseQuery.getExpenseDate();
            if (expenseDate.isBefore(from)) {
                continue;
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            throw new CustomException("The date format is not correct", HttpStatus.BAD_REQUEST);
        }

        try {
            LocalDate to = LocalDate.parse(dateTo, formatter);
            LocalDate expenseDate = dtoExpenseQuery.getExpenseDate();
            if (expenseDate.isAfter(to)) {
                continue;
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            throw new CustomException("The date format is not correct", HttpStatus.BAD_REQUEST);
        }

        dtoExpenseQueryList.add(dtoExpenseQuery);
    }
    return dtoExpenseQueryList;
}

   /**
 * Maps an ExpenseEntity to a DtoExpenseQuery.
 *
 * @param expenseEntity the ExpenseEntity to map
 * @return the mapped DtoExpenseQuery
 */
private DtoExpenseQuery mapEntityToDtoExpense(ExpenseEntity expenseEntity) {
    DtoExpenseQuery dtoExpenseQuery = modelMapper.map(expenseEntity, DtoExpenseQuery.class);

    // Set provider information
    if (expenseEntity.getProviderId() != null) {
        dtoExpenseQuery.setProvider(providerRestClient.getProvider(expenseEntity.getProviderId()));
    } else {
        dtoExpenseQuery.setProvider("");
    }

    // Set basic expense information
    dtoExpenseQuery.setExpenseDate(expenseEntity.getExpenseDate());
    dtoExpenseQuery.setFileId(expenseEntity.getFileId() != null ? expenseEntity.getFileId().toString() : null);
    dtoExpenseQuery.setCategory(expenseEntity.getCategory().getDescription());
    dtoExpenseQuery.setDistributionList(new ArrayList<>());
    dtoExpenseQuery.setInstallmentList(new ArrayList<>());

    // Map distributions
    for (ExpenseDistributionEntity distributionEntity : expenseEntity.getDistributions()) {
        String ownerName = ownerRestClient.getOwnerFullName(distributionEntity.getOwnerId());
        BigDecimal amount = expenseEntity.getAmount().multiply(distributionEntity.getProportion());

        DtoExpenseDistributionQuery dtoExpenseDistributionQuery = new DtoExpenseDistributionQuery();
        dtoExpenseDistributionQuery.setOwnerFullName(ownerName);
        dtoExpenseDistributionQuery.setAmount(amount);
        dtoExpenseDistributionQuery.setOwnerId(distributionEntity.getOwnerId());
        dtoExpenseDistributionQuery.setProportion(distributionEntity.getProportion());

        dtoExpenseQuery.getDistributionList().add(dtoExpenseDistributionQuery);
    }

    // Map installments
    for (ExpenseInstallmentEntity installmentEntity : expenseEntity.getInstallmentsList()) {
        DtoExpenseInstallment dtoExpenseInstallment = new DtoExpenseInstallment();
        dtoExpenseInstallment.setInstallmentNumber(installmentEntity.getInstallmentNumber());
        dtoExpenseInstallment.setPaymentDate(installmentEntity.getPaymentDate());

        dtoExpenseQuery.getInstallmentList().add(dtoExpenseInstallment);
    }

    return dtoExpenseQuery;
}
    private DtoExpenseQuery mapModelToDtoExpense(ExpenseModel expenseModel, List<OwnerDto> ownerDtos) {
        DtoExpenseQuery dtoExpenseQuery = new DtoExpenseQuery();

        try {
            dtoExpenseQuery = modelMapper.map(expenseModel, DtoExpenseQuery.class);
        } catch (Exception e) {
            // Maneja la excepción del ModelMapper
            System.err.println("Error al mapear el modelo a DTO: " + e.getMessage());
            // Puedes continuar el mapeo sin problema
        }

        // Set provider information
        try {
            if (expenseModel.getProviderId() != null) {
                dtoExpenseQuery.setProvider(providerRestClient.getProvider(expenseModel.getProviderId()));
            } else {
                dtoExpenseQuery.setProvider("");
            }
        } catch (Exception e) {
            System.err.println("Error al obtener el proveedor: " + e.getMessage());
        }

        // Set basic expense information
        dtoExpenseQuery.setExpenseDate(expenseModel.getExpenseDate());
        dtoExpenseQuery.setFileId(expenseModel.getFileId() != null ? expenseModel.getFileId().toString() : null);

        // Validate category and set it
        try {
            dtoExpenseQuery.setCategory(expenseModel.getCategory() != null ? expenseModel.getCategory().getDescription() : "Unknown");
        } catch (Exception e) {
            System.err.println("Error al mapear la categoría: " + e.getMessage());
        }

        dtoExpenseQuery.setDistributionList(new ArrayList<>());
        dtoExpenseQuery.setInstallmentList(new ArrayList<>());

        // Map distributions, continuando aunque no se encuentre el Owner
        if (expenseModel.getDistributions() != null) {
            for (ExpenseDistributionModel distributionModel : expenseModel.getDistributions()) {
                try {
                    // Intentar obtener el OwnerDto
                    Optional<OwnerDto> ownerDtoOptional = ownerDtos.stream()
                            .filter(m -> m.getId().equals(distributionModel.getOwnerId()))
                            .findFirst();

                    String ownerName = ownerDtoOptional
                            .map(ownerDto -> ownerDto.getLastName() + " " + ownerDto.getName())
                            .orElse("JUAN");

                    // Calcular el monto
                    BigDecimal amount = BigDecimal.ZERO;
                    if (expenseModel.getAmount() != null && distributionModel.getProportion() != null) {
                        amount = expenseModel.getAmount().multiply(distributionModel.getProportion());
                    }

                    DtoExpenseDistributionQuery dtoExpenseDistributionQuery = new DtoExpenseDistributionQuery();
                    dtoExpenseDistributionQuery.setOwnerFullName(ownerName);
                    dtoExpenseDistributionQuery.setAmount(amount);
                    dtoExpenseDistributionQuery.setOwnerId(distributionModel.getOwnerId());

                    dtoExpenseQuery.getDistributionList().add(dtoExpenseDistributionQuery);
                } catch (Exception e) {
                    System.err.println("Error al mapear la distribución: " + e.getMessage());
                    // Continuar el mapeo sin detener el flujo
                }
            }
        }

        // Map installments
        if (expenseModel.getInstallmentsList() != null) {
            for (ExpenseInstallmentModel installmentModel : expenseModel.getInstallmentsList()) {
                try {
                    DtoExpenseInstallment dtoExpenseInstallment = new DtoExpenseInstallment();
                    dtoExpenseInstallment.setInstallmentNumber(installmentModel.getInstallmentNumber());
                    dtoExpenseInstallment.setPaymentDate(installmentModel.getPaymentDate());

                    dtoExpenseQuery.getInstallmentList().add(dtoExpenseInstallment);
                } catch (Exception e) {
                    System.err.println("Error al mapear las cuotas: " + e.getMessage());
                }
            }
        }

        return dtoExpenseQuery;
    }



    /**
     * Deletes an expense logically by setting its enabled status to false.
     *
     * @param id The ID of the expense to be deleted.
     * @return A DtoResponseDeleteExpense object containing the deletion result.
     * @throws CustomException if the expense does not exist or has related bill installments.
     */
        @Override
        public DtoResponseDeleteExpense deteleExpense(Integer id) {
            Optional<ExpenseEntity> expenseEntityOptional = expenseRepository.findById(id);
            if (expenseEntityOptional.isEmpty()) {
                throw new CustomException("The expense does not exist", HttpStatus.BAD_REQUEST);
            }

            ExpenseEntity expenseEntity = expenseEntityOptional.get();
            Optional<List<BillExpenseInstallmentsEntity>> billExpenseInstallmentsEntity = billExpenseInstallmentsRepository.findByExpenseId(id);

            if (billExpenseInstallmentsEntity.map(List::isEmpty).orElse(true)) {
                performLogicalDeletion(expenseEntity);
                DtoResponseDeleteExpense dtoResponseDeleteExpense = new DtoResponseDeleteExpense();
                dtoResponseDeleteExpense.setExpense(expenseEntity.getDescription());
                dtoResponseDeleteExpense.setHttpStatus(HttpStatus.OK);
                dtoResponseDeleteExpense.setDescriptionResponse("Expense delete logic successfully");
                return dtoResponseDeleteExpense;
            } else {
                throw new CustomException("Expense has related bill installments", HttpStatus.CONFLICT);
            }
        }


    /**
     * Performs a logical deletion of an expense by setting its enabled status to false.
     *
     * @param expenseEntity The expense entity to be logically deleted.
     */
        private void performLogicalDeletion(ExpenseEntity expenseEntity) {
            expenseEntity.setEnabled(Boolean.FALSE);
            expenseRepository.save(expenseEntity);
        }

    /**
     * Creates a credit note for an existing expense.
     *
     * @param id The ID of the expense for which to create a credit note.
     * @return A DtoResponseExpense object containing the created credit note details.
     * @throws CustomException if the expense already has a credit note or other validation errors occur.
     */
        @Transactional
        public DtoResponseExpense createCreditNoteForExpense(Integer id) {

            DtoResponseExpense dtoResponseExpense = new DtoResponseExpense();
            Optional<ExpenseEntity> expenseEntityOptional = expenseRepository.findById(id);
            if (expenseEntityOptional.get().getNoteCredit()) {
                throw new CustomException("The expense have a note of credit", HttpStatus.CONFLICT);
            }

            ExpenseEntity expenseEntity = expenseEntityOptional.get();
            Optional<List<BillExpenseInstallmentsEntity>> billExpenseInstallmentsEntity = billExpenseInstallmentsRepository.findByExpenseId(id);

            if (billExpenseInstallmentsEntity.isPresent()) {
                expenseEntityOptional.get().setNoteCredit(Boolean.TRUE);
                expenseRepository.save(expenseEntityOptional.get());
                int sizeOfInstallments = billExpenseInstallmentsEntity.get().size();
                LocalDate paymentDate = LocalDate.now();

                ExpenseEntity newExpenseEntity = createCreditNoteEntity(expenseEntity);
                expenseRepository.save(newExpenseEntity);

                List<ExpenseInstallmentEntity> expenseInstallmentEntityList = createInstallments(newExpenseEntity, sizeOfInstallments, paymentDate);
                List<ExpenseDistributionEntity> newExpenseDistributionList = new ArrayList<>();
                if(!expenseEntity.getDistributions().isEmpty())
                {

                    for (ExpenseDistributionEntity originalDistribution : expenseEntity.getDistributions()) {
                        ExpenseDistributionEntity newDistribution = new ExpenseDistributionEntity();
                        newDistribution.setProportion(originalDistribution.getProportion());
                        newDistribution.setExpense(newExpenseEntity);
                        newDistribution.setLastUpdatedUser(1);
                        newDistribution.setLastUpdatedDatetime(LocalDateTime.now());
                        newDistribution.setCreatedDatetime(LocalDateTime.now());
                        newDistribution.setCreatedUser(1);
                        newDistribution.setEnabled(Boolean.TRUE);
                        newDistribution.setOwnerId(originalDistribution.getOwnerId());
                        newExpenseDistributionList.add(newDistribution);

                    }

                    expenseDistributionRepository.saveAll(newExpenseDistributionList);
                }
                dtoResponseExpense = setExpenseEntityToDtoResponse(expenseEntity,expenseInstallmentEntityList,newExpenseDistributionList);
                saveInstallments(expenseInstallmentEntityList, newExpenseEntity);
            }
            return dtoResponseExpense;
        }
    /**
     * Converts an ExpenseEntity and related data to a DtoResponseExpense.
     *
     * @param expenseEntity The expense entity to convert.
     * @param expenseInstallmentEntityList The list of installment entities associated with the expense.
     * @param newExpenseDistributionList The list of distribution entities associated with the expense.
     * @return A DtoResponseExpense object containing the expense details.
     */
    private DtoResponseExpense setExpenseEntityToDtoResponse(ExpenseEntity expenseEntity, List<ExpenseInstallmentEntity> expenseInstallmentEntityList, List<ExpenseDistributionEntity> newExpenseDistributionList) {
        DtoResponseExpense dtoResponseExpense = new DtoResponseExpense();
        dtoResponseExpense.setExpenseDate(expenseEntity.getExpenseDate());
        dtoResponseExpense.setExpenseType(ExpenseType.NOTE_OF_CREDIT);
        dtoResponseExpense.setFileId(expenseEntity.getFileId());
        dtoResponseExpense.setDescription(expenseEntity.getDescription());
        DtoCategory dtoCategory = new DtoCategory();
        dtoCategory.setId(expenseEntity.getCategory().getId());
        dtoCategory.setDescription(expenseEntity.getCategory().getDescription());
        dtoResponseExpense.setDtoCategory(dtoCategory);
        dtoResponseExpense.setInvoiceNumber(expenseEntity.getInvoiceNumber());
        dtoResponseExpense.setProviderId(expenseEntity.getProviderId());
        List<DtoInstallment> dtoInstallments = new ArrayList<>();
        for(ExpenseInstallmentEntity expenseInstallmentEntity : expenseEntity.getInstallmentsList())
        {
            DtoInstallment dtoInstallment = new DtoInstallment();
            dtoInstallment.setPaymentDate(expenseInstallmentEntity.getPaymentDate());
            dtoInstallment.setInstallmentNumber(expenseInstallmentEntity.getInstallmentNumber());
            dtoInstallments.add(dtoInstallment);
        }
        dtoResponseExpense.setDtoInstallmentList(dtoInstallments);
        List<DtoDistribution> dtoDistributions = new ArrayList<>();
        for(ExpenseDistributionEntity expenseDistributionEntity : expenseEntity.getDistributions()){
            DtoDistribution dtoDistribution = new DtoDistribution();
            dtoDistribution.setProportion(expenseDistributionEntity.getProportion());
            dtoDistribution.setOwnerId(expenseDistributionEntity.getOwnerId());
            dtoDistributions.add(dtoDistribution);
        }
        dtoResponseExpense.setDtoDistributionList(dtoDistributions);
        return dtoResponseExpense;
    }
    /**
     * Creates a new ExpenseEntity for a credit note based on an original expense.
     *
     * @param originalExpenseEntity The original expense entity.
     * @return A new ExpenseEntity representing the credit note.
     */
        private ExpenseEntity createCreditNoteEntity(ExpenseEntity originalExpenseEntity) {
            ExpenseEntity newExpenseEntity = new ExpenseEntity();
            newExpenseEntity.setExpenseType(ExpenseType.NOTE_OF_CREDIT);
            newExpenseEntity.setEnabled(Boolean.TRUE);
            newExpenseEntity.setNoteCredit(Boolean.TRUE);
            newExpenseEntity.setExpenseDate(LocalDate.now());
            newExpenseEntity.setDescription("Note of credit"); // description??
            newExpenseEntity.setDistributions(new ArrayList<>());
            newExpenseEntity.setAmount(originalExpenseEntity.getAmount().negate());
            newExpenseEntity.setFileId(originalExpenseEntity.getFileId());
            newExpenseEntity.setCategory(originalExpenseEntity.getCategory());
            newExpenseEntity.setInvoiceNumber(originalExpenseEntity.getInvoiceNumber());
            newExpenseEntity.setProviderId(originalExpenseEntity.getProviderId());
            newExpenseEntity.setCreatedDatetime(LocalDateTime.now());
            newExpenseEntity.setCreatedUser(1);
            newExpenseEntity.setLastUpdatedDatetime(LocalDateTime.now());
            newExpenseEntity.setLastUpdatedUser(1);
            newExpenseEntity.setInstallments(originalExpenseEntity.getInstallments());
            newExpenseEntity.setInstallmentsList(new ArrayList<>());

            return newExpenseEntity;
        }
    /**
     * Creates a list of ExpenseInstallmentEntity objects for a credit note.
     *
     * @param newExpenseEntity The new expense entity representing the credit note.
     * @param sizeOfInstallments The number of installments to create.
     * @param paymentDate The initial payment date for the installments.
     * @return A list of ExpenseInstallmentEntity objects.
     */
        private List<ExpenseInstallmentEntity> createInstallments(ExpenseEntity newExpenseEntity, int sizeOfInstallments, LocalDate paymentDate) {
            List<ExpenseInstallmentEntity> expenseInstallmentEntityList = new ArrayList<>();

            for (int i = 0; i < sizeOfInstallments; i++) {
                ExpenseInstallmentEntity installment = new ExpenseInstallmentEntity();
                installment.setExpense(newExpenseEntity);
                installment.setInstallmentNumber(i + 1);
                installment.setCreatedDatetime(LocalDateTime.now());
                installment.setEnabled(Boolean.TRUE);
                installment.setPaymentDate(paymentDate.plusMonths(i));
                installment.setCreatedUser(1);
                installment.setLastUpdatedDatetime(LocalDateTime.now());
                installment.setLastUpdatedUser(1);

                expenseInstallmentEntityList.add(installment);
            }

            return expenseInstallmentEntityList;
        }

    /**
     * Saves a list of ExpenseInstallmentEntity objects and associates them with a new expense entity.
     *
     * @param expenseInstallmentEntityList The list of expense installment entities to save.
     * @param newExpenseEntity The new expense entity to associate with the installments.
     */
        private void saveInstallments(List<ExpenseInstallmentEntity> expenseInstallmentEntityList, ExpenseEntity newExpenseEntity) {
            for (ExpenseInstallmentEntity expenseInstallmentEntity : expenseInstallmentEntityList) {
                expenseInstallmentEntity.setExpense(newExpenseEntity);
                expenseInstallmentRepository.save(expenseInstallmentEntity);
            }
        }

    /**
     * Retrieves a list of owners from the owner service using a REST client.
     * @return List of {@link OwnerDto} with owner details.
     */
    private List<OwnerDto> getOwners() {
        ResponseEntity<OwnerDto[]> response = ownerRestClient.getOwnerPlot();
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new CustomException("Could not retrieve owners", HttpStatus.BAD_REQUEST);
        }
        return Arrays.asList(response.getBody());
    }

    }



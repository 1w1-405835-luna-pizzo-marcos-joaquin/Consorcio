package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.comunication.FileServerRestClient;
import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.*;
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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private BillExpensesInstallmentsRepository billExpensesInstallmentsRepository;

    @Transactional
    public ResponseEntity<DtoResponseExpense> postExpense(DtoRequestExpense request, MultipartFile file) {
        Boolean expenseValid = fetchValidExpenseModel(request, file);
        if (expenseValid) {
            ExpenseModel expenseModel = setDataToExpenseModel(request);
            List<ExpenseInstallmentModel> expenseInstallmentModels = setExpenseInstallmentModels(request, expenseModel);
            List<ExpenseDistributionModel> expenseDistributionModels = setExpenseDistributionModels(request);
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
    public List<ExpenseModel> getExpenseByPaymentDateRange(LocalDate startDate, LocalDate endDate) {
        return List.of();
    }

    private void saveExpenseEntity(ExpenseModel expenseModel, List<ExpenseInstallmentModel> expenseInstallmentModels, List<ExpenseDistributionModel> expenseDistributionModels) {
        ExpenseEntity expenseEntity = modelMapper.map(expenseModel, ExpenseEntity.class);
        expenseEntity.setDistributions(new ArrayList<>());
        expenseEntity.setInstallmentsList(new ArrayList<>());

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

    private DtoResponseExpense setDtoResponseExpense(ExpenseModel expenseModel) {
        DtoResponseExpense dtoResponseExpense = new DtoResponseExpense();
        dtoResponseExpense.setExpenseDate(expenseModel.getExpenseDate());
        dtoResponseExpense.setExpenseType(expenseModel.getExpenseType());
        dtoResponseExpense.setDescription(expenseModel.getDescription());
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

    private List<ExpenseInstallmentModel> setExpenseInstallmentModels(DtoRequestExpense request, ExpenseModel expenseModel) {
        List<ExpenseInstallmentModel> expenseInstallmentModels = new ArrayList<>();
        Integer installments = 1;
        do {
            ExpenseInstallmentModel expenseInstallmentModel = new ExpenseInstallmentModel();
            expenseInstallmentModel.setInstallmentNumber(installments);
            expenseInstallmentModel.setEnabled(Boolean.TRUE);
            expenseInstallmentModel.setCreatedDatetime(LocalDateTime.now());
            expenseInstallmentModel.setCreatedUser(1);
            expenseInstallmentModel.setExpenseModel(expenseModel);
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


    @Override
    public void deteleExpense(Integer id) {
        Optional<ExpenseEntity> expenseEntityOptional = expenseRepository.findById(id);
        if (expenseEntityOptional.isEmpty()) {
            throw new CustomException("The expense does not exist", HttpStatus.BAD_REQUEST);
        }

        ExpenseEntity expenseEntity = expenseEntityOptional.get();
        Optional<List<BillExpenseInstallmentsEntity>> billExpenseInstallmentsEntity = billExpensesInstallmentsRepository.findByExpenseId(id);

        if (billExpenseInstallmentsEntity.map(List::isEmpty).orElse(true)) {
            performLogicalDeletion(expenseEntity);
        } else {
            throw new CustomException("Expense has related bill installments", HttpStatus.CONFLICT);
        }
    }


    /**
     * Método que realiza la baja lógica del gasto si no tiene cobros relacionados.
     */
    private void performLogicalDeletion(ExpenseEntity expenseEntity) {
        expenseEntity.setEnabled(Boolean.FALSE);
        expenseRepository.save(expenseEntity);
    }

    /**
     * Método que genera una nota de crédito en caso de existir cobros relacionados.
     */
    @Transactional
    public void createCreditNoteForExpense(Integer id) {

        Optional<ExpenseEntity> expenseEntityOptional = expenseRepository.findById(id);
        if (expenseEntityOptional.get().getNoteCredit()) {
            throw new CustomException("The expense have a note of credit", HttpStatus.CONFLICT);
        }

        ExpenseEntity expenseEntity = expenseEntityOptional.get();
        Optional<List<BillExpenseInstallmentsEntity>> billExpenseInstallmentsEntity = billExpensesInstallmentsRepository.findByExpenseId(id);

        if (billExpenseInstallmentsEntity.isPresent()) {
            int sizeOfInstallments = billExpenseInstallmentsEntity.get().size();
            BigDecimal amount = expenseEntity.getAmount().negate();
            LocalDate paymentDate = LocalDate.now();

            ExpenseEntity newExpenseEntity = createCreditNoteEntity(expenseEntity);
            expenseRepository.save(newExpenseEntity);

            List<ExpenseInstallmentEntity> expenseInstallmentEntityList = createInstallments(newExpenseEntity, sizeOfInstallments, paymentDate);
            if(!expenseEntity.getDistributions().isEmpty())
            {
                List<ExpenseDistributionEntity> newExpenseDistributionList = new ArrayList<>();

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
            saveInstallments(expenseInstallmentEntityList, newExpenseEntity);
        }
    }

    /**
     * Método que crea una entidad de gasto para la nota de crédito.
     */
    private ExpenseEntity createCreditNoteEntity(ExpenseEntity originalExpenseEntity) {
        ExpenseEntity newExpenseEntity = new ExpenseEntity();
        newExpenseEntity.setExpenseType(ExpenseType.NOTE_OF_CREDIT);
        newExpenseEntity.setEnabled(Boolean.TRUE);
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
     * Método que crea las cuotas para la nueva nota de crédito.
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
     * Método que guarda las cuotas en la base de datos.
     */
    private void saveInstallments(List<ExpenseInstallmentEntity> expenseInstallmentEntityList, ExpenseEntity newExpenseEntity) {
        for (ExpenseInstallmentEntity expenseInstallmentEntity : expenseInstallmentEntityList) {
            expenseInstallmentEntity.setExpense(newExpenseEntity);
            expenseInstallmentRepository.save(expenseInstallmentEntity);
        }
    }


}

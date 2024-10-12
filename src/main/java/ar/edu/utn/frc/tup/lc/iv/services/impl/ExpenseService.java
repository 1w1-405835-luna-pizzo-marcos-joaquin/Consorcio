package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.comunication.FileServerRestClient;
import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.*;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseInstallmentEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseCategoryModel;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseDistributionModel;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseInstallmentModel;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseModel;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseCategoryRepository;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseDistributionRepository;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseInstallmentRepository;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseRepository;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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

    @Override
    public ResponseEntity<DtoResponseExpense> postExpense(DtoRequestExpense request, MultipartFile file) {
        Boolean expenseValid = fetchValidExpenseModel(request, file);
        if (expenseValid) {
            ExpenseModel expenseModel = setDataToExpenseModel(request);
            List<ExpenseInstallmentModel> expenseInstallmentModels = setExpenseInstallmentModels(request, expenseModel);
            List<ExpenseDistributionModel> expenseDistributionModels = new ArrayList<>();
            if(ExpenseType.valueOf(request.getTypeExpense()).equals(ExpenseType.INDIVIDUAL)) {
                expenseInstallmentModels = setExpenseInstallmentModels(request, expenseModel);
            }
            expenseModel.setInstallmentsList(expenseInstallmentModels);
            expenseModel.setDistributions(expenseDistributionModels);
            UUID fileId = UUID.randomUUID();
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

    private List<ExpenseInstallmentModel> setExpenseInstallmentModels(DtoRequestExpense request, ExpenseModel expenseModel) {
        List<ExpenseInstallmentModel> expenseInstallmentModels = new ArrayList<>();
        Integer installments = 1;
        do {
            ExpenseInstallmentModel expenseInstallmentModel = new ExpenseInstallmentModel();
            expenseInstallmentModel.setInstallmentNumber(installments);
            expenseInstallmentModel.setEnabled(Boolean.TRUE);
            expenseInstallmentModel.setCreatedDatetime(LocalDateTime.now());
            expenseInstallmentModel.setCreatedUser(1);
            //TODO @Santi esto necesito que se pruebe asi, con esto comentado
            //expenseInstallmentModel.setExpenseModel(expenseModel);
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

        if (request.getDistributions() == null || request.getDistributions().isEmpty()) {
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

}

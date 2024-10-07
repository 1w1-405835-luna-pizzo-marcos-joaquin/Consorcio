package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DistributionDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoRequestExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseExpense;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseInstallmentEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseDistributionModel;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseInstallmentModel;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseModel;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseRepository;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService implements IExpenseService {

    @Autowired
    private ExpenseCategoryService expenseCategoryService;
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public DtoResponseExpense postExpense(DtoRequestExpense request) {

        Boolean validateRequestData = validateRequestData(request);
        if(validateRequestData)
        {
            //REFACTORIZAR
            ExpenseModel expenseModel = mapRequestToModel(request);
            List<ExpenseDistributionModel> distributionModelList = new ArrayList<>();
            DtoResponseExpense dtoResponseExpense = new DtoResponseExpense();
            dtoResponseExpense.setExpenseModel(expenseModel);
            return dtoResponseExpense;
        }
        return null;
    }

    private ExpenseModel mapRequestToModel(DtoRequestExpense request) {
        ExpenseModel expenseModel = new ExpenseModel();
        List<ExpenseInstallmentModel> installmentModelList = new ArrayList<>();
        expenseModel.setDescription(request.getDescription());
        expenseModel.setProviderId(request.getProviderId());
        expenseModel.setExpenseDate(request.getExpenseDate());
        expenseModel.setInvoiceNumber(request.getInvoiceNumber());
        expenseModel.setExpenseType(ExpenseType.valueOf(request.getTypeExpense()));
        expenseModel.setCategoryId(request.getCategoryId());
        expenseModel.setAmount(request.getAmount());
        expenseModel.setInstallments(request.getInstallments());
        expenseModel.setCreatedDatetime(LocalDateTime.now());
        expenseModel.setLastUpdatedDatetime(LocalDateTime.now());
        expenseModel.setCreatedUser(1);
        expenseModel.setLastUpdatedUser(1);
        expenseModel.setEnabled(Boolean.TRUE);
        installmentModelList =  getInstallmentModel(request,installmentModelList);
        // falta refactorizar
        // falta agregar metodo de distribution

        //falta agregar metodo de save
     /*
        ExpenseEntity expenseEntity = modelMapper.map(expenseModel, ExpenseEntity.class);
        expenseEntity.setDistributions(new ArrayList<>());
        expenseEntity.setInstallmentsList(new ArrayList<>());
        for(ExpenseDistributionModel distributionModel : distributionModelList){
            expenseEntity.getDistributions().add(modelMapper.map(distributionModel, ExpenseDistributionEntity.class));
        }
        for(ExpenseInstallmentModel installmentModel : installmentModelList){
            expenseEntity.getInstallmentsList().add(modelMapper.map(installmentModel, ExpenseInstallmentEntity.class));
        }
        expenseRepository.save(expenseEntity);
        */
        return expenseModel;
    }

    private List<ExpenseInstallmentModel> getInstallmentModel(DtoRequestExpense request, List<ExpenseInstallmentModel> expenseInstallmentModels) {
        Integer numberOfInstallments = 1;
        do {
        ExpenseInstallmentModel installmentModel = new ExpenseInstallmentModel();
        installmentModel.setInstallmentNumber(numberOfInstallments);
        installmentModel.setEnabled(Boolean.TRUE);
        installmentModel.setCreatedDatetime(LocalDateTime.now());
        installmentModel.setCreatedUser(1);
        if(numberOfInstallments.equals(1)){
            installmentModel.setPaymentDate(LocalDate.now());
        }else{
            installmentModel.setPaymentDate(LocalDate.now().plusMonths(numberOfInstallments));
        }
        installmentModel.setLastUpdatedDatetime(LocalDateTime.now());
        installmentModel.setLastUpdatedUser(1);
        expenseInstallmentModels.add(installmentModel);
        numberOfInstallments++;
    }while (numberOfInstallments <= request.getInstallments());
        return expenseInstallmentModels;
    }


    private Boolean validateRequestData(DtoRequestExpense request) {

        Optional<ExpenseEntity> expenseEntityExist = expenseRepository.findExpenseEntitiesByInvoiceNumberAndProviderId(request.getInvoiceNumber(),request.getProviderId());

        if(expenseEntityExist.isPresent()){
            throw new CustomException("The expense have already exist", HttpStatus.BAD_REQUEST);
        }
        //validar category
        return true;
    }
}

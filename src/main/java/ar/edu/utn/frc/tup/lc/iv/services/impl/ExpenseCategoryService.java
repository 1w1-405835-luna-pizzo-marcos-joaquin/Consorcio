package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseDeleteExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseCategoryDTO;
import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseInstallmentsEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseCategoryEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseEntity;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseCategoryModel;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseCategoryRepository;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseCategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseCategoryService implements IExpenseCategoryService {
    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Override
    public ExpenseCategoryModel getCategoryModel(Integer id) {
        Optional<ExpenseCategoryEntity> expenseCategoryEntity = expenseCategoryRepository.findById(id);
        return expenseCategoryEntity.map(categoryEntity -> modelMapper.map(categoryEntity, ExpenseCategoryModel.class)).orElse(null);
    }


    private void performLogicalDeletion(ExpenseCategoryEntity expenseCategoryEntity) {
        expenseCategoryEntity.setEnabled(Boolean.FALSE);
        expenseCategoryRepository.save(expenseCategoryEntity);
    }
    @Override
    public DtoResponseDeleteExpense deteleCategory(Integer id) {
        Optional<ExpenseCategoryEntity> expenseCategoryEntityOptional = expenseCategoryRepository.findById(id);
        if (expenseCategoryEntityOptional.isEmpty()) {
            throw new CustomException("The category does not exist", HttpStatus.BAD_REQUEST);
        }

        ExpenseCategoryEntity expenseCategoryEntity = expenseCategoryEntityOptional.get();

        performLogicalDeletion(expenseCategoryEntity);
        DtoResponseDeleteExpense dtoResponseDeleteExpense = new DtoResponseDeleteExpense();
        dtoResponseDeleteExpense.setExpense(expenseCategoryEntity.getDescription());
        dtoResponseDeleteExpense.setHttpStatus(HttpStatus.OK);
        dtoResponseDeleteExpense.setDescriptionResponse("Category delete logic successfully");
        return dtoResponseDeleteExpense;

    }
    @Override
    public ExpenseCategoryDTO putCategory(Integer id, String description) {

        Optional<ExpenseCategoryEntity> existingCategory = expenseCategoryRepository.findByDescription(description);

        Optional<ExpenseCategoryEntity> expenseCategoryEntityOptional = expenseCategoryRepository.findById(id);


        if (expenseCategoryEntityOptional.isEmpty()) {
            throw new CustomException("The category does not exist", HttpStatus.BAD_REQUEST);
        }

        ExpenseCategoryEntity expenseCategoryEntity = expenseCategoryEntityOptional.get();

        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {

            if (existingCategory.get().getEnabled()) {
                throw new CustomException("A category with this description already exists", HttpStatus.BAD_REQUEST);
            }
        }

        expenseCategoryEntity.setDescription(description);
        expenseCategoryEntity.setLastUpdatedDatetime(LocalDateTime.now());

        expenseCategoryRepository.save(expenseCategoryEntity);

        ExpenseCategoryDTO expenseCategoryDTO = new ExpenseCategoryDTO();
        expenseCategoryDTO.setDescription(description);
        expenseCategoryDTO.setId(expenseCategoryEntity.getId());

        return expenseCategoryDTO;
    }

}

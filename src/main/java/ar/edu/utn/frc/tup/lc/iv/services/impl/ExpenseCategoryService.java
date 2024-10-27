package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoCategory;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoExpenseQuery;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseDeleteExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseCategoryDTO;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseCategoryEntity;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseCategoryModel;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseCategoryRepository;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseCategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseCategoryService implements IExpenseCategoryService {
    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;
    @Autowired
    private ModelMapper modelMapper;
    private final Integer CREATE_USER =1;

    @Override
    public ExpenseCategoryDTO postCategory(String description) {
        Optional<ExpenseCategoryEntity> existingCategory = expenseCategoryRepository.findByDescription(description);

        if (existingCategory.isPresent()) {
            if (existingCategory.get().getEnabled()) {
                throw new CustomException("A category with this description already exists", HttpStatus.BAD_REQUEST);
            }
        }

        ExpenseCategoryEntity newCategory = new ExpenseCategoryEntity();
        newCategory.setDescription(description);
        newCategory.setEnabled(true);
        newCategory.setCreatedUser(CREATE_USER);
        newCategory.setLastUpdatedUser(CREATE_USER);
        newCategory.setLastUpdatedDatetime(LocalDateTime.now());

        expenseCategoryRepository.save(newCategory);

        return ExpenseCategoryDTO.builder()
                .id(newCategory.getId())
                .description(description)
                .build();
    }


    /**
     * Retrieves an {@link ExpenseCategoryModel} based on the category ID.
     *
     * @param id the ID of the expense category to retrieve.
     * @return the corresponding {@link ExpenseCategoryModel}, or {@code null} if not found.
     */
    @Override
    public ExpenseCategoryModel getCategoryModel(Integer id) {
        Optional<ExpenseCategoryEntity> expenseCategoryEntity = expenseCategoryRepository.findById(id);
        return expenseCategoryEntity.map(categoryEntity -> modelMapper.map(categoryEntity, ExpenseCategoryModel.class)).orElse(null);
    }

    /**
     * Retrieves a list of all enabled categories as DtoCategory objects.
     *
     * @return a list of enabled categories, mapped to DtoCategory DTOs.
     * @throws CustomException if no categories are found.
     */
    public List<DtoCategory>  getAllCategories() {
        List<ExpenseCategoryEntity> expenseCategoryEntities = expenseCategoryRepository.findAll();
        if ( Collections.emptyList().equals(expenseCategoryEntities) ) {
            throw new CustomException("No categories found", HttpStatus.NOT_FOUND);
        }

        List<DtoCategory> dtoCategories = new ArrayList<>();
        for (ExpenseCategoryEntity categoryEntity : expenseCategoryEntities) {
            DtoCategory dtoCategory = new DtoCategory();
            dtoCategory.setId(categoryEntity.getId());
            dtoCategory.setDescription(categoryEntity.getDescription());
            dtoCategory.setLastUpdatedDatetime(categoryEntity.getLastUpdatedDatetime());
            if (categoryEntity.getEnabled()){
                dtoCategory.setState("Activo");
            } else {
                dtoCategory.setState("Inactivo");
            }
            dtoCategories.add(dtoCategory);
        }

        return dtoCategories;
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

package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoCategory;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseCategoryEntity;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseCategoryModel;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseCategoryRepository;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseCategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
    @Override
    public ExpenseCategoryModel getCategoryModel(Integer id) {
        Optional<ExpenseCategoryEntity> expenseCategoryEntity = expenseCategoryRepository.findById(id);
        return expenseCategoryEntity.map(categoryEntity -> modelMapper.map(categoryEntity, ExpenseCategoryModel.class)).orElse(null);
    }

    public List<DtoCategory>  getAllCategories() {
        List<ExpenseCategoryEntity> expenseCategoryEntities = expenseCategoryRepository.findAllEnabled();
        if ( Collections.emptyList().equals(expenseCategoryEntities) ) {
            throw new CustomException("No categories found", HttpStatus.NOT_FOUND);
        }
        return expenseCategoryEntities.stream().map(categoryEntity -> modelMapper.map(categoryEntity, DtoCategory.class)).toList();
    }

}

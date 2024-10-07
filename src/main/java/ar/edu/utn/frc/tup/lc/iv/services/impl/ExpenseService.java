package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseQueryDto;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseEntity;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseCategoryRepository;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseDistributionRepository;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseInstallmentRepository;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseRepository;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService implements IExpenseService {

    @Autowired
    private  ExpenseRepository expenseRepository;
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
    private ObjectMapper objectMapper;


}

package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseDistributionRepository;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseDistributionService implements IExpenseDistributionService {
    @Autowired
    private ExpenseDistributionRepository repository;



    public List<ExpenseDistributionEntity> findByOwnerAndExpenseDateRangeAndTypeAndCategory(
            Integer ownerId, LocalDate startDate, LocalDate endDate, ExpenseType expenseType, Integer categoryId) {
        return repository.findByOwnerAndExpenseDateRangeAndTypeAndCategory(ownerId, startDate, endDate, expenseType, categoryId);
    }

}

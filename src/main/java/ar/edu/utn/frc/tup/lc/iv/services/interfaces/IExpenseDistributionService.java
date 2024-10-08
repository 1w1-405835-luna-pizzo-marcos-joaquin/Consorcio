package ar.edu.utn.frc.tup.lc.iv.services.interfaces;

import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


public interface IExpenseDistributionService {
    List<ExpenseDistributionEntity> findByOwnerAndExpenseDateRangeAndTypeAndCategory(
            Integer ownerId, LocalDate startDate, LocalDate endDate, ExpenseType expenseType, Integer categoryId);
}

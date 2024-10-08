package ar.edu.utn.frc.tup.lc.iv.services.interfaces;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseCategoryDTO;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseDTO;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseCategoryEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

public interface IExpenseService {
        List<ExpenseDTO> searchExpenses(LocalDate startDate, LocalDate endDate, ExpenseCategoryDTO category, String providerName);
}


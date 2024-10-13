package ar.edu.utn.frc.tup.lc.iv.services.interfaces;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseOwnerVisualizerDTO;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public interface IExpenseDistributionService {
    List<ExpenseOwnerVisualizerDTO> findAll();
    List<ExpenseOwnerVisualizerDTO> filterExpenses(Integer ownerId, LocalDate startDate, LocalDate endDate, ExpenseType expenseType,
                                                   Integer categoryId, String description, BigDecimal amountFrom, BigDecimal amountTo);
    List<ExpenseOwnerVisualizerDTO> findByOwnerId(Integer ownerId, LocalDate startDate, LocalDate endDate);
}

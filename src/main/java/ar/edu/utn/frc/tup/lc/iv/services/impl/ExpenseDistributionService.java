package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseCategoryDTO;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseOwnerVisualizerDTO;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseCategoryEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseDistributionRepository;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseDistributionService implements IExpenseDistributionService {
    @Autowired
    private ExpenseDistributionRepository repository;
    
    public List<ExpenseOwnerVisualizerDTO> findVisualizersByOwnerAndFilters(
            Integer ownerId, LocalDate startDate, LocalDate endDate, ExpenseType expenseType,
            Integer categoryId, String description, BigDecimal amountFrom, BigDecimal amountTo) {


        List<ExpenseDistributionEntity> entities = repository.findByOwnerAndFilters(
                ownerId, startDate, endDate, expenseType, categoryId, description, amountFrom, amountTo);

        return entities.stream()
                .map(ExpenseDistributionService::toDto)
                .toList();
    }

    //Mapper  expenseCategory TO expenseCategoryDTO

    public static ExpenseCategoryDTO toDto (ExpenseCategoryEntity entity){
        if (entity == null){
            return null;
        }
        ExpenseCategoryDTO dto = new ExpenseCategoryDTO();
        dto.setId(entity.getId());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    //MAPPER ENTITY TO EXPENSEOWNERVISUALIZERDTO

    public static ExpenseOwnerVisualizerDTO toDto(ExpenseDistributionEntity entity) {
        if (entity == null || entity.getExpense() == null) {
            return null;
        }

        ExpenseOwnerVisualizerDTO dto = new ExpenseOwnerVisualizerDTO();
        dto.setId(entity.getId());
        dto.setDescription(entity.getExpense().getDescription());
        dto.setProviderId(entity.getExpense().getProviderId());
        dto.setExpenseDate(entity.getExpense().getExpenseDate());
        dto.setFileId(entity.getExpense().getFileId());
        dto.setInvoiceNumber(entity.getExpense().getInvoiceNumber());
        dto.setExpenseType(entity.getExpense().getExpenseType());
        dto.setCategory(toDto(entity.getExpense().getCategory()));
        dto.setAmount(entity.getExpense().getAmount());
        dto.setProportion(entity.getProportion());
        dto.setInstallments(entity.getExpense().getInstallments());

        return dto;
    }

}

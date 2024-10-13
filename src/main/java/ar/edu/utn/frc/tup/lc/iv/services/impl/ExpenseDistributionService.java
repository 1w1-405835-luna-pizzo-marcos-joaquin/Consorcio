package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseCategoryDTO;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseOwnerVisualizerDTO;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseCategoryEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseDistributionRepository;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseRepository;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseDistributionService implements IExpenseDistributionService {
    @Autowired
    private ExpenseDistributionRepository repository;



    public List<ExpenseOwnerVisualizerDTO> findAll(){
        List<ExpenseDistributionEntity> getAllDistribution = repository.findAllDistinct();
        List<ExpenseOwnerVisualizerDTO> expenseOwnerVisualizerDTOList = new ArrayList<>();
        for (ExpenseDistributionEntity entity: getAllDistribution){
            ExpenseOwnerVisualizerDTO dto = entityDistributiontoDto(entity);
            expenseOwnerVisualizerDTOList.add(dto);
        }
        return expenseOwnerVisualizerDTOList;
    }

    public List<ExpenseOwnerVisualizerDTO> findByOwnerId(Integer ownerId) {
        List<ExpenseDistributionEntity> entities = repository.findAllByOwnerId(ownerId);
        List<ExpenseOwnerVisualizerDTO> expenseOwnerVisualizerDTOList=   entities.stream()
                .map(this::entityDistributiontoDto)
                .collect(Collectors.toList());
        for(ExpenseOwnerVisualizerDTO expenseOwner : expenseOwnerVisualizerDTOList){
            BigDecimal amount = expenseOwner.getAmount();
            BigDecimal proportion = expenseOwner.getProportion();
            BigDecimal updatedAmount = amount.multiply(proportion);
            expenseOwner.setAmount(updatedAmount);
        }
        return  expenseOwnerVisualizerDTOList;
    }


    @Override
    public List<ExpenseOwnerVisualizerDTO> filterExpenses(Integer ownerId, LocalDate startDate, LocalDate endDate, ExpenseType expenseType,
          Integer categoryId, String description, BigDecimal amountFrom, BigDecimal amountTo){


        if (ownerId <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero.");
        }


        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        if (amountFrom != null && amountFrom.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto 'Desde' no puede ser negativo.");
        }

        if (amountTo != null && amountTo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto 'Hasta' no puede ser negativo.");
        }

        if (amountFrom != null && amountTo != null && amountFrom.compareTo(amountTo) > 0) {
            throw new IllegalArgumentException("El monto 'Desde' no puede ser mayor que el monto 'Hasta'.");
        }
        List<ExpenseDistributionEntity> getAllDistribution = repository.findAllDistinct();


        return getAllDistribution.stream()
                // Filtrer for ownerId
                .filter(entity -> ownerId == null || ownerId.equals(entity.getOwnerId()))
                // Filtrer for startDate
                .filter(entity -> startDate == null || !entity.getExpense().getExpenseDate().isBefore(startDate))
                // Filtrer for endDate
                .filter(entity -> endDate == null || !entity.getExpense().getExpenseDate().isAfter(endDate))
                // Filtrer for expenseType
                .filter(entity -> expenseType == null || expenseType.equals(entity.getExpense().getExpenseType()))
                // Filtrer for categoryId
                .filter(entity -> categoryId == null || (entity.getExpense().getCategory() != null && categoryId.equals(entity.getExpense().getCategory().getId())))
                // Filtrer for description
                .filter(entity -> description == null || entity.getExpense().getDescription().toLowerCase().contains(description.toLowerCase()))
                // Filtrer for amountFrom
                .filter(entity -> amountFrom == null || entity.getExpense().getAmount().compareTo(amountFrom) >= 0)
                // Filtrer for amountTo
                .filter(entity -> amountTo == null || entity.getExpense().getAmount().compareTo(amountTo) <= 0)
                // Convert entity in DTO
                .map(this::entityDistributiontoDto)
                // ollect the results in a list
                .collect(Collectors.toList());


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

    public ExpenseOwnerVisualizerDTO entityDistributiontoDto(ExpenseDistributionEntity entity) {
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
        dto.setEnabled(entity.getEnabled());

        return dto;
    }


}

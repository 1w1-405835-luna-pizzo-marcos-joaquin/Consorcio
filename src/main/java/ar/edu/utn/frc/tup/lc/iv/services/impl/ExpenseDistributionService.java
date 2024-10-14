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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for managing expense distributions.
 * This class provides methods to retrieve and filter expense distribution data
 * as well as to map entities to DTOs for easier visualization.
 */
@Service
public class ExpenseDistributionService implements IExpenseDistributionService {

    @Autowired
    private ExpenseDistributionRepository repository;
    private final ExpenseRepository expenseRepository;

    @Autowired
    public ExpenseDistributionService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    /**
     * Retrieves a list of all distinct expense distributions and maps them to
     * ExpenseOwnerVisualizerDTO objects.
     *
     * @return a list of ExpenseOwnerVisualizerDTO representing all distinct expense distributions.
     */
    public List<ExpenseOwnerVisualizerDTO> findAll() {
        List<ExpenseDistributionEntity> getAllDistribution = repository.findAllDistinct();
        List<ExpenseOwnerVisualizerDTO> expenseOwnerVisualizerDTOList = new ArrayList<>();
        for (ExpenseDistributionEntity entity : getAllDistribution) {
            ExpenseOwnerVisualizerDTO dto = entityDistributiontoDto(entity);
            expenseOwnerVisualizerDTOList.add(dto);
        }
        return expenseOwnerVisualizerDTOList;
    }

    /**
     * Finds all expense distributions for a specific owner by their owner ID.
     * It also updates the amount by applying the proportion.
     *
     * @param ownerId the ID of the owner.
     * @return a list of ExpenseOwnerVisualizerDTO for the specified owner.
     */
    public List<ExpenseOwnerVisualizerDTO> findByOwnerId(Integer ownerId,String startDateString, String endDateString) {


        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("El ID del propietario no puede ser nulo o negativo.");
        }

        // Formateador de fechas en formato yyyy-MM-dd
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startDate;
        LocalDate endDate;

        // Validar y convertir la fecha de inicio
        try {
            startDate = LocalDate.parse(startDateString, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha de inicio debe estar en el formato yyyy-MM-dd.");
        }

        // Validar y convertir la fecha de fin
        try {
            endDate = LocalDate.parse(endDateString, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha de fin debe estar en el formato yyyy-MM-dd.");
        }
        List<ExpenseEntity> expenses = expenseRepository.findAllByDate(startDate, endDate);
        if (expenses == null || expenses.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron gastos en el rango de fechas proporcionado.");
        }
        List<ExpenseOwnerVisualizerDTO> expenseOwnerVisualizerDTOList = new ArrayList<>();
        for (ExpenseEntity expense : expenses) {
            if(expense.getDistributions().isEmpty())
                expenseOwnerVisualizerDTOList.add(expenseOwnerVisualizerDTOBuilder(expense,ownerId,BigDecimal.valueOf(1)));
            else {
                Optional<ExpenseDistributionEntity> expenseOwner =  expense.getDistributions().stream().filter(m->m.getOwnerId().equals(ownerId) && m.getEnabled()).findFirst();
                if(expenseOwner.isPresent())
                    expenseOwnerVisualizerDTOList.add(expenseOwnerVisualizerDTOBuilder(expense,ownerId,expenseOwner.get().getProportion()));
            }
        }
        //List<ExpenseDistributionEntity> entities = repository.findAllByOwnerId(ownerId);
//        List<ExpenseOwnerVisualizerDTO> expenseOwnerVisualizerDTOList = entities.stream()
//                .map(this::entityDistributiontoDto)
//                .collect(Collectors.toList());
//        for (ExpenseOwnerVisualizerDTO expenseOwner : expenseOwnerVisualizerDTOList) {
//            BigDecimal amount = expenseOwner.getAmount();
//            BigDecimal proportion = expenseOwner.getProportion();
//            BigDecimal updatedAmount = amount.multiply(proportion);
//            expenseOwner.setAmount(updatedAmount);
//        }
        return expenseOwnerVisualizerDTOList;
    }

    private ExpenseOwnerVisualizerDTO expenseOwnerVisualizerDTOBuilder(ExpenseEntity expense,Integer ownerId,BigDecimal proportion) {
        if (expense == null) {
            throw new IllegalArgumentException("El gasto no puede ser nulo.");
        }
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("El ID del propietario no puede ser nulo o negativo.");
        }
        if (proportion == null || proportion.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La proporción no puede ser nula o negativa.");
        }

        return ExpenseOwnerVisualizerDTO.builder()
                .id(ownerId)
                .expenseId(expense.getId())
                .description(expense.getDescription())
                .providerId(expense.getProviderId())
                .expenseDate(expense.getExpenseDate())
                .fileId(expense.getFileId())
                .invoiceNumber(expense.getInvoiceNumber())
                .expenseType(expense.getExpenseType())
                .category(ExpenseCategoryDTO.builder()
                        .id(expense.getCategory().getId())
                        .description(expense.getCategory().getDescription())
                        .build())
                .proportion(proportion)
                .amount(expense.getAmount().multiply(proportion).setScale(2, RoundingMode.HALF_UP))
                .installments(expense.getInstallments())
                .enabled(expense.getEnabled())
                .build();
    }

//    /**
//     * Finds all expense distributions for a specific owner and filters by date range.
//     *
//     * @param ownerId the ID of the owner.
//     * @param startDate the start date for the expense filter.
//     * @param endDate the end date for the expense filter.
//     * @return a list of ExpenseOwnerVisualizerDTO for the specified owner and date range.
//     */
//    public List<ExpenseOwnerVisualizerDTO> findByOwnerIdAndDateRange(Integer ownerId, LocalDate startDate, LocalDate endDate) {
//
//        if (ownerId == null || ownerId <= 0) {
//            throw new IllegalArgumentException("El ID del propietario debe ser mayor que cero.");
//        }
//
//
//        List<ExpenseDistributionEntity> allEntities = repository.findAllByOwnerId(ownerId);
//
//
//        List<ExpenseDistributionEntity> filteredEntities = allEntities.stream()
//                .filter(entity -> {
//                    LocalDate expenseDate = entity.getExpense().getExpenseDate();
//                    return (startDate == null || !expenseDate.isBefore(startDate)) &&
//                            (endDate == null || !expenseDate.isAfter(endDate));
//                })
//                .collect(Collectors.toList());
//
//
//        return filteredEntities.stream()
//                .map(this::entityDistributiontoDto)
//                .collect(Collectors.toList());
//    }

    /**
     * Filters expense distributions based on various criteria such as owner ID, dates,
     * expense type, category, description, and amount range.
     *
     * @param ownerId the owner's ID.
     * @param startDate the start date for the expense filter.
     * @param endDate the end date for the expense filter.
     * @param expenseType the type of expense.
     * @param categoryId the ID of the expense category.
     * @param description the description of the expense.
     * @param amountFrom the minimum amount to filter.
     * @param amountTo the maximum amount to filter.
     * @return a list of ExpenseOwnerVisualizerDTO matching the filter criteria.
     */
    @Override
    public List<ExpenseOwnerVisualizerDTO> filterExpenses(Integer ownerId, LocalDate startDate, LocalDate endDate, ExpenseType expenseType,
                                                          Integer categoryId, String description, BigDecimal amountFrom, BigDecimal amountTo) {
        if (ownerId <= 0) {
            throw new IllegalArgumentException("El ID del propietario debe ser mayor que cero.");
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de finalización.");
        }

        if (amountFrom != null && amountFrom.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto 'Desde' no puede ser negativo.");
        }

        if (amountTo != null && amountTo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto 'Hasta' no puede ser negativo.");
        }

        if (amountFrom != null && amountFrom.compareTo(amountTo) > 0) {
            throw new IllegalArgumentException("El monto 'Desde' no puede ser mayor que el monto 'Hasta'.");
        }

        List<ExpenseDistributionEntity> getAllDistribution = repository.findAllDistinct();

        return getAllDistribution.stream()
                .filter(entity -> ownerId == null || ownerId.equals(entity.getOwnerId()))
                .filter(entity -> startDate == null || !entity.getExpense().getExpenseDate().isBefore(startDate))
                .filter(entity -> endDate == null || !entity.getExpense().getExpenseDate().isAfter(endDate))
                .filter(entity -> expenseType == null || expenseType.equals(entity.getExpense().getExpenseType()))
                .filter(entity -> categoryId == null || (entity.getExpense().getCategory() != null && categoryId.equals(entity.getExpense().getCategory().getId())))
                .filter(entity -> description == null || entity.getExpense().getDescription().toLowerCase().contains(description.toLowerCase()))
                .filter(entity -> amountFrom == null || entity.getExpense().getAmount().compareTo(amountFrom) >= 0)
                .filter(entity -> amountTo == null || entity.getExpense().getAmount().compareTo(amountTo) <= 0)
                .map(this::entityDistributiontoDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts an ExpenseCategoryEntity into an ExpenseCategoryDTO.
     *
     * @param entity the ExpenseCategoryEntity to convert.
     * @return the corresponding ExpenseCategoryDTO.
     */
    public static ExpenseCategoryDTO toDto(ExpenseCategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        ExpenseCategoryDTO dto = new ExpenseCategoryDTO();
        dto.setId(entity.getId());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    /**
     * Converts an ExpenseDistributionEntity into an ExpenseOwnerVisualizerDTO.
     *
     * @param entity the ExpenseDistributionEntity to convert.
     * @return the corresponding ExpenseOwnerVisualizerDTO.
     */
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

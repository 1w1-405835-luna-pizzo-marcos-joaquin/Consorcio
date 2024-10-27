package ar.edu.utn.frc.tup.lc.iv.models;

import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseModel {
    private Integer id;
    private String description;
    private Integer providerId;
    private LocalDate expenseDate;
    private UUID fileId;
    private String invoiceNumber;
    private ExpenseType expenseType;
    private ExpenseCategoryModel category;
    private BigDecimal amount;
    private Integer installments;
    private LocalDateTime createdDatetime;
    private Integer createdUser;
    private LocalDateTime lastUpdatedDatetime;
    private Integer lastUpdatedUser;
    private Boolean noteCredit;
    private Boolean enabled;
    private List<ExpenseDistributionModel> distributions;
    private List<ExpenseInstallmentModel> installmentsList;
}

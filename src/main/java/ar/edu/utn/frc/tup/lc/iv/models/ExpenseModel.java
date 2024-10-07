package ar.edu.utn.frc.tup.lc.iv.models;

import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private Integer invoiceNumber;
    private ExpenseType expenseType;
    private Integer categoryId;
    private BigDecimal amount;
    private Integer installments;
    private LocalDateTime createdDatetime;
    private Integer createdUser;
    private LocalDateTime lastUpdatedDatetime;
    private Integer lastUpdatedUser;
    private Boolean enabled;
}

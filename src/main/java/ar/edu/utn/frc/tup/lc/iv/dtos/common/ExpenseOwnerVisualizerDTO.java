package ar.edu.utn.frc.tup.lc.iv.dtos.common;

import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseOwnerVisualizerDTO {
    private Integer id;
    private String description;
    private Integer providerId;
    private LocalDate expenseDate;
    private UUID fileId;
    private Integer invoiceNumber;
    private ExpenseType expenseType;
    private ExpenseCategoryDTO category;
    private BigDecimal amount;
    private BigDecimal proportion;
    private Integer installments;
    private Boolean enabled;
}

package ar.edu.utn.frc.tup.lc.iv.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {
    private Integer id;
    private String description;
    private ProviderDTO provider;
    private LocalDate expenseDate;
    private String fileId;
    private Integer invoiceNumber;
    private String expenseType;
    private ExpenseCategoryDTO category;
    private BigDecimal amount;
    private Integer installments;
    private OwnerDTO owner;
}

package ar.edu.utn.frc.tup.lc.iv.models;

import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseOwnerEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import lombok.*;

import java.math.BigDecimal;
@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillExpenseInstallmentModel extends AuditModel{
    private ExpenseType expenseType;
    private ExpenseInstallmentModel expenseInstallment;
    private String description;
    private BigDecimal amount;
}

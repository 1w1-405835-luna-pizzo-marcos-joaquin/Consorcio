package ar.edu.utn.frc.tup.lc.iv.models;

import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseOwnerEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class BillExpenseInstallmentModel extends AuditModel{
    private String description;
    private BigDecimal amount;
}

package ar.edu.utn.frc.tup.lc.iv.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class BillExpenseOwnerModel extends AuditModel {
    private Integer ownerId;
    private Integer fieldSize;
    private List<BillExpenseFineModel> billExpenseFines;
    private List<BillExpenseInstallmentModel>billExpenseInstallments;
}

package ar.edu.utn.frc.tup.lc.iv.models;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillRecordModel extends AuditModel {
    private LocalDate start;
    private LocalDate end;
    private List<BillExpenseOwnerModel> billExpenseOwner;
}

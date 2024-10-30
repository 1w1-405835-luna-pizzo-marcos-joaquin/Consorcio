package ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.response;


import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.PeriodDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class BillExpenseDto extends PeriodDto {
    @JsonProperty("bill_expense_id")
    private Integer id;
    @JsonProperty("owners")
    private List<BillOwnerDto> owners;
}

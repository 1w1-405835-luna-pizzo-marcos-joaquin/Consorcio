package ar.edu.utn.frc.tup.lc.iv.dtos.billExpense;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillExpenseDto extends PeriodDto {
    @JsonProperty("bill_expense_id")
    private Integer id;
    @JsonProperty("owners")
    private List<OwnerPlotDto> owners;
}

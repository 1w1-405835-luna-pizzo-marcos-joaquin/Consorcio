package ar.edu.utn.frc.tup.lc.iv.dtos.ReportChart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseCategoryPeriodDto {
    @JsonProperty("category")
    private String category;
    @JsonProperty("amount")
    private BigDecimal amount;
}

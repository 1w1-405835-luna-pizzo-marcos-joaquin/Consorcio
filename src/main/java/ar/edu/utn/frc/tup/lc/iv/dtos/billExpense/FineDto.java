package ar.edu.utn.frc.tup.lc.iv.dtos.billExpense;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FineDto {
    @JsonProperty("fine_id")
    private Integer id;
    @JsonProperty("owner_id")
    private Integer ownerId;
    @JsonProperty("description")
    private String description;
    @JsonProperty("amount")
    private BigDecimal amount;

}

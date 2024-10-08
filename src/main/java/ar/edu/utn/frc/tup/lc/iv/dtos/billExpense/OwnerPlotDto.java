package ar.edu.utn.frc.tup.lc.iv.dtos.billExpense;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerPlotDto {
    @JsonProperty("owner_id")
    private Integer id;
    @JsonProperty("field_size")
    private Integer fieldSize;
    @JsonProperty("fines")
    private List<ItemDto> fines;
    private List<ItemDto> expenses_common;
    private List<ItemDto> expenses_extraordinary;
    private List<ItemDto> expenses_individual;
}

package ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.response;

import ar.edu.utn.frc.tup.lc.iv.dtos.sanction.FineDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillOwnerDto {
    @JsonProperty("owner_id")
    private Integer id;
    @JsonProperty("field_size")
    private Integer fieldSize;
    @JsonProperty("fines")
    private List<FineDto> fines;
    @JsonProperty("expenses_common")
    private List<ItemDto> expensesCommon;
    @JsonProperty("expenses_extraordinary")
    private List<ItemDto> expensesExtraordinary;
    @JsonProperty("expenses_individual")
    private List<ItemDto> expensesIndividual;
    @JsonProperty("notes_credit")
    private List<ItemDto> notesOfCredit;
}

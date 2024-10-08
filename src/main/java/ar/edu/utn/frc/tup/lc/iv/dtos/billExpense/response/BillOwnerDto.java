package ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BillOwnerDto {
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

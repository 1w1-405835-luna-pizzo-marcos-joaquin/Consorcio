package ar.edu.utn.frc.tup.lc.iv.dtos.owner;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//NO TOCAR, YA DEFINIDO CONTRATO CON OWNERAPI
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlotDto {
    @JsonProperty("plot_id")
    private Integer id;
    @JsonProperty("field_size")
    private Integer fieldSize;
}

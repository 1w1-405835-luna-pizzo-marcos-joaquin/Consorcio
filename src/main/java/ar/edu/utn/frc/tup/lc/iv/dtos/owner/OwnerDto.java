package ar.edu.utn.frc.tup.lc.iv.dtos.owner;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

//NO TOCAR, YA DEFINIDO CONTRATO CON OWNERAPI

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDto {
    @JsonProperty("owner_id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("dni")
    private String dni;
    @JsonProperty("plots")
    private List<PlotDto> plots;
}

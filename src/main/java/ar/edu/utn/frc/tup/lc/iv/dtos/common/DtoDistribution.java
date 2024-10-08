package ar.edu.utn.frc.tup.lc.iv.dtos.common;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DtoDistribution {
    private Integer ownerId;
    private BigDecimal proportion;
}

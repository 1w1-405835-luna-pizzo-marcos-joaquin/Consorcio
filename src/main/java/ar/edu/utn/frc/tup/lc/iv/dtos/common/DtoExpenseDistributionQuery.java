package ar.edu.utn.frc.tup.lc.iv.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoExpenseDistributionQuery {
    private int ownerId;
    private String ownerFullName;
    private BigDecimal amount;

    private double proportion; //Proporcion
}
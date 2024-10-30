package ar.edu.utn.frc.tup.lc.iv.dtos.common;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoExpenseInstallment {

    private LocalDate paymentDate;
    private Integer installmentNumber;

}

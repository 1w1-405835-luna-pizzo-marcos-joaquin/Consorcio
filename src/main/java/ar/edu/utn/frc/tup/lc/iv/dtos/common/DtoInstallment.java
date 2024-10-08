package ar.edu.utn.frc.tup.lc.iv.dtos.common;

import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DtoInstallment {
    private Integer installmentNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;
    private ExpenseType expenseType;
}

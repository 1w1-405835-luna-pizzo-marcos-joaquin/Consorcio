package ar.edu.utn.frc.tup.lc.iv.dtos.common;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class DtoRequestExpense {
    private Integer id;
    private String description;
    private Integer providerId;
    private LocalDate expenseDate;
    private String invoiceNumber;
    private String typeExpense;
    private Integer categoryId;
    private BigDecimal amount;
    private Integer installments;
    private List<DtoDistribution> distributions;
}

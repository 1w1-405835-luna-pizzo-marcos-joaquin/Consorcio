package ar.edu.utn.frc.tup.lc.iv.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoExpenseQuery {
    private int id;
    private String category;
    private String provider;
    private BigDecimal amount;
    private String expenseType;
    private LocalDate expenseDate;
    private String fileId;
    private List<DtoExpenseDistributionQuery> distributionList;
    private List<DtoExpenseInstallment> installmentList;


}
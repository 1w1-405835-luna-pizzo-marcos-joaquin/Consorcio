package ar.edu.utn.frc.tup.lc.iv.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private String createdDatetime;
    private List<DtoExpenseDistributionQuery> distributionList;


}
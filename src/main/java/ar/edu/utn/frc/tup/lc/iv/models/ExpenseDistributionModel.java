package ar.edu.utn.frc.tup.lc.iv.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDistributionModel {
    private Integer id;
    private Integer ownerId;
    private BigDecimal proportion;
    private LocalDateTime createdDatetime;
    private Integer createdUser;
    private LocalDateTime lastUpdatedDatetime;
    private Integer lastUpdatedUser;
    private Boolean enabled;
}

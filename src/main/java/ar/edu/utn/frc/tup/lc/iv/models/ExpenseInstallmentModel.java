package ar.edu.utn.frc.tup.lc.iv.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseInstallmentModel {
    private Integer id;
    private Integer expenseId;  // Asegúrate de que la clase ExpenseEntity esté definida
    private LocalDate paymentDate;
    private Integer installmentNumber;
    private LocalDateTime createdDatetime;
    private Integer createdUser;
    private LocalDateTime lastUpdatedDatetime;
    private Integer lastUpdatedUser;
    private Boolean enabled;
}

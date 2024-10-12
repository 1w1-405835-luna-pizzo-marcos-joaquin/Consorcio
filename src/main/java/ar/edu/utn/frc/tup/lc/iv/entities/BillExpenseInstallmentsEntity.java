package ar.edu.utn.frc.tup.lc.iv.entities;

import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bills_expense_installments")
public class BillExpenseInstallmentsEntity extends AuditEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "description")
    private String description;
    @ManyToOne
    @JoinColumn(name = "bill_expense_owner_id", nullable = false)
    private BillExpenseOwnerEntity billExpenseOwner;
    @Transient
    private ExpenseType expenseType;
    @ManyToOne
    @JoinColumn(name = "expense_installment_id", nullable = false)
    private ExpenseInstallmentEntity expenseInstallment;
    @Column(name = "amount", nullable = false, precision = 11, scale = 2)
    private BigDecimal amount;

}

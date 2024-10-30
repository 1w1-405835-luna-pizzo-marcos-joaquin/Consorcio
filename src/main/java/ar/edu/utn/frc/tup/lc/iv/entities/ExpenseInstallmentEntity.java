package ar.edu.utn.frc.tup.lc.iv.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "expense_installments")
public class ExpenseInstallmentEntity extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "expense_id", nullable = false)
    private ExpenseEntity expense;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;


}

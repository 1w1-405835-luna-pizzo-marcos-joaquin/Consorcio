package ar.edu.utn.frc.tup.lc.iv.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bills_expense_fines")
public class BillExpenseFineEntity extends AuditEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "description")
    private String description;
    @ManyToOne
    @JoinColumn(name = "bill_expense_owner_id", nullable = false)
    private BillExpenseOwnerEntity billExpenseOwner;

    @Column(name = "fine_id", nullable = false)
    private Integer fineId;

    @Column(name= "plot_id", nullable = false)
    private Integer plotId;

    @Column(name = "amount", nullable = false, precision = 11, scale = 2)
    private BigDecimal amount;
}

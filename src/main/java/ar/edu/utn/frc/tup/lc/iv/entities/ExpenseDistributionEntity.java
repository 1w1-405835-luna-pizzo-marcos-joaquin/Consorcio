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
@Table(name = "expense_distribution")
public class ExpenseDistributionEntity extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @ManyToOne
    @JoinColumn(name = "expense_id", nullable = false)
    private ExpenseEntity expense;

    @Column(name = "proportion", nullable = false, precision = 3, scale = 2)
    private BigDecimal proportion;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

}

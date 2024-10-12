package ar.edu.utn.frc.tup.lc.iv.entities;

import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "expenses")
public class ExpenseEntity extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "provider_id", nullable = false)
    private Integer providerId;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "invoice_number")
    private Integer invoiceNumber;

    @Column(name = "expense_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExpenseType expenseType;

    @ManyToOne
    @JoinColumn(name = "expense_category_id", nullable = false)
    private ExpenseCategoryEntity category;

    @Column(name = "amount", nullable = false, precision = 11, scale = 2)
    private BigDecimal amount;

    @Column(name = "installments")
    private Integer installments;

    @Column(name = "note_credit")
    private Boolean noteCredit;

    @JsonIgnore
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL)
    private List<ExpenseDistributionEntity> distributions;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL)
    private List<ExpenseInstallmentEntity> installmentsList;

}

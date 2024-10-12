package ar.edu.utn.frc.tup.lc.iv.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bills_expense_owners")
public class BillExpenseOwnerEntity extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "bill_record_id", nullable = false)
    private BillRecordEntity billRecord;

    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @Column(name = "field_size")
    private Integer fieldSize;

    @OneToMany(mappedBy = "billExpenseOwner",cascade = CascadeType.ALL)
    private List<BillExpenseFineEntity> billExpenseFines;
    @OneToMany(mappedBy = "billExpenseOwner",cascade = CascadeType.ALL)
    private List<BillExpenseInstallmentsEntity> billExpenseInstallments;

}

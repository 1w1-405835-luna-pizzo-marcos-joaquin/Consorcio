package ar.edu.utn.frc.tup.lc.iv.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bills_expense_owners")
public class BillExpenseOwnerEntity {
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
    @Column(name = "created_datetime", nullable = false)
    private LocalDateTime createdDatetime;

    @Column(name = "created_user", nullable = false)
    private Integer createdUser;

    @Column(name = "last_updated_datetime", nullable = false)
    private LocalDateTime lastUpdatedDatetime;

    @Column(name = "last_updated_user", nullable = false)
    private Integer lastUpdatedUser;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @OneToMany(mappedBy = "billExpenseOwner",cascade = CascadeType.ALL)
    private List<BillExpenseFineEntity> billExpenseFines;
    @OneToMany(mappedBy = "billExpenseOwner",cascade = CascadeType.ALL)
    private List<BillExpenseInstallmentsEntity> billExpenseInstallments;

}

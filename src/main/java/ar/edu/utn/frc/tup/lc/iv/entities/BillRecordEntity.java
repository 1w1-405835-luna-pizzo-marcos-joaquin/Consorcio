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
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bills_record")
public class BillRecordEntity extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "start_date", nullable = false)
    private LocalDate start;
    @Column(name = "end_date", nullable = false)
    private LocalDate end;
    @OneToMany(mappedBy = "billRecord",cascade = CascadeType.ALL)
    private List<BillExpenseOwnerEntity> billExpenseOwner;
}

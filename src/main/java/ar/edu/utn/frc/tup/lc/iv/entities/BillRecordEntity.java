package ar.edu.utn.frc.tup.lc.iv.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bills_record")
public class BillRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "start_date", nullable = false)
    private LocalDate start;
    @Column(name = "end_date", nullable = false)
    private LocalDate end;
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

    @OneToMany(mappedBy = "billRecord",cascade = CascadeType.ALL)
    private List<BillExpenseOwnerEntity> billExpenseOwner;
}

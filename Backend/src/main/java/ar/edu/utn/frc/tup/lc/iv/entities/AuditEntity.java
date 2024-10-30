package ar.edu.utn.frc.tup.lc.iv.entities;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class AuditEntity {

    @Column(name = "created_datetime", insertable = false, updatable = false)
    private LocalDateTime createdDatetime;

    @Column(name = "created_user", insertable = true, updatable = false)
    private Integer createdUser;

    @Column(name = "last_updated_datetime", insertable = false, updatable = false)
    private LocalDateTime lastUpdatedDatetime;

    @Column(name = "last_updated_user", insertable = true, updatable = true)
    private Integer lastUpdatedUser;

    @Column(name = "enabled", insertable = false, updatable = true)
    private Boolean enabled;
}
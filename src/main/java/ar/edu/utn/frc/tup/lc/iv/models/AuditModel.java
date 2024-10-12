package ar.edu.utn.frc.tup.lc.iv.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class AuditModel {
    private Integer id;
    private LocalDateTime createdDatetime;
    private Integer createdUser;
    private LocalDateTime lastUpdatedDatetime;
    private Integer lastUpdatedUser;
    private Boolean enabled;
}

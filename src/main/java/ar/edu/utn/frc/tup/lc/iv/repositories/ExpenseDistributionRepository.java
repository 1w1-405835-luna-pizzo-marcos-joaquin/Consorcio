package ar.edu.utn.frc.tup.lc.iv.repositories;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseOwnerVisualizerDTO;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseDistributionRepository extends JpaRepository<ExpenseDistributionEntity,Integer> {
    @Query("SELECT DISTINCT e FROM ExpenseDistributionEntity e")
    List<ExpenseDistributionEntity> findAllDistinct();
    List<ExpenseDistributionEntity> findAllByOwnerId(Integer ownerId);

}

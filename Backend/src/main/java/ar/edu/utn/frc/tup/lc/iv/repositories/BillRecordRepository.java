package ar.edu.utn.frc.tup.lc.iv.repositories;

import ar.edu.utn.frc.tup.lc.iv.entities.BillRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BillRecordRepository extends JpaRepository<BillRecordEntity,Integer> {
    Optional<BillRecordEntity> findFirstByStartAndEndAndEnabledTrue(LocalDate start, LocalDate end);
    @Query("select b from BillRecordEntity b where (b.start between :start and :end or b.end between :start and :end) and b.enabled")
    Optional<BillRecordEntity> findAnyByStartAndEnd(@Param("start") LocalDate start, @Param("end") LocalDate end);
}

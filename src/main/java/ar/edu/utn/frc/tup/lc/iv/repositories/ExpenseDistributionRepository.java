package ar.edu.utn.frc.tup.lc.iv.repositories;

import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseDistributionRepository extends JpaRepository<ExpenseDistributionEntity,Integer> {
    @Query("SELECT ed FROM ExpenseDistributionEntity ed " +
            "JOIN ed.expense e " +
            "JOIN e.category c " +
            "WHERE ed.ownerId = :ownerId AND e.expenseDate BETWEEN :startDate AND :endDate " +
            "AND e.expenseType = :expenseType " +
            "AND c.id = :categoryId")
    List<ExpenseDistributionEntity> findByOwnerAndExpenseDateRangeAndTypeAndCategory(
            @Param("ownerId") Integer ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("expenseType") ExpenseType expenseType,
            @Param("categoryId") Integer categoryId);
}
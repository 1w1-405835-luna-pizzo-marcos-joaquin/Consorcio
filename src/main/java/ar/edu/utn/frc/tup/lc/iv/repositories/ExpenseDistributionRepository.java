package ar.edu.utn.frc.tup.lc.iv.repositories;

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
    @Query("SELECT e FROM ExpenseDistributionEntity e " +
            "WHERE e.ownerId = :ownerId " +
            "AND e.expense.expenseDate BETWEEN :startDate AND :endDate " +
            "AND (:expenseType IS NULL OR e.expense.expenseType = :expenseType) " +
            "AND (:categoryId IS NULL OR e.expense.category.id = :categoryId) " +
            "AND (:description IS NULL OR LOWER(e.expense.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
            "AND (:amountFrom IS NULL OR e.expense.amount >= :amountFrom) " +
            "AND (:amountTo IS NULL OR e.expense.amount <= :amountTo) " +
            "AND e.enabled = true")
    List<ExpenseDistributionEntity> findByOwnerAndFilters(
            @Param("ownerId") Integer ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("expenseType") ExpenseType expenseType,
            @Param("categoryId") Integer categoryId,
            @Param("description") String description,
            @Param("amountFrom") BigDecimal amountFrom,
            @Param("amountTo") BigDecimal amountTo);

    @Query("SELECT e FROM ExpenseDistributionEntity e " +
            "WHERE e.ownerId = :ownerId " +
            "AND e.expense.expenseDate BETWEEN :startDate AND :endDate " +
            "AND (:categoryId IS NULL OR e.expense.category.id = :categoryId) " +
            "AND (:description IS NULL OR LOWER(e.expense.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
            "AND (:amountFrom IS NULL OR e.expense.amount >= :amountFrom) " +
            "AND (:amountTo IS NULL OR e.expense.amount <= :amountTo)" +
            "AND e.enabled = true")
    List<ExpenseDistributionEntity> findByOwnerAndExpenseDateRangeAndAllTypesAndCategory(
            @Param("ownerId") Integer ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Integer categoryId,
            @Param("description") String description,
            @Param("amountFrom") BigDecimal amountFrom,
            @Param("amountTo") BigDecimal amountTo);

}

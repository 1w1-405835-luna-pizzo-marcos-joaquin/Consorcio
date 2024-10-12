package ar.edu.utn.frc.tup.lc.iv.repositories;

import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseInstallmentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillExpensesInstallmentsRepository extends JpaRepository<BillExpenseInstallmentsEntity,Integer> {

    @Query("SELECT bei FROM BillExpenseInstallmentsEntity bei " +
            "JOIN bei.expenseInstallment ei " +
            "WHERE ei.expense.id = :expenseId")
    Optional<List<BillExpenseInstallmentsEntity>> findByExpenseId(@Param("expenseId") Integer expenseId);
}

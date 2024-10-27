package ar.edu.utn.frc.tup.lc.iv.repositories;

import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity,Integer> {

    Optional<ExpenseEntity> findExpenseEntitiesByInvoiceNumberAndProviderId(String  invoiceNumber,Integer providerId);
    @Query("select e from ExpenseEntity e join e.installmentsList i where i.paymentDate between :from and :to and e.enabled")
    List<ExpenseEntity> findAllByPaymentDate(@Param("from") LocalDate from, @Param("to") LocalDate to);
    /**
     * Finds all enabled ExpenseEntity records where the expense date is between the specified dates.
     *
     * @param from the start date
     * @param to the end date
     * @return a list of ExpenseEntity records
     */
    @Query("select e from ExpenseEntity e where e.expenseDate between :from and :to and e.enabled")
    List<ExpenseEntity> findAllByDate(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /**
     * Retrieves a list of expenses grouped by year and month for a specified period.
     * The query calculates the total amount of expenses for each year and month within the
     * specified range, where the 'expense_date' falls between the provided start and end year
     * values. Only enabled expenses (enabled = true) are considered in the calculation.
     * The result includes:
     * - Year: The year of the expense.
     * - Month: The month of the expense.
     * - Total amount: The sum of expenses for each year and month combination.
     *
     * @param start The start year of the period to filter expenses (inclusive).
     * @param end   The end year of the period to filter expenses (inclusive).
     * @return A list of Object arrays, where each array contains:
     *         - The year (Integer)
     *         - The month (Integer)
     *         - The total amount of expenses for that year and month (BigDecimal)
     */
    @Query(value = "SELECT YEAR(expense_date) AS year, MONTH(expense_date) AS month, SUM(amount) AS amount " +
            "FROM expenses " +
            "WHERE YEAR(expense_date) BETWEEN :start AND :end AND enabled IS TRUE and expenses.expense_type != 'individual' " +
            "GROUP BY YEAR(expense_date), MONTH(expense_date) " +
            "ORDER BY 1, 2", nativeQuery = true)
    List<Object[]> findAllByPeriodGroupByYearMonth(@Param("start") Integer start, @Param("end") Integer end);


    /**
     * Retrieves a list of expenses grouped by category for a specified period.
     * The query calculates the total amount of expenses for each category where the 'expense_date'
     * falls between the provided start and end date values. Only enabled expenses (expenses.enabled = true)
     * are considered in the calculation.
     *
     * The result includes:
     * - Category description: The name of the expense category.
     * - Total amount: The sum of expenses for each category.
     *
     * @param start The start date of the period to filter expenses (inclusive).
     * @param end   The end date of the period to filter expenses (inclusive).
     * @return A list of Object arrays, where each array contains:
     *         - The category description (String)
     *         - The total amount of expenses for that category (BigDecimal)
     */
    @Query(value = "SELECT expense_categories.description, SUM(expenses.amount) AS amount " +
            "FROM expenses " +
            "INNER JOIN expense_categories ON expenses.expense_category_id = expense_categories.id " +
            "WHERE expense_date BETWEEN :start AND :end AND expenses.enabled IS TRUE and expenses.expense_type != 'individual' " +
            "GROUP BY expense_categories.description " +
            "ORDER BY 1", nativeQuery = true)
    List<Object[]> findAllByPeriodGroupByCategory(@Param("start") LocalDate start, @Param("end") LocalDate end);

}

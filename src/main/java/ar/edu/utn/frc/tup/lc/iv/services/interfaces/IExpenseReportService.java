package ar.edu.utn.frc.tup.lc.iv.services.interfaces;

import ar.edu.utn.frc.tup.lc.iv.dtos.ReportChart.ExpenseCategoryPeriodDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.ReportChart.ExpenseYearDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.PeriodDto;

import java.util.List;

public interface IExpenseReportService {
    List<ExpenseCategoryPeriodDto> getExpenseCategoriesPeriod(PeriodDto periodDto);
    List<ExpenseYearDto> getExpenseYears(PeriodDto periodDto);
}

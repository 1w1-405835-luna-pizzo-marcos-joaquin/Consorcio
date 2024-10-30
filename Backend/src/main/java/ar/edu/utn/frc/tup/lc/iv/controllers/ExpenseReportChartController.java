package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.ReportChart.ExpenseCategoryPeriodDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.ReportChart.ExpenseYearDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.PeriodDto;

import ar.edu.utn.frc.tup.lc.iv.services.impl.ExpenseReportService;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reportchart")
public class ExpenseReportChartController {
    private final IExpenseReportService expenseReportServiceice;
    @Autowired
    public ExpenseReportChartController(final ExpenseReportService expenseReportServiceiceParam) {
        this.expenseReportServiceice = expenseReportServiceiceParam;
    }
    @GetMapping("/yearmonth")
    ResponseEntity<List<ExpenseYearDto>> getYearMonth(@RequestParam(value = "start_date")@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                      @RequestParam("end_date")@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        PeriodDto periodDto = new PeriodDto(startDate, endDate);
        return new ResponseEntity<>(expenseReportServiceice.getExpenseYears(periodDto), HttpStatus.OK);
    }
    @GetMapping("/categoriesperiod")
    ResponseEntity<List<ExpenseCategoryPeriodDto>> getCategoriesPeriod(@RequestParam(value = "start_date")@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                                       @RequestParam("end_date")@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        PeriodDto periodDto = new PeriodDto(startDate, endDate);
        return new ResponseEntity<>(expenseReportServiceice.getExpenseCategoriesPeriod(periodDto), HttpStatus.OK);
    }
}

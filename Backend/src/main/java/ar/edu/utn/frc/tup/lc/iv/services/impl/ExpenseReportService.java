package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.ReportChart.ExpenseCategoryPeriodDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.ReportChart.ExpenseYearDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.PeriodDto;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseRepository;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseReportService implements IExpenseReportService {
    private final ExpenseRepository expenseRepository;
    @Autowired
    public ExpenseReportService(ExpenseRepository expenseRepositoryParam) {
        this.expenseRepository = expenseRepositoryParam;
    }
    @Override
    public List<ExpenseCategoryPeriodDto> getExpenseCategoriesPeriod(PeriodDto periodDto) {
       validPeriod(periodDto);
       try {
           return repoToExpenseCategoryPeriodDto(periodDto);
       }catch (Exception e) {
           throw new CustomException("Ocurrio un error al obtener los datos",HttpStatus.INTERNAL_SERVER_ERROR,e);
       }
    }

    @Override
    public List<ExpenseYearDto> getExpenseYears(PeriodDto periodDto) {
        validPeriod(periodDto);
        try {
            return repoToExpenseYearDto(periodDto.getEndDate().plusYears(-1),periodDto.getEndDate());
        }catch (Exception e) {
            throw new CustomException("Ocurrio un error al obtener los datos",HttpStatus.INTERNAL_SERVER_ERROR,e);
        }
    }

    private List<ExpenseCategoryPeriodDto> repoToExpenseCategoryPeriodDto(PeriodDto periodDto) {
        List<Object[]>repo = expenseRepository.findAllByPeriodGroupByCategory(periodDto.getStartDate(),periodDto.getEndDate());
        return repo.stream()
                .map(result -> new ExpenseCategoryPeriodDto((String) result[0], (BigDecimal) result[1]))
                .collect(Collectors.toList());
    }
    private List<ExpenseYearDto> repoToExpenseYearDto(LocalDate startDate, LocalDate endDate) {

        List<Object[]> repo = expenseRepository.findAllByPeriodGroupByYearMonth(startDate.getYear(),endDate.getYear());
        return repo.stream()
                .map(result-> new ExpenseYearDto((Integer) result[0],(Integer) result[1],(BigDecimal) result[2]))
                .collect(Collectors.toList());
    }

    private void validPeriod(PeriodDto periodDto) {
        // Validate that the period is not null
        if(periodDto == null)
            throw new CustomException("The period be can't null", HttpStatus.BAD_REQUEST);
        // Validate that the start date is not null
        if(periodDto.getStartDate() == null)
            throw new CustomException("The start date be can't null", HttpStatus.BAD_REQUEST);
        if(periodDto.getEndDate() == null)
            throw new CustomException("The end date be can't null", HttpStatus.BAD_REQUEST);
        // Validate that the start date is before the end date
        if (periodDto.getStartDate().isAfter(periodDto.getEndDate())) {
            throw new CustomException("The start date must be earlier than the end date.", HttpStatus.BAD_REQUEST);
        }
    }
}

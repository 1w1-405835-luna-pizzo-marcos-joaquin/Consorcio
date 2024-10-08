package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.services.impl.ExpenseDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses/distributions")
public class ExpenseDistributionController {
    @Autowired
    private ExpenseDistributionService expenseDistributionService;

    @GetMapping("/filter")
    public ResponseEntity<List<ExpenseDistributionEntity>> filterExpenseDistributions(
            @RequestParam(required = false) Integer ownerId,
            @RequestParam(required = false)LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) ExpenseType expenseType,
            @RequestParam (required = false)Integer categoryId) {

        List<ExpenseDistributionEntity> Ownerdistributions = expenseDistributionService.findByOwnerAndExpenseDateRangeAndTypeAndCategory(ownerId, startDate, endDate, expenseType, categoryId);
        return ResponseEntity.ok(Ownerdistributions);
    }
}

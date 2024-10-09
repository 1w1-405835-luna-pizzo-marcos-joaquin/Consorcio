package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseOwnerVisualizerDTO;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.services.impl.ExpenseDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses/distributions")
public class ExpenseDistributionController {
    @Autowired
    private ExpenseDistributionService expenseDistributionService;

    @GetMapping("/filter")
    public ResponseEntity<List<ExpenseOwnerVisualizerDTO>> filterExpenseDistributions(
            @RequestHeader(value = "ownerId")Integer ownerId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) ExpenseType expenseType,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal amountFrom,
            @RequestParam(required = false) BigDecimal amountTo) {


        List<ExpenseOwnerVisualizerDTO> ownerDistributions = expenseDistributionService.findVisualizersByOwnerAndFilters(
                ownerId, startDate, endDate, expenseType, categoryId, description, amountFrom, amountTo);

        return ResponseEntity.ok(ownerDistributions);
    }
}

package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseOwnerVisualizerDTO;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.services.impl.ExpenseDistributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(summary = "Get ALL Expenses",
            description = "Get all expenses ")
    @ApiResponse(responseCode = "200", description = "get all successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ExpenseDistributionEntity.class)))
    @GetMapping("/getAll")
    public ResponseEntity<List<ExpenseOwnerVisualizerDTO>> getAllExpenses(){
        List<ExpenseOwnerVisualizerDTO> list = expenseDistributionService.findAll();
        return ResponseEntity.ok(list);
    }


    @Operation(summary = "Get expenses with filters",
            description = "Bring expense with optional  filters")
    @ApiResponse(responseCode = "200", description = "Filter dispensers brought in correctly",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ExpenseOwnerVisualizerDTO.class)))
   @GetMapping("/filterExpenses")
    public ResponseEntity<List<ExpenseOwnerVisualizerDTO>> filterExpenseDistributions(
          @RequestHeader(value = "ownerId")Integer ownerId,
          @RequestParam(required = false) String description,
          @RequestParam(required = false) LocalDate startDate,
          @RequestParam(required = false) LocalDate endDate,
          @RequestParam(required = false) ExpenseType expenseType,
          @RequestParam(required = false) Integer categoryId,
          @RequestParam(required = false) BigDecimal amountFrom,
          @RequestParam(required = false) BigDecimal amountTo) {


        List<ExpenseOwnerVisualizerDTO> ownerDistributions = expenseDistributionService.filterExpenses(
                ownerId, startDate, endDate, expenseType, categoryId, description, amountFrom, amountTo);

        return ResponseEntity.ok(ownerDistributions);
   }
}

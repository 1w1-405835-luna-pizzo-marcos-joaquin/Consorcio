package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoRequestExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseExpense;
import ar.edu.utn.frc.tup.lc.iv.services.impl.ExpenseService;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private final IExpenseService expenseService;

    @Operation(summary = "Create a new expense",
            description = "Creates a new expense with the given details and optional file")
    @ApiResponse(responseCode = "200", description = "Expense created successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DtoResponseExpense.class)))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DtoResponseExpense> postExpense(
            @RequestPart("expense") DtoRequestExpense request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return expenseService.postExpense(request, file);
    }

    @Operation(summary = "Delete an expense")
    @ApiResponse(responseCode = "204", description = "Expense deleted successfully")
    @DeleteMapping()
    public ResponseEntity<Void> deleteExpenseById(@RequestParam Integer id) {
        expenseService.deteleExpense(id);
        return ResponseEntity.noContent().build();
    }

}

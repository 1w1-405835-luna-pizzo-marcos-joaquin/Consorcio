package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoExpenseQuery;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private final IExpenseService expenseService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DtoResponseExpense postExpense(
            @RequestPart("expense") DtoRequestExpense request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return expenseService.postExpense(request, file);
    }

    @GetMapping("/getById")
    @Operation(summary = "Get expense by id")
    @ApiResponse(responseCode = "200", description = "Expenses get successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DtoExpenseQuery.class)))
    public ResponseEntity<DtoExpenseQuery> getExpenseById(@RequestParam (required = true)int expenseId) {
        return ResponseEntity.ok(expenseService.getExpenseById(expenseId)) ;
    }

    @GetMapping("/getByFilters")
    @Operation(summary = "Get expenses by filters")
    @ApiResponse(responseCode = "200", description = "Expenses get successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DtoExpenseQuery.class)))
    public ResponseEntity<List <DtoExpenseQuery>> getExpenses(@RequestParam (required = false)String expenseType,
                                                              @RequestParam (required = false)String category,
                                                              @RequestParam (required = false)String provider,
                                                              @RequestParam (required = false)String dateFrom,
                                                              @RequestParam (required = false)String dateTo) {
        return ResponseEntity.ok(expenseService.getExpenses(expenseType, category, provider, dateFrom, dateTo)) ;
    }


}

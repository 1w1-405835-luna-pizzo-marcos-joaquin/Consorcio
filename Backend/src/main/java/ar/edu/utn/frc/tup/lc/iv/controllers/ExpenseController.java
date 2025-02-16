package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoExpenseQuery;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoRequestExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseDeleteExpense;
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

import java.time.LocalDate;
import java.util.List;

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
    }@Operation(summary = "Edit a expense",
            description = "Edit a expense with the given details and optional file")
    @ApiResponse(responseCode = "200", description = "Expense updated successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DtoResponseExpense.class)))
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DtoResponseExpense putExpense(
            @RequestPart("expense") DtoRequestExpense request,
            @RequestPart(value = "file", required = false) MultipartFile file){
        return expenseService.putExpense(request,file);
    }


    @Operation(summary = "Delete logic or expense")
    @ApiResponse(responseCode = "204", description = "Expense delete logic successfully",
    content = @Content(mediaType = "application/json"))
    @DeleteMapping()
    public DtoResponseDeleteExpense deleteExpenseByIdLogicOrThrowException(@RequestParam Integer id) {
        return expenseService.deteleExpense(id);

    }
    @Operation(summary = "Create note of credit")
    @ApiResponse(responseCode = "204", description = "Note of credit created succcessfully",
            content = @Content(mediaType = "application/json"))
    @DeleteMapping("/note_credit")
    public DtoResponseExpense createNoteOfCredit(@RequestParam Integer id) {
        return expenseService.createCreditNoteForExpense(id);
    }

    /**
 * Retrieves an expense by its ID.
 *
 * @param expenseId the ID of the expense to retrieve (required)
 * @return a ResponseEntity containing the DtoExpenseQuery object for the specified expense
 */
@GetMapping("/getById")
@Operation(summary = "Get expense by id")
@ApiResponse(responseCode = "200", description = "Expenses retrieved successfully",
        content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DtoExpenseQuery.class)))
public ResponseEntity<DtoExpenseQuery> getExpenseById(@RequestParam(required = true) int expenseId) {
    return ResponseEntity.ok(expenseService.getExpenseById(expenseId));
}


    //TODO REQUIRED FROM AND TO ok
    //TODO AL DTORESPONSE DEBERIA TENER EL FILEID Y EL INSTALLMENT LIST ok
    //TODO LA FECHA FORMATO YYYY-MM-DD ok
    /**
 * Retrieves a list of expenses filtered by the given parameters.
 *
 * @param expenseType the type of expense to filter by (optional)
 * @param category the category of the expense to filter by (optional)
 * @param provider the provider of the expense to filter by (optional)
 * @param dateFrom the start date for filtering expenses (required, format: YYYY-MM-DD)
 * @param dateTo the end date for filtering expenses (required, format: YYYY-MM-DD)
 * @return a ResponseEntity containing a list of DtoExpenseQuery objects that match the filters
 */
@GetMapping("/getByFilters")
@Operation(summary = "Get expenses by filters")
@ApiResponse(responseCode = "200", description = "Expenses retrieved successfully",
        content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DtoExpenseQuery.class)))
public ResponseEntity<List<DtoExpenseQuery>> getExpenses(
        @RequestParam(required = false) String expenseType,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String provider,
        @RequestParam(required = true) String dateFrom,
        @RequestParam(required = true) String dateTo) {
    return ResponseEntity.ok(expenseService.getExpenses(expenseType, category, provider, dateFrom, dateTo));
}

}

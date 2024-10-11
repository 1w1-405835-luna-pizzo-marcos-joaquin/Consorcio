package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.response.BillExpenseDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.PeriodDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.ErrorApi;
import ar.edu.utn.frc.tup.lc.iv.services.impl.BillExpenseService;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IBillExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billexpenses")
public class BillExpenseController {
    private final IBillExpenseService billExpenseService;
    @Autowired
    public BillExpenseController(BillExpenseService billExpenseService) {
        this.billExpenseService = billExpenseService;
    }

    /**
     * Generates bill expenses for a given period. This method processes the period dates and
     * returns the calculated bill expenses or an appropriate error response.
     *
     * @param periodDto The period with start and end dates.
     * @return {@link ResponseEntity<BillExpenseDto>} The generated bill expenses.
     */
    @Operation(
            summary = "Generate bill expenses for a given period",
            description = "Generates bill expenses for the provided period. Validates that the period is in the past and that no overlapping bill already exists."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bill expenses generated successfully",
                    content = @Content(schema = @Schema(implementation = BillExpenseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid period or validation failure",
                    content = @Content(schema = @Schema(implementation = ErrorApi.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Overlapping period detected",
                    content = @Content(schema = @Schema(implementation = ErrorApi.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ErrorApi.class))
            )
    })
    @PostMapping("/generate")
    public ResponseEntity<BillExpenseDto> generateExpenses(@RequestBody(required = true)PeriodDto periodDto){
        BillExpenseDto result = billExpenseService.generateBillExpense(periodDto);
        return ResponseEntity.ok(result);
    }
}

package ar.edu.utn.frc.tup.lc.iv.dtos.common;

import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseCategoryModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class DtoResponseExpense {
    private String description;
    private Integer providerId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;
    private UUID fileId;
    private Integer invoiceNumber;
    private ExpenseType expenseType;
    private DtoCategory dtoCategory;
    private List<DtoDistribution> dtoDistributionList;
    private List<DtoInstallment> dtoInstallmentList;
}

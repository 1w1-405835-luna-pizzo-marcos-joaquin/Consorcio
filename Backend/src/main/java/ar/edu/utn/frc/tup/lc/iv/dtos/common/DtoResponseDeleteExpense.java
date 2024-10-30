package ar.edu.utn.frc.tup.lc.iv.dtos.common;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class DtoResponseDeleteExpense {

    private String expense;
    private String descriptionResponse;
    private HttpStatus httpStatus;
}

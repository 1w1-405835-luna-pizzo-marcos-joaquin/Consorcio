package ar.edu.utn.frc.tup.lc.iv.dtos.fileManager;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data Transfer Object (DTO) for representing a UUID response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UuidResponseDto {
    /**
     * The unique identifier (UUID) to be returned in the response.
     */
    @JsonProperty(namespace = "uuid")
    private UUID uuid;
}

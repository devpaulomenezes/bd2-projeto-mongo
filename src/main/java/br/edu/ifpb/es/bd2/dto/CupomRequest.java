package br.edu.ifpb.es.bd2.dto;

import br.edu.ifpb.es.bd2.model.StatusCupom;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CupomRequest(
        @NotBlank String codigo,
        @NotNull @DecimalMin("0.01") @DecimalMax("100.00") BigDecimal percentualDesconto,
        @NotNull @Future LocalDateTime dataExpiracao,
        StatusCupom status
) {}
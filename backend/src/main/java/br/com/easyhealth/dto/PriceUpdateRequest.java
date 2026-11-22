package br.com.easyhealth.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PriceUpdateRequest(
        @NotBlank(message = "Informe o serviço.")
        @Size(max = 120, message = "O nome do serviço deve ter no máximo 120 caracteres.")
        String servico,
        @NotNull(message = "Informe o valor.")
        @DecimalMin(value = "0.00", message = "O valor não pode ser negativo.")
        @DecimalMax(value = "100000.00", message = "O valor é alto demais.")
        BigDecimal valor) {
}

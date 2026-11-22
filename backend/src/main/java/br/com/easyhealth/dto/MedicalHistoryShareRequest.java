
package br.com.easyhealth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicalHistoryShareRequest(
        @NotBlank(message = "Informe o identificador do médico.")
        @Size(max = 36, message = "Identificador do médico inválido.")
        String medicoId) {
}

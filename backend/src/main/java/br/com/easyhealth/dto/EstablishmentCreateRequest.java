
package br.com.easyhealth.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EstablishmentCreateRequest(

        @NotBlank
        @Size(max = 255)
        String nome,

        @NotBlank
        @Pattern(regexp = "hospital|upa|ubs|clinica|centro_saude|laboratorio|consultorio|farmacia|outros")
        String tipo,

        @NotBlank
        @Pattern(regexp = "publico|privado|filantropica|misto")
        String redeAtendimento,

        @Size(max = 255)
        String razaoSocial,

        @Size(max = 18)
        String cnpj,

        @NotBlank
        @Size(max = 255)
        String logradouro,

        @Size(max = 20)
        String numero,

        @NotBlank
        @Size(max = 120)
        String bairro,

        @NotBlank
        @Size(max = 120)
        String cidade,

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{2}")
        String uf,

        @Size(max = 9)
        String cep,

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        BigDecimal latitude,

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        BigDecimal longitude,

        @Size(max = 32)
        String telefone,

        @Size(max = 32)
        String whatsapp,

        @Email
        @Size(max = 255)
        String email,

        @Size(max = 255)
        String siteUrl,

        @Size(max = 100)
        String horarioPadrao
) {
}

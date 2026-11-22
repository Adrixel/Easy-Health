
package br.com.easyhealth.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import br.com.easyhealth.dto.EstablishmentCreateRequest;
import br.com.easyhealth.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstablishmentRegistrationService {

    private final JdbcTemplate jdbc;

    public EstablishmentRegistrationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Transactional
    public Map<String, Object> register(EstablishmentCreateRequest request, User owner) {
        String id = UUID.randomUUID().toString();

        String numero = optional(request.numero());
        String cnpj = optional(request.cnpj());

        String endereco = request.logradouro().trim()
                + (numero == null ? "" : ", " + numero)
                + " - " + request.bairro().trim()
                + ", " + request.cidade().trim()
                + " - " + request.uf().toUpperCase();

        if (endereco.length() > 500) {
            throw new IllegalArgumentException(
                    "O endereço completo não pode ultrapassar 500 caracteres."
            );
        }

        String horario = optional(request.horarioPadrao());

        jdbc.update("""
                INSERT INTO easyhealth_institutions.instituicoes_saude (
                    id, nome, tipo, rede_atendimento, razao_social, cnpj,
                    logradouro, numero, bairro, cidade, uf, cep,
                    endereco_completo, latitude, longitude, telefone,
                    whatsapp, email, site_url, horario_padrao
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                request.nome().trim(),
                request.tipo(),
                request.redeAtendimento(),
                optional(request.razaoSocial()),
                cnpj,
                request.logradouro().trim(),
                numero,
                request.bairro().trim(),
                request.cidade().trim(),
                request.uf().toUpperCase(),
                optional(request.cep()),
                endereco,
                request.latitude(),
                request.longitude(),
                optional(request.telefone()),
                optional(request.whatsapp()),
                optional(request.email()),
                optional(request.siteUrl()),
                horario == null ? "24h" : horario
        );

        jdbc.update("""
                INSERT INTO establishment_staff (user_id, establishment_id, cargo)
                VALUES (?, ?, ?)
                """,
                owner.id(), id, owner.cargo().valor()
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("nome", request.nome().trim());
        result.put("cargo", owner.cargo().valor());
        result.put("mensagem", "Clínica cadastrada e vinculada ao proprietário.");

        return result;
    }
}

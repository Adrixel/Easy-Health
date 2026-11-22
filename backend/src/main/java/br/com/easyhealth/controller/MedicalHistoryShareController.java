
package br.com.easyhealth.controller;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import br.com.easyhealth.dto.MedicalHistoryShareRequest;
import br.com.easyhealth.model.Cargo;
import br.com.easyhealth.model.User;
import br.com.easyhealth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/historico/compartilhamentos")
public class MedicalHistoryShareController {

    private final JdbcTemplate jdbc;
    private final AuthService auth;

    public MedicalHistoryShareController(
            JdbcTemplate jdbc,
            AuthService auth) {
        this.jdbc = jdbc;
        this.auth = auth;
    }

    @PostMapping
    public Map<String, Object> compartilhar(
            @Valid @RequestBody MedicalHistoryShareRequest body,
            HttpServletRequest request) {

        User paciente = auth.requireCargo(request, Cargo.PACIENTE);

        if (paciente.id().equals(body.medicoId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Você não pode compartilhar o histórico consigo mesmo.");
        }

        List<Map<String, Object>> medicos = jdbc.queryForList(
                """
                SELECT id, nome
                FROM users
                WHERE id = ?
                  AND cargo = 'medico'
                """,
                body.medicoId());

        if (medicos.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Médico não encontrado.");
        }

        String id = UUID.randomUUID().toString();

        try {
            jdbc.update(
                    """
                    INSERT INTO medical_history_shares (
                        id, patient_id, medico_id, active_medico_id
                    ) VALUES (?, ?, ?, ?)
                    """,
                    id,
                    paciente.id(),
                    body.medicoId(),
                    body.medicoId());
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este médico já tem acesso ativo ao seu histórico.");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", id);
        response.put("medicoId", body.medicoId());
        response.put("medico", medicos.get(0).get("nome"));
        response.put("active", true);

        return response;
    }

    @GetMapping
    public List<Map<String, Object>> listarCompartilhamentos(
            HttpServletRequest request) {

        User paciente = auth.requireCargo(request, Cargo.PACIENTE);

        return jdbc.query(
                """
                SELECT
                    s.id,
                    s.medico_id,
                    u.nome AS medico,
                    s.created_at,
                    s.revoked_at
                FROM medical_history_shares s
                INNER JOIN users u ON u.id = s.medico_id
                WHERE s.patient_id = ?
                ORDER BY s.created_at DESC
                """,
                (rs, rowNum) -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", rs.getString("id"));
                    item.put("medicoId", rs.getString("medico_id"));
                    item.put("medico", rs.getString("medico"));

                    Timestamp criadoEm = rs.getTimestamp("created_at");
                    Timestamp revogadoEm = rs.getTimestamp("revoked_at");

                    item.put(
                            "createdAt",
                            criadoEm != null ? criadoEm.toInstant().toString() : null);
                    item.put(
                            "revokedAt",
                            revogadoEm != null ? revogadoEm.toInstant().toString() : null);
                    item.put("active", revogadoEm == null);

                    return item;
                },
                paciente.id());
    }

    @DeleteMapping("/{id}")
    public void revogarCompartilhamento(
            @PathVariable String id,
            HttpServletRequest request) {

        User paciente = auth.requireCargo(request, Cargo.PACIENTE);

        int atualizados = jdbc.update(
                """
                UPDATE medical_history_shares
               SET revoked_at = CURRENT_TIMESTAMP,
                    active_medico_id = NULL
                WHERE id = ?
                  AND patient_id = ?
                  AND revoked_at IS NULL
                """,
                id,
                paciente.id());

        if (atualizados == 0) {
            Integer existentes = jdbc.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM medical_history_shares
                    WHERE id = ?
                      AND patient_id = ?
                    """,
                    Integer.class,
                    id,
                    paciente.id());

            if (existentes == null || existentes == 0) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Compartilhamento não encontrado.");
            }
        }
    }
}

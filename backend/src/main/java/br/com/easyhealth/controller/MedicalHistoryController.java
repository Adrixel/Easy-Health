
package br.com.easyhealth.controller;

import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import br.com.easyhealth.dto.MedicalHistoryRecordRequest;
import br.com.easyhealth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MedicalHistoryController {
    private static final List<String> RECORD_TYPES =
            List.of("exame", "vacina", "medicamento", "documento");

    private final JdbcTemplate jdbc;
    private final AuthService auth;

    public MedicalHistoryController(JdbcTemplate jdbc, AuthService auth) {
        this.jdbc = jdbc;
        this.auth = auth;
    }

    @GetMapping("/api/historico/registros")
    public List<Map<String, Object>> records(
            HttpServletRequest request,
            @RequestParam(required = false) String tipo) {

        String userId = auth.requireUserId(request);

        if (tipo != null && !RECORD_TYPES.contains(tipo)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Tipo de registro inválido.");
        }

        String sql = """
                SELECT r.id, r.user_id, p.nome AS paciente,
                       r.medico_id, m.nome AS medico,
                       r.establishment_id, e.nome AS estabelecimento,
                       r.tipo, r.titulo, r.data, r.descricao,
                       r.profissional, r.local, r.resultado, r.laudo_url
                FROM medical_history_records r
                JOIN users p ON p.id = r.user_id
                LEFT JOIN users m ON m.id = r.medico_id
                LEFT JOIN easyhealth_institutions.instituicoes_saude e
                    ON e.id = r.establishment_id
                WHERE r.user_id = ?
                """ + (tipo == null ? "" : " AND r.tipo = ?")
                + " ORDER BY r.data DESC, r.created_at DESC";

        return tipo == null
                ? jdbc.query(sql, this::mapRecord, userId)
                : jdbc.query(sql, this::mapRecord, userId, tipo);
    }

    @PostMapping("/api/historico/registros")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createRecord(
            HttpServletRequest request,
            @Valid @RequestBody MedicalHistoryRecordRequest body) {

        String userId = auth.requireUserId(request);
        validateAssociation(body);

        String id = UUID.randomUUID().toString();

        jdbc.update("""
                INSERT INTO medical_history_records
                    (id, user_id, medico_id, establishment_id,
                     tipo, titulo, data, descricao, profissional,
                     local, resultado, laudo_url)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                userId,
                emptyToNull(body.medicoId()),
                emptyToNull(body.establishmentId()),
                body.tipo(),
                body.titulo().trim(),
                Date.valueOf(body.data()),
                emptyToNull(body.descricao()),
                emptyToNull(body.profissional()),
                emptyToNull(body.local()),
                emptyToNull(body.resultado()),
                emptyToNull(body.laudoUrl()));

        return findRecord(id, userId);
    }

    @PutMapping("/api/historico/registros/{id}")
    public Map<String, Object> updateRecord(
            HttpServletRequest request,
            @PathVariable String id,
            @Valid @RequestBody MedicalHistoryRecordRequest body) {

        String userId = auth.requireUserId(request);
        findRecord(id, userId);
        validateAssociation(body);

        jdbc.update("""
                UPDATE medical_history_records
                SET medico_id = ?, establishment_id = ?,
                    tipo = ?, titulo = ?, data = ?, descricao = ?,
                    profissional = ?, local = ?, resultado = ?, laudo_url = ?
                WHERE id = ? AND user_id = ?
                """,
                emptyToNull(body.medicoId()),
                emptyToNull(body.establishmentId()),
                body.tipo(),
                body.titulo().trim(),
                Date.valueOf(body.data()),
                emptyToNull(body.descricao()),
                emptyToNull(body.profissional()),
                emptyToNull(body.local()),
                emptyToNull(body.resultado()),
                emptyToNull(body.laudoUrl()),
                id,
                userId);

        return findRecord(id, userId);
    }

    @DeleteMapping("/api/historico/registros/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecord(
            HttpServletRequest request,
            @PathVariable String id) {

        String userId = auth.requireUserId(request);
        findRecord(id, userId);

        jdbc.update(
                "DELETE FROM medical_history_records WHERE id = ? AND user_id = ?",
                id, userId);
    }

    private void validateAssociation(MedicalHistoryRecordRequest body) {
        String medicoId = emptyToNull(body.medicoId());
        String establishmentId = emptyToNull(body.establishmentId());

        if (medicoId == null && establishmentId == null) {
            return;
        }

        if (medicoId == null || establishmentId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o médico e a instituição juntos.");
        }

        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM users u
                JOIN establishment_staff es ON es.user_id = u.id
                WHERE u.id = ?
                  AND u.cargo = 'medico'
                  AND es.establishment_id = ?
                  AND es.cargo = 'medico'
                """, Integer.class, medicoId, establishmentId);

        if (count == null || count == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O médico não está vinculado a essa instituição.");
        }

        Integer establishmentCount = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM easyhealth_institutions.instituicoes_saude
                WHERE id = ?
                """, Integer.class, establishmentId);

        if (establishmentCount == null || establishmentCount == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Instituição não encontrada.");
        }
    }

    private Map<String, Object> findRecord(String id, String userId) {
        return jdbc.query("""
                SELECT r.id, r.user_id, p.nome AS paciente,
                       r.medico_id, m.nome AS medico,
                       r.establishment_id, e.nome AS estabelecimento,
                       r.tipo, r.titulo, r.data, r.descricao,
                       r.profissional, r.local, r.resultado, r.laudo_url
                FROM medical_history_records r
                JOIN users p ON p.id = r.user_id
                LEFT JOIN users m ON m.id = r.medico_id
                LEFT JOIN easyhealth_institutions.instituicoes_saude e
                    ON e.id = r.establishment_id
                WHERE r.id = ? AND r.user_id = ?
                """, this::mapRecord, id, userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registro do histórico não encontrado."));
    }

    private Map<String, Object> mapRecord(
            java.sql.ResultSet row, int index)
            throws java.sql.SQLException {

        Map<String, Object> record = new LinkedHashMap<>();

        record.put("id", row.getString("id"));
        record.put("userId", row.getString("user_id"));
        record.put("paciente", row.getString("paciente"));
        record.put("medicoId", row.getString("medico_id"));
        record.put("medico", row.getString("medico"));
        record.put("establishmentId", row.getString("establishment_id"));
        record.put("estabelecimento", row.getString("estabelecimento"));
        record.put("tipo", row.getString("tipo"));
        record.put("titulo", row.getString("titulo"));
        record.put("data", row.getDate("data").toLocalDate().toString());
        record.put("descricao", row.getString("descricao"));
        record.put("profissional", row.getString("profissional"));
        record.put("local", row.getString("local"));
        record.put("resultado", row.getString("resultado"));
        record.put("laudoUrl", row.getString("laudo_url"));

        return record;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

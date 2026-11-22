package br.com.easyhealth.controller;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import br.com.easyhealth.model.Cargo;
import br.com.easyhealth.model.User;
import br.com.easyhealth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/medicos/pacientes")
public class MedicalHistoryDoctorController {

    private static final List<String> RECORD_TYPES =
            List.of("exame", "vacina", "medicamento", "documento");

    private final JdbcTemplate jdbc;
    private final AuthService auth;

    public MedicalHistoryDoctorController(
            JdbcTemplate jdbc,
            AuthService auth) {
        this.jdbc = jdbc;
        this.auth = auth;
    }

    @GetMapping("/{pacienteId}/historico/registros")
    @Transactional
    public List<Map<String, Object>> patientRecords(
            HttpServletRequest request,
            @PathVariable String pacienteId,
            @RequestParam(required = false) String tipo) {

        User medico = auth.requireCargo(request, Cargo.MEDICO);

        if (tipo != null && !RECORD_TYPES.contains(tipo)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de registro inválido.");
        }

        List<String> shares = jdbc.query("""
                SELECT id
                FROM medical_history_shares
                WHERE patient_id = ?
                  AND medico_id = ?
                  AND active_medico_id = ?
                  AND revoked_at IS NULL
                FOR UPDATE
                """,
                (row, index) -> row.getString("id"),
                pacienteId,
                medico.id(),
                medico.id());

        if (shares.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não possui autorização ativa para acessar este histórico.");
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

        List<Map<String, Object>> records = tipo == null
                ? jdbc.query(sql, this::mapRecord, pacienteId)
                : jdbc.query(sql, this::mapRecord, pacienteId, tipo);

        jdbc.update("""
                INSERT INTO medical_history_access_logs (
                    id, patient_id, medico_id, tipo, records_count
                ) VALUES (?, ?, ?, ?, ?)
                """,
                UUID.randomUUID().toString(),
                pacienteId,
                medico.id(),
                tipo,
                records.size());

        return records;
    }

    private Map<String, Object> mapRecord(
            ResultSet row,
            int index) throws java.sql.SQLException {

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
}
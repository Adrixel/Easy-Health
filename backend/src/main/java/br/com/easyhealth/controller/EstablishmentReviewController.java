
package br.com.easyhealth.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import br.com.easyhealth.dto.EstablishmentReviewRequest;
import br.com.easyhealth.model.Cargo;
import br.com.easyhealth.model.User;
import br.com.easyhealth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/estabelecimentos/{id}/avaliacoes")
public class EstablishmentReviewController {

    private final AuthService auth;
    private final JdbcTemplate jdbc;

    public EstablishmentReviewController(AuthService auth, JdbcTemplate jdbc) {
        this.auth = auth;
        this.jdbc = jdbc;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            HttpServletRequest request,
            @PathVariable String id,
            @Valid @RequestBody EstablishmentReviewRequest body) {

        User patient = auth.requireCargo(request, Cargo.PACIENTE);
        requireEstablishment(id);

        String comentario = normalizeComment(body.comentario());

        try {
            jdbc.update("""
                    INSERT INTO establishment_reviews
                        (id, establishment_id, patient_id, nota, comentario)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    UUID.randomUUID().toString(),
                    id,
                    patient.id(),
                    body.nota(),
                    comentario);
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Você já avaliou esta clínica. Edite sua avaliação.");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(getMyReview(id, patient.id()));
    }

    @PutMapping("/minha")
    public Map<String, Object> update(
            HttpServletRequest request,
            @PathVariable String id,
            @Valid @RequestBody EstablishmentReviewRequest body) {

        User patient = auth.requireCargo(request, Cargo.PACIENTE);
        requireEstablishment(id);

        Integer exists = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM establishment_reviews
                WHERE establishment_id = ? AND patient_id = ?
                """, Integer.class, id, patient.id());

        if (exists == null || exists == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Você ainda não avaliou esta clínica.");
        }

        jdbc.update("""
                UPDATE establishment_reviews
                SET nota = ?, comentario = ?
                WHERE establishment_id = ? AND patient_id = ?
                """,
                body.nota(),
                normalizeComment(body.comentario()),
                id,
                patient.id());

        return getMyReview(id, patient.id());
    }

    @GetMapping
    public Map<String, Object> list(@PathVariable String id) {
        requireEstablishment(id);

        List<Map<String, Object>> avaliacoes = jdbc.query("""
        SELECT 'Paciente' AS paciente, r.nota, r.comentario,
               r.created_at, r.updated_at
        FROM establishment_reviews r
        WHERE r.establishment_id = ?
        ORDER BY r.created_at DESC
        """,
        (row, index) -> {
            Map<String, Object> review = new LinkedHashMap<>();
            review.put("paciente", row.getString("paciente"));
            review.put("nota", row.getInt("nota"));
            review.put("comentario", row.getString("comentario"));
            review.put("createdAt", row.getTimestamp("created_at"));
            review.put("updatedAt", row.getTimestamp("updated_at"));
            return review;
        },
        id);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("avaliacoes", avaliacoes);
        resultado.put("avaliacoesCount", avaliacoes.size());

        Double media = jdbc.queryForObject("""
                SELECT AVG(nota)
                FROM establishment_reviews
                WHERE establishment_id = ?
                """, Double.class, id);

        resultado.put("avaliacaoMedia", media == null ? 0.0 : media);

        return resultado;
    }

    private void requireEstablishment(String id) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM easyhealth_institutions.instituicoes_saude
                WHERE id = ?
                """, Integer.class, id);

        if (count == null || count == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Clínica não encontrada.");
        }
    }

    private Map<String, Object> getMyReview(String establishmentId, String patientId) {
        return jdbc.queryForObject("""
                SELECT id, nota, comentario, created_at, updated_at
                FROM establishment_reviews
                WHERE establishment_id = ? AND patient_id = ?
                """,
                (row, index) -> {
                    Map<String, Object> review = new LinkedHashMap<>();
                    review.put("id", row.getString("id"));
                    review.put("nota", row.getInt("nota"));
                    review.put("comentario", row.getString("comentario"));
                    review.put("createdAt", row.getTimestamp("created_at"));
                    review.put("updatedAt", row.getTimestamp("updated_at"));
                    return review;
                },
                establishmentId,
                patientId);
    }

    private String normalizeComment(String comentario) {
        if (comentario == null || comentario.isBlank()) {
            return null;
        }
        return comentario.trim();
    }
}

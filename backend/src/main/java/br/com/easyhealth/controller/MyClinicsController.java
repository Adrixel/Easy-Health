package br.com.easyhealth.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.easyhealth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Diz ao app em quais clínicas a pessoa logada trabalha. O app precisa disso para chamar as rotas
 * /api/estabelecimentos/{id}/precos e /equipe. Paciente recebe lista vazia.
 */
@RestController
public class MyClinicsController {
    private final AuthService auth;
    private final JdbcTemplate jdbc;

    public MyClinicsController(AuthService auth, JdbcTemplate jdbc) {
        this.auth = auth;
        this.jdbc = jdbc;
    }

    @GetMapping("/api/equipe/minhas-clinicas")
    public List<Map<String, Object>> myClinics(HttpServletRequest request) {
        String userId = auth.requireUserId(request);
        // O cargo na clínica precisa ser o mesmo da conta, igual à checagem do EstablishmentAccess.
        return jdbc.query("""
                SELECT s.establishment_id, i.nome, s.cargo
                FROM establishment_staff s
                JOIN users u ON u.id = s.user_id AND u.cargo = s.cargo
                LEFT JOIN easyhealth_institutions.instituicoes_saude i ON i.id = s.establishment_id
                WHERE s.user_id = ?
                ORDER BY i.nome
                """, (row, index) -> {
                    Map<String, Object> clinic = new LinkedHashMap<>();
                    clinic.put("id", row.getString("establishment_id"));
                    clinic.put("nome", row.getString("nome"));
                    clinic.put("cargo", row.getString("cargo"));
                    return clinic;
                }, userId);
    }
}


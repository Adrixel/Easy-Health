package br.com.easyhealth.controller;

import java.util.List;
import java.util.Map;

import br.com.easyhealth.dto.PriceUpdateRequest;
import br.com.easyhealth.dto.TeamMemberRequest;
import br.com.easyhealth.model.Cargo;
import br.com.easyhealth.model.User;
import br.com.easyhealth.service.EstablishmentAccess;
import br.com.easyhealth.service.EstablishmentPrices;
import br.com.easyhealth.service.EstablishmentTeamService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ações de quem cuida de uma clínica:
 * - preços: dono e funcionário daquela clínica;
 * - equipe: só o dono daquela clínica.
 * A leitura dos preços (GET) continua pública, no CatalogController.
 */
@RestController
@RequestMapping("/api/estabelecimentos/{id}")
public class EstablishmentAdminController {
    private final EstablishmentAccess access;
    private final EstablishmentTeamService team;
    private final JdbcTemplate jdbc;

    public EstablishmentAdminController(EstablishmentAccess access, EstablishmentTeamService team,
            JdbcTemplate jdbc) {
        this.access = access;
        this.team = team;
        this.jdbc = jdbc;
    }

    @PutMapping("/precos")
    public List<Map<String, Object>> updatePrice(HttpServletRequest request, @PathVariable String id,
            @Valid @RequestBody PriceUpdateRequest body) {
        User user = access.requireStaff(request, id, Cargo.DONO, Cargo.FUNCIONARIO);
        EstablishmentPrices.save(jdbc, id, body.servico(), body.valor(), user.id());
        return EstablishmentPrices.list(jdbc, id);
    }

    @GetMapping("/equipe")
    public List<Map<String, Object>> listTeam(HttpServletRequest request, @PathVariable String id) {
        access.requireStaff(request, id, Cargo.DONO);
        return team.list(id);
    }

    @PostMapping("/equipe")
    public ResponseEntity<Map<String, Object>> addMember(HttpServletRequest request,
            @PathVariable String id, @Valid @RequestBody TeamMemberRequest body) {
        access.requireStaff(request, id, Cargo.DONO);
        return ResponseEntity.status(HttpStatus.CREATED).body(team.add(id, body.email(), body.cargo()));
    }

    @DeleteMapping("/equipe/{userId}")
    public void removeMember(HttpServletRequest request, @PathVariable String id,
            @PathVariable String userId) {
        access.requireStaff(request, id, Cargo.DONO);
        team.remove(id, userId);
    }
}

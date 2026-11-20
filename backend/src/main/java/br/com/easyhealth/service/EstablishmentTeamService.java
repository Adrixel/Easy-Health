package br.com.easyhealth.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.easyhealth.model.Cargo;
import br.com.easyhealth.model.User;
import br.com.easyhealth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Equipe de uma clínica. O dono coloca uma conta que já existe (pelo e-mail) como médico ou
 * funcionário: a pessoa se cadastra normalmente e o dono nunca cria a senha de ninguém.
 * Quem chama já conferiu, com EstablishmentAccess, que quem pede é o dono daquela clínica.
 */
@Service
public class EstablishmentTeamService {
    private final JdbcTemplate jdbc;
    private final UserRepository users;

    public EstablishmentTeamService(JdbcTemplate jdbc, UserRepository users) {
        this.jdbc = jdbc;
        this.users = users;
    }

    public List<Map<String, Object>> list(String establishmentId) {
        return jdbc.query("""
                SELECT u.id, u.nome, u.email, s.cargo
                FROM establishment_staff s
                JOIN users u ON u.id = s.user_id
                WHERE s.establishment_id = ?
                ORDER BY s.cargo, u.nome
                """, (row, index) -> member(row.getString("id"), row.getString("nome"),
                        row.getString("email"), row.getString("cargo")), establishmentId);
    }

    @Transactional
    public Map<String, Object> add(String establishmentId, String email, String cargoText) {
        Cargo cargo = memberCargo(cargoText);
        User target = users.findByEmail(email.trim()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Nenhuma conta com esse e-mail. A pessoa precisa se cadastrar primeiro."));
        if (target.cargo() == Cargo.PACIENTE) {
            jdbc.update("UPDATE users SET cargo = ? WHERE id = ?", cargo.valor(), target.id());
        } else if (target.cargo() != cargo) {
            // Dono de outra clínica, ou médico sendo colocado como funcionário, e assim por diante.
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Essa conta já tem outro cargo.");
        }
        // Se a pessoa já está na equipe desta clínica, não faz nada (pedido repetido é inofensivo).
        jdbc.update("""
                INSERT IGNORE INTO establishment_staff (user_id, establishment_id, cargo)
                VALUES (?, ?, ?)
                """, target.id(), establishmentId, cargo.valor());
        return member(target.id(), target.nome(), target.email(), cargo.valor());
    }

    @Transactional
    public void remove(String establishmentId, String userId) {
        String cargo = jdbc.query("""
                SELECT cargo FROM establishment_staff WHERE user_id = ? AND establishment_id = ?
                """, (row, index) -> row.getString("cargo"), userId, establishmentId)
                .stream().findFirst().orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Essa pessoa não faz parte da equipe desta clínica."));
        if (Cargo.fromValor(cargo) == Cargo.DONO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O dono não pode ser removido da equipe.");
        }
        jdbc.update("DELETE FROM establishment_staff WHERE user_id = ? AND establishment_id = ?",
                userId, establishmentId);
        // Sem nenhuma clínica, a pessoa volta a ser paciente.
        Integer remaining = jdbc.queryForObject(
                "SELECT COUNT(*) FROM establishment_staff WHERE user_id = ?", Integer.class, userId);
        if (remaining != null && remaining == 0) {
            jdbc.update("UPDATE users SET cargo = 'paciente' WHERE id = ?", userId);
        }
    }

    // O dono só pode colocar médico ou funcionário; qualquer outro valor é recusado.
    private static Cargo memberCargo(String text) {
        Cargo cargo = Cargo.fromValor(text);
        if (cargo != Cargo.MEDICO && cargo != Cargo.FUNCIONARIO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O cargo deve ser 'medico' ou 'funcionario'.");
        }
        return cargo;
    }

    private static Map<String, Object> member(String id, String nome, String email, String cargo) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", id);
        view.put("nome", nome);
        view.put("email", email);
        view.put("cargo", cargo);
        return view;
    }
}

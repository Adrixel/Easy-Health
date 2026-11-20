package br.com.easyhealth.service;

import br.com.easyhealth.model.Cargo;
import br.com.easyhealth.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controle de acesso por clínica. Além do cargo da pessoa (AccessControl), exige que ela esteja
 * ligada àquela clínica específica, com o mesmo cargo, na tabela establishment_staff.
 * Assim um funcionário da clínica A não mexe nos preços da clínica B.
 */
@Service
public class EstablishmentAccess {
    private final AccessControl access;
    private final JdbcTemplate jdbc;

    public EstablishmentAccess(AccessControl access, JdbcTemplate jdbc) {
        this.access = access;
        this.jdbc = jdbc;
    }

    public User requireStaff(HttpServletRequest request, String establishmentId, Cargo... allowed) {
        // 401 sem login e 403 se o cargo da pessoa não está entre os permitidos.
        User user = access.requireCargo(request, allowed);
        Integer links = jdbc.queryForObject("""
                SELECT COUNT(*) FROM establishment_staff
                WHERE user_id = ? AND establishment_id = ? AND cargo = ?
                """, Integer.class, user.id(), establishmentId, user.cargo().valor());
        if (links == null || links == 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não tem permissão nesta clínica.");
        }
        return user;
    }
}

package br.com.easyhealth.controller;

import java.util.Map;

import br.com.easyhealth.model.Cargo;
import br.com.easyhealth.model.User;
import br.com.easyhealth.service.AccessControl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de exemplo para testar os cargos. Depois eles viram os endpoints reais da equipe
 * (agenda do estabelecimento, cadastro de funcionários e assim por diante).
 */
@RestController
@RequestMapping("/api/equipe")
public class StaffController {
    private final AccessControl access;

    public StaffController(AccessControl access) {
        this.access = access;
    }

    // Funcionário, médico e dono.
    @GetMapping("/painel")
    public Map<String, Object> panel(HttpServletRequest request) {
        User user = access.requireCargo(request, Cargo.FUNCIONARIO, Cargo.MEDICO, Cargo.DONO);
        return Map.of(
                "mensagem", "Painel da equipe",
                "nome", user.nome(),
                "cargo", user.cargo().valor());
    }

    // Só o dono.
    @GetMapping("/administracao")
    public Map<String, Object> administration(HttpServletRequest request) {
        User user = access.requireCargo(request, Cargo.DONO);
        return Map.of(
                "mensagem", "Área de administração",
                "nome", user.nome(),
                "cargo", user.cargo().valor());
    }
}

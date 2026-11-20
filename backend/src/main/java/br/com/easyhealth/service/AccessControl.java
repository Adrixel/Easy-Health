package br.com.easyhealth.service;

import java.util.Arrays;

import br.com.easyhealth.model.Cargo;
import br.com.easyhealth.model.User;
import br.com.easyhealth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controle de acesso por cargo. Um endpoint restrito chama requireCargo(request, ...) no lugar de
 * auth.requireUserId(request): sem login a resposta é 401, e com login mas sem o cargo certo é 403.
 * O cargo sempre vem do banco, nunca de algo que o app envia.
 */
@Service
public class AccessControl {
    private final AuthService auth;
    private final UserRepository users;

    public AccessControl(AuthService auth, UserRepository users) {
        this.auth = auth;
        this.users = users;
    }

    public User requireCargo(HttpServletRequest request, Cargo... allowed) {
        User user = users.findById(auth.requireUserId(request))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Autenticação necessária."));
        if (!Arrays.asList(allowed).contains(user.cargo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não tem permissão para fazer isso.");
        }
        return user;
    }
}

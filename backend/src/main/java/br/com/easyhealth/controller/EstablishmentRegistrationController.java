
package br.com.easyhealth.controller;

import java.util.Map;

import br.com.easyhealth.dto.EstablishmentCreateRequest;
import br.com.easyhealth.model.Cargo;
import br.com.easyhealth.model.User;
import br.com.easyhealth.service.AccessControl;
import br.com.easyhealth.service.EstablishmentRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EstablishmentRegistrationController {

    private final AccessControl access;
    private final EstablishmentRegistrationService registration;

    public EstablishmentRegistrationController(
            AccessControl access,
            EstablishmentRegistrationService registration) {
        this.access = access;
        this.registration = registration;
    }

    @PostMapping("/api/estabelecimentos")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody EstablishmentCreateRequest body,
            HttpServletRequest request) {

        User owner = access.requireCargo(request, Cargo.DONO);

        Map<String, Object> result = registration.register(body, owner);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}

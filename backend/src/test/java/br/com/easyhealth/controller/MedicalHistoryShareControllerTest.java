
package br.com.easyhealth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import br.com.easyhealth.dto.MedicalHistoryShareRequest;
import br.com.easyhealth.model.Cargo;
import br.com.easyhealth.model.User;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import br.com.easyhealth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MedicalHistoryShareControllerTest {

    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private AuthService auth;

    @Mock
    private HttpServletRequest request;

    private MedicalHistoryShareController controller;
    private User paciente;

    @BeforeEach
    void setUp() {
        controller = new MedicalHistoryShareController(jdbc, auth);

        paciente = new User(
                "paciente-id", "Maria Silva", "maria@example.com",
                null, null, null, null, null, null, null, null,
                false, null, null, null, Cargo.PACIENTE);
    }

    @Test
    void rejectsSharingWithOwnUserId() {
        when(auth.requireCargo(request, Cargo.PACIENTE))
                .thenReturn(paciente);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.compartilhar(
                        new MedicalHistoryShareRequest("paciente-id"),
                        request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(auth).requireCargo(request, Cargo.PACIENTE);
        verifyNoMoreInteractions(jdbc);
    }

    @Test
    void rejectsSharingWithUserThatIsNotDoctor() {
        when(auth.requireCargo(request, Cargo.PACIENTE))
                .thenReturn(paciente);

        when(jdbc.queryForList(anyString(), eq("usuario-invalido")))
                .thenReturn(List.of());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.compartilhar(
                        new MedicalHistoryShareRequest("usuario-invalido"),
                        request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(jdbc).queryForList(anyString(), eq("usuario-invalido"));
        verifyNoMoreInteractions(jdbc);
    }

    @Test
    void createsShareForValidDoctor() {
        when(auth.requireCargo(request, Cargo.PACIENTE))
                .thenReturn(paciente);

        when(jdbc.queryForList(anyString(), eq("medico-id")))
                .thenReturn(List.of(Map.of(
                        "id", "medico-id",
                        "nome", "Dra. Ana")));

        when(jdbc.update(anyString(), any(Object[].class)))
                .thenReturn(1);

        Map<String, Object> response = controller.compartilhar(
                new MedicalHistoryShareRequest("medico-id"),
                request);

        assertEquals("medico-id", response.get("medicoId"));
        assertEquals("Dra. Ana", response.get("medico"));
        assertEquals(true, response.get("active"));
        org.junit.jupiter.api.Assertions.assertNotNull(response.get("id"));

        verify(jdbc).queryForList(anyString(), eq("medico-id"));
        verify(jdbc).update(anyString(), any(Object[].class));
        verifyNoMoreInteractions(jdbc);
    }

    @Test
    void rejectsDuplicateActiveShare() {
        when(auth.requireCargo(request, Cargo.PACIENTE))
                .thenReturn(paciente);

        when(jdbc.queryForList(anyString(), eq("medico-id")))
                .thenReturn(List.of(Map.of(
                        "id", "medico-id",
                        "nome", "Dra. Ana")));

        when(jdbc.update(anyString(), any(Object[].class)))
                .thenThrow(new DuplicateKeyException("Compartilhamento duplicado"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.compartilhar(
                        new MedicalHistoryShareRequest("medico-id"),
                        request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(jdbc).queryForList(anyString(), eq("medico-id"));
        verify(jdbc).update(anyString(), any(Object[].class));
        verifyNoMoreInteractions(jdbc);
    }

        @Test
    void revokesShareSuccessfully() {
        when(auth.requireCargo(request, Cargo.PACIENTE))
                .thenReturn(paciente);

        when(jdbc.update(anyString(), any(Object[].class)))
                .thenReturn(1);

        assertDoesNotThrow(
                () -> controller.revogarCompartilhamento("compartilhamento-id", request));

        verify(auth).requireCargo(request, Cargo.PACIENTE);
        verify(jdbc).update(anyString(), any(Object[].class));
        verifyNoMoreInteractions(jdbc);
    }

    @Test
    void returnsNotFoundWhenRevokingUnknownShare() {
        when(auth.requireCargo(request, Cargo.PACIENTE))
                .thenReturn(paciente);

        when(jdbc.update(anyString(), any(Object[].class)))
                .thenReturn(0);

        when(jdbc.queryForObject(
                anyString(),
                eq(Integer.class),
                any(Object[].class)))
                .thenReturn(0);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.revogarCompartilhamento("inexistente", request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(jdbc).update(anyString(), any(Object[].class));
        verify(jdbc).queryForObject(
                anyString(), eq(Integer.class), any(Object[].class));
        verifyNoMoreInteractions(jdbc);
    }

    @Test
    void allowsRevokingAlreadyRevokedShare() {
        when(auth.requireCargo(request, Cargo.PACIENTE))
                .thenReturn(paciente);

        when(jdbc.update(anyString(), any(Object[].class)))
                .thenReturn(0);

        when(jdbc.queryForObject(
                anyString(),
                eq(Integer.class),
                any(Object[].class)))
                .thenReturn(1);

        assertDoesNotThrow(
                () -> controller.revogarCompartilhamento("ja-revogado", request));

        verify(jdbc).update(anyString(), any(Object[].class));
        verify(jdbc).queryForObject(
                anyString(), eq(Integer.class), any(Object[].class));
        verifyNoMoreInteractions(jdbc);
    }
}

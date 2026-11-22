package br.com.easyhealth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import br.com.easyhealth.model.Cargo;
import br.com.easyhealth.model.User;
import br.com.easyhealth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MedicalHistoryDoctorControllerTest {

    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private AuthService auth;

    @Mock
    private HttpServletRequest request;

    private MedicalHistoryDoctorController controller;
    private User medico;

    @BeforeEach
    void setUp() {
        controller = new MedicalHistoryDoctorController(jdbc, auth);

        medico = new User(
                "medico-id", "Dra. Ana", "ana@example.com",
                null, null, null, null, null, null, null, null,
                false, null, null, null, Cargo.MEDICO);
    }

    @Test
    void rejectsAccessWhenDoctorHasNoActiveShare() {
        when(auth.requireCargo(request, Cargo.MEDICO)).thenReturn(medico);

        when(jdbc.query(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any(),
                eq("paciente-id"),
                eq("medico-id"),
                eq("medico-id")))
                .thenReturn(List.of());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.patientRecords(
                        request, "paciente-id", null));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(auth).requireCargo(request, Cargo.MEDICO);
        verify(jdbc).query(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any(),
                eq("paciente-id"),
                eq("medico-id"),
                eq("medico-id"));
        verifyNoMoreInteractions(jdbc);
    }

    @Test
    void rejectsInvalidRecordType() {
        when(auth.requireCargo(request, Cargo.MEDICO)).thenReturn(medico);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.patientRecords(
                        request, "paciente-id", "tipo-invalido"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(auth).requireCargo(request, Cargo.MEDICO);
        verifyNoMoreInteractions(jdbc);
    }
}
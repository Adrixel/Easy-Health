package br.com.easyhealth.controller;

import br.com.easyhealth.model.Medicamento;
import br.com.easyhealth.service.MedicamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
public class MedicamentoController {
    private final MedicamentoService service;

    public MedicamentoController(MedicamentoService service) {
        this.service = service;
    }

    @GetMapping("/hoje")
    public ResponseEntity<List<Medicamento>> listarHoje() {
        return ResponseEntity.ok(service.listarDoDia());
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<Medicamento> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(service.confirmarIngestao(id));
    }
}
    

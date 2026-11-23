package br.com.easyhealth.controller;

import br.com.easyhealth.model.GuiaPsicologico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/saude-mental")
public class SaudeMentalController {

    private final Map<String, GuiaPsicologico> guias = Map.of(
        "surto-psicotico", new GuiaPsicologico(
            "surto-psicotico",
            "Surto Psicótico",
            List.of("Mantenha distância segura e afaste objetos cortantes.", "Não confronte os delírios."),
            List.of("Fale com voz calma e frases curtas.", "Acione o SAMU (192) ou a família se houver risco.")
        ),
        "ansiedade", new GuiaPsicologico(
            "ansiedade",
            "Crise de Ansiedade",
            List.of("Leve a pessoa para um ambiente calmo e arejado."),
            List.of("Guie a respiração lenta (inspire 4s, segure 4s, solte 6s).", "Ajude-a a focar em elementos ao redor.")
        ),
        "sonambulismo", new GuiaPsicologico(
            "sonambulismo",
            "Sonambulismo",
            List.of("Bloqueie escadas e tranque portas/janelas.", "Não acorde a pessoa bruscamente."),
            List.of("Conduza-a suavemente de volta para a cama falando baixo.")
        )
    );

    @GetMapping
    public ResponseEntity<List<GuiaPsicologico>> listarTodos() {
        return ResponseEntity.ok(List.copyOf(guias.values()));
    }

    @GetMapping("/{doenca}")
    public ResponseEntity<GuiaPsicologico> buscarPorDoenca(@PathVariable String doenca) {
        GuiaPsicologico guia = guias.get(doenca);
        if (guia == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(guia);
    }
}
    


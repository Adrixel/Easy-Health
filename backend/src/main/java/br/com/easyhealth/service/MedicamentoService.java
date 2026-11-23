package br.com.easyhealth.service;

import br.com.easyhealth.model.Medicamento;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicamentoService {

    // Substitua esta lista pela chamada ao seu MedicamentoRepository quando conectar ao banco
    private final List<Medicamento> medicamentos = new ArrayList<>(List.of(
        new Medicamento(1L, "Dona Maria", "Losartana 50mg", "08:00", false),
        new Medicamento(2L, "Dona Maria", "Vitamina D", "14:00", false)
    ));

    public List<Medicamento> listarDoDia() {
        return medicamentos;
    }

    public Medicamento confirmarIngestao(Long id) {
        Medicamento med = medicamentos.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Medicamento não encontrado"));

        med.setTomado(true);
        notificarFamilia(med.getNomePaciente() + " confirmou que tomou: " + med.getNomeRemedio());
        return med;
    }

    public void verificarHorariosPendentes() {
        String horaAtual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        for (Medicamento med : medicamentos) {
            if (!med.isTomado() && med.getHorario().equals(horaAtual)) {
                notificarPaciente("Hora de tomar seu remédio: " + med.getNomeRemedio());
                notificarFamilia("Lembrete enviado para " + med.getNomePaciente() + " (" + med.getNomeRemedio() + ")");
            }
        }
    }

    private void notificarPaciente(String mensagem) {
        // Integre aqui com o Firebase Cloud Messaging (FCM) ou OneSignal
        System.out.println("[PUSH PACIENTE]: " + mensagem);
    }

    private void notificarFamilia(String mensagem) {
        // Integre aqui com o Firebase Cloud Messaging (FCM) para os familiares vinculados
        System.out.println("[PUSH FAMÍLIA]: " + mensagem);
    }
}

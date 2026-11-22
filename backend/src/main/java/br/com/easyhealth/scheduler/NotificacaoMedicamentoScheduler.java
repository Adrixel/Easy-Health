package br.com.easyhealth.scheduler;

import br.com.easyhealth.service.MedicamentoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoMedicamentoScheduler {
    
    private final MedicamentoService service;

    public NotificacaoMedicamentoScheduler(MedicamentoService service) {
        this.service = service;
    }

    // Executa automaticamente a cada 60 segundos (60000 ms)
    @Scheduled(fixedRate = 60000)
    public void checarMedicamentos() {
        service.verificarHorariosPendentes();
    }
}


package br.com.easyhealth.model;

public class Medicamento {
    private Long id;
    private String nomePaciente;
    private String nomeRemedio;
    private String horario; // Formato "HH:mm", ex: "08:00"
    private boolean tomado;

    public Medicamento() {}

    public Medicamento(Long id, String nomePaciente, String nomeRemedio, String horario, boolean tomado) {
        this.id = id;
        this.nomePaciente = nomePaciente;
        this.nomeRemedio = nomeRemedio;
        this.horario = horario;
        this.tomado = tomado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomePaciente() { return nomePaciente; }
    public void setNomePaciente(String nomePaciente) { this.nomePaciente = nomePaciente; }

    public String getNomeRemedio() { return nomeRemedio; }
    public void setNomeRemedio(String nomeRemedio) { this.nomeRemedio = nomeRemedio; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public boolean isTomado() { return tomado; }
    public void setTomado(boolean tomado) { this.tomado = tomado; }
}
    


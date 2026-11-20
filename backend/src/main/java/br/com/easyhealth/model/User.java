package br.com.easyhealth.model;

import java.util.LinkedHashMap;
import java.util.Map;

public record User(
        String id,
        String nome,
        String email,
        String senhaHash,
        String telefone,
        String dataNascimento,
        String avatarUrl,
        String tipoSanguineo,
        String alergias,
        String medicamentosEmUso,
        String planoDeSaude,
        boolean emailVerificado,
        String contatoEmergenciaNome,
        String contatoEmergenciaTelefone,
        String contatoEmergenciaParentesco,
        Cargo cargo) {

    // Sem cargo informado, vale o cargo com menos poder.
    public User {
        if (cargo == null) {
            cargo = Cargo.PACIENTE;
        }
    }

    // Construtor antigo (sem cargo), mantido para não quebrar quem já chama "new User(...)" com 15 argumentos.
    public User(
            String id,
            String nome,
            String email,
            String senhaHash,
            String telefone,
            String dataNascimento,
            String avatarUrl,
            String tipoSanguineo,
            String alergias,
            String medicamentosEmUso,
            String planoDeSaude,
            boolean emailVerificado,
            String contatoEmergenciaNome,
            String contatoEmergenciaTelefone,
            String contatoEmergenciaParentesco) {
        this(id, nome, email, senhaHash, telefone, dataNascimento, avatarUrl, tipoSanguineo,
                alergias, medicamentosEmUso, planoDeSaude, emailVerificado,
                contatoEmergenciaNome, contatoEmergenciaTelefone, contatoEmergenciaParentesco,
                Cargo.PACIENTE);
    }

    public Map<String, Object> publicView() {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", id);
        view.put("nome", nome);
        view.put("email", email);
        view.put("telefone", telefone);
        view.put("dataNascimento", dataNascimento);
        view.put("avatarUrl", avatarUrl);
        view.put("tipoSanguineo", tipoSanguineo);
        view.put("alergias", alergias);
        view.put("medicamentosEmUso", medicamentosEmUso);
        view.put("planoDeSaude", planoDeSaude);
        view.put("emailVerificado", emailVerificado);
        view.put("contatoEmergenciaNome", contatoEmergenciaNome);
        view.put("contatoEmergenciaTelefone", contatoEmergenciaTelefone);
        view.put("contatoEmergenciaParentesco", contatoEmergenciaParentesco);
        view.put("cargo", cargo.valor());
        return view;
    }
}

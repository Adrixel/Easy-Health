package br.com.easyhealth.model;

/**
 * Cargos de quem usa o app. O valor em minúsculas é o que fica gravado na coluna
 * "cargo" da tabela users e o que o app recebe no JSON.
 */
public enum Cargo {
    PACIENTE("paciente"),
    FUNCIONARIO("funcionario"),
    MEDICO("medico"),
    DONO("dono");

    private final String valor;

    Cargo(String valor) {
        this.valor = valor;
    }

    public String valor() {
        return valor;
    }

    /** Converte o texto do banco; qualquer valor desconhecido vira PACIENTE (o cargo com menos poder). */
    public static Cargo fromValor(String valor) {
        for (Cargo cargo : values()) {
            if (cargo.valor.equalsIgnoreCase(valor)) {
                return cargo;
            }
        }
        return PACIENTE;
    }
}

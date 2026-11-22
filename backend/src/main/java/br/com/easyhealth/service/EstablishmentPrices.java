package br.com.easyhealth.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

/** Preços de cada clínica (tabela establishment_prices). Segue o estilo do AppointmentAvailability. */
public final class EstablishmentPrices {
    private EstablishmentPrices() {
    }

    /** Lista os preços gravados; devolve lista vazia se a clínica ainda não tem nenhum. */
    public static List<Map<String, Object>> list(JdbcTemplate jdbc, String establishmentId) {
        return jdbc.query("""
                SELECT servico, valor_centavos FROM establishment_prices
                WHERE establishment_id = ? ORDER BY servico
                """, (row, index) -> {
                    Map<String, Object> price = new LinkedHashMap<>();
                    price.put("servico", row.getString("servico"));
                    price.put("valor", reais(row.getInt("valor_centavos")));
                    return price;
                }, establishmentId);
    }

    /** Insere ou atualiza o preço de um serviço da clínica. */
    public static void save(JdbcTemplate jdbc, String establishmentId, String servico,
            BigDecimal valor, String userId) {
        int centavos = valor.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValueExact();
        jdbc.update("""
                INSERT INTO establishment_prices (establishment_id, servico, valor_centavos, updated_by)
                VALUES (?, ?, ?, ?) AS novo
                ON DUPLICATE KEY UPDATE
                    valor_centavos = novo.valor_centavos,
                    updated_by = novo.updated_by
                """, establishmentId, servico.trim(), centavos, userId);
    }

    // 28000 -> "R$ 280" (igual ao valor fixo de antes); 15050 -> "R$ 150,50".
    static String reais(int centavos) {
        int inteiro = centavos / 100;
        int resto = centavos % 100;
        return resto == 0
                ? "R$ " + inteiro
                : String.format(Locale.ROOT, "R$ %d,%02d", inteiro, resto);
    }
}

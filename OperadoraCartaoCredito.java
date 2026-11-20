package br.com.minharefeicao.servico;

import br.com.minharefeicao.entidade.CartaoCredito;

import java.time.YearMonth;

/**
 * Ator secundário (simulado). Regras de autorização usadas nos testes:
 * número com 16 dígitos, validade MM/AA futura, e cartões terminados em "0000" são sempre recusados.
 */
public class OperadoraCartaoCredito {

    /** Mensagem do diagrama de sequência: autorizar(cartao, valor). */
    public boolean autorizar(CartaoCredito cartao, double valor) {
        if (valor <= 0 || cartao.getNumero().length() != 16 || cartao.getNumero().endsWith("0000")) {
            return false;
        }
        return validadeFutura(cartao.getValidade());
    }

    private boolean validadeFutura(String validade) {
        try {
            String[] partes = validade.trim().split("/");
            int mes = Integer.parseInt(partes[0]);
            int ano = 2000 + Integer.parseInt(partes[1]);
            return !YearMonth.of(ano, mes).isBefore(YearMonth.now());
        } catch (RuntimeException e) {
            return false;
        }
    }
}

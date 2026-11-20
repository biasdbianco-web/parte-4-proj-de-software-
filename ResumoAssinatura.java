package br.com.minharefeicao.entidade;

import java.time.LocalDate;

/** Dados exibidos na confirmação (Tela 11). */
public record ResumoAssinatura(
        String plano,
        int quantidadeRefeicoes,
        String periodicidade,
        String endereco,
        LocalDate previsaoPrimeiraEntrega,
        String numeroProtocolo,
        double valor) {
}

package br.com.minharefeicao.entidade;

import java.time.LocalDate;

/** Entidade de domínio: registro da cobrança da assinatura. */
public class Pagamento {

    private final double valor;
    private final CartaoCredito cartao;
    private String status = "PENDENTE";
    private LocalDate dataPagamento;

    public Pagamento(double valor, CartaoCredito cartao) {
        this.valor = valor;
        this.cartao = cartao;
    }

    /** Mensagem do diagrama de sequência: registrar() — chamada após a autorização. */
    public void registrar() {
        this.status = "APROVADO";
        this.dataPagamento = LocalDate.now();
    }

    public double getValor() { return valor; }
    public CartaoCredito getCartao() { return cartao; }
    public String getStatus() { return status; }
    public LocalDate getDataPagamento() { return dataPagamento; }
}

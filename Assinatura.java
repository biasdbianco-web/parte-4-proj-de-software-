package br.com.minharefeicao.entidade;

import br.com.minharefeicao.excecao.QuantidadeExcedidaException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/** Entidade de domínio central: contrato entre o assinante e um plano. Agrega Pedido, Endereço e Pagamento. */
public class Assinatura {

    private int id;
    private final Assinante assinante;
    private final PlanoAssinatura plano;
    private final Pedido pedido = new Pedido();
    private EnderecoEntrega endereco;
    private Pagamento pagamento;
    private StatusAssinatura status = StatusAssinatura.EM_CRIACAO;
    private String numeroProtocolo;
    private LocalDate dataContratacao;
    private LocalDate previsaoPrimeiraEntrega;

    public Assinatura(Assinante assinante, PlanoAssinatura plano) {
        this.assinante = assinante;
        this.plano = plano;
    }

    /**
     * Mensagem do diagrama de sequência: adicionarRefeicoes(pratos).
     * A cota do plano conta os pratos PRINCIPAL; a operação é atômica (FA2): se estourar, nada é adicionado.
     */
    public void adicionarRefeicoes(List<Prato> pratos) throws QuantidadeExcedidaException {
        int novosPrincipais = 0;
        for (Prato p : pratos) {
            if (p.getCategoria() == CategoriaPrato.PRINCIPAL) {
                novosPrincipais++;
            }
        }
        int limite = plano.getQuantidadeRefeicoes();
        int total = pedido.getQuantidadePorCategoria(CategoriaPrato.PRINCIPAL) + novosPrincipais;
        if (total > limite) {
            throw new QuantidadeExcedidaException(limite, total);
        }
        for (Prato p : pratos) {
            pedido.adicionarItem(p, 1);
        }
    }

    public void definirEndereco(EnderecoEntrega endereco) { this.endereco = endereco; }

    public void alterarStatus(StatusAssinatura novo) { this.status = novo; }

    public double calcularValorTotal() { return plano.getValor(); }

    /** Ativa a assinatura após o pagamento aprovado; entregas às segundas-feiras. */
    public void ativar(Pagamento pagamentoAprovado) {
        this.pagamento = pagamentoAprovado;
        this.pedido.aprovar();
        this.status = StatusAssinatura.ATIVA;
        this.dataContratacao = LocalDate.now();
        this.previsaoPrimeiraEntrega = dataContratacao.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    public void definirProtocolo(String numero) { this.numeroProtocolo = numero; }

    public void encerrarSemAtivacao() { this.status = StatusAssinatura.ENCERRADA; }

    public ResumoAssinatura getResumoConfirmacao() {
        return new ResumoAssinatura(plano.getTipo(), plano.getQuantidadeRefeicoes(), plano.getPeriodicidade(),
                String.valueOf(endereco), previsaoPrimeiraEntrega, numeroProtocolo, plano.getValor());
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Assinante getAssinante() { return assinante; }
    public PlanoAssinatura getPlano() { return plano; }
    public Pedido getPedido() { return pedido; }
    public EnderecoEntrega getEndereco() { return endereco; }
    public Pagamento getPagamento() { return pagamento; }
    public StatusAssinatura getStatus() { return status; }
    public String getNumeroProtocolo() { return numeroProtocolo; }
    public LocalDate getDataContratacao() { return dataContratacao; }
    public LocalDate getPrevisaoPrimeiraEntrega() { return previsaoPrimeiraEntrega; }
}

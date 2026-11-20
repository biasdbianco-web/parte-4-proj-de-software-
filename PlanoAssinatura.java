package br.com.minharefeicao.entidade;

/** Entidade de domínio: oferta comercial contratável. */
public class PlanoAssinatura {

    private final int id;
    private final String tipo;
    private final int quantidadeRefeicoes;
    private final String periodicidade;
    private final double valor;

    public PlanoAssinatura(int id, String tipo, int quantidadeRefeicoes, String periodicidade, double valor) {
        this.id = id;
        this.tipo = tipo;
        this.quantidadeRefeicoes = quantidadeRefeicoes;
        this.periodicidade = periodicidade;
        this.valor = valor;
    }

    public int getId() { return id; }
    public String getTipo() { return tipo; }
    public int getQuantidadeRefeicoes() { return quantidadeRefeicoes; }
    public String getPeriodicidade() { return periodicidade; }
    public double getValor() { return valor; }
}

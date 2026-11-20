package br.com.minharefeicao.excecao;

/** Sinaliza o Fluxo Alternativo FA2: quantidade de refeições acima do limite do plano. */
public class QuantidadeExcedidaException extends Exception {

    private final int limite;
    private final int atual;

    public QuantidadeExcedidaException(int limite, int atual) {
        super("O plano permite " + limite + " refeição(ões) e a seleção resultaria em " + atual + ".");
        this.limite = limite;
        this.atual = atual;
    }

    public int getLimite() { return limite; }
    public int getAtual() { return atual; }
}

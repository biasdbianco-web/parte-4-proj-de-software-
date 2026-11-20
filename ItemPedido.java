package br.com.minharefeicao.entidade;

/** Classe associativa entre Pedido e Prato (carrega a quantidade escolhida). */
public class ItemPedido {

    private final Prato prato;
    private int quantidade;

    public ItemPedido(Prato prato, int quantidade) {
        this.prato = prato;
        this.quantidade = quantidade;
    }

    public void somar(int qtd) { this.quantidade += qtd; }

    public Prato getPrato() { return prato; }
    public int getQuantidade() { return quantidade; }
}

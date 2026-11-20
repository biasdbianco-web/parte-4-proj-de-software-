package br.com.minharefeicao.entidade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Entidade de domínio: composição de refeições escolhidas dentro de uma assinatura. */
public class Pedido {

    private String status = "EM_COMPOSICAO";
    private final LocalDate dataCriacao = LocalDate.now();
    private final List<ItemPedido> itens = new ArrayList<>();

    /** Mensagem do diagrama de sequência: adicionarItem(prato, quantidade). */
    public void adicionarItem(Prato prato, int quantidade) {
        for (ItemPedido item : itens) {
            if (item.getPrato().getId() == prato.getId()) {
                item.somar(quantidade);
                return;
            }
        }
        itens.add(new ItemPedido(prato, quantidade));
    }

    /** Mensagem do diagrama de sequência: getComposicao(). */
    public List<ItemPedido> getComposicao() {
        return new ArrayList<>(itens);
    }

    /** Total de unidades de todas as categorias. */
    public int getQuantidadeTotal() {
        int total = 0;
        for (ItemPedido i : itens) {
            total += i.getQuantidade();
        }
        return total;
    }

    /** Unidades de uma categoria; a cota do plano conta apenas os pratos PRINCIPAL. */
    public int getQuantidadePorCategoria(CategoriaPrato categoria) {
        int total = 0;
        for (ItemPedido i : itens) {
            if (i.getPrato().getCategoria() == categoria) {
                total += i.getQuantidade();
            }
        }
        return total;
    }

    public void aprovar() { this.status = "APROVADO"; }

    public String getStatus() { return status; }
    public LocalDate getDataCriacao() { return dataCriacao; }
}

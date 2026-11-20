package br.com.minharefeicao.catalogo;

import br.com.minharefeicao.entidade.Assinatura;
import br.com.minharefeicao.entidade.ItemPedido;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Persistência de assinaturas em data/assinaturas.csv e dos itens do pedido em data/itens_pedido.csv. */
public class RepositorioAssinaturas {

    private static final String CAB_ASSINATURAS =
            "id;celular;plano;status;protocolo;dataContratacao;previsaoEntrega;endereco;valor;cartao";
    private static final String CAB_ITENS = "idAssinatura;idPrato;prato;categoria;quantidade";

    private final Path arquivoAssinaturas;
    private final Path arquivoItens;

    public RepositorioAssinaturas(Path arquivoAssinaturas, Path arquivoItens) {
        this.arquivoAssinaturas = arquivoAssinaturas;
        this.arquivoItens = arquivoItens;
        CsvUtil.garantirArquivo(arquivoAssinaturas, CAB_ASSINATURAS);
        CsvUtil.garantirArquivo(arquivoItens, CAB_ITENS);
    }

    /** Grava a assinatura (ATIVA ou ENCERRADA) e os itens do pedido; atribui o id sequencial. */
    public void salvar(Assinatura a) {
        int id = CsvUtil.ler(arquivoAssinaturas).size() + 1;
        a.setId(id);
        String cartao = a.getPagamento() == null ? "" : a.getPagamento().getCartao().getNumeroMascarado();
        String valor = a.getPagamento() == null ? "0.00"
                : String.format(Locale.US, "%.2f", a.getPagamento().getValor());
        String linha = String.join(CsvUtil.SEP,
                String.valueOf(id),
                a.getAssinante().getCelular(),
                a.getPlano().getTipo(),
                a.getStatus().name(),
                a.getNumeroProtocolo() == null ? "" : a.getNumeroProtocolo(),
                a.getDataContratacao() == null ? "" : a.getDataContratacao().toString(),
                a.getPrevisaoPrimeiraEntrega() == null ? "" : a.getPrevisaoPrimeiraEntrega().toString(),
                CsvUtil.limpar(String.valueOf(a.getEndereco())),
                valor,
                cartao);
        CsvUtil.acrescentar(arquivoAssinaturas, List.of(linha));

        List<String> itens = new ArrayList<>();
        for (ItemPedido i : a.getPedido().getComposicao()) {
            itens.add(String.join(CsvUtil.SEP, String.valueOf(id), String.valueOf(i.getPrato().getId()),
                    CsvUtil.limpar(i.getPrato().getNome()), i.getPrato().getCategoria().name(),
                    String.valueOf(i.getQuantidade())));
        }
        if (!itens.isEmpty()) {
            CsvUtil.acrescentar(arquivoItens, itens);
        }
    }

    /** Linhas legíveis das assinaturas gravadas, para a opção de consulta do menu. */
    public List<String> listarResumos() {
        List<String> resumos = new ArrayList<>();
        for (String[] c : CsvUtil.ler(arquivoAssinaturas)) {
            resumos.add(String.format("#%s | celular %s | plano %s | %s | protocolo %s | entrega prevista %s | R$ %s",
                    c[0], c[1], c[2], c[3], c[4].isEmpty() ? "-" : c[4], c[6].isEmpty() ? "-" : c[6], c[8]));
        }
        return resumos;
    }
}

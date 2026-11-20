package br.com.minharefeicao.controle;

import br.com.minharefeicao.catalogo.Cardapio;
import br.com.minharefeicao.catalogo.CatalogoAssinantes;
import br.com.minharefeicao.catalogo.CatalogoPlanos;
import br.com.minharefeicao.catalogo.RepositorioAssinaturas;
import br.com.minharefeicao.entidade.Assinante;
import br.com.minharefeicao.entidade.Assinatura;
import br.com.minharefeicao.entidade.CartaoCredito;
import br.com.minharefeicao.entidade.CategoriaPrato;
import br.com.minharefeicao.entidade.EnderecoEntrega;
import br.com.minharefeicao.entidade.ItemPedido;
import br.com.minharefeicao.entidade.Pagamento;
import br.com.minharefeicao.entidade.PlanoAssinatura;
import br.com.minharefeicao.entidade.PreferenciaAlimentar;
import br.com.minharefeicao.entidade.Prato;
import br.com.minharefeicao.entidade.ResumoAssinatura;
import br.com.minharefeicao.entidade.StatusAssinatura;
import br.com.minharefeicao.excecao.QuantidadeExcedidaException;
import br.com.minharefeicao.servico.GeradorProtocolo;
import br.com.minharefeicao.servico.OperadoraCartaoCredito;
import br.com.minharefeicao.servico.ServicoSMS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Objeto de controle (GRASP Controller): recebe os estímulos do Assinante vindos da fronteira
 * ({@code TelaAssinatura}) e coordena os objetos de domínio, os catálogos e os serviços,
 * exatamente como nos diagramas de sequência.
 */
public class AssinaturaController {

    public enum ResultadoCodigo { VALIDO, INVALIDO, TENTATIVAS_ESGOTADAS }

    private static final int MAX_TENTATIVAS = 3;

    private final CatalogoAssinantes catalogoAssinantes;
    private final CatalogoPlanos catalogoPlanos;
    private final Cardapio cardapio;
    private final ServicoSMS servicoSMS;
    private final GeradorProtocolo geradorProtocolo;
    private final OperadoraCartaoCredito operadora;
    private final RepositorioAssinaturas repositorio;

    private String celular;
    private Assinante assinante;
    private Assinatura assinaturaAtual;
    private boolean codigoValidado;
    private int tentativas;

    public AssinaturaController(CatalogoAssinantes catalogoAssinantes, CatalogoPlanos catalogoPlanos,
                                Cardapio cardapio, ServicoSMS servicoSMS, GeradorProtocolo geradorProtocolo,
                                OperadoraCartaoCredito operadora, RepositorioAssinaturas repositorio) {
        this.catalogoAssinantes = catalogoAssinantes;
        this.catalogoPlanos = catalogoPlanos;
        this.cardapio = cardapio;
        this.servicoSMS = servicoSMS;
        this.geradorProtocolo = geradorProtocolo;
        this.operadora = operadora;
        this.repositorio = repositorio;
    }

    // ---------------- Passos 1-4: identificação (Telas 1 e 2) ----------------

    /** Estímulo: informarCelular(celular). Cria/recupera o assinante e envia o código por SMS. */
    public void informarCelular(String celularInformado) {
        String limpo = celularInformado == null ? "" : celularInformado.replaceAll("\\D", "");
        if (limpo.length() < 10 || limpo.length() > 11) {
            throw new IllegalArgumentException("Celular inválido. Informe DDD + número (10 ou 11 dígitos).");
        }
        this.celular = limpo;
        this.assinante = catalogoAssinantes.buscarOuCriar(limpo);
        this.codigoValidado = false;
        this.tentativas = 0;
        servicoSMS.enviarCodigo(limpo);
    }

    /** Estímulo: informarCodigo(codigo). Implementa também o FA1 (3 tentativas inválidas). */
    public ResultadoCodigo informarCodigo(String codigo) {
        exigir(celular != null, "Informe o celular antes do código.");
        if (servicoSMS.validarCodigo(celular, codigo == null ? "" : codigo.trim())) {
            codigoValidado = true;
            return ResultadoCodigo.VALIDO;
        }
        tentativas++;
        if (tentativas >= MAX_TENTATIVAS) {
            servicoSMS.invalidarCodigo(celular);
            tentativas = 0;
            return ResultadoCodigo.TENTATIVAS_ESGOTADAS;
        }
        return ResultadoCodigo.INVALIDO;
    }

    public int getTentativasRestantes() {
        return MAX_TENTATIVAS - tentativas;
    }

    // ---------------- Passos 5-9: plano e preferências (Telas 3 e 4) ----------------

    public List<PlanoAssinatura> listarPlanos() {
        exigir(codigoValidado, "Valide o código de confirmação antes de continuar.");
        return catalogoPlanos.listarDisponiveis();
    }

    /** Estímulo: selecionarPlano(idPlano). Cria a Assinatura (que cria o Pedido). */
    public PlanoAssinatura selecionarPlano(int idPlano) {
        exigir(codigoValidado, "Valide o código de confirmação antes de continuar.");
        PlanoAssinatura plano = catalogoPlanos.buscarPorId(idPlano);
        this.assinaturaAtual = new Assinatura(assinante, plano);
        return plano;
    }

    /** Estímulo: informarPreferencias(preferencias). Lista vazia = sem restrição. */
    public void informarPreferencias(List<String> preferencias) {
        exigir(assinaturaAtual != null, "Selecione um plano antes das preferências.");
        List<String> validas = Arrays.asList(PreferenciaAlimentar.OPCOES);
        for (String p : preferencias) {
            if (!validas.contains(p)) {
                throw new IllegalArgumentException("Preferência inválida: " + p);
            }
        }
        assinante.definirPreferencias(preferencias);
        catalogoAssinantes.salvar();
    }

    // ---------------- Passos 10-19: montagem do pedido (Telas 5, 6 e 7) ----------------

    public List<Prato> listarPratos(CategoriaPrato categoria) {
        exigir(assinaturaAtual != null, "Selecione um plano antes do cardápio.");
        return cardapio.listarPratos(categoria, assinante.getTiposPreferencias());
    }

    /** Estímulo: selecionarPratos(idsPratos). Pode lançar QuantidadeExcedidaException (FA2). */
    public void selecionarPratos(List<Integer> ids) throws QuantidadeExcedidaException {
        adicionar(CategoriaPrato.PRINCIPAL, ids);
    }

    /** Estímulo: selecionarAcompanhamentos(ids). */
    public void selecionarAcompanhamentos(List<Integer> ids) throws QuantidadeExcedidaException {
        adicionar(CategoriaPrato.ACOMPANHAMENTO, ids);
    }

    /** Estímulo: selecionarSobremesas(ids). */
    public void selecionarSobremesas(List<Integer> ids) throws QuantidadeExcedidaException {
        adicionar(CategoriaPrato.SOBREMESA, ids);
    }

    private void adicionar(CategoriaPrato categoria, List<Integer> ids) throws QuantidadeExcedidaException {
        List<Prato> disponiveis = listarPratos(categoria);
        List<Prato> escolhidos = new ArrayList<>();
        for (int id : ids) {
            Prato achado = null;
            for (Prato p : disponiveis) {
                if (p.getId() == id) {
                    achado = p;
                }
            }
            if (achado == null) {
                throw new IllegalArgumentException("Prato " + id + " não está disponível nesta lista.");
            }
            escolhidos.add(achado);
        }
        assinaturaAtual.adicionarRefeicoes(escolhidos);
    }

    public int getLimiteRefeicoes() {
        return assinaturaAtual.getPlano().getQuantidadeRefeicoes();
    }

    public int getQuantidadePrincipaisEscolhidos() {
        return assinaturaAtual.getPedido().getQuantidadePorCategoria(CategoriaPrato.PRINCIPAL);
    }

    /** Estímulo: consultarResumoPedido(). */
    public List<ItemPedido> consultarResumoPedido() {
        exigir(assinaturaAtual != null, "Não há pedido em andamento.");
        return assinaturaAtual.getPedido().getComposicao();
    }

    // ---------------- Passos 20-28: entrega e pagamento (Telas 8, 9 e 10) ----------------

    /** Estímulo: informarEndereco(endereco). Retorna o valor total a pagar. */
    public double informarEndereco(EnderecoEntrega endereco) {
        exigir(assinaturaAtual != null, "Não há pedido em andamento.");
        assinaturaAtual.definirEndereco(endereco);
        assinaturaAtual.alterarStatus(StatusAssinatura.AGUARDANDO_PAGAMENTO);
        return assinaturaAtual.calcularValorTotal();
    }

    /**
     * Estímulo: informarDadosPagamento(cartao). Se a operadora autorizar (passos 26-32): registra o pagamento,
     * ativa a assinatura, gera o protocolo e persiste. Se recusar (FA3): retorna false e a assinatura
     * permanece AGUARDANDO_PAGAMENTO.
     */
    public boolean informarDadosPagamento(CartaoCredito cartao) {
        exigir(assinaturaAtual != null && assinaturaAtual.getStatus() == StatusAssinatura.AGUARDANDO_PAGAMENTO,
                "A assinatura não está aguardando pagamento.");
        double valor = assinaturaAtual.calcularValorTotal();
        Pagamento pagamento = new Pagamento(valor, cartao);
        if (!operadora.autorizar(cartao, valor)) {
            return false;
        }
        pagamento.registrar();
        assinaturaAtual.ativar(pagamento);
        assinaturaAtual.definirProtocolo(geradorProtocolo.gerar());
        repositorio.salvar(assinaturaAtual);
        return true;
    }

    /** Passos 29-32: dados exibidos na confirmação. */
    public ResumoAssinatura obterResumoConfirmacao() {
        exigir(assinaturaAtual != null && assinaturaAtual.getStatus() == StatusAssinatura.ATIVA,
                "A assinatura ainda não está ativa.");
        return assinaturaAtual.getResumoConfirmacao();
    }

    /** FA3: o assinante desiste; a assinatura é encerrada sem ativação (e registrada como ENCERRADA). */
    public void cancelarAssinatura() {
        exigir(assinaturaAtual != null, "Não há assinatura em andamento.");
        assinaturaAtual.encerrarSemAtivacao();
        repositorio.salvar(assinaturaAtual);
        assinaturaAtual = null;
    }

    public List<String> listarAssinaturasGravadas() {
        return repositorio.listarResumos();
    }

    private void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new IllegalStateException(mensagem);
        }
    }
}

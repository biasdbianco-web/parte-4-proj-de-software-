package br.com.minharefeicao.fronteira;

import br.com.minharefeicao.controle.AssinaturaController;
import br.com.minharefeicao.controle.AssinaturaController.ResultadoCodigo;
import br.com.minharefeicao.entidade.CartaoCredito;
import br.com.minharefeicao.entidade.CategoriaPrato;
import br.com.minharefeicao.entidade.EnderecoEntrega;
import br.com.minharefeicao.entidade.ItemPedido;
import br.com.minharefeicao.entidade.PlanoAssinatura;
import br.com.minharefeicao.entidade.PreferenciaAlimentar;
import br.com.minharefeicao.entidade.Prato;
import br.com.minharefeicao.entidade.ResumoAssinatura;
import br.com.minharefeicao.excecao.QuantidadeExcedidaException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Objeto de fronteira (interface com o Assinante, em console). Cada método "tela" corresponde a uma
 * tela do protótipo (Telas 1 a 11) e traduz a interação em estímulos enviados ao {@link AssinaturaController}.
 * Os fluxos alternativos FA1, FA2 e FA3 são tratados aqui a partir das respostas do controlador.
 */
public class TelaAssinatura {

    /** Lançada quando a entrada de dados termina (fim do arquivo/CTRL+D). */
    public static class EntradaEncerradaException extends RuntimeException {
        public EntradaEncerradaException() {
            super("Entrada encerrada.");
        }
    }

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final AssinaturaController controller;
    private final BufferedReader entrada;
    private final PrintStream saida;

    public TelaAssinatura(AssinaturaController controller, BufferedReader entrada, PrintStream saida) {
        this.controller = controller;
        this.entrada = entrada;
        this.saida = saida;
    }

    // ------------------------------------------------------------------ menu

    public void iniciar() {
        try {
            while (true) {
                saida.println();
                saida.println("================ MINHA REFEIÇÃO ================");
                saida.println("1 - Assinar plano de refeições");
                saida.println("2 - Ver assinaturas gravadas");
                saida.println("0 - Sair");
                String opcao = perguntar("Opção");
                switch (opcao) {
                    case "1" -> executarAssinatura();
                    case "2" -> listarGravadas();
                    case "0" -> {
                        saida.println("Até logo!");
                        return;
                    }
                    default -> saida.println("Opção inválida.");
                }
            }
        } catch (EntradaEncerradaException e) {
            saida.println();
            saida.println("(fim da entrada de dados)");
        }
    }

    private void listarGravadas() {
        saida.println();
        saida.println("--- Assinaturas gravadas (data/assinaturas.csv) ---");
        List<String> resumos = controller.listarAssinaturasGravadas();
        if (resumos.isEmpty()) {
            saida.println("Nenhuma assinatura gravada ainda.");
        }
        resumos.forEach(saida::println);
    }

    // ------------------------------------------------------- caso de uso

    /** Executa o cenário "Assinar Plano de Refeições" (fluxo principal + alternativos). */
    private void executarAssinatura() {
        String celular = telaInformarCelular();       // Tela 1
        telaConfirmarCodigo(celular);                 // Tela 2 (FA1)
        telaEscolherPlano();                          // Tela 3
        telaPreferencias();                           // Tela 4
        telaPratosPrincipais();                       // Tela 5 (FA2)
        telaAcompanhamentosESobremesas();             // Tela 6
        telaResumoPedido();                           // Tela 7
        double valor = telaEndereco();                // Tela 8
        telaPagamento(valor);                         // Telas 9, 10 e 11 (FA3)
    }

    // ---------------------------------------------------------------- Tela 1

    private String telaInformarCelular() {
        cabecalho("Tela 1 - Informar celular");
        while (true) {
            String celular = perguntar("Informe seu celular com DDD");
            try {
                controller.informarCelular(celular);
                return celular.replaceAll("\\D", "");
            } catch (IllegalArgumentException e) {
                saida.println("! " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------- Tela 2 (FA1)

    private void telaConfirmarCodigo(String celular) {
        cabecalho("Tela 2 - Confirmar código SMS");
        while (true) {
            String codigo = perguntar("Digite o código recebido");
            ResultadoCodigo r = controller.informarCodigo(codigo);
            if (r == ResultadoCodigo.VALIDO) {
                saida.println("Código validado.");
                return;
            }
            if (r == ResultadoCodigo.INVALIDO) {
                saida.println("! Código inválido. Tentativas restantes: " + controller.getTentativasRestantes());
            } else {
                saida.println("! Três tentativas inválidas. O código foi cancelado; enviando um novo código...");
                controller.informarCelular(celular);
            }
        }
    }

    // ---------------------------------------------------------------- Tela 3

    private void telaEscolherPlano() {
        cabecalho("Tela 3 - Escolher plano");
        List<PlanoAssinatura> planos = controller.listarPlanos();
        for (PlanoAssinatura p : planos) {
            saida.printf(PT_BR, "%d - %-15s %2d refeições | entrega %-9s | %s%n", p.getId(), p.getTipo(),
                    p.getQuantidadeRefeicoes(), p.getPeriodicidade(), moeda(p.getValor()));
        }
        while (true) {
            try {
                PlanoAssinatura escolhido = controller.selecionarPlano(lerInteiro("Número do plano"));
                saida.println("Plano selecionado: " + escolhido.getTipo());
                return;
            } catch (IllegalArgumentException e) {
                saida.println("! " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------- Tela 4

    private void telaPreferencias() {
        cabecalho("Tela 4 - Preferências alimentares");
        for (int i = 0; i < PreferenciaAlimentar.OPCOES.length; i++) {
            saida.println((i + 1) + " - " + PreferenciaAlimentar.OPCOES[i]);
        }
        while (true) {
            String linha = perguntar("Números separados por vírgula (ENTER = sem restrição)");
            List<String> escolhidas = new ArrayList<>();
            try {
                for (int n : parseInteiros(linha)) {
                    if (n < 1 || n > PreferenciaAlimentar.OPCOES.length) {
                        throw new IllegalArgumentException("Opção inexistente: " + n);
                    }
                    escolhidas.add(PreferenciaAlimentar.OPCOES[n - 1]);
                }
                controller.informarPreferencias(escolhidas);
                saida.println("Preferências salvas: " + (escolhidas.isEmpty() ? "nenhuma" : escolhidas));
                return;
            } catch (IllegalArgumentException e) {
                saida.println("! " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------- Tela 5 (FA2)

    private void telaPratosPrincipais() {
        cabecalho("Tela 5 - Pratos principais");
        mostrarPratos(controller.listarPratos(CategoriaPrato.PRINCIPAL));
        saida.println("Seu plano permite até " + controller.getLimiteRefeicoes() + " pratos principais.");
        while (true) {
            try {
                List<Integer> ids = parseInteiros(perguntar("Números dos pratos, separados por vírgula (repita para mais de uma unidade)"));
                if (ids.isEmpty()) {
                    saida.println("! Escolha ao menos um prato principal.");
                    continue;
                }
                controller.selecionarPratos(ids);
                saida.println("Pratos adicionados (" + controller.getQuantidadePrincipaisEscolhidos() + "/"
                        + controller.getLimiteRefeicoes() + ").");
                return;
            } catch (QuantidadeExcedidaException e) {
                // FA2: informa o limite e solicita ajuste da seleção
                saida.println("! " + e.getMessage() + " Ajuste a seleção.");
            } catch (IllegalArgumentException e) {
                saida.println("! " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------- Tela 6

    private void telaAcompanhamentosESobremesas() {
        cabecalho("Tela 6 - Acompanhamentos e sobremesas");
        saida.println("Acompanhamentos:");
        mostrarPratos(controller.listarPratos(CategoriaPrato.ACOMPANHAMENTO));
        escolherOpcionais(CategoriaPrato.ACOMPANHAMENTO);
        saida.println("Sobremesas:");
        mostrarPratos(controller.listarPratos(CategoriaPrato.SOBREMESA));
        escolherOpcionais(CategoriaPrato.SOBREMESA);
    }

    private void escolherOpcionais(CategoriaPrato categoria) {
        while (true) {
            try {
                List<Integer> ids = parseInteiros(perguntar("Números (ENTER = nenhum)"));
                if (!ids.isEmpty()) {
                    if (categoria == CategoriaPrato.ACOMPANHAMENTO) {
                        controller.selecionarAcompanhamentos(ids);
                    } else {
                        controller.selecionarSobremesas(ids);
                    }
                }
                return;
            } catch (QuantidadeExcedidaException | IllegalArgumentException e) {
                saida.println("! " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------- Tela 7

    private void telaResumoPedido() {
        cabecalho("Tela 7 - Resumo do pedido");
        for (ItemPedido i : controller.consultarResumoPedido()) {
            saida.printf("%dx %s (%s)%n", i.getQuantidade(), i.getPrato().getNome(),
                    i.getPrato().getCategoria().getRotulo());
        }
        perguntar("Pressione ENTER para escolher o endereço de entrega");
    }

    // ---------------------------------------------------------------- Tela 8

    private double telaEndereco() {
        cabecalho("Tela 8 - Endereço de entrega");
        while (true) {
            String rua = perguntar("Rua");
            String numero = perguntar("Número");
            String complemento = perguntar("Complemento (ENTER = nenhum)");
            String cidade = perguntar("Cidade/UF");
            String cep = perguntar("CEP");
            if (rua.isBlank() || numero.isBlank() || cidade.isBlank() || cep.isBlank()) {
                saida.println("! Rua, número, cidade e CEP são obrigatórios.");
                continue;
            }
            double valor = controller.informarEndereco(new EnderecoEntrega(rua, numero, complemento, cidade, cep));
            saida.println("Endereço confirmado. Valor total da assinatura: " + moeda(valor));
            return valor;
        }
    }

    // ---------------------------------------------------------------- Telas 9, 10 e 11 (FA3)

    private void telaPagamento(double valor) {
        while (true) {
            cabecalho("Tela 9 - Pagamento (" + moeda(valor) + ")");
            String numero = perguntar("Número do cartão");
            String validade = perguntar("Validade (MM/AA)");
            String nome = perguntar("Nome impresso no cartão");

            cabecalho("Tela 10 - Aguardando autorização da operadora");
            saida.println("Status da assinatura: AGUARDANDO_PAGAMENTO");
            boolean aprovado = controller.informarDadosPagamento(new CartaoCredito(numero, validade, nome));

            if (aprovado) {
                telaConfirmacao();
                return;
            }
            // FA3: pagamento não autorizado
            saida.println("! Pagamento não autorizado pela operadora.");
            saida.println("1 - Tentar novamente / informar outro cartão");
            saida.println("2 - Desistir da assinatura");
            while (true) {
                String opcao = perguntar("Opção");
                if (opcao.equals("1")) {
                    break;
                }
                if (opcao.equals("2")) {
                    controller.cancelarAssinatura();
                    saida.println("Processo encerrado sem ativação do plano.");
                    return;
                }
                saida.println("Opção inválida.");
            }
        }
    }

    private void telaConfirmacao() {
        cabecalho("Tela 11 - Assinatura ativa");
        ResumoAssinatura r = controller.obterResumoConfirmacao();
        saida.println("Assinatura ativa com sucesso!");
        saida.println("Plano .................: " + r.plano() + " (" + r.quantidadeRefeicoes() + " refeições)");
        saida.println("Periodicidade .........: " + r.periodicidade());
        saida.println("Endereço de entrega ...: " + r.endereco());
        saida.println("Primeira entrega ......: " + r.previsaoPrimeiraEntrega());
        saida.println("Valor .................: " + moeda(r.valor()));
        saida.println("Protocolo .............: " + r.numeroProtocolo());
    }

    // ---------------------------------------------------------------- utilitários de E/S

    private void cabecalho(String titulo) {
        saida.println();
        saida.println("--- " + titulo + " ---");
    }

    private void mostrarPratos(List<Prato> pratos) {
        for (Prato p : pratos) {
            saida.printf("%d - %s (%s)%n", p.getId(), p.getNome(), p.getDescricao());
        }
    }

    private String moeda(double v) {
        return String.format(PT_BR, "R$ %,.2f", v);
    }

    private String perguntar(String rotulo) {
        saida.print(rotulo + ": ");
        saida.flush();
        try {
            String linha = entrada.readLine();
            if (linha == null) {
                throw new EntradaEncerradaException();
            }
            if (System.console() == null) {
                // Entrada redirecionada (demo/vídeo): repete o que foi "digitado" para a saída ficar legível.
                saida.println(linha);
            }
            return linha.trim();
        } catch (IOException e) {
            throw new EntradaEncerradaException();
        }
    }

    private int lerInteiro(String rotulo) {
        while (true) {
            try {
                return Integer.parseInt(perguntar(rotulo));
            } catch (NumberFormatException e) {
                saida.println("! Digite um número.");
            }
        }
    }

    private List<Integer> parseInteiros(String linha) {
        List<Integer> numeros = new ArrayList<>();
        if (linha.isBlank()) {
            return numeros;
        }
        for (String parte : linha.split("[,; ]+")) {
            try {
                numeros.add(Integer.parseInt(parte.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Valor inválido: \"" + parte + "\". Use apenas números.");
            }
        }
        return numeros;
    }
}

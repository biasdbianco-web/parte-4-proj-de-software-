package br.com.minharefeicao;

import br.com.minharefeicao.catalogo.Cardapio;
import br.com.minharefeicao.catalogo.CatalogoAssinantes;
import br.com.minharefeicao.catalogo.CatalogoPlanos;
import br.com.minharefeicao.catalogo.RepositorioAssinaturas;
import br.com.minharefeicao.controle.AssinaturaController;
import br.com.minharefeicao.fronteira.TelaAssinatura;
import br.com.minharefeicao.servico.GeradorProtocolo;
import br.com.minharefeicao.servico.OperadoraCartaoCredito;
import br.com.minharefeicao.servico.ServicoSMS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Ponto de entrada: monta os objetos (fronteira, controle, entidades/catálogos, serviços) e inicia a tela. */
public class Main {

    public static void main(String[] args) throws Exception {
        PrintStream saida = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        Path dados = localizarPastaDados();
        if (dados == null) {
            saida.println("ERRO: não encontrei a pasta 'data' com os arquivos planos.csv e pratos.csv.");
            saida.println("Pasta atual: " + Path.of("").toAbsolutePath());
            saida.println("Execute o programa a partir da RAIZ do projeto (a pasta que contém 'data' e 'src'),");
            saida.println("ou informe o caminho com -Ddata.dir=CAMINHO_DA_PASTA_DATA");
            System.exit(1);
            return;
        }
        saida.println("(dados lidos de: " + dados.toAbsolutePath().normalize() + ")");

        BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        AssinaturaController controller = new AssinaturaController(
                new CatalogoAssinantes(dados.resolve("assinantes.csv")),
                new CatalogoPlanos(dados.resolve("planos.csv")),
                new Cardapio(dados.resolve("pratos.csv")),
                new ServicoSMS(saida::println),
                new GeradorProtocolo(dados.resolve("protocolo.txt")),
                new OperadoraCartaoCredito(),
                new RepositorioAssinaturas(dados.resolve("assinaturas.csv"), dados.resolve("itens_pedido.csv")));

        new TelaAssinatura(controller, entrada, saida).iniciar();
    }

    /** Usa -Ddata.dir se informado; senão procura "data" na pasta atual e em até 2 pastas acima. */
    private static Path localizarPastaDados() {
        String informado = System.getProperty("data.dir");
        if (informado != null && !informado.isBlank()) {
            Path p = Path.of(informado);
            return temArquivosBase(p) ? p : null;
        }
        Path base = Path.of("").toAbsolutePath();
        for (int i = 0; i < 3 && base != null; i++, base = base.getParent()) {
            Path candidato = base.resolve("data");
            if (temArquivosBase(candidato)) {
                return candidato;
            }
        }
        return null;
    }

    private static boolean temArquivosBase(Path pasta) {
        return Files.exists(pasta.resolve("planos.csv")) && Files.exists(pasta.resolve("pratos.csv"));
    }
}

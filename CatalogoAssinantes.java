package br.com.minharefeicao.catalogo;

import br.com.minharefeicao.entidade.Assinante;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Collection de Assinante persistida em data/assinantes.csv. */
public class CatalogoAssinantes {

    private static final String CABECALHO = "id;celular;nome;preferencias";

    private final Path arquivo;
    private final List<Assinante> assinantes = new ArrayList<>();

    public CatalogoAssinantes(Path arquivo) {
        this.arquivo = arquivo;
        CsvUtil.garantirArquivo(arquivo, CABECALHO);
        for (String[] c : CsvUtil.ler(arquivo)) {
            Assinante a = new Assinante(Integer.parseInt(c[0]), c[1], c[2]);
            if (!c[3].isBlank()) {
                a.definirPreferencias(Arrays.asList(c[3].split("\\|")));
            }
            assinantes.add(a);
        }
    }

    /** Mensagem do diagrama de sequência: buscarOuCriar(celular). */
    public Assinante buscarOuCriar(String celular) {
        for (Assinante a : assinantes) {
            if (a.getCelular().equals(celular)) {
                return a;
            }
        }
        Assinante novo = new Assinante(assinantes.size() + 1, celular, "");
        assinantes.add(novo);
        salvar();
        return novo;
    }

    /** Persiste todos os assinantes (chamado quando preferências ou dados mudam). */
    public void salvar() {
        List<String> linhas = new ArrayList<>();
        for (Assinante a : assinantes) {
            linhas.add(a.getId() + CsvUtil.SEP + a.getCelular() + CsvUtil.SEP + CsvUtil.limpar(a.getNome())
                    + CsvUtil.SEP + String.join("|", a.getTiposPreferencias()));
        }
        CsvUtil.reescrever(arquivo, CABECALHO, linhas);
    }
}

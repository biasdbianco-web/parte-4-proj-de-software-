package br.com.minharefeicao.catalogo;

import br.com.minharefeicao.entidade.PlanoAssinatura;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Collection de PlanoAssinatura; os dados são lidos de data/planos.csv. */
public class CatalogoPlanos {

    private final List<PlanoAssinatura> planos = new ArrayList<>();

    public CatalogoPlanos(Path arquivo) {
        for (String[] c : CsvUtil.ler(arquivo)) {
            planos.add(new PlanoAssinatura(Integer.parseInt(c[0]), c[1], Integer.parseInt(c[2]), c[3],
                    Double.parseDouble(c[4])));
        }
    }

    /** Mensagem do diagrama de sequência: listarDisponiveis(). */
    public List<PlanoAssinatura> listarDisponiveis() {
        return new ArrayList<>(planos);
    }

    /** Mensagem do diagrama de sequência: buscarPorId(idPlano). */
    public PlanoAssinatura buscarPorId(int idPlano) {
        for (PlanoAssinatura p : planos) {
            if (p.getId() == idPlano) {
                return p;
            }
        }
        throw new IllegalArgumentException("Plano inexistente: " + idPlano);
    }
}

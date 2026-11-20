package br.com.minharefeicao.catalogo;

import br.com.minharefeicao.entidade.CategoriaPrato;
import br.com.minharefeicao.entidade.Prato;

import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/** Collection de Prato; os dados são lidos de data/pratos.csv. */
public class Cardapio {

    private final LocalDate semanaVigencia = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    private final List<Prato> pratos = new ArrayList<>();

    public Cardapio(Path arquivo) {
        for (String[] c : CsvUtil.ler(arquivo)) {
            pratos.add(new Prato(Integer.parseInt(c[0]), c[1], CategoriaPrato.valueOf(c[2]), c[3],
                    new HashSet<>(Arrays.asList(c[4].split("\\|")))));
        }
    }

    /** Mensagem do diagrama de sequência: listarPratos(categoria, preferencias). */
    public List<Prato> listarPratos(CategoriaPrato categoria, List<String> preferencias) {
        List<Prato> resultado = new ArrayList<>();
        for (Prato p : pratos) {
            if (p.getCategoria() == categoria && p.atende(preferencias)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    public LocalDate getSemanaVigencia() {
        return semanaVigencia;
    }
}

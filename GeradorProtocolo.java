package br.com.minharefeicao.servico;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Gera números de protocolo sequenciais; o último número fica persistido em data/protocolo.txt. */
public class GeradorProtocolo {

    private static final int PRIMEIRO = 10245;

    private final Path arquivo;

    public GeradorProtocolo(Path arquivo) {
        this.arquivo = arquivo;
    }

    /** Mensagem do diagrama de sequência: gerar(). */
    public String gerar() {
        try {
            int proximo = PRIMEIRO;
            if (Files.exists(arquivo)) {
                proximo = Integer.parseInt(Files.readString(arquivo).trim()) + 1;
            } else if (arquivo.getParent() != null) {
                Files.createDirectories(arquivo.getParent());
            }
            Files.writeString(arquivo, String.valueOf(proximo));
            return "#" + proximo;
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao gerar protocolo", e);
        }
    }
}

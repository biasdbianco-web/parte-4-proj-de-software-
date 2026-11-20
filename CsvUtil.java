package br.com.minharefeicao.catalogo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** Utilitário de leitura/escrita de arquivos CSV (separador ';', UTF-8, primeira linha = cabeçalho). */
final class CsvUtil {

    static final String SEP = ";";

    private CsvUtil() {
    }

    /** Lê as linhas de dados (ignora cabeçalho e linhas vazias). Retorna lista vazia se o arquivo não existe. */
    static List<String[]> ler(Path arquivo) {
        List<String[]> linhas = new ArrayList<>();
        if (!Files.exists(arquivo)) {
            return linhas;
        }
        try {
            List<String> todas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
            for (int i = 1; i < todas.size(); i++) {
                String l = todas.get(i);
                if (!l.isBlank()) {
                    linhas.add(l.split(SEP, -1));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao ler " + arquivo, e);
        }
        return linhas;
    }

    /** Garante que o arquivo existe com o cabeçalho informado. */
    static void garantirArquivo(Path arquivo, String cabecalho) {
        try {
            if (arquivo.getParent() != null) {
                Files.createDirectories(arquivo.getParent());
            }
            if (!Files.exists(arquivo)) {
                Files.writeString(arquivo, cabecalho + System.lineSeparator(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao criar " + arquivo, e);
        }
    }

    /** Reescreve o arquivo inteiro (cabeçalho + linhas). */
    static void reescrever(Path arquivo, String cabecalho, List<String> linhas) {
        try {
            List<String> saida = new ArrayList<>();
            saida.add(cabecalho);
            saida.addAll(linhas);
            Files.write(arquivo, saida, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao gravar " + arquivo, e);
        }
    }

    /** Acrescenta linhas ao final do arquivo. */
    static void acrescentar(Path arquivo, List<String> linhas) {
        try {
            Files.write(arquivo, linhas, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao gravar " + arquivo, e);
        }
    }

    /** Remove o separador de um texto livre, para não quebrar o CSV. */
    static String limpar(String texto) {
        return texto == null ? "" : texto.replace(SEP, ",").replace("\n", " ").trim();
    }
}

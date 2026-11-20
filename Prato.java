package br.com.minharefeicao.entidade;

import java.util.Collection;
import java.util.Set;

/** Entidade de domínio: item do cardápio. */
public class Prato {

    private final int id;
    private final String nome;
    private final CategoriaPrato categoria;
    private final String descricao;
    /** Perfis alimentares atendidos pelo prato (ex.: VEGETARIANA, SEM_LACTOSE). */
    private final Set<String> perfis;

    public Prato(int id, String nome, CategoriaPrato categoria, String descricao, Set<String> perfis) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.descricao = descricao;
        this.perfis = perfis;
    }

    /** Sem preferências informadas, o prato é sempre aceito. */
    public boolean atende(Collection<String> preferencias) {
        if (preferencias == null || preferencias.isEmpty()) {
            return true;
        }
        for (String p : preferencias) {
            if (perfis.contains(p)) {
                return true;
            }
        }
        return false;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public CategoriaPrato getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public Set<String> getPerfis() { return perfis; }
}

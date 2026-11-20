package br.com.minharefeicao.entidade;

import java.util.ArrayList;
import java.util.List;

/** Entidade de domínio: pessoa que contrata um plano de refeições. */
public class Assinante {

    private final int id;
    private final String celular;
    private String nome;
    private final List<PreferenciaAlimentar> preferencias = new ArrayList<>();

    public Assinante(int id, String celular, String nome) {
        this.id = id;
        this.celular = celular;
        this.nome = nome;
    }

    /** Mensagem do diagrama de sequência: definirPreferencias(preferencias). */
    public void definirPreferencias(List<String> tipos) {
        preferencias.clear();
        for (String tipo : tipos) {
            preferencias.add(new PreferenciaAlimentar(tipo));
        }
    }

    public List<PreferenciaAlimentar> getPreferencias() {
        return new ArrayList<>(preferencias);
    }

    public List<String> getTiposPreferencias() {
        List<String> tipos = new ArrayList<>();
        for (PreferenciaAlimentar p : preferencias) {
            tipos.add(p.getTipo());
        }
        return tipos;
    }

    public int getId() { return id; }
    public String getCelular() { return celular; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}

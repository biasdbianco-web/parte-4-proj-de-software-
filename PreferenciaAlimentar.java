package br.com.minharefeicao.entidade;

/** Preferência/restrição alimentar do assinante (ex.: VEGETARIANA, SEM_LACTOSE, TRADICIONAL). */
public class PreferenciaAlimentar {

    public static final String[] OPCOES = {"VEGETARIANA", "TRADICIONAL", "SEM_LACTOSE"};

    private final String tipo;

    public PreferenciaAlimentar(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return tipo;
    }
}

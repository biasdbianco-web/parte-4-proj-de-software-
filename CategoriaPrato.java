package br.com.minharefeicao.entidade;

/** Categoria de um prato do cardápio (enumeração do diagrama de classes de projeto). */
public enum CategoriaPrato {
    PRINCIPAL("Prato principal"),
    ACOMPANHAMENTO("Acompanhamento"),
    SOBREMESA("Sobremesa");

    private final String rotulo;

    CategoriaPrato(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}

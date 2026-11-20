package br.com.minharefeicao.entidade;

/** Entidade de domínio: meio de pagamento informado pelo assinante. */
public class CartaoCredito {

    private final String numero;
    private final String validade;
    private final String nomeImpresso;

    public CartaoCredito(String numero, String validade, String nomeImpresso) {
        this.numero = numero.replaceAll("\\D", "");
        this.validade = validade;
        this.nomeImpresso = nomeImpresso;
    }

    /** Nunca persistimos nem exibimos o número completo. */
    public String getNumeroMascarado() {
        if (numero.length() < 4) {
            return "****";
        }
        return "**** **** **** " + numero.substring(numero.length() - 4);
    }

    public String getNumero() { return numero; }
    public String getValidade() { return validade; }
    public String getNomeImpresso() { return nomeImpresso; }
}

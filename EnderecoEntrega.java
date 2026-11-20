package br.com.minharefeicao.entidade;

/** Entidade de domínio: endereço de entrega das marmitas. */
public class EnderecoEntrega {

    private final String rua;
    private final String numero;
    private final String complemento;
    private final String cidade;
    private final String cep;

    public EnderecoEntrega(String rua, String numero, String complemento, String cidade, String cep) {
        this.rua = rua;
        this.numero = numero;
        this.complemento = complemento;
        this.cidade = cidade;
        this.cep = cep;
    }

    public String getRua() { return rua; }
    public String getNumero() { return numero; }
    public String getComplemento() { return complemento; }
    public String getCidade() { return cidade; }
    public String getCep() { return cep; }

    @Override
    public String toString() {
        String comp = (complemento == null || complemento.isBlank()) ? "" : " - " + complemento;
        return rua + ", " + numero + comp + " - " + cidade + " - CEP " + cep;
    }
}

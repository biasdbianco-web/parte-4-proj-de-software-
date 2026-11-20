package br.com.minharefeicao.servico;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Serviço de verificação por SMS (simulado). O "SMS" é entregue por um canal de saída (por padrão, o console).
 * Para testes repetíveis, defina -Dsms.codigo=1234 para fixar o código.
 */
public class ServicoSMS {

    private final Map<String, String> codigosPendentes = new HashMap<>();
    private final Random random = new Random();
    private final Consumer<String> canalEnvio;

    public ServicoSMS(Consumer<String> canalEnvio) {
        this.canalEnvio = canalEnvio;
    }

    /** Mensagem do diagrama de sequência: enviarCodigo(celular). */
    public void enviarCodigo(String celular) {
        String fixo = System.getProperty("sms.codigo");
        String codigo = (fixo != null && !fixo.isBlank()) ? fixo : String.format("%04d", random.nextInt(10000));
        codigosPendentes.put(celular, codigo);
        canalEnvio.accept("[SMS simulado para " + celular + "] Minha Refeição: seu código de verificação é " + codigo);
    }

    /** Mensagem do diagrama de sequência: validarCodigo(celular, codigo). */
    public boolean validarCodigo(String celular, String codigo) {
        String esperado = codigosPendentes.get(celular);
        return esperado != null && esperado.equals(codigo);
    }

    /** Mensagem do diagrama de sequência (FA1): invalidarCodigo(celular). */
    public void invalidarCodigo(String celular) {
        codigosPendentes.remove(celular);
    }
}

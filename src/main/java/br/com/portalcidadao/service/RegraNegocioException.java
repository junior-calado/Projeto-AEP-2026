package br.com.portalcidadao.service;

/**
 * Erro de regra de negócio (ex.: idade abaixo de 16 anos, comentário vazio).
 * A mensagem é escrita para ser mostrada direto para o usuário.
 *
 * [CONCEITO] Exceção personalizada: criamos o nosso próprio tipo de erro herdando de
 * RuntimeException. Assim o Menu consegue separar dois casos com dois catch diferentes:
 *   - RegraNegocioException -> o usuário fez algo não permitido: mostra a mensagem e segue
 *   - SQLException          -> problema técnico no banco
 *
 * [CONCEITO] Exceção checada x não checada:
 *   - SQLException é CHECADA: o compilador obriga a declarar "throws" ou tratar com try/catch.
 *   - RuntimeException (e as filhas dela, como esta) é NÃO CHECADA: pode ser lançada sem
 *     "throws", o que deixa as regras de validação mais limpas de escrever.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem); // guarda a mensagem, que depois é lida com e.getMessage()
    }
}

package br.com.portalcidadao.model;

import java.time.LocalDateTime;

/**
 * Comentário (opinião) de um usuário sobre uma proposta (RF02).
 *
 * [CONCEITO] Herança de classe abstrata: Comentario herda de Interacao o usuário, a proposta
 * e a data, acrescenta o texto e as curtidas, e é OBRIGADO a implementar exibirInteracao().
 */
public class Comentario extends Interacao {

    public static final int TAMANHO_MAXIMO = 500; // mesmo limite da coluna VARCHAR(500) no banco

    private String texto;
    private int curtidas;

    /** Construtor usado pelo DAO, com todos os dados que vieram do banco. */
    public Comentario(int id, Usuario usuario, int propostaId, LocalDateTime dataRegistro,
                      String texto, int curtidas) {
        super(id, usuario, propostaId, dataRegistro);
        this.texto = texto;
        this.curtidas = curtidas;
    }

    /** Comentário novo, ainda não salvo: sem id, com data de agora e nenhuma curtida. */
    public Comentario(Usuario usuario, int propostaId, String texto) {
        this(0, usuario, propostaId, LocalDateTime.now(), texto, 0);
    }

    // [CONCEITO] Polimorfismo: esta é a versão do exibirInteracao() para COMENTÁRIOS.
    // Compare com a versão de Avaliacao: mesmo método, resultado diferente.
    @Override
    public String exibirInteracao() {
        // getId(), getUsuario()... são herdados de Interacao
        return String.format("[Comentário #%d] %s em %s (%d curtida%s)%n    \"%s\"",
                getId(), getUsuario().getNome(), getDataRegistro().format(FORMATO_DATA),
                curtidas, curtidas == 1 ? "" : "s", texto);
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public int getCurtidas() {
        return curtidas;
    }
}

package br.com.portalcidadao.model;

import java.time.LocalDateTime;

/**
 * Avaliação de 1 a 5 estrelas de um usuário sobre uma proposta (RF03).
 * Assim como Comentario, é uma filha de Interacao.
 */
public class Avaliacao extends Interacao {

    public static final int MIN_ESTRELAS = 1;
    public static final int MAX_ESTRELAS = 5;

    private int estrela;

    public Avaliacao(int id, Usuario usuario, int propostaId, LocalDateTime dataRegistro, int estrela) {
        super(id, usuario, propostaId, dataRegistro);
        this.estrela = estrela;
    }

    /** Avaliação nova, ainda não salva no banco. */
    public Avaliacao(Usuario usuario, int propostaId, int estrela) {
        this(0, usuario, propostaId, LocalDateTime.now(), estrela);
    }

    // [CONCEITO] Polimorfismo: a versão do exibirInteracao() para AVALIAÇÕES mostra
    // estrelas em vez de texto. Quem chama não precisa saber qual das duas está chamando.
    @Override
    public String exibirInteracao() {
        // "*".repeat(3) gera "***"
        return String.format("[Avaliação] %s deu %d de 5 estrelas %s em %s",
                getUsuario().getNome(), estrela, "*".repeat(estrela),
                getDataRegistro().format(FORMATO_DATA));
    }

    public int getEstrela() {
        return estrela;
    }
}

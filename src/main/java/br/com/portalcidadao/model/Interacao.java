package br.com.portalcidadao.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Participação de um usuário em uma proposta. É a classe base de Comentario e Avaliacao.
 *
 * [CONCEITO] Classe abstrata ("abstract"): representa uma ideia geral que não existe sozinha.
 * Não dá para fazer "new Interacao(...)": toda interação concreta é um Comentario OU uma Avaliacao.
 * Ela serve para reunir o que as duas têm em comum (quem fez, em qual proposta, quando)
 * e não repetir esse código nas duas classes filhas.
 *
 * [CONCEITO] Abstração: o resto do sistema pode trabalhar com "Interacao" sem se preocupar
 * com o tipo exato (veja o método imprimirDetalhes() no Menu).
 */
public abstract class Interacao {

    // [CONCEITO] protected: visível nesta classe e nas classes filhas (Comentario e Avaliacao),
    // que usam este formato no exibirInteracao() delas.
    protected static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private Usuario usuario;    // quem fez: associação com Usuario
    private int propostaId;     // em qual proposta
    private LocalDateTime dataRegistro;

    public Interacao(int id, Usuario usuario, int propostaId, LocalDateTime dataRegistro) {
        this.id = id;
        this.usuario = usuario;
        this.propostaId = propostaId;
        this.dataRegistro = dataRegistro;
    }

    /**
     * Texto mostrado na tela de detalhes da proposta.
     *
     * [CONCEITO] Método abstrato: só tem a assinatura, sem corpo. Ele OBRIGA cada classe filha
     * a escrever a sua própria versão. Esse é o polimorfismo descrito no trabalho:
     * o mesmo método exibirInteracao() se comporta de um jeito em Comentario e de outro em Avaliacao.
     */
    public abstract String exibirInteracao();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public int getPropostaId() {
        return propostaId;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }
}

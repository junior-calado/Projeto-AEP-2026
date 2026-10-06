package br.com.portalcidadao.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Proposta legislativa (projeto de lei) publicada por um vereador. */
public class Proposta {

    // Formato brasileiro de data, usado só para exibir. No banco a data fica como DATE.
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int id;
    private String titulo;
    private String descricao;
    private String resumo;                  // RF10: explicação em linguagem simples
    private LocalDate dataPublicacao;
    private SituacaoProposta situacao;      // RF08: em análise, aprovada ou rejeitada
    private int votosFavoraveis;            // RF09: resultado da votação
    private int votosContrarios;

    // [CONCEITO] Associação: a proposta guarda o OBJETO Vereador inteiro, e não só o id dele.
    // No banco a relação é feita pela chave estrangeira vereador_id; no Java, por esta referência.
    // É a linha entre Vereador e Proposta no diagrama de classes.
    private Vereador vereador;

    // Estes dois não são colunas da tabela proposta: são calculados a partir da tabela
    // avaliacao (AVG e COUNT) e preenchidos pelo PropostaDAO.
    private double mediaEstrelas;
    private int totalAvaliacoes;

    /** Construtor vazio: usado pelo DAO, que vai preenchendo os campos com os setters. */
    public Proposta() {
    }

    /** Proposta nova: começa em análise, sem votos e com a data de hoje. */
    public Proposta(String titulo, String descricao, String resumo, Vereador vereador) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.resumo = resumo;
        this.vereador = vereador;
        this.dataPublicacao = LocalDate.now();
        this.situacao = SituacaoProposta.EM_ANALISE;
    }

    /** Uma linha para a listagem de propostas (RF04). */
    public String linhaListagem() {
        // String.format: %d é substituído por número inteiro e %s por texto, na ordem dos argumentos
        return String.format("#%d | %s | %s | %s | %s | %s",
                id, titulo, situacao.getDescricao(), dataPublicacao.format(FORMATO_DATA),
                vereador.getNome(), textoAvaliacao());
    }

    /** Todas as informações da proposta, para a tela de detalhes (RF01). Nome igual ao do diagrama de classes. */
    public String detalhes() {
        // StringBuilder monta um texto grande pedaço por pedaço, sem criar uma String nova a cada "+"
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(id).append(" - ").append(titulo).append("\n");
        sb.append("Autor: ").append(vereador.getNome()).append(" (").append(vereador.getPartido()).append(")\n");
        sb.append("Publicada em: ").append(dataPublicacao.format(FORMATO_DATA)).append("\n");
        sb.append("Situação: ").append(situacao.getDescricao()).append("\n");
        sb.append("Votação: ").append(textoVotacao()).append("\n");
        sb.append("Avaliação dos cidadãos: ").append(textoAvaliacao()).append("\n");
        sb.append("\nResumo em linguagem simples:\n  ")
          .append(resumo == null || resumo.isBlank() ? "(o autor ainda não escreveu um resumo)" : resumo)
          .append("\n");
        sb.append("\nTexto da proposta:\n  ").append(descricao).append("\n");
        return sb.toString();
    }

    // [CONCEITO] Métodos private: são detalhes internos da classe, usados só pelos métodos
    // acima. Quem está fora só enxerga linhaListagem() e detalhes(). Isso também é encapsulamento.

    private String textoVotacao() {
        if (situacao == SituacaoProposta.EM_ANALISE) {
            return "ainda não foi votada";
        }
        return votosFavoraveis + " votos favoráveis x " + votosContrarios + " votos contrários";
    }

    private String textoAvaliacao() {
        if (totalAvaliacoes == 0) {
            return "sem avaliações";
        }
        // %.1f = número com uma casa decimal
        return String.format("%.1f de 5 estrelas (%d %s)", mediaEstrelas, totalAvaliacoes,
                totalAvaliacoes == 1 ? "avaliação" : "avaliações");
    }

    // ---- Getters e setters ----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getResumo() {
        return resumo;
    }

    public void setResumo(String resumo) {
        this.resumo = resumo;
    }

    public LocalDate getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(LocalDate dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public SituacaoProposta getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoProposta situacao) {
        this.situacao = situacao;
    }

    public int getVotosFavoraveis() {
        return votosFavoraveis;
    }

    public void setVotosFavoraveis(int votosFavoraveis) {
        this.votosFavoraveis = votosFavoraveis;
    }

    public int getVotosContrarios() {
        return votosContrarios;
    }

    public void setVotosContrarios(int votosContrarios) {
        this.votosContrarios = votosContrarios;
    }

    public Vereador getVereador() {
        return vereador;
    }

    public void setVereador(Vereador vereador) {
        this.vereador = vereador;
    }

    public double getMediaEstrelas() {
        return mediaEstrelas;
    }

    public void setMediaEstrelas(double mediaEstrelas) {
        this.mediaEstrelas = mediaEstrelas;
    }

    public int getTotalAvaliacoes() {
        return totalAvaliacoes;
    }

    public void setTotalAvaliacoes(int totalAvaliacoes) {
        this.totalAvaliacoes = totalAvaliacoes;
    }
}

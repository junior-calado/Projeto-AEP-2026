package br.com.portalcidadao.model;

/**
 * Andamento da tramitação de uma proposta (RF08).
 *
 * [CONCEITO] enum: usado quando existe uma lista FIXA de valores possíveis.
 * Com uma String comum alguém poderia gravar "aprovado", "Aprovada", "APROVADO"...
 * Com o enum, o compilador só aceita um destes três valores.
 * No banco, a mesma garantia é feita pela CHECK ck_proposta_situacao.
 */
public enum SituacaoProposta {

    // Cada constante chama o construtor lá embaixo passando o texto para exibir na tela
    EM_ANALISE("Em análise"),
    APROVADA("Aprovada"),
    REJEITADA("Rejeitada");

    private final String descricao;

    SituacaoProposta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

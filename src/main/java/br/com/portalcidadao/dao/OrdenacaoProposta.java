package br.com.portalcidadao.dao;

/**
 * Formas de ordenar a lista de propostas (RF04).
 *
 * [CONCEITO] enum: um tipo com uma lista FIXA de valores possíveis. Em Java, cada valor
 * do enum é um objeto e pode ter atributos e métodos, como a descrição e o trecho SQL abaixo.
 *
 * O trecho de ORDER BY fica aqui dentro, e não vem do que o usuário digita.
 * Por isso é seguro concatená-lo no SQL do PropostaDAO (sem risco de SQL Injection).
 */
public enum OrdenacaoProposta {

    MAIS_RECENTES("Mais recentes primeiro", "p.data_publicacao DESC, p.proposta_id DESC"),
    TITULO("Ordem alfabética", "p.titulo"),
    MELHOR_AVALIADAS("Melhor avaliadas", "media_estrelas DESC, total_avaliacoes DESC");

    private final String descricao;
    private final String orderBy;

    OrdenacaoProposta(String descricao, String orderBy) {
        this.descricao = descricao;
        this.orderBy = orderBy;
    }

    public String getDescricao() {
        return descricao;
    }

    // [CONCEITO] Encapsulamento: sem "public", só as classes do pacote dao enxergam o SQL.
    // O Menu só vê a descrição amigável.
    String getOrderBy() {
        return orderBy;
    }
}

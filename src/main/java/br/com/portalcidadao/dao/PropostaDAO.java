package br.com.portalcidadao.dao;

import br.com.portalcidadao.model.Proposta;
import br.com.portalcidadao.model.SituacaoProposta;
import br.com.portalcidadao.model.Vereador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso à tabela proposta: o CRUD do vereador (RF06), a listagem (RF04),
 * a pesquisa (RF07) e a busca por id para a tela de detalhes (RF01).
 *
 * [CONCEITO] CRUD = Create, Read, Update, Delete (inserir, consultar, atualizar, excluir),
 * as quatro operações básicas sobre os dados.
 */
public class PropostaDAO {

    // SQL base usado por listar, pesquisar e buscarPorId. Em uma consulta só ele traz:
    //   - os dados da proposta (p)
    //   - o vereador autor          -> JOIN usuario: toda proposta TEM um autor
    //   - as avaliações             -> LEFT JOIN avaliacao: traz a proposta mesmo se ninguém avaliou ainda
    //   - média e total de estrelas -> AVG e COUNT, agrupados por proposta com o GROUP BY
    // COALESCE(x, 0) troca o NULL por 0 quando a proposta ainda não tem avaliações.
    //
    // [CONCEITO] Text block (""" ... """): string de várias linhas, deixa o SQL legível no código.
    private static final String SELECT_BASE = """
            SELECT p.proposta_id, p.titulo, p.descricao, p.resumo, p.data_publicacao, p.situacao,
                   p.votos_favoraveis, p.votos_contrarios,
                   u.usuario_id, u.nome, u.cpf, u.idade, u.cidade, u.tipo_usuario, u.partido,
                   COALESCE(AVG(a.estrela), 0) AS media_estrelas,
                   COUNT(a.avaliacao_id)       AS total_avaliacoes
            FROM proposta p
            JOIN usuario u        ON u.usuario_id = p.vereador_id
            LEFT JOIN avaliacao a ON a.proposta_id = p.proposta_id
            """;

    private static final String GROUP_BY = " GROUP BY p.proposta_id, u.usuario_id ";

    // [CONCEITO] List<Proposta>: a variável é declarada pela INTERFACE (List) e criada pela
    // implementação (ArrayList). O <Proposta> é um "generic": garante que a lista só aceita Proposta.
    public List<Proposta> listar(OrdenacaoProposta ordem) throws SQLException {
        String sql = SELECT_BASE + GROUP_BY + "ORDER BY " + ordem.getOrderBy();

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return montarLista(rs);
        }
    }

    /**
     * Procura a palavra no título, na descrição e no resumo (RF07).
     * O LIKE com % antes e depois significa "contém". Como o banco usa a collation
     * utf8mb4_unicode_ci, a busca ignora acentos e maiúsculas: "iluminacao" acha "Iluminação".
     */
    public List<Proposta> pesquisar(String palavraChave) throws SQLException {
        String sql = SELECT_BASE
                + "WHERE p.titulo LIKE ? OR p.descricao LIKE ? OR p.resumo LIKE ?"
                + GROUP_BY + "ORDER BY p.data_publicacao DESC";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Os % ficam no valor, e não no SQL: o texto digitado continua protegido pelo "?"
            String filtro = "%" + palavraChave + "%";
            stmt.setString(1, filtro);
            stmt.setString(2, filtro);
            stmt.setString(3, filtro);
            try (ResultSet rs = stmt.executeQuery()) {
                return montarLista(rs);
            }
        }
    }

    public Proposta buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.proposta_id = ?" + GROUP_BY;

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                // Devolve null quando não encontra: quem decide o que fazer com isso é o service
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    public void inserir(Proposta proposta) throws SQLException {
        String sql = """
                INSERT INTO proposta (titulo, descricao, resumo, data_publicacao, situacao,
                                      votos_favoraveis, votos_contrarios, vereador_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, proposta.getTitulo());
            stmt.setString(2, proposta.getDescricao());
            stmt.setString(3, proposta.getResumo());
            stmt.setObject(4, proposta.getDataPublicacao()); // LocalDate do Java -> DATE do MySQL
            stmt.setString(5, proposta.getSituacao().name()); // name() devolve o nome da constante: "EM_ANALISE"
            stmt.setInt(6, proposta.getVotosFavoraveis());
            stmt.setInt(7, proposta.getVotosContrarios());
            // Na tabela guardamos só o id do autor (chave estrangeira vereador_id)
            stmt.setInt(8, proposta.getVereador().getId());
            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    proposta.setId(chaves.getInt(1));
                }
            }
        }
    }

    public void atualizar(Proposta proposta) throws SQLException {
        String sql = """
                UPDATE proposta
                SET titulo = ?, descricao = ?, resumo = ?, situacao = ?,
                    votos_favoraveis = ?, votos_contrarios = ?
                WHERE proposta_id = ?
                """;

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, proposta.getTitulo());
            stmt.setString(2, proposta.getDescricao());
            stmt.setString(3, proposta.getResumo());
            stmt.setString(4, proposta.getSituacao().name());
            stmt.setInt(5, proposta.getVotosFavoraveis());
            stmt.setInt(6, proposta.getVotosContrarios());
            stmt.setInt(7, proposta.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Os comentários e avaliações da proposta são apagados junto pelo próprio banco,
     * graças ao ON DELETE CASCADE definido nas chaves estrangeiras (veja o portal_cidadao.sql).
     */
    public void excluir(int id) throws SQLException {
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM proposta WHERE proposta_id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private List<Proposta> montarLista(ResultSet rs) throws SQLException {
        List<Proposta> propostas = new ArrayList<>();
        while (rs.next()) {
            propostas.add(montar(rs));
        }
        return propostas;
    }

    /** Converte uma linha do banco (colunas) em um objeto Proposta (atributos). */
    private Proposta montar(ResultSet rs) throws SQLException {
        Proposta p = new Proposta();
        p.setId(rs.getInt("proposta_id"));
        p.setTitulo(rs.getString("titulo"));
        p.setDescricao(rs.getString("descricao"));
        p.setResumo(rs.getString("resumo"));
        p.setDataPublicacao(rs.getObject("data_publicacao", LocalDate.class));
        // valueOf faz o caminho inverso do name(): texto "APROVADA" -> constante SituacaoProposta.APROVADA
        p.setSituacao(SituacaoProposta.valueOf(rs.getString("situacao")));
        p.setVotosFavoraveis(rs.getInt("votos_favoraveis"));
        p.setVotosContrarios(rs.getInt("votos_contrarios"));
        // [CONCEITO] Casting: montar() devolve um Usuario, e aqui afirmamos que ele é um Vereador.
        // É seguro porque o PropostaService só deixa vereadores criarem propostas.
        p.setVereador((Vereador) UsuarioDAO.montar(rs));
        p.setMediaEstrelas(rs.getDouble("media_estrelas"));
        p.setTotalAvaliacoes(rs.getInt("total_avaliacoes"));
        return p;
    }
}

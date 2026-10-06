package br.com.portalcidadao.dao;

import br.com.portalcidadao.model.Avaliacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Acesso à tabela avaliacao (RF03). */
public class AvaliacaoDAO {

    public List<Avaliacao> listarPorProposta(int propostaId) throws SQLException {
        String sql = """
                SELECT a.avaliacao_id, a.estrela, a.data_registro, a.proposta_id,
                       u.usuario_id, u.nome, u.cpf, u.idade, u.cidade, u.tipo_usuario, u.partido
                FROM avaliacao a
                JOIN usuario u ON u.usuario_id = a.usuario_id
                WHERE a.proposta_id = ?
                ORDER BY a.data_registro
                """;

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, propostaId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Avaliacao> avaliacoes = new ArrayList<>();
                while (rs.next()) {
                    avaliacoes.add(new Avaliacao(
                            rs.getInt("avaliacao_id"),
                            UsuarioDAO.montar(rs),
                            rs.getInt("proposta_id"),
                            rs.getObject("data_registro", LocalDateTime.class),
                            rs.getInt("estrela")));
                }
                return avaliacoes;
            }
        }
    }

    /**
     * Grava a avaliação. Devolve true se foi uma avaliação nova e false se foi uma troca de nota.
     *
     * A tabela tem UNIQUE (usuario_id, proposta_id): cada pessoa só pode ter UMA avaliação
     * por proposta. O "ON DUPLICATE KEY UPDATE" do MySQL aproveita isso: tenta inserir e,
     * se aquela dupla já existir, atualiza a nota em vez de dar erro.
     */
    public boolean salvar(Avaliacao avaliacao) throws SQLException {
        String sql = """
                INSERT INTO avaliacao (estrela, data_registro, usuario_id, proposta_id)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE estrela = ?, data_registro = ?
                """;

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, avaliacao.getEstrela());
            stmt.setObject(2, avaliacao.getDataRegistro());
            stmt.setInt(3, avaliacao.getUsuario().getId());
            stmt.setInt(4, avaliacao.getPropostaId());
            stmt.setInt(5, avaliacao.getEstrela());
            stmt.setObject(6, avaliacao.getDataRegistro());
            // O MySQL informa 1 linha afetada quando insere e 2 quando atualiza
            return stmt.executeUpdate() == 1;
        }
    }
}

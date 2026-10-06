package br.com.portalcidadao.dao;

import br.com.portalcidadao.model.Comentario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Acesso às tabelas comentario (RF02) e curtida (RF05). */
public class ComentarioDAO {

    // A quantidade de curtidas não fica gravada em uma coluna: ela é contada na hora,
    // com uma subconsulta (um SELECT dentro do outro) na tabela curtida.
    private static final String SELECT_BASE = """
            SELECT c.comentario_id, c.texto, c.data_registro, c.proposta_id,
                   u.usuario_id, u.nome, u.cpf, u.idade, u.cidade, u.tipo_usuario, u.partido,
                   (SELECT COUNT(*) FROM curtida ct WHERE ct.comentario_id = c.comentario_id) AS curtidas
            FROM comentario c
            JOIN usuario u ON u.usuario_id = c.usuario_id
            """;

    public List<Comentario> listarPorProposta(int propostaId) throws SQLException {
        String sql = SELECT_BASE + "WHERE c.proposta_id = ? ORDER BY c.data_registro";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, propostaId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Comentario> comentarios = new ArrayList<>();
                while (rs.next()) {
                    comentarios.add(montar(rs));
                }
                return comentarios;
            }
        }
    }

    public Comentario buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE c.comentario_id = ?";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    public void inserir(Comentario comentario) throws SQLException {
        String sql = "INSERT INTO comentario (texto, data_registro, usuario_id, proposta_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, comentario.getTexto());
            stmt.setObject(2, comentario.getDataRegistro());
            // As duas chaves estrangeiras: QUEM comentou e EM QUAL proposta
            stmt.setInt(3, comentario.getUsuario().getId());
            stmt.setInt(4, comentario.getPropostaId());
            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    comentario.setId(chaves.getInt(1));
                }
            }
        }
    }

    public void atualizarTexto(int id, String texto) throws SQLException {
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement("UPDATE comentario SET texto = ? WHERE comentario_id = ?")) {
            stmt.setString(1, texto);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    /** As curtidas do comentário são apagadas junto (ON DELETE CASCADE na tabela curtida). */
    public void excluir(int id) throws SQLException {
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM comentario WHERE comentario_id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Registra a curtida e devolve false se o usuário já tinha curtido.
     *
     * Quem garante "uma curtida por pessoa" é o próprio banco: a chave primária da tabela
     * curtida é (usuario_id, comentario_id), então a mesma dupla não pode se repetir.
     * O INSERT IGNORE, em vez de dar erro, simplesmente não insere nada, e o
     * executeUpdate() devolve quantas linhas foram inseridas (1 ou 0).
     */
    public boolean curtir(int usuarioId, int comentarioId) throws SQLException {
        String sql = "INSERT IGNORE INTO curtida (usuario_id, comentario_id) VALUES (?, ?)";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setInt(2, comentarioId);
            return stmt.executeUpdate() == 1;
        }
    }

    private Comentario montar(ResultSet rs) throws SQLException {
        return new Comentario(
                rs.getInt("comentario_id"),
                UsuarioDAO.montar(rs), // o autor do comentário, vindo do JOIN com usuario
                rs.getInt("proposta_id"),
                rs.getObject("data_registro", LocalDateTime.class),
                rs.getString("texto"),
                rs.getInt("curtidas"));
    }
}

package br.com.portalcidadao.dao;

import br.com.portalcidadao.model.Usuario;
import br.com.portalcidadao.model.Vereador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Acesso à tabela usuario.
 *
 * [CONCEITO] DAO (Data Access Object): é um padrão de projeto em que uma classe concentra
 * todo o SQL de uma tabela. O resto do sistema não escreve SQL: só chama métodos como
 * buscarPorCpf() ou inserir(). Se o banco mudar, só o DAO precisa mudar.
 *
 * Os outros DAOs seguem a mesma receita que está explicada aqui em detalhe:
 *   1. abrir a conexão          -> Conexao.getConexao()
 *   2. preparar o SQL           -> conn.prepareStatement(sql)
 *   3. preencher os "?"         -> stmt.setString(...), stmt.setInt(...)
 *   4. executar                 -> executeQuery() para SELECT, executeUpdate() para INSERT/UPDATE/DELETE
 *   5. ler o resultado          -> ResultSet
 *   6. fechar tudo              -> automático com try-with-resources
 */
public class UsuarioDAO {

    public Usuario buscarPorCpf(String cpf) throws SQLException {
        // [CONCEITO] PreparedStatement e o "?": o valor NUNCA é colado direto no texto do SQL.
        // O driver envia o valor separado, o que impede SQL Injection (quando alguém digita
        // um pedaço de SQL no lugar do CPF para invadir ou apagar o banco).
        String sql = "SELECT usuario_id, nome, cpf, idade, cidade, tipo_usuario, partido FROM usuario WHERE cpf = ?";

        // [CONCEITO] try-with-resources: tudo que é aberto entre os parênteses do try é fechado
        // automaticamente no final, mesmo se acontecer um erro. Sem isso, conexões esquecidas
        // abertas acabariam travando o banco.
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf); // 1 = primeiro "?" do SQL
            try (ResultSet rs = stmt.executeQuery()) {
                // [CONCEITO] ResultSet: funciona como um cursor sobre as linhas retornadas.
                // rs.next() avança para a próxima linha e devolve false quando não há mais nenhuma.
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    public void inserir(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nome, cpf, idade, cidade, tipo_usuario, partido) VALUES (?, ?, ?, ?, ?, ?)";

        // RETURN_GENERATED_KEYS pede ao banco o id criado pelo AUTO_INCREMENT
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getCpf());
            stmt.setInt(3, usuario.getIdade());
            stmt.setString(4, usuario.getCidade());
            // [CONCEITO] Polimorfismo: getTipo() devolve "CIDADAO" ou "VEREADOR" conforme a classe
            // real do objeto, sem precisar de if.
            stmt.setString(5, usuario.getTipo());
            // [CONCEITO] instanceof: testa se o objeto é um Vereador e, se for, já cria a variável v.
            // Só vereador tem partido; para cidadão gravamos NULL.
            stmt.setString(6, usuario instanceof Vereador v ? v.getPartido() : null);
            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    usuario.setId(chaves.getInt(1));
                }
            }
        }
    }

    /**
     * Transforma a linha atual do ResultSet em objeto Java ("mapeamento objeto-relacional").
     *
     * [CONCEITO] Herança + polimorfismo: o tipo de retorno é Usuario, mas o objeto devolvido
     * pode ser um Vereador, porque todo Vereador É UM Usuario.
     *
     * Não tem "public": fica visível só dentro do pacote dao, porque os outros DAOs
     * também usam este método quando fazem JOIN com a tabela usuario.
     */
    static Usuario montar(ResultSet rs) throws SQLException {
        int id = rs.getInt("usuario_id");
        String nome = rs.getString("nome");
        String cpf = rs.getString("cpf");
        int idade = rs.getInt("idade");
        String cidade = rs.getString("cidade");

        if ("VEREADOR".equals(rs.getString("tipo_usuario"))) {
            return new Vereador(id, nome, cpf, idade, cidade, rs.getString("partido"));
        }
        return new Usuario(id, nome, cpf, idade, cidade);
    }
}

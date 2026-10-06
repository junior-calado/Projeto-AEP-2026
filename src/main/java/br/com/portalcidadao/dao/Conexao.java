package br.com.portalcidadao.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Responsável por abrir conexões com o banco portal_cidadao.
 *
 * [CONCEITO] JDBC (Java Database Connectivity): é a API padrão do Java para conversar com
 * bancos de dados. O Java só define as interfaces (Connection, PreparedStatement, ResultSet);
 * quem faz o trabalho de verdade é o DRIVER do banco, no nosso caso o mysql-connector-j,
 * declarado como dependência no pom.xml.
 *
 * Os dados de acesso (URL, usuário e senha) ficam em src/main/resources/db.properties,
 * fora do código: assim cada pessoa ajusta para o seu MySQL sem precisar mexer no Java.
 */
public class Conexao {

    // [CONCEITO] static: o atributo pertence à CLASSE, e não a cada objeto. Ele é carregado
    // uma única vez e reaproveitado em todas as conexões.
    // [CONCEITO] final: depois de atribuído, não pode passar a apontar para outro objeto.
    private static final Properties config = carregarConfig();

    private static Properties carregarConfig() {
        Properties props = new Properties();
        // getResourceAsStream procura o arquivo no classpath: tudo que está em
        // src/main/resources vai junto com as classes compiladas.
        try (InputStream in = Conexao.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new IllegalStateException("Arquivo db.properties não encontrado no classpath.");
            }
            props.load(in);
        } catch (IOException e) {
            // Sem configuração não há como continuar, então o programa para com uma mensagem clara.
            throw new IllegalStateException("Erro ao ler db.properties.", e);
        }
        return props;
    }

    /**
     * Abre uma nova conexão com o MySQL.
     * Quem chamar deve fechá-la depois; nos DAOs isso é feito com try-with-resources.
     *
     * [CONCEITO] throws SQLException: avisa quem chama que essa operação pode falhar
     * (banco desligado, senha errada...). Como SQLException é uma exceção CHECADA,
     * o compilador obriga quem chama a tratar (try/catch) ou repassar (throws) o erro.
     */
    public static Connection getConexao() throws SQLException {
        // A URL "jdbc:mysql://servidor:porta/banco" diz ao DriverManager qual driver usar e onde está o banco
        return DriverManager.getConnection(
                config.getProperty("db.url"),
                config.getProperty("db.usuario"),
                config.getProperty("db.senha"));
    }
}

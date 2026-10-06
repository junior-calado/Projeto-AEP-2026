package br.com.portalcidadao.service;

import br.com.portalcidadao.dao.UsuarioDAO;
import br.com.portalcidadao.model.Usuario;

import java.sql.SQLException;

/**
 * Regras de cadastro e entrada de usuários.
 *
 * [CONCEITO] Camada de serviço (regras de negócio): fica entre a tela e o banco.
 * Antes de mandar algo para o DAO, ela confere se a operação é permitida.
 * Se não for, lança uma RegraNegocioException e o DAO nem chega a ser chamado.
 * Assim as regras ficam em UM lugar só, e qualquer tela que use o service as respeita.
 */
public class UsuarioService {

    // O service USA o DAO para falar com o banco (ele não escreve SQL)
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * "Login" simples pelo CPF. A tabela usuario não tem senha,
     * então basta o CPF existir para a pessoa entrar.
     */
    public Usuario entrar(String cpf) throws SQLException {
        Usuario usuario = usuarioDAO.buscarPorCpf(formatarCpf(cpf));
        if (usuario == null) {
            // [CONCEITO] throw: interrompe o método na hora e "sobe" o erro para quem chamou
            // (no caso, o Menu, que mostra a mensagem na tela).
            throw new RegraNegocioException("Nenhum usuário encontrado com esse CPF.");
        }
        return usuario;
    }

    public Usuario cadastrarCidadao(String nome, String cpf, int idade, String cidade) throws SQLException {
        // Validações: se alguma falhar, o cadastro para aqui e nada é gravado
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome é obrigatório.");
        }
        // REGRA DE NEGÓCIO do trabalho: idade mínima de 16 anos para participar.
        // Ela também existe no banco (CHECK idade >= 16), mas checando aqui o usuário
        // recebe uma mensagem amigável em vez de um erro do MySQL.
        if (idade < Usuario.IDADE_MINIMA) {
            throw new RegraNegocioException("É preciso ter pelo menos " + Usuario.IDADE_MINIMA + " anos para participar.");
        }

        String cpfFormatado = formatarCpf(cpf);
        if (usuarioDAO.buscarPorCpf(cpfFormatado) != null) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com esse CPF.");
        }

        Usuario usuario = new Usuario(nome.trim(), cpfFormatado, idade, cidade == null ? null : cidade.trim());
        usuarioDAO.inserir(usuario);
        return usuario; // depois do inserir, o objeto já está com o id gerado pelo banco
    }

    /**
     * Aceita o CPF com ou sem pontuação e devolve no formato 000.000.000-00,
     * que é como ele fica gravado no banco.
     */
    private String formatarCpf(String cpf) {
        // replaceAll com a expressão regular "\\D" (qualquer coisa que NÃO é dígito) remove pontos e traço
        String numeros = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (numeros.length() != 11) {
            throw new RegraNegocioException("CPF inválido: digite os 11 números.");
        }
        return numeros.substring(0, 3) + "." + numeros.substring(3, 6) + "."
                + numeros.substring(6, 9) + "-" + numeros.substring(9);
    }
}

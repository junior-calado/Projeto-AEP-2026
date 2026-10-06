package br.com.portalcidadao.service;

import br.com.portalcidadao.dao.OrdenacaoProposta;
import br.com.portalcidadao.dao.PropostaDAO;
import br.com.portalcidadao.model.Proposta;
import br.com.portalcidadao.model.Usuario;
import br.com.portalcidadao.model.Vereador;

import java.sql.SQLException;
import java.util.List;

/**
 * Regras das propostas legislativas.
 *
 * Principal regra daqui: só VEREADOR cadastra proposta, e só o vereador AUTOR
 * pode editar ou excluir a proposta dele (RF06).
 */
public class PropostaService {

    private final PropostaDAO propostaDAO = new PropostaDAO();

    // Quando não há regra para aplicar, o service só repassa a chamada para o DAO.
    // Mesmo assim, a tela continua falando apenas com o service, sem pular camadas.
    public List<Proposta> listar(OrdenacaoProposta ordem) throws SQLException {
        return propostaDAO.listar(ordem);
    }

    public List<Proposta> pesquisar(String palavraChave) throws SQLException {
        if (palavraChave == null || palavraChave.isBlank()) {
            throw new RegraNegocioException("Digite uma palavra para pesquisar.");
        }
        return propostaDAO.pesquisar(palavraChave.trim());
    }

    /** Busca a proposta ou lança erro se ela não existir: quem chama nunca recebe null. */
    public Proposta buscar(int id) throws SQLException {
        Proposta proposta = propostaDAO.buscarPorId(id);
        if (proposta == null) {
            throw new RegraNegocioException("Proposta #" + id + " não encontrada.");
        }
        return proposta;
    }

    public Proposta cadastrar(Usuario usuario, String titulo, String descricao, String resumo) throws SQLException {
        // Polimorfismo de novo: isVereador() responde conforme a classe real do objeto
        if (!usuario.isVereador()) {
            throw new RegraNegocioException("Só vereadores podem cadastrar propostas.");
        }
        // Casting seguro: acabamos de conferir que o usuário é um Vereador
        Proposta proposta = new Proposta(titulo, descricao, resumo, (Vereador) usuario);
        validar(proposta);
        propostaDAO.inserir(proposta);
        return proposta;
    }

    /**
     * Busca a proposta já conferindo se o usuário pode alterá-la.
     * O Menu chama este método ANTES de pedir os novos dados, para não fazer o
     * usuário digitar tudo e só no final descobrir que não tinha permissão.
     */
    public Proposta buscarParaAlterar(Usuario usuario, int id) throws SQLException {
        Proposta proposta = buscar(id);
        if (!usuario.isVereador() || proposta.getVereador().getId() != usuario.getId()) {
            throw new RegraNegocioException("Só o vereador autor da proposta pode alterá-la ou excluí-la.");
        }
        return proposta;
    }

    public void editar(Usuario usuario, Proposta proposta) throws SQLException {
        buscarParaAlterar(usuario, proposta.getId()); // confere a permissão de novo antes de gravar
        validar(proposta);
        propostaDAO.atualizar(proposta);
    }

    public void excluir(Usuario usuario, int id) throws SQLException {
        buscarParaAlterar(usuario, id);
        propostaDAO.excluir(id);
    }

    /** Validações usadas tanto no cadastro quanto na edição: escritas uma vez, usadas nos dois. */
    private void validar(Proposta proposta) {
        if (proposta.getTitulo() == null || proposta.getTitulo().isBlank()) {
            throw new RegraNegocioException("O título é obrigatório.");
        }
        if (proposta.getTitulo().length() > 150) { // mesmo limite do VARCHAR(150) no banco
            throw new RegraNegocioException("O título pode ter no máximo 150 caracteres.");
        }
        if (proposta.getDescricao() == null || proposta.getDescricao().isBlank()) {
            throw new RegraNegocioException("A descrição é obrigatória.");
        }
        if (proposta.getResumo() != null && proposta.getResumo().length() > 500) {
            throw new RegraNegocioException("O resumo pode ter no máximo 500 caracteres.");
        }
        if (proposta.getVotosFavoraveis() < 0 || proposta.getVotosContrarios() < 0) {
            throw new RegraNegocioException("A quantidade de votos não pode ser negativa.");
        }
    }
}

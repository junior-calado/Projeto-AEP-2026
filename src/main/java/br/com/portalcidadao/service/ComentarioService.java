package br.com.portalcidadao.service;

import br.com.portalcidadao.dao.ComentarioDAO;
import br.com.portalcidadao.model.Comentario;
import br.com.portalcidadao.model.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * Regras dos comentários (RF02) e das curtidas (RF05):
 *   - comentário não pode ser vazio nem passar de 500 caracteres
 *   - só o autor edita ou exclui o próprio comentário
 *   - ninguém curte o próprio comentário, e cada pessoa curte no máximo uma vez
 */
public class ComentarioService {

    private final ComentarioDAO comentarioDAO = new ComentarioDAO();
    // Um service pode usar outro service para reaproveitar regras que já existem
    private final PropostaService propostaService = new PropostaService();

    public List<Comentario> listarPorProposta(int propostaId) throws SQLException {
        return comentarioDAO.listarPorProposta(propostaId);
    }

    public Comentario comentar(Usuario usuario, int propostaId, String texto) throws SQLException {
        propostaService.buscar(propostaId); // lança erro se a proposta não existir
        validarTexto(texto);
        Comentario comentario = new Comentario(usuario, propostaId, texto.trim());
        comentarioDAO.inserir(comentario);
        return comentario;
    }

    public void editar(Usuario usuario, int propostaId, int comentarioId, String novoTexto) throws SQLException {
        Comentario comentario = buscarDoAutor(usuario, propostaId, comentarioId);
        validarTexto(novoTexto);
        comentarioDAO.atualizarTexto(comentario.getId(), novoTexto.trim());
    }

    public void excluir(Usuario usuario, int propostaId, int comentarioId) throws SQLException {
        Comentario comentario = buscarDoAutor(usuario, propostaId, comentarioId);
        comentarioDAO.excluir(comentario.getId());
    }

    public void curtir(Usuario usuario, int propostaId, int comentarioId) throws SQLException {
        Comentario comentario = buscar(propostaId, comentarioId);
        if (comentario.getUsuario().getId() == usuario.getId()) {
            throw new RegraNegocioException("Você não pode curtir o seu próprio comentário.");
        }
        // A regra "só uma curtida por pessoa" é garantida pelo banco (veja ComentarioDAO.curtir);
        // aqui só transformamos o resultado em uma mensagem para o usuário.
        if (!comentarioDAO.curtir(usuario.getId(), comentarioId)) {
            throw new RegraNegocioException("Você já curtiu esse comentário.");
        }
    }

    /** Busca o comentário garantindo que ele é da proposta que está aberta na tela. */
    private Comentario buscar(int propostaId, int comentarioId) throws SQLException {
        Comentario comentario = comentarioDAO.buscarPorId(comentarioId);
        if (comentario == null || comentario.getPropostaId() != propostaId) {
            throw new RegraNegocioException("Comentário #" + comentarioId + " não encontrado nesta proposta.");
        }
        return comentario;
    }

    /** Igual ao buscar(), mas também exige que o comentário seja de quem está logado. */
    private Comentario buscarDoAutor(Usuario usuario, int propostaId, int comentarioId) throws SQLException {
        Comentario comentario = buscar(propostaId, comentarioId);
        if (comentario.getUsuario().getId() != usuario.getId()) {
            throw new RegraNegocioException("Você só pode alterar os seus próprios comentários.");
        }
        return comentario;
    }

    private void validarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new RegraNegocioException("O comentário não pode ficar vazio.");
        }
        if (texto.trim().length() > Comentario.TAMANHO_MAXIMO) {
            throw new RegraNegocioException("O comentário pode ter no máximo " + Comentario.TAMANHO_MAXIMO + " caracteres.");
        }
    }
}

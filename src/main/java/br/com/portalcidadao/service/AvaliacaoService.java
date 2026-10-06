package br.com.portalcidadao.service;

import br.com.portalcidadao.dao.AvaliacaoDAO;
import br.com.portalcidadao.model.Avaliacao;
import br.com.portalcidadao.model.Usuario;

import java.sql.SQLException;
import java.util.List;

/** Regras das avaliações por estrelas (RF03). */
public class AvaliacaoService {

    private final AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();
    private final PropostaService propostaService = new PropostaService();

    public List<Avaliacao> listarPorProposta(int propostaId) throws SQLException {
        return avaliacaoDAO.listarPorProposta(propostaId);
    }

    /** Devolve true se foi a primeira avaliação do usuário e false se ele trocou a nota. */
    public boolean avaliar(Usuario usuario, int propostaId, int estrelas) throws SQLException {
        // A nota precisa estar entre 1 e 5. O banco também confere isso (CHECK ck_avaliacao_estrela),
        // mas validar aqui permite mostrar uma mensagem clara.
        if (estrelas < Avaliacao.MIN_ESTRELAS || estrelas > Avaliacao.MAX_ESTRELAS) {
            throw new RegraNegocioException("A avaliação deve ser de "
                    + Avaliacao.MIN_ESTRELAS + " a " + Avaliacao.MAX_ESTRELAS + " estrelas.");
        }
        propostaService.buscar(propostaId); // lança erro se a proposta não existir
        return avaliacaoDAO.salvar(new Avaliacao(usuario, propostaId, estrelas));
    }
}

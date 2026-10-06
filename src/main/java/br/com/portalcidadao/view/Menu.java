package br.com.portalcidadao.view;

import br.com.portalcidadao.dao.OrdenacaoProposta;
import br.com.portalcidadao.model.Interacao;
import br.com.portalcidadao.model.Proposta;
import br.com.portalcidadao.model.SituacaoProposta;
import br.com.portalcidadao.model.Usuario;
import br.com.portalcidadao.service.AvaliacaoService;
import br.com.portalcidadao.service.ComentarioService;
import br.com.portalcidadao.service.PropostaService;
import br.com.portalcidadao.service.RegraNegocioException;
import br.com.portalcidadao.service.UsuarioService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * Interface de terminal: mostra os menus, lê o que o usuário digita e chama os serviços.
 *
 * [CONCEITO] Camada de apresentação (view): o Menu NÃO tem regra de negócio e NÃO escreve SQL.
 * Ele só pergunta, chama o service e mostra o resultado (ou a mensagem de erro).
 * Exemplo: quem decide que menor de 16 anos não pode se cadastrar é o UsuarioService;
 * o Menu só mostra a mensagem que veio de lá.
 */
public class Menu {

    // Scanner lê o que é digitado no teclado (System.in)
    private final Scanner scanner = new Scanner(System.in);
    private final UsuarioService usuarioService = new UsuarioService();
    private final PropostaService propostaService = new PropostaService();
    private final ComentarioService comentarioService = new ComentarioService();
    private final AvaliacaoService avaliacaoService = new AvaliacaoService();

    // Quem está usando o sistema no momento. null = ninguém entrou ainda.
    // Pode ser um Usuario ou um Vereador (herança), e os menus mudam conforme o tipo.
    private Usuario usuarioLogado;

    /**
     * Uma ação do menu que pode acessar o banco.
     *
     * [CONCEITO] Interface funcional: interface com um único método abstrato. Ela permite
     * passar um "pedaço de código" como parâmetro, usando lambda ou referência de método
     * (veja o método executar() lá embaixo e como ele é chamado nos menus).
     */
    private interface Acao {
        void executar() throws SQLException;
    }

    public void iniciar() {
        System.out.println("=========================================");
        System.out.println("            PORTAL DO CIDADÃO            ");
        System.out.println("=========================================");

        // Laço principal: enquanto ninguém escolher "Sair", o programa continua mostrando menus.
        // Se ninguém entrou, mostra o menu inicial; se alguém entrou, mostra o menu principal.
        boolean rodando = true;
        while (rodando) {
            if (usuarioLogado == null) {
                rodando = menuInicial();
            } else {
                menuPrincipal();
            }
        }
        System.out.println("Até logo!");
    }

    // ----------------------------------------------------------------
    // Menus
    // ----------------------------------------------------------------

    /** Devolve false quando o usuário escolhe sair do programa. */
    private boolean menuInicial() {
        System.out.println();
        System.out.println("1 - Entrar (com CPF)");
        System.out.println("2 - Cadastrar cidadão");
        System.out.println("0 - Sair");

        // [CONCEITO] switch com "->": cada case executa só a sua linha, sem precisar de break.
        // [CONCEITO] Referência de método: "this::entrar" passa o método entrar() como se fosse
        // um valor. Quem decide QUANDO executá-lo é o método executar().
        switch (lerInteiro("Opção: ")) {
            case 1 -> executar(this::entrar);
            case 2 -> executar(this::cadastrarCidadao);
            case 0 -> {
                return false;
            }
            default -> System.out.println("Opção inválida.");
        }
        return true;
    }

    private void menuPrincipal() {
        System.out.println();
        System.out.println("=== " + usuarioLogado.getNome() + " (" + usuarioLogado.getDescricaoTipo() + ") ===");
        System.out.println("1 - Listar propostas");
        System.out.println("2 - Pesquisar propostas por palavra-chave");
        System.out.println("3 - Ver detalhes de uma proposta (comentar, curtir, avaliar)");
        // O menu muda conforme o tipo de usuário: só o vereador vê as opções de gestão
        if (usuarioLogado.isVereador()) {
            System.out.println("4 - Cadastrar nova proposta");
            System.out.println("5 - Editar proposta (inclui situação e votação)");
            System.out.println("6 - Excluir proposta");
        }
        System.out.println("0 - Sair da conta");

        int opcao = lerInteiro("Opção: ");
        // Esconder a opção não basta: um cidadão poderia digitar 4 mesmo sem vê-la.
        // (E mesmo que passasse daqui, o PropostaService ainda bloquearia.)
        if (opcao >= 4 && opcao <= 6 && !usuarioLogado.isVereador()) {
            System.out.println("Opção inválida.");
            return;
        }

        switch (opcao) {
            case 1 -> executar(this::listarPropostas);
            case 2 -> executar(this::pesquisarPropostas);
            case 3 -> executar(this::verDetalhes);
            case 4 -> executar(this::cadastrarProposta);
            case 5 -> executar(this::editarProposta);
            case 6 -> executar(this::excluirProposta);
            case 0 -> {
                System.out.println("Você saiu da conta.");
                usuarioLogado = null; // volta para o menu inicial no próximo giro do laço
            }
            default -> System.out.println("Opção inválida.");
        }
    }

    // ----------------------------------------------------------------
    // Usuário
    // ----------------------------------------------------------------

    // Repare no padrão que se repete em todas as ações daqui para baixo:
    //   1. ler os dados do teclado
    //   2. chamar o service
    //   3. mostrar a mensagem de sucesso
    // Se o service lançar um erro, o passo 3 não acontece e o executar() mostra a mensagem do erro.

    private void entrar() throws SQLException {
        String cpf = lerTexto("CPF: ");
        usuarioLogado = usuarioService.entrar(cpf);
        System.out.println("Bem-vindo(a), " + usuarioLogado.getNome() + "!");
    }

    private void cadastrarCidadao() throws SQLException {
        System.out.println("\n--- Cadastro de cidadão ---");
        String nome = lerTexto("Nome completo: ");
        String cpf = lerTexto("CPF: ");
        int idade = lerInteiro("Idade: ");
        String cidade = lerTexto("Cidade: ");

        usuarioLogado = usuarioService.cadastrarCidadao(nome, cpf, idade, cidade);
        System.out.println("Cadastro realizado! Você já está conectado(a).");
    }

    // ----------------------------------------------------------------
    // Consulta de propostas
    // ----------------------------------------------------------------

    private void listarPropostas() throws SQLException {
        System.out.println("\nComo deseja ordenar?");
        // values() devolve um array com todas as constantes do enum, na ordem em que foram declaradas
        OrdenacaoProposta[] ordens = OrdenacaoProposta.values();
        for (int i = 0; i < ordens.length; i++) {
            System.out.println((i + 1) + " - " + ordens[i].getDescricao());
        }
        int escolha = lerInteiro("Opção: ");
        if (escolha < 1 || escolha > ordens.length) {
            System.out.println("Opção inválida.");
            return;
        }

        // A tela mostra 1, 2, 3, mas o array começa no índice 0, daí o "- 1"
        imprimirLista(propostaService.listar(ordens[escolha - 1]));
    }

    private void pesquisarPropostas() throws SQLException {
        String palavra = lerTexto("Palavra-chave: ");
        imprimirLista(propostaService.pesquisar(palavra));
    }

    private void imprimirLista(List<Proposta> propostas) {
        System.out.println();
        if (propostas.isEmpty()) {
            System.out.println("Nenhuma proposta encontrada.");
            return;
        }
        System.out.println("ID | Título | Situação | Publicação | Autor | Avaliação");
        // for-each: percorre a lista inteira, um elemento por vez
        for (Proposta p : propostas) {
            System.out.println(p.linhaListagem());
        }
        System.out.println("\nUse a opção 3 do menu para ver os detalhes de uma proposta.");
    }

    private void verDetalhes() throws SQLException {
        int id = lerInteiro("ID da proposta: ");
        propostaService.buscar(id); // se não existir, o erro aparece aqui e nem entra no submenu

        boolean voltar = false;
        while (!voltar) {
            // Busca de novo a cada volta do laço, para a tela já mostrar o comentário
            // ou a avaliação que acabou de ser feita
            imprimirDetalhes(propostaService.buscar(id));

            System.out.println("1 - Comentar");
            System.out.println("2 - Editar meu comentário");
            System.out.println("3 - Excluir meu comentário");
            System.out.println("4 - Curtir um comentário");
            System.out.println("5 - Avaliar a proposta (1 a 5 estrelas)");
            System.out.println("0 - Voltar");

            // [CONCEITO] Lambda: "() -> comentar(id)" cria uma Acao na hora. Foi usada aqui
            // (em vez de this::comentar) porque esses métodos precisam receber o id da proposta.
            switch (lerInteiro("Opção: ")) {
                case 1 -> executar(() -> comentar(id));
                case 2 -> executar(() -> editarComentario(id));
                case 3 -> executar(() -> excluirComentario(id));
                case 4 -> executar(() -> curtirComentario(id));
                case 5 -> executar(() -> avaliar(id));
                case 0 -> voltar = true;
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private void imprimirDetalhes(Proposta proposta) throws SQLException {
        System.out.println("\n=========================================");
        System.out.print(proposta.detalhes());

        // [CONCEITO] POLIMORFISMO NA PRÁTICA (o exemplo principal do trabalho):
        // a lista é do tipo Interacao, mas guarda objetos Comentario E Avaliacao misturados.
        // No for lá embaixo chamamos interacao.exibirInteracao() sem saber qual é qual,
        // e o Java executa automaticamente a versão certa de cada objeto.
        List<Interacao> manifestacoes = new ArrayList<>();
        manifestacoes.addAll(comentarioService.listarPorProposta(proposta.getId()));
        manifestacoes.addAll(avaliacaoService.listarPorProposta(proposta.getId()));
        // Coloca tudo em ordem de data. Funciona para os dois tipos porque getDataRegistro()
        // foi herdado da classe mãe Interacao.
        manifestacoes.sort(Comparator.comparing(Interacao::getDataRegistro));

        System.out.println("\nManifestações dos cidadãos:");
        if (manifestacoes.isEmpty()) {
            System.out.println("  Ninguém se manifestou ainda. Seja o primeiro!");
        }
        for (Interacao interacao : manifestacoes) {
            System.out.println("  " + interacao.exibirInteracao());
        }
        System.out.println("=========================================");
    }

    // ----------------------------------------------------------------
    // Comentários, curtidas e avaliações
    // ----------------------------------------------------------------

    private void comentar(int propostaId) throws SQLException {
        String texto = lerTexto("Seu comentário: ");
        comentarioService.comentar(usuarioLogado, propostaId, texto);
        System.out.println("Comentário publicado!");
    }

    private void editarComentario(int propostaId) throws SQLException {
        int comentarioId = lerInteiro("Número do comentário: ");
        String texto = lerTexto("Novo texto: ");
        comentarioService.editar(usuarioLogado, propostaId, comentarioId, texto);
        System.out.println("Comentário editado!");
    }

    private void excluirComentario(int propostaId) throws SQLException {
        int comentarioId = lerInteiro("Número do comentário: ");
        if (confirmar("Tem certeza que deseja excluir o comentário #" + comentarioId + "?")) {
            comentarioService.excluir(usuarioLogado, propostaId, comentarioId);
            System.out.println("Comentário excluído.");
        }
    }

    private void curtirComentario(int propostaId) throws SQLException {
        int comentarioId = lerInteiro("Número do comentário: ");
        comentarioService.curtir(usuarioLogado, propostaId, comentarioId);
        System.out.println("Curtida registrada!");
    }

    private void avaliar(int propostaId) throws SQLException {
        int estrelas = lerInteiro("Quantas estrelas (1 a 5)? ");
        boolean nova = avaliacaoService.avaliar(usuarioLogado, propostaId, estrelas);
        System.out.println(nova ? "Avaliação registrada!" : "Sua avaliação foi atualizada!");
    }

    // ----------------------------------------------------------------
    // Gestão de propostas (somente vereador)
    // ----------------------------------------------------------------

    private void cadastrarProposta() throws SQLException {
        System.out.println("\n--- Nova proposta ---");
        String titulo = lerTexto("Título: ");
        String descricao = lerTexto("Descrição (texto da proposta): ");
        String resumo = lerTexto("Resumo em linguagem simples (opcional): ");

        // Resumo em branco vira null, que é gravado como NULL no banco
        Proposta proposta = propostaService.cadastrar(usuarioLogado, titulo, descricao,
                resumo.isBlank() ? null : resumo);
        System.out.println("Proposta #" + proposta.getId() + " cadastrada!");
    }

    private void editarProposta() throws SQLException {
        int id = lerInteiro("ID da proposta: ");
        // Confere a permissão ANTES de pedir os dados (veja o comentário no PropostaService)
        Proposta proposta = propostaService.buscarParaAlterar(usuarioLogado, id);

        // A edição altera o objeto em memória com os setters e, no final,
        // manda o objeto inteiro para o service gravar.
        System.out.println("\nDeixe em branco (só aperte Enter) para manter o valor atual.");
        proposta.setTitulo(lerTextoOuManter("Título", proposta.getTitulo()));
        proposta.setDescricao(lerTextoOuManter("Descrição", proposta.getDescricao()));
        proposta.setResumo(lerTextoOuManter("Resumo", proposta.getResumo()));

        System.out.println("Situação atual: " + proposta.getSituacao().getDescricao());
        SituacaoProposta[] situacoes = SituacaoProposta.values();
        for (int i = 0; i < situacoes.length; i++) {
            System.out.println("  " + (i + 1) + " - " + situacoes[i].getDescricao());
        }
        Integer escolha = lerInteiroOpcional("Nova situação: ");
        if (escolha != null) {
            if (escolha < 1 || escolha > situacoes.length) {
                throw new RegraNegocioException("Situação inválida.");
            }
            proposta.setSituacao(situacoes[escolha - 1]);
        }

        // Só faz sentido informar votos depois que a proposta foi votada (RF09)
        if (proposta.getSituacao() != SituacaoProposta.EM_ANALISE) {
            Integer favoraveis = lerInteiroOpcional("Votos favoráveis [" + proposta.getVotosFavoraveis() + "]: ");
            if (favoraveis != null) {
                proposta.setVotosFavoraveis(favoraveis);
            }
            Integer contrarios = lerInteiroOpcional("Votos contrários [" + proposta.getVotosContrarios() + "]: ");
            if (contrarios != null) {
                proposta.setVotosContrarios(contrarios);
            }
        }

        propostaService.editar(usuarioLogado, proposta);
        System.out.println("Proposta atualizada!");
    }

    private void excluirProposta() throws SQLException {
        int id = lerInteiro("ID da proposta: ");
        Proposta proposta = propostaService.buscarParaAlterar(usuarioLogado, id);

        if (confirmar("Excluir \"" + proposta.getTitulo() + "\" e todos os comentários e avaliações dela?")) {
            propostaService.excluir(usuarioLogado, id);
            System.out.println("Proposta excluída.");
        }
    }

    // ----------------------------------------------------------------
    // Utilitários de entrada e tratamento de erros
    // ----------------------------------------------------------------

    /**
     * Executa a ação e, se der erro, mostra uma mensagem amigável em vez de derrubar o programa.
     *
     * [CONCEITO] Tratamento de exceções (try/catch): todos os erros lançados lá no service
     * ("throw new RegraNegocioException") ou no DAO (SQLException) sobem até aqui.
     * Como todas as ações passam por este método, o tratamento de erro fica em um lugar só,
     * em vez de repetir try/catch em cada opção do menu.
     */
    private void executar(Acao acao) {
        try {
            acao.executar();
        } catch (RegraNegocioException e) {
            System.out.println("Atenção: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Erro ao acessar o banco de dados: " + e.getMessage());
        }
    }

    private String lerLinha() {
        // Se a entrada acabar (Ctrl+D / Ctrl+Z), responde "0" para ir voltando e sair dos menus
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "0";
    }

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return lerLinha();
    }

    private String lerTextoOuManter(String campo, String valorAtual) {
        System.out.println(campo + " atual: " + (valorAtual == null ? "(vazio)" : valorAtual));
        String novo = lerTexto("Novo valor: ");
        return novo.isBlank() ? valorAtual : novo;
    }

    /** Fica perguntando até o usuário digitar um número válido. */
    private int lerInteiro(String mensagem) {
        while (true) {
            Integer numero = lerInteiroOpcional(mensagem);
            if (numero != null) {
                return numero;
            }
            System.out.println("Digite um número.");
        }
    }

    /**
     * Lê um número e devolve null se o usuário só apertar Enter ou digitar algo que não é número.
     *
     * [CONCEITO] Integer x int: int é um tipo primitivo e não aceita null. Integer é a
     * "classe empacotadora" (wrapper) do int e aceita null, que aqui significa "não informou".
     */
    private Integer lerInteiroOpcional(String mensagem) {
        String texto = lerTexto(mensagem);
        try {
            return texto.isBlank() ? null : Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            // parseInt lança essa exceção quando o texto não é número (ex.: "abc")
            return null;
        }
    }

    private boolean confirmar(String pergunta) {
        String resposta = lerTexto(pergunta + " (s/n): ");
        return resposta.equalsIgnoreCase("s");
    }
}

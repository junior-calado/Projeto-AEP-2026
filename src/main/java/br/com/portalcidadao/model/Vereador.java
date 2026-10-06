package br.com.portalcidadao.model;

/**
 * Vereador é um usuário que também tem partido e pode
 * cadastrar, editar e excluir propostas.
 *
 * [CONCEITO] Herança ("extends"): Vereador herda TUDO de Usuario (nome, cpf, idade, getters...)
 * e só acrescenta o que é dele (o partido). Lemos assim: "todo Vereador É UM Usuario".
 * Por isso, em qualquer lugar do sistema que espera um Usuario, também é possível passar um Vereador.
 */
public class Vereador extends Usuario {

    private String partido;

    public Vereador(int id, String nome, String cpf, int idade, String cidade, String partido) {
        // [CONCEITO] super(...): chama o construtor da classe mãe (Usuario) para preencher
        // os atributos herdados. Precisa ser a primeira linha do construtor.
        super(id, nome, cpf, idade, cidade);
        this.partido = partido;
    }

    // [CONCEITO] Sobrescrita (@Override): o método já existe em Usuario, mas aqui ele ganha
    // um comportamento diferente. A anotação faz o compilador conferir se o método da
    // classe mãe existe mesmo (se você errar o nome, ele avisa).

    @Override
    public String getTipo() {
        return "VEREADOR";
    }

    @Override
    public boolean isVereador() {
        return true;
    }

    @Override
    public String getDescricaoTipo() {
        return "Vereador - " + partido;
    }

    public String getPartido() {
        return partido;
    }
}

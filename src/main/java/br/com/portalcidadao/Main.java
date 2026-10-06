package br.com.portalcidadao;

import br.com.portalcidadao.view.Menu;

/**
 * Ponto de entrada do Portal do Cidadão (versão terminal).
 *
 * [CONCEITO] Arquitetura em camadas: o sistema é dividido em pacotes, e cada camada
 * tem UMA responsabilidade e só conversa com a camada logo abaixo dela:
 *
 *   view     (Menu)          -> mostra as telas e lê o que o usuário digita
 *      |
 *   service  (XxxService)    -> aplica as regras de negócio (ex.: idade mínima de 16 anos)
 *      |
 *   dao      (XxxDAO)        -> executa os comandos SQL no MySQL
 *      |
 *   banco    (MySQL)
 *
 * As classes do pacote model (Usuario, Proposta, Comentario...) são os "dados"
 * que viajam entre essas camadas.
 *
 * Vantagem: se um dia o terminal virar um site, só a camada view muda;
 * as regras e o acesso ao banco continuam os mesmos.
 *
 * Sugestão de ordem de leitura: model -> dao (comece pela Conexao) -> service -> view.
 */
public class Main {

    // [CONCEITO] Método main: é o método que a JVM procura para iniciar o programa.
    // Precisa ser "public static void main(String[] args)".
    public static void main(String[] args) {
        new Menu().iniciar();
    }
}

-- =============================================================
-- Portal do Cidadão — script de criação do banco (MySQL 8.0.16+)
-- Tabelas: usuario, proposta, comentario, curtida e avaliacao.
--
-- ATENÇÃO: se você já rodou uma versão antiga deste script, apague
-- o banco antes (DROP DATABASE portal_cidadao;) e rode de novo,
-- porque o CREATE TABLE IF NOT EXISTS não altera tabelas que já existem.
-- =============================================================

-- Garante que os acentos sejam gravados certo, qualquer que seja o cliente.
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS portal_cidadao
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE portal_cidadao;

-- -------------------------------------------------------------
-- Tabela usuario
-- Guarda cidadãos e vereadores na mesma tabela.
-- tipo_usuario diferencia os dois ('CIDADAO' ou 'VEREADOR');
-- partido só é preenchido quando o usuário é vereador.
-- Idade mínima de 16 anos para participar.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    usuario_id    INT          NOT NULL AUTO_INCREMENT,
    nome          VARCHAR(100) NOT NULL,
    cpf           VARCHAR(20)  NOT NULL,
    idade         INT          NOT NULL,
    cidade        VARCHAR(100),
    tipo_usuario  VARCHAR(20)  NOT NULL,
    partido       VARCHAR(100),
    CONSTRAINT pk_usuario PRIMARY KEY (usuario_id),
    CONSTRAINT uq_usuario_cpf UNIQUE (cpf),
    CONSTRAINT ck_usuario_tipo CHECK (tipo_usuario IN ('CIDADAO', 'VEREADOR')),
    CONSTRAINT ck_usuario_idade CHECK (idade >= 16)
);

-- -------------------------------------------------------------
-- Tabela proposta
-- Cada proposta pertence a um vereador (vereador_id -> usuario).
-- A FK só garante que o usuário existe; quem confere se ele é
-- mesmo VEREADOR é a camada de serviço no Java.
--   resumo           -> explicação em linguagem simples (RF10)
--   situacao         -> andamento da tramitação (RF08)
--   votos_*          -> resultado da votação (RF09)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS proposta (
    proposta_id       INT          NOT NULL AUTO_INCREMENT,
    titulo            VARCHAR(150) NOT NULL,
    descricao         TEXT         NOT NULL,
    resumo            VARCHAR(500),
    data_publicacao   DATE         NOT NULL,
    situacao          VARCHAR(20)  NOT NULL DEFAULT 'EM_ANALISE',
    votos_favoraveis  INT          NOT NULL DEFAULT 0,
    votos_contrarios  INT          NOT NULL DEFAULT 0,
    vereador_id       INT          NOT NULL,
    CONSTRAINT pk_proposta PRIMARY KEY (proposta_id),
    CONSTRAINT fk_proposta_vereador FOREIGN KEY (vereador_id)
        REFERENCES usuario (usuario_id),
    CONSTRAINT ck_proposta_situacao CHECK (situacao IN ('EM_ANALISE', 'APROVADA', 'REJEITADA')),
    CONSTRAINT ck_proposta_votos CHECK (votos_favoraveis >= 0 AND votos_contrarios >= 0)
);

-- -------------------------------------------------------------
-- Tabela comentario (RF02)
-- ON DELETE CASCADE: ao excluir a proposta, os comentários dela vão junto.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comentario (
    comentario_id  INT          NOT NULL AUTO_INCREMENT,
    texto          VARCHAR(500) NOT NULL,
    data_registro  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id     INT          NOT NULL,
    proposta_id    INT          NOT NULL,
    CONSTRAINT pk_comentario PRIMARY KEY (comentario_id),
    CONSTRAINT fk_comentario_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (usuario_id),
    CONSTRAINT fk_comentario_proposta FOREIGN KEY (proposta_id)
        REFERENCES proposta (proposta_id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- Tabela curtida (RF05)
-- Uma linha por "usuário curtiu comentário". A chave primária
-- composta impede que a mesma pessoa curta o mesmo comentário duas vezes.
-- A quantidade de curtidas é calculada com COUNT nesta tabela.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS curtida (
    usuario_id     INT NOT NULL,
    comentario_id  INT NOT NULL,
    CONSTRAINT pk_curtida PRIMARY KEY (usuario_id, comentario_id),
    CONSTRAINT fk_curtida_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (usuario_id),
    CONSTRAINT fk_curtida_comentario FOREIGN KEY (comentario_id)
        REFERENCES comentario (comentario_id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- Tabela avaliacao (RF03)
-- Nota de 1 a 5 estrelas. Cada usuário avalia uma proposta uma única
-- vez (uq_avaliacao); se avaliar de novo, a nota é atualizada.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avaliacao (
    avaliacao_id   INT      NOT NULL AUTO_INCREMENT,
    estrela        INT      NOT NULL,
    data_registro  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id     INT      NOT NULL,
    proposta_id    INT      NOT NULL,
    CONSTRAINT pk_avaliacao PRIMARY KEY (avaliacao_id),
    CONSTRAINT uq_avaliacao UNIQUE (usuario_id, proposta_id),
    CONSTRAINT ck_avaliacao_estrela CHECK (estrela BETWEEN 1 AND 5),
    CONSTRAINT fk_avaliacao_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (usuario_id),
    CONSTRAINT fk_avaliacao_proposta FOREIGN KEY (proposta_id)
        REFERENCES proposta (proposta_id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- Dados de teste (nomes fictícios)
-- INSERT IGNORE: se o script rodar de novo, não duplica.
-- -------------------------------------------------------------
INSERT IGNORE INTO usuario (usuario_id, nome, cpf, idade, cidade, tipo_usuario, partido) VALUES
    (1, 'Carlos Almeida',  '111.111.111-11', 52, 'Cianorte', 'VEREADOR', 'Partido A'),
    (2, 'Juliana Costa',   '222.222.222-22', 38, 'Cianorte', 'VEREADOR', 'Partido B'),
    (3, 'Rafael Moreira',  '333.333.333-33', 19, 'Cianorte', 'CIDADAO',  NULL),
    (4, 'Mariana Souza',   '444.444.444-44', 27, 'Cianorte', 'CIDADAO',  NULL);

INSERT IGNORE INTO proposta (proposta_id, titulo, descricao, resumo, data_publicacao,
                             situacao, votos_favoraveis, votos_contrarios, vereador_id) VALUES
    (1, 'Ciclovia na Avenida América',
        'Cria uma ciclovia de 3 km ligando o centro ao terminal rodoviário.',
        'A prefeitura vai fazer uma faixa só para bicicletas na Avenida América, do centro até a rodoviária.',
        '2026-09-01', 'EM_ANALISE', 0, 0, 1),
    (2, 'Iluminação em LED nas praças',
        'Substitui a iluminação das praças públicas por lâmpadas de LED para reduzir o consumo de energia.',
        'As lâmpadas das praças vão ser trocadas por LED, que gasta menos energia e ilumina melhor.',
        '2026-09-10', 'APROVADA', 9, 2, 2),
    (3, 'Coleta seletiva nos bairros',
        'Amplia a coleta seletiva para todos os bairros, com recolhimento semanal de recicláveis.',
        'O caminhão de recicláveis vai passar uma vez por semana em todos os bairros da cidade.',
        '2026-09-15', 'EM_ANALISE', 0, 0, 1);

INSERT IGNORE INTO comentario (comentario_id, texto, data_registro, usuario_id, proposta_id) VALUES
    (1, 'Ótima ideia, vou usar muito a ciclovia para ir trabalhar!', '2026-09-03 10:15:00', 3, 1),
    (2, 'Precisa pensar também na iluminação da ciclovia à noite.',  '2026-09-04 19:40:00', 4, 1),
    (3, 'Já passou da hora de trocar essas lâmpadas.',               '2026-09-12 08:05:00', 3, 2);

INSERT IGNORE INTO curtida (usuario_id, comentario_id) VALUES
    (4, 1),
    (3, 2);

INSERT IGNORE INTO avaliacao (avaliacao_id, estrela, data_registro, usuario_id, proposta_id) VALUES
    (1, 5, '2026-09-03 10:16:00', 3, 1),
    (2, 4, '2026-09-04 19:41:00', 4, 1),
    (3, 4, '2026-09-12 08:06:00', 3, 2);

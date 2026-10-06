

SELECT * FROM usuario;

SELECT nome, partido
FROM usuario
WHERE tipo_usuario = 'VEREADOR';

SELECT usuario_id, nome, cpf, idade, cidade, tipo_usuario, partido
FROM usuario
WHERE cpf = '104.268.129-56';

SELECT proposta_id, titulo, vereador_id FROM proposta;

SELECT p.proposta_id, p.titulo, u.nome AS autor, u.partido
FROM proposta p
JOIN usuario u ON u.usuario_id = p.vereador_id;

SELECT p.titulo, a.estrela
FROM proposta p
JOIN avaliacao a ON a.proposta_id = p.proposta_id;

SELECT p.titulo, a.estrela
FROM proposta p
LEFT JOIN avaliacao a ON a.proposta_id = p.proposta_id;

SELECT p.titulo,
       COALESCE(AVG(a.estrela), 0) AS media_estrelas,
       COUNT(a.avaliacao_id)       AS total_avaliacoes
FROM proposta p
LEFT JOIN avaliacao a ON a.proposta_id = p.proposta_id
GROUP BY p.proposta_id
ORDER BY media_estrelas DESC;

SELECT p.proposta_id, p.titulo, p.situacao, p.data_publicacao,
       u.nome AS autor,
       COALESCE(AVG(a.estrela), 0) AS media_estrelas,
       COUNT(a.avaliacao_id)       AS total_avaliacoes
FROM proposta p
JOIN usuario u        ON u.usuario_id = p.vereador_id
LEFT JOIN avaliacao a ON a.proposta_id = p.proposta_id
GROUP BY p.proposta_id, u.usuario_id
ORDER BY p.data_publicacao DESC;

SELECT u.nome, COUNT(p.proposta_id) AS total_propostas
FROM usuario u
LEFT JOIN proposta p ON p.vereador_id = u.usuario_id
WHERE u.tipo_usuario = 'VEREADOR'
GROUP BY u.usuario_id;

SELECT proposta_id, titulo
FROM proposta
WHERE titulo LIKE '%ILUMINACAO%'
   OR descricao LIKE '%ILUMINACAO%'
   OR resumo LIKE '%ILUMINACAO%';

SELECT c.comentario_id, u.nome AS autor, c.texto, c.data_registro,
       (SELECT COUNT(*) FROM curtida ct WHERE ct.comentario_id = c.comentario_id) AS curtidas
FROM comentario c
JOIN usuario u ON u.usuario_id = c.usuario_id
WHERE c.proposta_id = 1
ORDER BY c.data_registro;

SELECT quem_curtiu.nome AS quem_curtiu,
       autor.nome       AS autor_do_comentario,
       c.texto
FROM curtida ct
JOIN usuario quem_curtiu ON quem_curtiu.usuario_id = ct.usuario_id
JOIN comentario c        ON c.comentario_id = ct.comentario_id
JOIN usuario autor       ON autor.usuario_id = c.usuario_id;

SELECT 'Comentário' AS tipo, u.nome, c.texto AS conteudo, c.data_registro
FROM comentario c
JOIN usuario u ON u.usuario_id = c.usuario_id
WHERE c.proposta_id = 1
UNION ALL
SELECT 'Avaliação', u.nome, CONCAT(a.estrela, ' estrelas'), a.data_registro
FROM avaliacao a
JOIN usuario u ON u.usuario_id = a.usuario_id
WHERE a.proposta_id = 1
ORDER BY data_registro;

SELECT u.nome,
       (SELECT COUNT(*) FROM comentario c WHERE c.usuario_id = u.usuario_id) AS comentarios,
       (SELECT COUNT(*) FROM avaliacao a  WHERE a.usuario_id = u.usuario_id) AS avaliacoes,
       (SELECT COUNT(*) FROM curtida ct   WHERE ct.usuario_id = u.usuario_id) AS curtidas_dadas
FROM usuario u
ORDER BY comentarios + avaliacoes + curtidas_dadas DESC;

INSERT INTO usuario (nome, cpf, idade, cidade, tipo_usuario)
VALUES ('Teste Menor', '999.999.999-99', 15, 'Cianorte', 'CIDADAO');

INSERT INTO avaliacao (estrela, usuario_id, proposta_id) VALUES (6, 4, 3);

INSERT INTO curtida (usuario_id, comentario_id) VALUES (4, 1);

START TRANSACTION;

SELECT
    (SELECT COUNT(*) FROM comentario WHERE proposta_id = 1) AS comentarios,
    (SELECT COUNT(*) FROM avaliacao  WHERE proposta_id = 1) AS avaliacoes;

DELETE FROM proposta WHERE proposta_id = 1;

SELECT
    (SELECT COUNT(*) FROM comentario WHERE proposta_id = 1) AS comentarios,
    (SELECT COUNT(*) FROM avaliacao  WHERE proposta_id = 1) AS avaliacoes;

ROLLBACK;

SELECT proposta_id, titulo FROM proposta WHERE proposta_id = 1;

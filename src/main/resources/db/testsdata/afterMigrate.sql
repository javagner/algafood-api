SET FOREIGN_KEY_CHECKS=0;

DELETE FROM cidade;
DELETE FROM cozinha;
DELETE FROM estado;
DELETE FROM forma_pagamento;
DELETE FROM grupo;
DELETE FROM grupo_permissao;
DELETE FROM permissao;
DELETE FROM produto;
DELETE FROM restaurante;
DELETE FROM restaurante_forma_pagamento;
DELETE FROM usuario;
DELETE FROM usuario_grupo;

SET FOREIGN_KEY_CHECKS=1;

alter table cidade auto_increment = 1;
alter table cozinha auto_increment = 1;
alter table estado auto_increment = 1;
alter table forma_pagamento auto_increment = 1;
alter table grupo auto_increment = 1;
alter table permissao  auto_increment = 1;
alter table produto auto_increment = 1;
alter table restaurante auto_increment = 1;
alter table usuario  auto_increment = 1;

INSERT INTO cozinha (id, nome) VALUES (1, 'Tailandesa');
INSERT INTO cozinha (id, nome) VALUES (2, 'Indiana');



INSERT INTO algafood2.estado (id, nome) VALUES(1, 'Parana');
INSERT INTO algafood2.estado (id, nome) VALUES(2, 'Sao Paulo');



INSERT INTO algafood2.cidade (nome, estado_id) VALUES('Londrina', 1);
INSERT INTO algafood2.cidade (nome, estado_id) VALUES('Maringa', 1);
INSERT INTO algafood2.cidade (nome, estado_id) VALUES('Campinas', 2);



insert into algafood2.restaurante (id, nome, taxa_frete, cozinha_id, data_cadastro, data_atualizacao, cidade_id, cep, logradouro, numero, bairro) values (1, 'Thai Gourmet', 10, 1, utc_timestamp, utc_timestamp, 1, '38400-999', 'Rua João Pinheiro', '1000', 'Centro');
insert into algafood2.restaurante (id, nome, taxa_frete, cozinha_id, data_cadastro, data_atualizacao) values (2, 'Thai Delivery', 9.50, 1, utc_timestamp, utc_timestamp);
insert into algafood2.restaurante (id, nome, taxa_frete, cozinha_id, data_cadastro, data_atualizacao) values (3, 'Tuk Tuk Comida Indiana', 15, 2, utc_timestamp, utc_timestamp);



INSERT INTO algafood2.forma_pagamento (descricao) VALUES('Dinheiro');
INSERT INTO algafood2.forma_pagamento (descricao) VALUES('Boleto');
INSERT INTO algafood2.forma_pagamento (descricao) VALUES('Cartão');



INSERT INTO algafood2.permissao (descricao, nome) VALUES('consulta produtos', 'Permite apenas a consulta de produtos');
INSERT INTO algafood2.permissao (descricao, nome) VALUES('gerencia produtos', 'Permissão total sobre produtos');



INSERT INTO algafood2.restaurante_forma_pagamento (rastaurante_id, forma_pagamento_id) VALUES(1, 1), (1, 2), (1, 3), (2, 1), (2, 2);

-- EASY HEALTH
-- 40 USUÁRIOS DE DEMONSTRAÇÃO
-- MySQL 8+
--
-- 10 pacientes
-- 10 médicos
-- 10 funcionários
-- 10 donos
--
-- Senha de todas as contas: senha123
--
-- Não modifica registros existentes.


START TRANSACTION;


-- PACIENTES (10)


INSERT IGNORE INTO users (
    id, nome, email, senha_hash, telefone,
    data_nascimento, tipo_sanguineo, alergias,
    medicamentos_em_uso, plano_de_saude,
    email_verificado, cargo
)
VALUES
(
    'demo-paciente-01', 'Maria Silva',
    'maria.silva@email.com',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(11) 99999-9999', '1990-05-12', 'O+',
    'Nenhuma', NULL, 'Plano Horizonte', TRUE, 'paciente'
),
(
    'demo-paciente-02', 'Pedro Santos',
    'pedro.santos@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(19) 98888-4444', '1998-11-19', 'AB+',
    'Pólen', 'Antialérgico conforme prescrição',
    'Plano Bem Estar', TRUE, 'paciente'
),
(
    'demo-paciente-03', 'Camila Oliveira',
    'camila.oliveira@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(21) 98888-5555', '2001-07-08', 'O-',
    'Amoxicilina', NULL, 'Plano Horizonte', TRUE, 'paciente'
),
(
    'demo-paciente-04', 'Julia Ferreira',
    'julia.ferreira@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-1001', '1995-03-15', 'A+',
    'Lactose', NULL, 'Plano Horizonte', TRUE, 'paciente'
),
(
    'demo-paciente-05', 'Lucas Ribeiro',
    'lucas.ribeiro@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-1002', '2003-06-22', 'O+',
    NULL, NULL, 'Plano Vida Total', TRUE, 'paciente'
),
(
    'demo-paciente-06', 'Mariana Souza',
    'mariana.souza@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-1003', '1989-12-10', 'B-',
    'Dipirona', NULL, 'Plano Bem Estar', TRUE, 'paciente'
),
(
    'demo-paciente-07', 'Gabriel Lima',
    'gabriel.lima@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-1004', '1997-08-04', 'AB+',
    NULL, 'Vitamina D', NULL, TRUE, 'paciente'
),
(
    'demo-paciente-08', 'Beatriz Martins',
    'beatriz.martins@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-1005', '2000-01-19', 'O-',
    'Amendoim', NULL, 'Plano Horizonte', TRUE, 'paciente'
),
(
    'demo-paciente-09', 'Ricardo Nunes',
    'ricardo.nunes@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-1006', '1993-04-25', 'A-',
    NULL, NULL, 'Plano Saúde Mais', TRUE, 'paciente'
),
(
    'demo-paciente-10', 'Isabela Rocha',
    'isabela.rocha@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-1007', '2002-09-14', 'B+',
    'Frutos do mar', NULL, 'Plano Horizonte', TRUE, 'paciente'
);

-- MÉDICOS (10)

INSERT INTO users (id, nome, email, senha_hash, telefone, email_verificado, cargo)
VALUES
  ('demo-funcionario', 'Carlos Funcionário', 'carlos.funcionario@email.com',
   '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby', '(11) 98888-0001', true, 'funcionario'),
  ('demo-medico', 'Dra. Ana Médica', 'ana.medica@email.com',
   '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby', '(11) 98888-0002', true, 'medico'),
  ('demo-dono', 'Roberto Dono', 'roberto.dono@email.com',
   '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby', '(11) 98888-0003', true, 'dono')
AS incoming
ON DUPLICATE KEY UPDATE id = users.id;
COMMIT;

```sql
-- ============================================================
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
-- ============================================================

START TRANSACTION;

-- ============================================================
-- PACIENTES (10)
-- ============================================================

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


-- ============================================================
-- MÉDICOS (10)
-- ============================================================

INSERT IGNORE INTO users (
    id, nome, email, senha_hash, telefone,
    data_nascimento, tipo_sanguineo,
    email_verificado, cargo
)
VALUES
(
    'demo-medico-01', 'Ana Costa',
    'ana.costa@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(11) 98888-1111', '1985-04-12', 'O+', TRUE, 'medico'
),
(
    'demo-medico-02', 'Carlos Eduardo',
    'carlos.eduardo@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-2001', '1980-05-20', 'A+', TRUE, 'medico'
),
(
    'demo-medico-03', 'Patricia Gomes',
    'patricia.gomes@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-2002', '1987-10-11', 'O+', TRUE, 'medico'
),
(
    'demo-medico-04', 'Roberto Almeida',
    'roberto.almeida@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-2003', '1979-02-18', 'B+', TRUE, 'medico'
),
(
    'demo-medico-05', 'Juliana Mendes',
    'juliana.mendes@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-2004', '1988-06-09', 'AB+', TRUE, 'medico'
),
(
    'demo-medico-06', 'Felipe Carvalho',
    'felipe.carvalho@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-2005', '1983-12-03', 'O-', TRUE, 'medico'
),
(
    'demo-medico-07', 'Fernanda Lopes',
    'fernanda.lopes@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-2006', '1990-08-21', 'A+', TRUE, 'medico'
),
(
    'demo-medico-08', 'Andre Martins',
    'andre.martins@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-2007', '1977-11-16', 'B-', TRUE, 'medico'
),
(
    'demo-medico-09', 'Camila Torres',
    'camila.torres@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-2008', '1986-03-30', 'AB-', TRUE, 'medico'
),
(
    'demo-medico-10', 'Marcelo Ribeiro',
    'marcelo.ribeiro@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-2009', '1982-07-12', 'O+', TRUE, 'medico'
);


-- ============================================================
-- FUNCIONÁRIOS (10)
-- ============================================================

INSERT IGNORE INTO users (
    id, nome, email, senha_hash, telefone,
    data_nascimento, tipo_sanguineo,
    email_verificado, contato_emergencia_nome,
    contato_emergencia_telefone,
    contato_emergencia_parentesco, cargo
)
VALUES
(
    'demo-funcionario-01', 'Joao Mendes',
    'joao.mendes@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(11) 98888-2222', '1992-09-27', 'A+', TRUE,
    'Clara Mendes', '(11) 97777-2222', 'Esposa', 'funcionario'
),
(
    'demo-funcionario-02', 'Ana Paula Costa',
    'ana.paula@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-3001', '1994-02-17', 'B+', TRUE,
    'Carlos Costa', '(82) 97777-3001', 'Pai', 'funcionario'
),
(
    'demo-funcionario-03', 'Marcos Vinicius',
    'marcos.vinicius@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-3002', '1990-07-29', 'AB+', TRUE,
    'Laura Vinicius', '(82) 97777-3002', 'Mãe', 'funcionario'
),
(
    'demo-funcionario-04', 'Jessica Alves',
    'jessica.alves@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-3003', '1996-01-13', 'O+', TRUE,
    'Pedro Alves', '(82) 97777-3003', 'Pai', 'funcionario'
),
(
    'demo-funcionario-05', 'Bruno Martins',
    'bruno.martins@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-3004', '1988-05-06', 'A-', TRUE,
    'Mariana Martins', '(82) 97777-3004', 'Esposa', 'funcionario'
),
(
    'demo-funcionario-06', 'Carolina Souza',
    'carolina.souza@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-3005', '1993-10-22', 'B+', TRUE,
    'Roberto Souza', '(82) 97777-3005', 'Pai', 'funcionario'
),
(
    'demo-funcionario-07', 'Diego Ferreira',
    'diego.ferreira@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-3006', '1991-04-08', 'O-', TRUE,
    'Julia Ferreira', '(82) 97777-3006', 'Irmã', 'funcionario'
),
(
    'demo-funcionario-08', 'Renata Lima',
    'renata.lima@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-3007', '1997-12-19', 'AB-', TRUE,
    'Marcos Lima', '(82) 97777-3007', 'Pai', 'funcionario'
),
(
    'demo-funcionario-09', 'Gustavo Rocha',
    'gustavo.rocha@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-3008', '1985-08-14', 'A+', TRUE,
    'Claudia Rocha', '(82) 97777-3008', 'Esposa', 'funcionario'
),
(
    'demo-funcionario-10', 'Leticia Barros',
    'leticia.barros@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-3009', '1998-06-25', 'O+', TRUE,
    'Paulo Barros', '(82) 97777-3009', 'Pai', 'funcionario'
);


-- ============================================================
-- DONOS (10)
-- ============================================================

INSERT IGNORE INTO users (
    id, nome, email, senha_hash, telefone,
    data_nascimento, tipo_sanguineo, alergias,
    plano_de_saude, email_verificado,
    contato_emergencia_nome, contato_emergencia_telefone,
    contato_emergencia_parentesco, cargo
)
VALUES
(
    'demo-dono-01', 'Ricardo Almeida',
    'ricardo.almeida@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(11) 98888-3333', '1978-02-05', 'B+', 'Nenhuma',
    'Plano Vida Total', TRUE, 'Sandra Almeida',
    '(11) 97777-3333', 'Esposa', 'dono'
),
(
    'demo-dono-02', 'Fernanda Carvalho',
    'fernanda.carvalho@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-4001', '1975-11-03', 'O+', NULL,
    NULL, TRUE, 'Roberto Carvalho',
    '(82) 97777-4001', 'Marido', 'dono'
),
(
    'demo-dono-03', 'Andre Oliveira',
    'andre.oliveira@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-4002', '1972-04-18', 'A+', NULL,
    NULL, TRUE, 'Clara Oliveira',
    '(82) 97777-4002', 'Esposa', 'dono'
),
(
    'demo-dono-04', 'Monica Ribeiro',
    'monica.ribeiro@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-4003', '1980-09-21', 'B+', NULL,
    NULL, TRUE, 'Carlos Ribeiro',
    '(82) 97777-4003', 'Marido', 'dono'
),
(
    'demo-dono-05', 'Paulo Mendes',
    'paulo.mendes@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-4004', '1976-01-30', 'O-', NULL,
    NULL, TRUE, 'Ana Mendes',
    '(82) 97777-4004', 'Esposa', 'dono'
),
(
    'demo-dono-06', 'Luciana Costa',
    'luciana.costa@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-4005', '1982-07-14', 'AB+', NULL,
    NULL, TRUE, 'Pedro Costa',
    '(82) 97777-4005', 'Marido', 'dono'
),
(
    'demo-dono-07', 'Rodrigo Santos',
    'rodrigo.santos@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-4006', '1979-12-08', 'A-', NULL,
    NULL, TRUE, 'Mariana Santos',
    '(82) 97777-4006', 'Esposa', 'dono'
),
(
    'demo-dono-08', 'Silvia Martins',
    'silvia.martins@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-4007', '1974-03-26', 'B-', NULL,
    NULL, TRUE, 'Joao Martins',
    '(82) 97777-4007', 'Marido', 'dono'
),
(
    'demo-dono-09', 'Fabio Ferreira',
    'fabio.ferreira@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-4008', '1981-06-17', 'O+', NULL,
    NULL, TRUE, 'Carla Ferreira',
    '(82) 97777-4008', 'Esposa', 'dono'
),
(
    'demo-dono-10', 'Renata Almeida',
    'renata.almeida@example.org',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(82) 98888-4009', '1977-10-02', 'AB-', NULL,
    NULL, TRUE, 'Marcos Almeida',
    '(82) 97777-4009', 'Marido', 'dono'
);


-- ============================================================
-- CONFERÊNCIA
-- ============================================================

SELECT
    cargo,
    COUNT(*) AS quantidade
FROM users
WHERE id LIKE 'demo-%'
GROUP BY cargo
ORDER BY cargo;

SELECT
    COUNT(*) AS total_usuarios_demo
FROM users
WHERE id LIKE 'demo-%';

COMMIT;
```
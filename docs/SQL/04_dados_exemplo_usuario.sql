-- Conta de demonstração para desenvolvimento local.
-- Credenciais: maria.silva@email.com / senha123
-- Não substitui os dados de uma conta já existente.

INSERT INTO users (id, nome, email, senha_hash, telefone, email_verificado)
VALUES (
    'demo-user',
    'Maria Silva',
    'maria.silva@email.com',
    '$2a$10$TkitRKaX92MNJYDjt9qDce8qyClhMW0rOZsUl48GI5gfQ2F/hoEby',
    '(11) 99999-9999',
    true
)
AS incoming
ON DUPLICATE KEY UPDATE id = users.id;

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
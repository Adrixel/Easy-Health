-- Banco principal do Easy Health.
-- Persistência própria do app. Catálogos de demonstração ficam fora deste banco:
-- instituições em 02_instituicoes_medicas.sql / 03_dados_exemplo_instituicoes.sql
-- primeiros socorros em frontend/src/data/firstAidGuides.json e a conta de
-- demonstração em 04_dados_exemplo_usuario.sql.

CREATE TABLE IF NOT EXISTS users (
    id varchar(36) PRIMARY KEY,
    nome text NOT NULL,
    email varchar(254) NOT NULL UNIQUE,
    senha_hash text NOT NULL,
    telefone varchar(64),
    data_nascimento date,
    avatar_url text,
    tipo_sanguineo varchar(8),
    alergias text,
    medicamentos_em_uso text,
    plano_de_saude text,
    email_verificado boolean NOT NULL DEFAULT false,
    contato_emergencia_nome text,
    contato_emergencia_telefone varchar(64),
    contato_emergencia_parentesco text,
    push_token text,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cargo varchar(16) NOT NULL DEFAULT 'paciente',
    CONSTRAINT chk_users_cargo CHECK (cargo IN ('paciente', 'funcionario', 'medico', 'dono'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Migração: acrescenta a coluna cargo em bancos que já existiam, sem apagar dados.
-- Só altera se a coluna ainda não existir, então é seguro rodar em toda inicialização.
SET @add_cargo = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'cargo') = 0,
    'ALTER TABLE users ADD COLUMN cargo varchar(16) NOT NULL DEFAULT ''paciente'', ADD CONSTRAINT chk_users_cargo CHECK (cargo IN (''paciente'', ''funcionario'', ''medico'', ''dono''))',
    'SELECT 1');
PREPARE stmt_cargo FROM @add_cargo;
EXECUTE stmt_cargo;
DEALLOCATE PREPARE stmt_cargo;

-- Quem trabalha em qual clínica (dono, médico ou funcionário).
CREATE TABLE IF NOT EXISTS establishment_staff (
    user_id varchar(36) NOT NULL,
    establishment_id varchar(191) NOT NULL,
    cargo varchar(16) NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, establishment_id),
    KEY idx_establishment_staff_establishment (establishment_id),
    CONSTRAINT fk_establishment_staff_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_establishment_staff_cargo CHECK (cargo IN ('funcionario', 'medico', 'dono'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
 
-- Preços de cada serviço por clínica (valor em centavos, para não ter erro de arredondamento).
CREATE TABLE IF NOT EXISTS establishment_prices (
    establishment_id varchar(191) NOT NULL,
    servico varchar(120) NOT NULL,
    valor_centavos int NOT NULL,
    updated_by varchar(36) NULL,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (establishment_id, servico),
    CONSTRAINT chk_establishment_prices_valor CHECK (valor_centavos >= 0),
    CONSTRAINT fk_establishment_prices_user FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
 
CREATE TABLE IF NOT EXISTS auth_tokens (
    token_hash varchar(64) PRIMARY KEY,
    user_id varchar(36) NOT NULL,
    token_type varchar(16) NOT NULL,
    expires_at timestamp NOT NULL,
    revoked boolean NOT NULL DEFAULT false,
    KEY idx_auth_tokens_user (user_id),
    CONSTRAINT fk_auth_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_auth_tokens_type CHECK (token_type IN ('access', 'refresh'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS oauth_identities (
    provider varchar(32) NOT NULL,
    subject varchar(255) NOT NULL,
    user_id varchar(36) NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (provider, subject),
    KEY idx_oauth_identities_user (user_id),
    CONSTRAINT fk_oauth_identities_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS password_reset_codes (
    code_hash varchar(64) PRIMARY KEY,
    user_id varchar(36) NOT NULL,
    expires_at timestamp NOT NULL,
    used boolean NOT NULL DEFAULT false,
    CONSTRAINT fk_password_reset_codes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS medical_history_records (
    id varchar(36) PRIMARY KEY,
    user_id varchar(36) NOT NULL,
    medico_id varchar(36) NULL,
    establishment_id varchar(191) NULL,
    tipo varchar(24) NOT NULL,
    titulo varchar(180) NOT NULL,
    data date NOT NULL,
    descricao text,
    resultado text,
    laudo_url varchar(1000),
    profissional varchar(180),
    local varchar(255),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_medical_history_user_date (user_id, data),
    KEY idx_medical_history_medico_date (medico_id, data),
    KEY idx_medical_history_establishment (establishment_id),
    CONSTRAINT fk_medical_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_medical_history_medico FOREIGN KEY (medico_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_medical_history_type CHECK (tipo IN ('exame', 'vacina', 'medicamento', 'documento'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Autorizações de compartilhamento do histórico entre paciente e médico.
CREATE TABLE IF NOT EXISTS medical_history_shares (
    id varchar(36) PRIMARY KEY,
    patient_id varchar(36) NOT NULL,
    medico_id varchar(36) NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at timestamp NULL,
    active_medico_id varchar(36) NULL,
    UNIQUE KEY uq_medical_history_shares_active (patient_id, active_medico_id),
    KEY idx_medical_history_shares_patient (patient_id, created_at),
    KEY idx_medical_history_shares_medico (medico_id, revoked_at),
    CONSTRAINT fk_medical_history_shares_patient
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_medical_history_shares_medico
        FOREIGN KEY (medico_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_medical_history_shares_different_users
        CHECK (patient_id <> medico_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS appointments (
    id varchar(36) PRIMARY KEY,
    user_id varchar(36) NOT NULL,
    establishment_id varchar(191) NOT NULL,
    medico_id varchar(36) NULL,
    especialidade varchar(255) NOT NULL,
    data date NOT NULL,
    horario time NOT NULL,
    status varchar(32) NOT NULL DEFAULT 'agendado',
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_appointments_user_date (user_id, data, horario),
    KEY idx_appointments_medico_data (medico_id, data, horario),
    CONSTRAINT fk_appointments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointments_medico FOREIGN KEY (medico_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Auditoria de consultas ao histórico compartilhado com médicos.
CREATE TABLE IF NOT EXISTS medical_history_access_logs (
    id varchar(36) PRIMARY KEY,
    patient_id varchar(36) NOT NULL,
    medico_id varchar(36) NOT NULL,
    accessed_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo varchar(24) NULL,
    records_count int NOT NULL DEFAULT 0,
    KEY idx_history_access_patient (patient_id, accessed_at),
    KEY idx_history_access_medico (medico_id, accessed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Linhas locais usadas para serializar reservas por estabelecimento sem
-- conceder permissões de escrita no schema de catálogo.
CREATE TABLE IF NOT EXISTS establishment_booking_locks (
    establishment_id varchar(191) PRIMARY KEY,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS favorites (
    user_id varchar(36) NOT NULL,
    establishment_id varchar(191) NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, establishment_id),
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS notifications (
    id varchar(36) PRIMARY KEY,
    user_id varchar(36) NOT NULL,
    icon varchar(64) NOT NULL DEFAULT 'notifications',
    titulo text NOT NULL,
    descricao text NOT NULL,
    lida boolean NOT NULL DEFAULT false,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_notifications_user_date (user_id, created_at),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Volumes criados por versões anteriores mantinham estas relações com a
-- antiga tabela `establishments`, que deixou de ser usada pelo catálogo.
-- Remova-as quando existirem para permitir os IDs do schema atual.
SET @drop_legacy_favorites_fk = (
    SELECT IF(COUNT(*) > 0,
        'ALTER TABLE favorites DROP FOREIGN KEY fk_favorites_establishment',
        'SELECT 1')
    FROM information_schema.REFERENTIAL_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'favorites'
      AND CONSTRAINT_NAME = 'fk_favorites_establishment'
);
PREPARE drop_legacy_favorites_fk FROM @drop_legacy_favorites_fk;
EXECUTE drop_legacy_favorites_fk;
DEALLOCATE PREPARE drop_legacy_favorites_fk;

SET @drop_legacy_appointments_fk = (
    SELECT IF(COUNT(*) > 0,
        'ALTER TABLE appointments DROP FOREIGN KEY fk_appointments_establishment',
        'SELECT 1')
    FROM information_schema.REFERENTIAL_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'appointments'
      AND CONSTRAINT_NAME = 'fk_appointments_establishment'
);
PREPARE drop_legacy_appointments_fk FROM @drop_legacy_appointments_fk;
EXECUTE drop_legacy_appointments_fk;
DEALLOCATE PREPARE drop_legacy_appointments_fk;
-- Adiciona medico_id em bancos criados antes da associação de médicos.
SET @add_appointments_medico_id = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE appointments ADD COLUMN medico_id varchar(36) NULL',
        'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'appointments'
      AND COLUMN_NAME = 'medico_id'
);
PREPARE add_appointments_medico_id FROM @add_appointments_medico_id;
EXECUTE add_appointments_medico_id;
DEALLOCATE PREPARE add_appointments_medico_id;

-- Cria o índice de busca por médico, data e horário.
SET @add_appointments_medico_index = (
    SELECT IF(COUNT(*) = 0,
        'CREATE INDEX idx_appointments_medico_data ON appointments (medico_id, data, horario)',
        'SELECT 1')
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'appointments'
      AND INDEX_NAME = 'idx_appointments_medico_data'
);
PREPARE add_appointments_medico_index FROM @add_appointments_medico_index;
EXECUTE add_appointments_medico_index;
DEALLOCATE PREPARE add_appointments_medico_index;

-- Cria a relação entre o agendamento e o médico.
SET @add_appointments_medico_fk = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE appointments ADD CONSTRAINT fk_appointments_medico FOREIGN KEY (medico_id) REFERENCES users(id) ON DELETE SET NULL',
        'SELECT 1')
    FROM information_schema.REFERENTIAL_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'appointments'
      AND CONSTRAINT_NAME = 'fk_appointments_medico'
);
PREPARE add_appointments_medico_fk FROM @add_appointments_medico_fk;
EXECUTE add_appointments_medico_fk;
DEALLOCATE PREPARE add_appointments_medico_fk;

-- Adiciona os campos de associação e resultado dos registros médicos.
SET @add_medical_history_medico_id = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE medical_history_records ADD COLUMN medico_id varchar(36) NULL',
        'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medical_history_records'
      AND COLUMN_NAME = 'medico_id'
);
PREPARE add_medical_history_medico_id FROM @add_medical_history_medico_id;
EXECUTE add_medical_history_medico_id;
DEALLOCATE PREPARE add_medical_history_medico_id;

SET @add_medical_history_establishment_id = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE medical_history_records ADD COLUMN establishment_id varchar(191) NULL',
        'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medical_history_records'
      AND COLUMN_NAME = 'establishment_id'
);
PREPARE add_medical_history_establishment_id FROM @add_medical_history_establishment_id;
EXECUTE add_medical_history_establishment_id;
DEALLOCATE PREPARE add_medical_history_establishment_id;

SET @add_medical_history_resultado = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE medical_history_records ADD COLUMN resultado text NULL',
        'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medical_history_records'
      AND COLUMN_NAME = 'resultado'
);
PREPARE add_medical_history_resultado FROM @add_medical_history_resultado;
EXECUTE add_medical_history_resultado;
DEALLOCATE PREPARE add_medical_history_resultado;

SET @add_medical_history_laudo_url = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE medical_history_records ADD COLUMN laudo_url varchar(1000) NULL',
        'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medical_history_records'
      AND COLUMN_NAME = 'laudo_url'
);
PREPARE add_medical_history_laudo_url FROM @add_medical_history_laudo_url;
EXECUTE add_medical_history_laudo_url;
DEALLOCATE PREPARE add_medical_history_laudo_url;

-- Cria os índices para consultas por médico e estabelecimento.
SET @add_medical_history_medico_index = (
    SELECT IF(COUNT(*) = 0,
        'CREATE INDEX idx_medical_history_medico_date ON medical_history_records (medico_id, data)',
        'SELECT 1')
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medical_history_records'
      AND INDEX_NAME = 'idx_medical_history_medico_date'
);
PREPARE add_medical_history_medico_index FROM @add_medical_history_medico_index;
EXECUTE add_medical_history_medico_index;
DEALLOCATE PREPARE add_medical_history_medico_index;

SET @add_medical_history_establishment_index = (
    SELECT IF(COUNT(*) = 0,
        'CREATE INDEX idx_medical_history_establishment ON medical_history_records (establishment_id)',
        'SELECT 1')
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medical_history_records'
      AND INDEX_NAME = 'idx_medical_history_establishment'
);
PREPARE add_medical_history_establishment_index FROM @add_medical_history_establishment_index;
EXECUTE add_medical_history_establishment_index;
DEALLOCATE PREPARE add_medical_history_establishment_index;

-- Relaciona o registro médico ao usuário cadastrado como médico.
SET @add_medical_history_medico_fk = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE medical_history_records ADD CONSTRAINT fk_medical_history_medico FOREIGN KEY (medico_id) REFERENCES users(id) ON DELETE SET NULL',
        'SELECT 1')
    FROM information_schema.REFERENTIAL_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medical_history_records'
      AND CONSTRAINT_NAME = 'fk_medical_history_medico'
);
PREPARE add_medical_history_medico_fk FROM @add_medical_history_medico_fk;
EXECUTE add_medical_history_medico_fk;
DEALLOCATE PREPARE add_medical_history_medico_fk;

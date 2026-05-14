-- Criar o banco de dados
CREATE DATABASE IF NOT EXISTS monitoramento_tensao
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE monitoramento_tensao;

-- =====================================================
-- TABELA: clientes
-- =====================================================
CREATE TABLE IF NOT EXISTS clientes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tipo ENUM('FISICA', 'JURIDICA') NOT NULL,
    documento VARCHAR(18) NOT NULL UNIQUE COMMENT 'CPF ou CNPJ',
    nome VARCHAR(100) COMMENT 'Nome para PF',
    sobrenome VARCHAR(100) COMMENT 'Sobrenome para PF',
    razao_social VARCHAR(200) COMMENT 'Razão social para PJ',
    nome_fantasia VARCHAR(200) COMMENT 'Nome fantasia para PJ',
    logradouro VARCHAR(200) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    estado CHAR(2) NOT NULL,
    cep VARCHAR(10),
    telefone_ddd CHAR(2) NOT NULL,
    telefone_numero VARCHAR(10) NOT NULL,
    ip_cliente VARCHAR(45) NOT NULL COMMENT 'Endereço IP do equipamento',
    em_monitoramento BOOLEAN DEFAULT FALSE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_documento (documento),
    INDEX idx_ip_cliente (ip_cliente),
    INDEX idx_em_monitoramento (em_monitoramento),
    INDEX idx_cidade (cidade),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABELA: equipamentos
-- =====================================================
CREATE TABLE IF NOT EXISTS equipamentos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    tensao_nominal DECIMAL(6,2) NOT NULL CHECK (tensao_nominal BETWEEN 1 AND 1500),
    id_cliente INT NOT NULL,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_cliente) REFERENCES clientes(id) ON DELETE RESTRICT,
    INDEX idx_cliente (id_cliente),
    INDEX idx_marca_modelo (marca, modelo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABELA: medicoes_tensao
-- =====================================================
CREATE TABLE IF NOT EXISTS medicoes_tensao (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_cliente INT NOT NULL,
    tensao DECIMAL(6,2) NOT NULL CHECK (tensao >= 0),
    data_hora DATETIME NOT NULL,
    estado_rede ENUM('ATIVO', 'INATIVO') NOT NULL,
    situacao_rede ENUM('NORMAL', 'ALERTA', 'CRÍTICO') NOT NULL,
    data_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_cliente) REFERENCES clientes(id) ON DELETE CASCADE,
    INDEX idx_cliente_data (id_cliente, data_hora),
    INDEX idx_data_hora (data_hora),
    INDEX idx_situacao (situacao_rede)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABELA: departamentos
-- =====================================================
CREATE TABLE IF NOT EXISTS departamentos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_nome (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABELA: usuarios
-- =====================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    sobrenome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    matricula VARCHAR(50) NOT NULL UNIQUE,
    funcao ENUM('Administrador', 'Monitor', 'Técnico') NOT NULL,
    login VARCHAR(50) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    id_departamento INT,
    ativo BOOLEAN DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_acesso DATETIME,
    
    FOREIGN KEY (id_departamento) REFERENCES departamentos(id) ON DELETE SET NULL,
    INDEX idx_login (login),
    INDEX idx_matricula (matricula),
    INDEX idx_cpf (cpf),
    INDEX idx_funcao (funcao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABELA: ordens_servico
-- =====================================================
CREATE TABLE IF NOT EXISTS ordens_servico (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_cliente INT NOT NULL,
    id_equipamento INT NOT NULL,
    tipo_nivel ENUM('1º Nível', '2º Nível') NOT NULL DEFAULT '1º Nível',
    status ENUM('ABERTA', 'EM_ATENDIMENTO', 'FECHADA') NOT NULL DEFAULT 'ABERTA',
    motivo TEXT NOT NULL,
    descricao_solucao TEXT,
    falha_identificada ENUM('CLIENTE', 'MONITORAMENTO'),
    data_abertura DATETIME NOT NULL,
    data_fechamento DATETIME,
    id_usuario_abertura INT NOT NULL,
    id_usuario_fechamento INT,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_cliente) REFERENCES clientes(id) ON DELETE RESTRICT,
    FOREIGN KEY (id_equipamento) REFERENCES equipamentos(id) ON DELETE RESTRICT,
    FOREIGN KEY (id_usuario_abertura) REFERENCES usuarios(id) ON DELETE RESTRICT,
    FOREIGN KEY (id_usuario_fechamento) REFERENCES usuarios(id) ON DELETE SET NULL,
    INDEX idx_cliente (id_cliente),
    INDEX idx_status (status),
    INDEX idx_data_abertura (data_abertura),
    INDEX idx_tipo_nivel (tipo_nivel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABELA: logs_sistema
-- =====================================================
CREATE TABLE IF NOT EXISTS logs_sistema (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_usuario INT,
    acao VARCHAR(100) NOT NULL,
    descricao TEXT,
    ip_acesso VARCHAR(45),
    data_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE SET NULL,
    INDEX idx_usuario (id_usuario),
    INDEX idx_data_hora (data_hora),
    INDEX idx_acao (acao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABELA: configuracoes
-- =====================================================
CREATE TABLE IF NOT EXISTS configuracoes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    chave VARCHAR(100) NOT NULL UNIQUE,
    valor TEXT,
    descricao TEXT,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INSERTS INICIAIS
-- =====================================================

-- Inserir departamentos padrão
INSERT INTO departamentos (nome, descricao) VALUES
('Administração', 'Departamento administrativo responsável pela gestão do sistema'),
('Monitoramento', 'Departamento responsável pelo monitoramento remoto de tensão'),
('Suporte Técnico', 'Departamento responsável pelo suporte técnico presencial e remoto');

-- Inserir usuário administrador padrão (senha: admin123)
INSERT INTO usuarios (nome, sobrenome, cpf, matricula, funcao, login, senha, id_departamento) VALUES
('Administrador', 'Sistema', '00000000000', 'ADMIN001', 'Administrador', 'admin', 'admin123', 1);

-- Inserir configurações padrão
INSERT INTO configuracoes (chave, valor, descricao) VALUES
('timeout_sem_comunicacao', '2500', 'Tempo máximo sem comunicação em milissegundos'),
('intervalo_medicao', '500', 'Intervalo entre medições em milissegundos'),
('limite_alertas_consecutivos', '3', 'Número de alertas consecutivos para abrir OS'),
('limite_criticos_consecutivos', '3', 'Número de críticos consecutivos para abrir OS'),
('tempo_inativo_para_os', '5000', 'Tempo em milissegundos de inatividade para abrir OS'),
('dias_retencao_medicoes', '90', 'Dias para manter medições antes de excluir');

-- =====================================================
-- VIEWS PARA RELATÓRIOS
-- =====================================================

-- View: Resumo de disponibilidade dos clientes
CREATE OR REPLACE VIEW vw_disponibilidade_cliente AS
SELECT 
    c.id,
    c.nome,
    c.sobrenome,
    c.razao_social,
    c.tipo,
    COUNT(m.id) as total_medicoes,
    SUM(CASE WHEN m.estado_rede = 'ATIVO' THEN 1 ELSE 0 END) as medicoes_ativas,
    ROUND(SUM(CASE WHEN m.estado_rede = 'ATIVO' THEN 1 ELSE 0 END) * 100.0 / COUNT(m.id), 2) as disponibilidade,
    MIN(m.tensao) as tensao_minima,
    MAX(m.tensao) as tensao_maxima,
    AVG(m.tensao) as tensao_media,
    (SELECT m2.tensao FROM medicoes_tensao m2 WHERE m2.id_cliente = c.id ORDER BY m2.data_hora DESC LIMIT 1) as ultima_tensao,
    (SELECT m2.estado_rede FROM medicoes_tensao m2 WHERE m2.id_cliente = c.id ORDER BY m2.data_hora DESC LIMIT 1) as ultimo_estado
FROM clientes c
LEFT JOIN medicoes_tensao m ON c.id = m.id_cliente
WHERE c.em_monitoramento = TRUE
GROUP BY c.id;

-- View: Estatísticas de ordens de serviço
CREATE OR REPLACE VIEW vw_estatisticos_os AS
SELECT 
    DATE(data_abertura) as data,
    COUNT(*) as total_os,
    SUM(CASE WHEN status = 'ABERTA' THEN 1 ELSE 0 END) as abertas,
    SUM(CASE WHEN status = 'FECHADA' THEN 1 ELSE 0 END) as fechadas,
    SUM(CASE WHEN tipo_nivel = '1º Nível' THEN 1 ELSE 0 END) as primeiro_nivel,
    SUM(CASE WHEN tipo_nivel = '2º Nível' THEN 1 ELSE 0 END) as segundo_nivel,
    SUM(CASE WHEN falha_identificada = 'CLIENTE' THEN 1 ELSE 0 END) as falha_cliente,
    SUM(CASE WHEN falha_identificada = 'MONITORAMENTO' THEN 1 ELSE 0 END) as falha_monitoramento,
    ROUND(AVG(TIMESTAMPDIFF(HOUR, data_abertura, data_fechamento)), 2) as tempo_medio_resolucao_horas
FROM ordens_servico
GROUP BY DATE(data_abertura);

-- View: Alertas por cliente
CREATE OR REPLACE VIEW vw_alertas_cliente AS
SELECT 
    c.id as id_cliente,
    c.nome,
    c.sobrenome,
    c.razao_social,
    COUNT(m.id) as total_alertas,
    SUM(CASE WHEN m.situacao_rede = 'ALERTA' THEN 1 ELSE 0 END) as alertas,
    SUM(CASE WHEN m.situacao_rede = 'CRÍTICO' THEN 1 ELSE 0 END) as criticos,
    MAX(m.data_hora) as ultimo_alerta
FROM clientes c
JOIN medicoes_tensao m ON c.id = m.id_cliente
WHERE m.situacao_rede IN ('ALERTA', 'CRÍTICO')
AND m.data_hora >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY c.id;

-- =====================================================
-- PROCEDURES E FUNCTIONS
-- =====================================================

-- Procedure para limpar medições antigas
DELIMITER //

CREATE PROCEDURE sp_limpar_medicoes_antigas(IN dias_retencao INT)
BEGIN
    DELETE FROM medicoes_tensao 
    WHERE data_hora < DATE_SUB(NOW(), INTERVAL dias_retencao DAY);
    
    SELECT ROW_COUNT() as registros_removidos;
END//

-- Procedure para gerar relatório mensal
CREATE PROCEDURE sp_relatorio_mensal(
    IN ano INT,
    IN mes INT
)
BEGIN
    DECLARE data_inicio DATE;
    DECLARE data_fim DATE;
    
    SET data_inicio = DATE(CONCAT(ano, '-', mes, '-01'));
    SET data_fim = LAST_DAY(data_inicio);
    
    -- Resumo de medições
    SELECT 
        COUNT(*) as total_medicoes,
        ROUND(AVG(tensao), 2) as tensao_media,
        MIN(tensao) as tensao_minima,
        MAX(tensao) as tensao_maxima,
        SUM(CASE WHEN estado_rede = 'ATIVO' THEN 1 ELSE 0 END) as medicoes_ativas,
        SUM(CASE WHEN situacao_rede = 'ALERTA' THEN 1 ELSE 0 END) as alertas,
        SUM(CASE WHEN situacao_rede = 'CRÍTICO' THEN 1 ELSE 0 END) as criticos
    FROM medicoes_tensao
    WHERE DATE(data_hora) BETWEEN data_inicio AND data_fim;
    
    -- Resumo de ordens de serviço
    SELECT 
        COUNT(*) as total_os,
        SUM(CASE WHEN status = 'ABERTA' THEN 1 ELSE 0 END) as abertas,
        SUM(CASE WHEN status = 'FECHADA' THEN 1 ELSE 0 END) as fechadas,
        ROUND(AVG(TIMESTAMPDIFF(HOUR, data_abertura, data_fechamento)), 2) as tempo_medio_horas
    FROM ordens_servico
    WHERE DATE(data_abertura) BETWEEN data_inicio AND data_fim;
END//

DELIMITER ;

-- =====================================================
-- TRIGGERS
-- =====================================================

-- Trigger para atualizar último acesso do usuário
DELIMITER //

CREATE TRIGGER trg_usuario_ultimo_acesso
AFTER UPDATE ON usuarios
FOR EACH ROW
BEGIN
    IF NEW.ultimo_acesso IS NOT NULL AND OLD.ultimo_acesso IS NULL THEN
        INSERT INTO logs_sistema (id_usuario, acao, descricao)
        VALUES (NEW.id, 'LOGIN', CONCAT('Usuário ', NEW.login, ' realizou login'));
    END IF;
END//

-- Trigger para log de inserção de OS
CREATE TRIGGER trg_log_os_insert
AFTER INSERT ON ordens_servico
FOR EACH ROW
BEGIN
    INSERT INTO logs_sistema (id_usuario, acao, descricao)
    VALUES (NEW.id_usuario_abertura, 'CRIAR_OS', CONCAT('OS #', NEW.id, ' criada para cliente ID: ', NEW.id_cliente));
END//

-- Trigger para log de fechamento de OS
CREATE TRIGGER trg_log_os_update
AFTER UPDATE ON ordens_servico
FOR EACH ROW
BEGIN
    IF NEW.status = 'FECHADA' AND OLD.status != 'FECHADA' THEN
        INSERT INTO logs_sistema (id_usuario, acao, descricao)
        VALUES (NEW.id_usuario_fechamento, 'FECHAR_OS', CONCAT('OS #', NEW.id, ' fechada'));
    END IF;
END//

DELIMITER ;

-- =====================================================
-- PERMISSÕES (opcional - ajuste conforme necessidade)
-- =====================================================

-- Criar usuário da aplicação (ajuste a senha conforme necessário)
-- CREATE USER IF NOT EXISTS 'app_monitoramento'@'localhost' IDENTIFIED BY 'sua_senha_aqui';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON monitoramento_tensao.* TO 'app_monitoramento'@'localhost';
-- FLUSH PRIVILEGES;

-- =====================================================
-- VERIFICAÇÃO FINAL
-- =====================================================

-- Mostrar todas as tabelas criadas
SHOW TABLES;

-- Mostrar estrutura da tabela clientes
DESCRIBE clientes;

-- Contar registros inseridos
SELECT 'departamentos' as tabela, COUNT(*) as registros FROM departamentos
UNION ALL
SELECT 'usuarios', COUNT(*) FROM usuarios
UNION ALL
SELECT 'configuracoes', COUNT(*) FROM configuracoes;
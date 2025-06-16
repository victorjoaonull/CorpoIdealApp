-- Criação do banco de dados
CREATE DATABASE IF NOT EXISTS corpoideal;
USE corpoideal;

-- Tabela de usuários
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    telefone VARCHAR(20),
    data_nascimento DATE,
    genero ENUM('Masculino', 'Feminino', 'Outro'),
    altura DECIMAL(3,2), -- em metros
    peso DECIMAL(5,2),   -- em kg
    objetivo ENUM('Perder peso', 'Ganhar massa', 'Manter peso'),
    nivel_atividade ENUM('Sedentário', 'Leve', 'Moderado', 'Intenso', 'Muito intenso'),
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabela de planos nutricionais (atualizada com campo 'atual')
CREATE TABLE planos_nutricionais (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    calorias_diarias INT NOT NULL,
    proteinas_diarias INT NOT NULL,    -- em gramas
    carboidratos_diarias INT NOT NULL, -- em gramas
    gorduras_diarias INT NOT NULL,     -- em gramas
    motivo_alteracao VARCHAR(255),
    atual BOOLEAN DEFAULT FALSE,       -- indica se é o plano atual
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_plano_usuario (usuario_id),
    INDEX idx_plano_atual (usuario_id, atual) -- índice para busca rápida do plano atual
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabela de refeições
CREATE TABLE refeicoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    plano_id INT NOT NULL,
    tipo ENUM('Café da manhã', 'Lanche da manhã', 'Almoço', 'Lanche da tarde', 'Jantar', 'Ceia') NOT NULL,
    descricao TEXT NOT NULL,
    calorias INT NOT NULL,
    proteinas INT NOT NULL DEFAULT 0,    -- em gramas
    carboidratos INT NOT NULL DEFAULT 0, -- em gramas
    gorduras INT NOT NULL DEFAULT 0,     -- em gramas
    horario TIME, -- horário sugerido para a refeição
    FOREIGN KEY (plano_id) REFERENCES planos_nutricionais(id) ON DELETE CASCADE,
    INDEX idx_refeicao_plano (plano_id),
    INDEX idx_refeicao_tipo (tipo) -- para filtragem por tipo de refeição
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabela de acompanhamento
CREATE TABLE acompanhamento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    peso DECIMAL(5,2) NOT NULL, -- em kg
    observacoes TEXT,
    data_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_acompanhamento_usuario (usuario_id),
    INDEX idx_acompanhamento_data (data_registro) -- para ordenação por data
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabela de alimentos (para sugestões de refeições)
CREATE TABLE alimentos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    calorias_por_100g INT NOT NULL, -- por 100g
    proteinas_por_100g DECIMAL(5,2), -- por 100g
    carboidratos_por_100g DECIMAL(5,2), -- por 100g
    gorduras_por_100g DECIMAL(5,2), -- por 100g
    tipo ENUM('Cereal', 'Proteína', 'Vegetal', 'Fruta', 'Laticínio', 'Outro'),
    INDEX idx_alimento_tipo (tipo), -- para filtragem por tipo
    INDEX idx_alimento_nome (nome) -- para busca por nome
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
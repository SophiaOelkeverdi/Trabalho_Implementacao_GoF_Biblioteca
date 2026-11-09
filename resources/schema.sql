-- Esquema da familia relacional (MySQL). Executado por FabricaPersistenciaJdbc.criarEsquemaSeNecessario().
CREATE TABLE IF NOT EXISTS livro (
    id      VARCHAR(40)  PRIMARY KEY,
    titulo  VARCHAR(200) NOT NULL,
    autor   VARCHAR(200),
    estado  VARCHAR(20)  NOT NULL
);

CREATE TABLE IF NOT EXISTS pessoa (
    cpf       VARCHAR(14)  PRIMARY KEY,
    nome      VARCHAR(100) NOT NULL,
    sobrenome VARCHAR(100),
    email     VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS estudante (
    matricula VARCHAR(40) PRIMARY KEY,
    cpf       VARCHAR(14) NOT NULL
);

CREATE TABLE IF NOT EXISTS atendente (
    id  VARCHAR(40) PRIMARY KEY,
    cpf VARCHAR(14) NOT NULL
);

CREATE TABLE IF NOT EXISTS reserva (
    id           VARCHAR(40) PRIMARY KEY,
    data         DATE        NOT NULL,
    id_livro     VARCHAR(40) NOT NULL,
    id_estudante VARCHAR(40) NOT NULL,
    valida_ate   DATE        NOT NULL
);

CREATE TABLE IF NOT EXISTS emprestimo (
    id             VARCHAR(40) PRIMARY KEY,
    data           DATE        NOT NULL,
    id_livro       VARCHAR(40) NOT NULL,
    id_estudante   VARCHAR(40) NOT NULL,
    vencimento     DATE        NOT NULL,
    num_renovacoes INT         NOT NULL,
    ativo          BOOLEAN     NOT NULL
);

CREATE TABLE IF NOT EXISTS devolucao (
    id            VARCHAR(40) PRIMARY KEY,
    data          DATE        NOT NULL,
    id_livro      VARCHAR(40) NOT NULL,
    id_estudante  VARCHAR(40) NOT NULL,
    id_emprestimo VARCHAR(40) NOT NULL,
    vencimento    DATE        NOT NULL
);

CREATE TABLE IF NOT EXISTS multa (
    id           VARCHAR(40)   PRIMARY KEY,
    id_devolucao VARCHAR(40)   NOT NULL,
    id_estudante VARCHAR(40)   NOT NULL,
    dias_atraso  BIGINT        NOT NULL,
    valor        DECIMAL(10,2) NOT NULL,
    politica     VARCHAR(40)   NOT NULL,
    emitida_em   DATE          NOT NULL,
    id_cobranca  VARCHAR(60),
    situacao     VARCHAR(10)   NOT NULL
);

package persistencia.jdbc;

import persistencia.FabricaPersistencia;
import persistencia.FamiliaIncompativelException;
import persistencia.PersistenciaException;
import persistencia.RepositorioLivro;
import persistencia.RepositorioMovimentacao;
import persistencia.RepositorioRelacionamento;
import persistencia.UnidadeDeTrabalho;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Padrão Abstract Factory - fábrica concreta da família RELACIONAL (perfil producao, MySQL).
// O Oracle é só réplica do MySQL e não pode receber escrita da aplicação,
// por isso a fábrica recusa URL do Oracle.
public class FabricaPersistenciaJdbc implements FabricaPersistencia {
    public static final String FAMILIA = "mysql";

    private final String url;
    private final String usuario;
    private final String senha;

    public FabricaPersistenciaJdbc(String url, String usuario, String senha) {
        if (url == null || url.toLowerCase().startsWith("jdbc:oracle")) {
            throw new PersistenciaException("O Oracle e somente replica: a aplicacao grava apenas no MySQL");
        }
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    @Override
    public String familia() {
        return FAMILIA;
    }

    @Override
    public UnidadeDeTrabalho criarUnidadeDeTrabalho() {
        return new UnidadeDeTrabalhoJdbc(url, usuario, senha);
    }

    @Override
    public RepositorioLivro criarRepositorioLivro(UnidadeDeTrabalho unidade) {
        return new RepositorioLivroJdbc(daFamilia(unidade));
    }

    @Override
    public RepositorioMovimentacao criarRepositorioMovimentacao(UnidadeDeTrabalho unidade) {
        return new RepositorioMovimentacaoJdbc(daFamilia(unidade));
    }

    @Override
    public RepositorioRelacionamento criarRepositorioRelacionamento(UnidadeDeTrabalho unidade) {
        return new RepositorioRelacionamentoJdbc(daFamilia(unidade));
    }

    private UnidadeDeTrabalhoJdbc daFamilia(UnidadeDeTrabalho unidade) {
        if (unidade instanceof UnidadeDeTrabalhoJdbc j) {
            return j;
        }
        throw new FamiliaIncompativelException(FAMILIA, unidade);
    }

    // roda o schema.sql (CREATE TABLE IF NOT EXISTS ...)
    public void criarEsquemaSeNecessario() {
        String ddl;
        try (InputStream in = getClass().getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new PersistenciaException("schema.sql nao encontrado");
            }
            ddl = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenciaException("Falha ao ler schema.sql", e);
        }
        try (Connection c = DriverManager.getConnection(url, usuario, senha); Statement st = c.createStatement()) {
            for (String comando : ddl.split(";")) {
                if (!comando.isBlank()) {
                    st.execute(comando);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao criar as tabelas em " + url, e);
        }
    }
}

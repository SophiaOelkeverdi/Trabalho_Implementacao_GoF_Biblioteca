package persistencia.jdbc;

import persistencia.PersistenciaException;
import persistencia.UnidadeDeTrabalho;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Padrão Abstract Factory - produto concreto (família MySQL).
// Guarda UMA conexão com autoCommit desligado. Os 3 repositórios usam essa mesma conexão,
// então ficam na mesma transação do MySQL. A conexão só abre no iniciar().
public class UnidadeDeTrabalhoJdbc implements UnidadeDeTrabalho {
    private final String url;
    private final String usuario;
    private final String senha;
    private Connection conexao;

    UnidadeDeTrabalhoJdbc(String url, String usuario, String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    @Override
    public String familia() {
        return FabricaPersistenciaJdbc.FAMILIA;
    }

    @Override
    public void iniciar() {
        if (ativa()) {
            throw new PersistenciaException("Unidade de trabalho ja iniciada");
        }
        try {
            conexao = DriverManager.getConnection(url, usuario, senha);
            conexao.setAutoCommit(false);
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel conectar em " + url, e);
        }
    }

    @Override
    public void confirmar() {
        try {
            conexao().commit();
        } catch (SQLException e) {
            desfazer();
            throw new PersistenciaException("Falha ao confirmar a transacao", e);
        }
        fechar();
    }

    @Override
    public void desfazer() {
        if (!ativa()) {
            return;
        }
        try {
            conexao.rollback();
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao desfazer a transacao", e);
        } finally {
            fechar();
        }
    }

    @Override
    public boolean ativa() {
        return conexao != null;
    }

    Connection conexao() {
        if (!ativa()) {
            throw new PersistenciaException("Operacao de dados fora de uma unidade de trabalho");
        }
        return conexao;
    }

    private void fechar() {
        try {
            if (conexao != null) {
                conexao.close();
            }
        } catch (SQLException ignorada) {
            // se nem fechar deu, não tem o que fazer
        } finally {
            conexao = null;
        }
    }
}

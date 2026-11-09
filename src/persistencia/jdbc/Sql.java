package persistencia.jdbc;

import persistencia.PersistenciaException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Métodos auxiliares de JDBC usados pelos repositórios MySQL (evita repetir try/catch em todo lugar)
final class Sql {

    interface Mapeador<T> {
        T mapear(ResultSet rs) throws SQLException;
    }

    private Sql() {
    }

    static int executar(Connection c, String sql, Object... parametros) {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            preencher(ps, parametros);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro SQL: " + sql, e);
        }
    }

    static <T> List<T> listar(Connection c, String sql, Mapeador<T> m, Object... parametros) {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            preencher(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(m.mapear(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro SQL: " + sql, e);
        }
    }

    static <T> Optional<T> um(Connection c, String sql, Mapeador<T> m, Object... parametros) {
        return listar(c, sql, m, parametros).stream().findFirst();
    }

    // tenta UPDATE; se não alterou nenhuma linha, faz INSERT
    static void salvar(Connection c, String update, Object[] paramsUpdate, String insert, Object[] paramsInsert) {
        if (executar(c, update, paramsUpdate) == 0) {
            executar(c, insert, paramsInsert);
        }
    }

    static LocalDate data(ResultSet rs, String coluna) throws SQLException {
        Date d = rs.getDate(coluna);
        return d == null ? null : d.toLocalDate();
    }

    private static void preencher(PreparedStatement ps, Object... parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            Object p = parametros[i];
            if (p instanceof LocalDate d) {
                ps.setDate(i + 1, Date.valueOf(d));
            } else {
                ps.setObject(i + 1, p);
            }
        }
    }
}

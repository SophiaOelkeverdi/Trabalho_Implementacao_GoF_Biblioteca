package persistencia.memoria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

// "Banco de dados" da família em memória.
// Guarda registros imutáveis (records), igual o MySQL guarda linhas. Assim o objeto do domínio
// não é o mesmo que está armazenado, e dá pra desfazer uma transação voltando uma cópia das tabelas.
public class BaseEmMemoria {

    record LivroReg(String id, String titulo, String autor, String estado) { }

    record PessoaReg(String cpf, String nome, String sobrenome, String email) { }

    record PapelReg(String id, String cpf) { }

    record ReservaReg(String id, LocalDate data, String idLivro, String idEstudante, LocalDate validaAte) { }

    record EmprestimoReg(String id, LocalDate data, String idLivro, String idEstudante, LocalDate vencimento,
                         int numRenovacoes, boolean ativo) { }

    record DevolucaoReg(String id, LocalDate data, String idLivro, String idEstudante, String idEmprestimo,
                        LocalDate vencimento) { }

    record MultaReg(String id, String idDevolucao, String idEstudante, long diasAtraso, BigDecimal valor,
                    String politica, LocalDate emitidaEm, String idCobranca, String situacao) { }

    // as "tabelas"
    static final class Tabelas {
        final Map<String, LivroReg> livros = new LinkedHashMap<>();
        final Map<String, PessoaReg> pessoas = new LinkedHashMap<>();
        final Map<String, PapelReg> estudantes = new LinkedHashMap<>();
        final Map<String, PapelReg> atendentes = new LinkedHashMap<>();
        final Map<String, ReservaReg> reservas = new LinkedHashMap<>();
        final Map<String, EmprestimoReg> emprestimos = new LinkedHashMap<>();
        final Map<String, DevolucaoReg> devolucoes = new LinkedHashMap<>();
        final Map<String, MultaReg> multas = new LinkedHashMap<>();

        Tabelas copia() {
            Tabelas c = new Tabelas();
            c.livros.putAll(livros);
            c.pessoas.putAll(pessoas);
            c.estudantes.putAll(estudantes);
            c.atendentes.putAll(atendentes);
            c.reservas.putAll(reservas);
            c.emprestimos.putAll(emprestimos);
            c.devolucoes.putAll(devolucoes);
            c.multas.putAll(multas);
            return c;
        }
    }

    private Tabelas tabelas = new Tabelas();
    private final ReentrantLock trava = new ReentrantLock();
    private long gravacoes;

    Tabelas tabelas() {
        return tabelas;
    }

    ReentrantLock trava() {
        return trava;
    }

    Tabelas fotografar() {
        return tabelas.copia();
    }

    void restaurar(Tabelas fotografia) {
        this.tabelas = fotografia;
    }

    void contarGravacao() {
        gravacoes++;
    }

    // quantas escritas já foram feitas (usado nos testes do R07)
    public long totalDeGravacoes() {
        return gravacoes;
    }
}

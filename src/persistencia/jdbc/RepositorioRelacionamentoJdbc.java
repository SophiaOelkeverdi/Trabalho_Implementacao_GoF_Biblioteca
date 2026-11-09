package persistencia.jdbc;

import dominio.pessoa.Atendente;
import dominio.pessoa.Estudante;
import dominio.pessoa.Pessoa;
import persistencia.RepositorioRelacionamento;
import persistencia.UnidadeDeTrabalho;

import java.sql.Connection;
import java.util.Optional;

// Padrão Abstract Factory - produto concreto (família MySQL)
public class RepositorioRelacionamentoJdbc implements RepositorioRelacionamento {
    private final UnidadeDeTrabalhoJdbc unidade;

    RepositorioRelacionamentoJdbc(UnidadeDeTrabalhoJdbc unidade) {
        this.unidade = unidade;
    }

    private Connection c() {
        return unidade.conexao();
    }

    @Override
    public Optional<Pessoa> buscarPessoa(String cpf) {
        return Sql.um(c(), "SELECT * FROM pessoa WHERE cpf = ?", rs -> new Pessoa(rs.getString("nome"),
                rs.getString("sobrenome"), rs.getString("cpf"), rs.getString("email")), cpf);
    }

    @Override
    public void salvarPessoa(Pessoa p) {
        Sql.salvar(c(),
                "UPDATE pessoa SET nome = ?, sobrenome = ?, email = ? WHERE cpf = ?",
                new Object[]{p.getNome(), p.getSobrenome(), p.getEmail(), p.getCpf()},
                "INSERT INTO pessoa (cpf, nome, sobrenome, email) VALUES (?, ?, ?, ?)",
                new Object[]{p.getCpf(), p.getNome(), p.getSobrenome(), p.getEmail()});
    }

    @Override
    public void removerPessoa(String cpf) {
        Sql.executar(c(), "DELETE FROM pessoa WHERE cpf = ?", cpf);
    }

    @Override
    public Optional<Estudante> buscarEstudante(String matricula) {
        return Sql.um(c(), "SELECT cpf FROM estudante WHERE matricula = ?", rs -> rs.getString("cpf"), matricula)
                .flatMap(this::buscarPessoa)
                .map(p -> new Estudante(matricula, p));
    }

    @Override
    public void salvarEstudante(Estudante e) {
        Sql.salvar(c(),
                "UPDATE estudante SET cpf = ? WHERE matricula = ?", new Object[]{e.getCpf(), e.getId()},
                "INSERT INTO estudante (matricula, cpf) VALUES (?, ?)", new Object[]{e.getId(), e.getCpf()});
    }

    @Override
    public void removerEstudante(String matricula) {
        Sql.executar(c(), "DELETE FROM estudante WHERE matricula = ?", matricula);
    }

    @Override
    public Optional<Atendente> buscarAtendente(String id) {
        return Sql.um(c(), "SELECT cpf FROM atendente WHERE id = ?", rs -> rs.getString("cpf"), id)
                .flatMap(this::buscarPessoa)
                .map(p -> new Atendente(id, p));
    }

    @Override
    public void salvarAtendente(Atendente a) {
        Sql.salvar(c(),
                "UPDATE atendente SET cpf = ? WHERE id = ?", new Object[]{a.getCpf(), a.getId()},
                "INSERT INTO atendente (id, cpf) VALUES (?, ?)", new Object[]{a.getId(), a.getCpf()});
    }

    @Override
    public void removerAtendente(String id) {
        Sql.executar(c(), "DELETE FROM atendente WHERE id = ?", id);
    }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return unidade;
    }
}

package acesso;

import dominio.RegraDeNegocioException;
import dominio.pessoa.Atendente;
import dominio.pessoa.Estudante;
import dominio.pessoa.Pessoa;
import persistencia.RepositorioRelacionamento;

import java.util.Optional;

// Ponto interno de acesso "Relacionamento" (do Trabalho 02): pessoas e seus papéis
public class Relacionamento {
    private final RepositorioRelacionamento repositorio;

    public Relacionamento(RepositorioRelacionamento repositorio) {
        this.repositorio = repositorio;
    }

    public Optional<Pessoa> consultarPessoa(String cpf) {
        return repositorio.buscarPessoa(cpf);
    }

    public void inserirPessoa(Pessoa pessoa) {
        repositorio.salvarPessoa(pessoa);
    }

    public void atualizarPessoa(Pessoa pessoa) {
        repositorio.salvarPessoa(pessoa);
    }

    public void excluirPessoa(String cpf) {
        repositorio.removerPessoa(cpf);
    }

    public Optional<Estudante> consultarEstudante(String matricula) {
        return repositorio.buscarEstudante(matricula);
    }

    public Estudante obterEstudante(String matricula) {
        return repositorio.buscarEstudante(matricula)
                .orElseThrow(() -> new RegraDeNegocioException("Estudante " + matricula + " nao cadastrado"));
    }

    public void inserirEstudante(Estudante estudante) {
        repositorio.salvarEstudante(estudante);
    }

    public void excluirEstudante(String matricula) {
        repositorio.removerEstudante(matricula);
    }

    public Optional<Atendente> consultarAtendente(String id) {
        return repositorio.buscarAtendente(id);
    }

    public void inserirAtendente(Atendente atendente) {
        repositorio.salvarAtendente(atendente);
    }

    public void excluirAtendente(String id) {
        repositorio.removerAtendente(id);
    }
}

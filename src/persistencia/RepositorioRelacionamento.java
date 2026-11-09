package persistencia;

import dominio.pessoa.Atendente;
import dominio.pessoa.Estudante;
import dominio.pessoa.Pessoa;

import java.util.Optional;

// Padrão Abstract Factory - produto abstrato (pessoas e papéis)
public interface RepositorioRelacionamento {

    Optional<Pessoa> buscarPessoa(String cpf);

    void salvarPessoa(Pessoa pessoa);

    void removerPessoa(String cpf);

    Optional<Estudante> buscarEstudante(String matricula);

    void salvarEstudante(Estudante estudante);

    void removerEstudante(String matricula);

    Optional<Atendente> buscarAtendente(String id);

    void salvarAtendente(Atendente atendente);

    void removerAtendente(String id);

    UnidadeDeTrabalho unidadeDeTrabalho();
}

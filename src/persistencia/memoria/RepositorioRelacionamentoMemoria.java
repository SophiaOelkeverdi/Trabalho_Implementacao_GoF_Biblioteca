package persistencia.memoria;

import dominio.pessoa.Atendente;
import dominio.pessoa.Estudante;
import dominio.pessoa.Pessoa;
import persistencia.RepositorioRelacionamento;
import persistencia.UnidadeDeTrabalho;

import java.util.Optional;

// Padrão Abstract Factory - produto concreto (família em memória)
public class RepositorioRelacionamentoMemoria implements RepositorioRelacionamento {
    private final UnidadeDeTrabalhoMemoria unidade;

    RepositorioRelacionamentoMemoria(UnidadeDeTrabalhoMemoria unidade) {
        this.unidade = unidade;
    }

    private BaseEmMemoria.Tabelas t() {
        unidade.exigirAtiva();
        return unidade.base().tabelas();
    }

    private void gravou() {
        unidade.base().contarGravacao();
    }

    @Override
    public Optional<Pessoa> buscarPessoa(String cpf) {
        return Optional.ofNullable(t().pessoas.get(cpf))
                .map(p -> new Pessoa(p.nome(), p.sobrenome(), p.cpf(), p.email()));
    }

    @Override
    public void salvarPessoa(Pessoa p) {
        t().pessoas.put(p.getCpf(), new BaseEmMemoria.PessoaReg(p.getCpf(), p.getNome(), p.getSobrenome(),
                p.getEmail()));
        gravou();
    }

    @Override
    public void removerPessoa(String cpf) {
        t().pessoas.remove(cpf);
        gravou();
    }

    @Override
    public Optional<Estudante> buscarEstudante(String matricula) {
        return Optional.ofNullable(t().estudantes.get(matricula))
                .flatMap(r -> buscarPessoa(r.cpf()).map(p -> new Estudante(r.id(), p)));
    }

    @Override
    public void salvarEstudante(Estudante e) {
        t().estudantes.put(e.getId(), new BaseEmMemoria.PapelReg(e.getId(), e.getCpf()));
        gravou();
    }

    @Override
    public void removerEstudante(String matricula) {
        t().estudantes.remove(matricula);
        gravou();
    }

    @Override
    public Optional<Atendente> buscarAtendente(String id) {
        return Optional.ofNullable(t().atendentes.get(id))
                .flatMap(r -> buscarPessoa(r.cpf()).map(p -> new Atendente(r.id(), p)));
    }

    @Override
    public void salvarAtendente(Atendente a) {
        t().atendentes.put(a.getId(), new BaseEmMemoria.PapelReg(a.getId(), a.getCpf()));
        gravou();
    }

    @Override
    public void removerAtendente(String id) {
        t().atendentes.remove(id);
        gravou();
    }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return unidade;
    }
}

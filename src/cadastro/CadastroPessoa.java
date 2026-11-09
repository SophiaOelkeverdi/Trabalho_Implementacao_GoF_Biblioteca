package cadastro;

import acesso.Relacionamento;
import dominio.pessoa.Pessoa;

import java.util.Optional;

// Padrão Template Method - classe concreta: cadastro de Pessoa (RH)
public class CadastroPessoa extends CadastroTemplate<CadastroPessoa.Dados, Pessoa> {

    public record Dados(String nome, String sobrenome, String cpf, String email) { }

    private final Relacionamento relacionamento;

    public CadastroPessoa(Relacionamento relacionamento) {
        this.relacionamento = relacionamento;
    }

    @Override
    protected Optional<Pessoa> buscarExistente(Dados d) {
        return relacionamento.consultarPessoa(Cpf.normalizar(d.cpf()));
    }

    @Override
    protected void validar(Dados d) {
        exigir(preenchido(d.nome()), "Nome obrigatorio");
        exigir(Cpf.valido(d.cpf()), "CPF deve ter 11 digitos");
        exigir(d.email() == null || d.email().isBlank() || d.email().contains("@"), "E-mail invalido");
    }

    // não usa o gancho verificarPreRequisitos

    @Override
    protected Pessoa criar(Dados d) {
        return new Pessoa(d.nome(), d.sobrenome(), Cpf.normalizar(d.cpf()), d.email());
    }

    @Override
    protected void armazenar(Pessoa pessoa) {
        relacionamento.inserirPessoa(pessoa);
    }
}

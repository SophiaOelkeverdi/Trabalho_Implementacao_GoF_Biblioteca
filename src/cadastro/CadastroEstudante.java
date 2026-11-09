package cadastro;

import acesso.Relacionamento;
import dominio.pessoa.Estudante;
import dominio.pessoa.Pessoa;

import java.util.Optional;

// Padrão Template Method - classe concreta: cadastro de Estudante (Acadêmico).
// Usa o gancho verificarPreRequisitos: a pessoa tem que estar cadastrada no RH.
public class CadastroEstudante extends CadastroTemplate<CadastroEstudante.Dados, Estudante> {

    public record Dados(String matricula, String cpf) { }

    private final Relacionamento relacionamento;

    public CadastroEstudante(Relacionamento relacionamento) {
        this.relacionamento = relacionamento;
    }

    @Override
    protected Optional<Estudante> buscarExistente(Dados d) {
        return relacionamento.consultarEstudante(d.matricula());
    }

    @Override
    protected void validar(Dados d) {
        exigir(preenchido(d.matricula()), "Matricula obrigatoria");
        exigir(Cpf.valido(d.cpf()), "CPF deve ter 11 digitos");
    }

    @Override
    protected void verificarPreRequisitos(Dados d) {
        exigir(relacionamento.consultarPessoa(Cpf.normalizar(d.cpf())).isPresent(),
                "Pessoa com CPF " + d.cpf() + " nao cadastrada no RH");
    }

    @Override
    protected Estudante criar(Dados d) {
        Pessoa pessoa = relacionamento.consultarPessoa(Cpf.normalizar(d.cpf())).orElseThrow();
        return new Estudante(d.matricula(), pessoa);
    }

    @Override
    protected void armazenar(Estudante estudante) {
        relacionamento.inserirEstudante(estudante);
    }
}

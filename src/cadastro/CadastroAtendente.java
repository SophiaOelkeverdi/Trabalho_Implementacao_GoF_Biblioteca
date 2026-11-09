package cadastro;

import acesso.Relacionamento;
import dominio.pessoa.Atendente;
import dominio.pessoa.Pessoa;

import java.util.Optional;

// Padrão Template Method - classe concreta: cadastro de Atendente (RH).
// Usa o gancho verificarPreRequisitos: a pessoa tem que estar cadastrada no RH.
public class CadastroAtendente extends CadastroTemplate<CadastroAtendente.Dados, Atendente> {

    public record Dados(String id, String cpf) { }

    private final Relacionamento relacionamento;

    public CadastroAtendente(Relacionamento relacionamento) {
        this.relacionamento = relacionamento;
    }

    @Override
    protected Optional<Atendente> buscarExistente(Dados d) {
        return relacionamento.consultarAtendente(d.id());
    }

    @Override
    protected void validar(Dados d) {
        exigir(preenchido(d.id()), "Identificador do atendente obrigatorio");
        exigir(Cpf.valido(d.cpf()), "CPF deve ter 11 digitos");
    }

    @Override
    protected void verificarPreRequisitos(Dados d) {
        exigir(relacionamento.consultarPessoa(Cpf.normalizar(d.cpf())).isPresent(),
                "Pessoa com CPF " + d.cpf() + " nao cadastrada no RH");
    }

    @Override
    protected Atendente criar(Dados d) {
        Pessoa pessoa = relacionamento.consultarPessoa(Cpf.normalizar(d.cpf())).orElseThrow();
        return new Atendente(d.id(), pessoa);
    }

    @Override
    protected void armazenar(Atendente atendente) {
        relacionamento.inserirAtendente(atendente);
    }
}

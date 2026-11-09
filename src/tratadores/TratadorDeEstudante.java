package tratadores;

import acesso.ExecutorTransacional;
import cadastro.CadastroEstudante;
import cadastro.ResultadoCadastro;
import dominio.pessoa.Estudante;
import dominio.pessoa.Papel;
import dominio.pessoa.Pessoa;

// Tratador de Estudante (Acadêmico): implementa IEstudante
public class TratadorDeEstudante implements IEstudante {
    private final ExecutorTransacional transacao;

    public TratadorDeEstudante(ExecutorTransacional transacao) {
        this.transacao = transacao;
    }

    @Override
    public ResultadoCadastro<Estudante> inserirEstudante(String id, Pessoa p) {
        return inserirEstudante(id, p.getCpf());
    }

    // versão mais prática pra tela: só o CPF
    public ResultadoCadastro<Estudante> inserirEstudante(String id, String cpf) {
        return transacao.executar(ctx -> new CadastroEstudante(ctx.relacionamento())
                .incluir(new CadastroEstudante.Dados(id, cpf)));
    }

    @Override
    public Papel consultarEstudante(String id) {
        return transacao.executar(ctx -> ctx.relacionamento().obterEstudante(id));
    }

    @Override
    public Papel excluirEstudante(String id) {
        return transacao.executar(ctx -> {
            Estudante e = ctx.relacionamento().obterEstudante(id);
            ctx.relacionamento().excluirEstudante(id);
            return e;
        });
    }
}

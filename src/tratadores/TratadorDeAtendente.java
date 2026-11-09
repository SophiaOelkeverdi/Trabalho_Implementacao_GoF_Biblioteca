package tratadores;

import acesso.ExecutorTransacional;
import cadastro.CadastroAtendente;
import cadastro.ResultadoCadastro;
import dominio.RegraDeNegocioException;
import dominio.pessoa.Atendente;
import dominio.pessoa.Papel;
import dominio.pessoa.Pessoa;

// Tratador de Atendente (RH): implementa IAtendente
public class TratadorDeAtendente implements IAtendente {
    private final ExecutorTransacional transacao;

    public TratadorDeAtendente(ExecutorTransacional transacao) {
        this.transacao = transacao;
    }

    @Override
    public ResultadoCadastro<Atendente> inserirAtendente(String id, Pessoa p) {
        return inserirAtendente(id, p.getCpf());
    }

    public ResultadoCadastro<Atendente> inserirAtendente(String id, String cpf) {
        return transacao.executar(ctx -> new CadastroAtendente(ctx.relacionamento())
                .incluir(new CadastroAtendente.Dados(id, cpf)));
    }

    @Override
    public Papel consultarAtendente(String id) {
        return transacao.executar(ctx -> ctx.relacionamento().consultarAtendente(id)
                .orElseThrow(() -> new RegraDeNegocioException("Atendente " + id + " nao cadastrado")));
    }

    @Override
    public Papel excluirAtendente(String id) {
        return transacao.executar(ctx -> {
            Atendente a = ctx.relacionamento().consultarAtendente(id)
                    .orElseThrow(() -> new RegraDeNegocioException("Atendente " + id + " nao cadastrado"));
            ctx.relacionamento().excluirAtendente(id);
            return a;
        });
    }
}

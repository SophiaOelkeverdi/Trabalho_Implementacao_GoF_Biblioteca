package tratadores;

import acesso.ExecutorTransacional;
import cadastro.CadastroPessoa;
import cadastro.ResultadoCadastro;
import dominio.RegraDeNegocioException;
import dominio.pessoa.Pessoa;

// Tratador de Pessoa (RH): implementa IPessoa usando o Relacionamento
public class TratadorDePessoa implements IPessoa {
    private final ExecutorTransacional transacao;

    public TratadorDePessoa(ExecutorTransacional transacao) {
        this.transacao = transacao;
    }

    @Override
    public ResultadoCadastro<Pessoa> inserirPessoa(String nome, String sobrenome, String cpf, String email) {
        return transacao.executar(ctx -> new CadastroPessoa(ctx.relacionamento())
                .incluir(new CadastroPessoa.Dados(nome, sobrenome, cpf, email)));
    }

    @Override
    public Pessoa consultarPessoa(String cpf) {
        return transacao.executar(ctx -> ctx.relacionamento().consultarPessoa(somenteDigitos(cpf))
                .orElseThrow(() -> new RegraDeNegocioException("Pessoa " + cpf + " nao cadastrada")));
    }

    @Override
    public Pessoa atualizarPessoa(String nome, String sobrenome, String cpf, String email) {
        return transacao.executar(ctx -> {
            Pessoa p = ctx.relacionamento().consultarPessoa(somenteDigitos(cpf))
                    .orElseThrow(() -> new RegraDeNegocioException("Pessoa " + cpf + " nao cadastrada"));
            p.atualizar(nome, sobrenome, email);
            ctx.relacionamento().atualizarPessoa(p);
            return p;
        });
    }

    @Override
    public Pessoa excluirPessoa(String cpf) {
        return transacao.executar(ctx -> {
            Pessoa p = ctx.relacionamento().consultarPessoa(somenteDigitos(cpf))
                    .orElseThrow(() -> new RegraDeNegocioException("Pessoa " + cpf + " nao cadastrada"));
            ctx.relacionamento().excluirPessoa(p.getCpf());
            return p;
        });
    }

    private static String somenteDigitos(String cpf) {
        return cpf.replaceAll("\\D", "");
    }
}

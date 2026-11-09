package tratadores;

import acesso.ExecutorTransacional;
import cadastro.CadastroLivro;
import cadastro.ResultadoCadastro;
import consulta.ConsultaAcervo;
import consulta.MetadadosLivro;
import dominio.RegraDeNegocioException;
import dominio.livro.Livro;

import java.util.List;

// Tratador de Acervo: implementa ILivro e oferece a consulta (com decoradores)
public class TratadorDeAcervo implements ILivro {
    private final ExecutorTransacional transacao;
    private final ConsultaAcervo consulta;

    public TratadorDeAcervo(ExecutorTransacional transacao, ConsultaAcervo consulta) {
        this.transacao = transacao;
        this.consulta = consulta;
    }

    @Override
    public ResultadoCadastro<Livro> inserirLivro(String id, String titulo, String autor) {
        return transacao.executar(ctx ->
                new CadastroLivro(ctx.acervo()).incluir(new CadastroLivro.Dados(id, titulo, autor)));
    }

    @Override
    public Livro consultarLivro(String id) {
        return transacao.executar(ctx -> ctx.acervo().obterLivro(id));
    }

    @Override
    public Livro atualizarLivro(String id, String titulo, String autor) {
        return transacao.executar(ctx -> {
            Livro livro = ctx.acervo().obterLivro(id);
            livro.atualizarDados(titulo, autor);
            ctx.acervo().atualizar(livro);
            return livro;
        });
    }

    // quem decide se pode excluir é o estado (só LIVRE aceita)
    @Override
    public Livro excluirLivro(String id) {
        return transacao.executar(ctx -> {
            Livro livro = ctx.acervo().obterLivro(id);
            livro.excluir();
            ctx.acervo().excluir(id);
            return livro;
        });
    }

    public List<Livro> listarLivros() {
        return transacao.executar(ctx -> ctx.acervo().listar());
    }

    public MetadadosLivro consultarMetadados(String idLivro) {
        return consulta.consultarMetadados(idLivro)
                .orElseThrow(() -> new RegraDeNegocioException("Livro " + idLivro + " nao cadastrado"));
    }

    public List<MetadadosLivro> pesquisarPorTitulo(String trecho) {
        return consulta.pesquisarPorTitulo(trecho);
    }
}

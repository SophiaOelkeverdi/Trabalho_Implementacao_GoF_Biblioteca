package cadastro;

import acesso.Acervo;
import dominio.livro.Livro;

import java.util.Optional;

// Padrão Template Method - classe concreta: cadastro de Livro (nasce LIVRE)
public class CadastroLivro extends CadastroTemplate<CadastroLivro.Dados, Livro> {

    public record Dados(String id, String titulo, String autor) { }

    private final Acervo acervo;

    public CadastroLivro(Acervo acervo) {
        this.acervo = acervo;
    }

    @Override
    protected Optional<Livro> buscarExistente(Dados d) {
        return acervo.consultarLivro(d.id());
    }

    @Override
    protected void validar(Dados d) {
        exigir(preenchido(d.id()), "Identificador do livro obrigatorio");
        exigir(preenchido(d.titulo()), "Titulo do livro obrigatorio");
    }

    // não usa o gancho verificarPreRequisitos (livro não depende de outro cadastro)

    @Override
    protected Livro criar(Dados d) {
        return new Livro(d.id(), d.titulo(), d.autor());
    }

    @Override
    protected void armazenar(Livro livro) {
        acervo.inserir(livro);
    }
}

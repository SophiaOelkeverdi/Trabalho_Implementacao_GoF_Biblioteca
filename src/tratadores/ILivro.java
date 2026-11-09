package tratadores;

import cadastro.ResultadoCadastro;
import dominio.livro.Livro;

// Interface ILivro (Trabalho 02), implementada pelo TratadorDeAcervo
public interface ILivro {

    ResultadoCadastro<Livro> inserirLivro(String id, String titulo, String autor);

    Livro consultarLivro(String id);

    Livro atualizarLivro(String id, String titulo, String autor);

    Livro excluirLivro(String id);
}

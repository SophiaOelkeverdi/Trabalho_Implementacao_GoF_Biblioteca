package tratadores;

import cadastro.ResultadoCadastro;
import dominio.pessoa.Pessoa;

// Interface IPessoa (Trabalho 02), implementada pelo TratadorDePessoa
public interface IPessoa {

    ResultadoCadastro<Pessoa> inserirPessoa(String nome, String sobrenome, String cpf, String email);

    Pessoa consultarPessoa(String cpf);

    Pessoa atualizarPessoa(String nome, String sobrenome, String cpf, String email);

    Pessoa excluirPessoa(String cpf);
}

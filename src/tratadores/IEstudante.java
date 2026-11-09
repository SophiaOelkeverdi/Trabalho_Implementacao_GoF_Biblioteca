package tratadores;

import cadastro.ResultadoCadastro;
import dominio.pessoa.Papel;
import dominio.pessoa.Pessoa;

// Interface IEstudante (Trabalho 02), implementada pelo TratadorDeEstudante
public interface IEstudante {

    ResultadoCadastro<? extends Papel> inserirEstudante(String id, Pessoa p);

    Papel consultarEstudante(String id);

    Papel excluirEstudante(String id);
}

package tratadores;

import cadastro.ResultadoCadastro;
import dominio.pessoa.Papel;
import dominio.pessoa.Pessoa;

// Interface IAtendente (Trabalho 02), implementada pelo TratadorDeAtendente
public interface IAtendente {

    ResultadoCadastro<? extends Papel> inserirAtendente(String id, Pessoa p);

    Papel consultarAtendente(String id);

    Papel excluirAtendente(String id);
}

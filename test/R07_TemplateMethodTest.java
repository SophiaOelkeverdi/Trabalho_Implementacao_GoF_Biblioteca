import cadastro.CadastroInvalidoException;
import cadastro.CadastroTemplate;
import cadastro.ResultadoCadastro;
import dominio.RegraDeNegocioException;
import dominio.pessoa.Estudante;
import dominio.pessoa.Pessoa;
import persistencia.memoria.BaseEmMemoria;
import persistencia.memoria.FabricaPersistenciaMemoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("R07 - Rotina comum para cadastros (Template Method)")
class R07_TemplateMethodTest {

    @Test
    void R07_incluirEstudanteDuplicadoRetornaOCadastroExistente() {
        Cenario c = new Cenario();                         // E1 ja cadastrado com o CPF da Ana
        BaseEmMemoria base = ((FabricaPersistenciaMemoria) c.app.fabrica()).base();
        long gravacoesAntes = base.totalDeGravacoes();

        Pessoa bruno = c.app.tratadorDePessoa().consultarPessoa(Cenario.CPF_BRUNO);
        ResultadoCadastro<Estudante> r = c.app.tratadorDeEstudante().inserirEstudante("E1", bruno);

        assertFalse(r.novo());
        assertEquals("E1", r.entidade().getId());
        assertEquals(Cenario.CPF_ANA, r.entidade().getCpf(), "devolve o cadastro existente, sem sobrescrever");
        assertEquals(gravacoesAntes, base.totalDeGravacoes(), "nada foi gravado");
    }

    @Test
    void R07_novoEstudanteComCpfNaoCadastradoFalhaAntesDeQualquerGravacao() {
        Cenario c = new Cenario();
        BaseEmMemoria base = ((FabricaPersistenciaMemoria) c.app.fabrica()).base();
        long gravacoesAntes = base.totalDeGravacoes();
        Pessoa desconhecida = new Pessoa("Carla", "Dias", "99988877766", "carla@x.br");

        assertThrows(CadastroInvalidoException.class,
                () -> c.app.tratadorDeEstudante().inserirEstudante("E9", desconhecida));

        assertEquals(gravacoesAntes, base.totalDeGravacoes(), "nenhuma gravacao");
        assertThrows(RegraDeNegocioException.class, () -> c.app.tratadorDeEstudante().consultarEstudante("E9"));
    }

    @Test
    void R07_metodoModeloIncluirNaoPodeSerSobrescrito() throws Exception {
        assertTrue(Modifier.isFinal(CadastroTemplate.class.getMethod("incluir", Object.class).getModifiers()));
    }

    @Test
    void R07_osQuatroCadastrosUsamOMesmoMetodoModelo() {
        Cenario c = new Cenario();
        assertTrue(c.app.tratadorDePessoa().inserirPessoa("Carla", "Dias", "99988877766", "c@x.br").novo());
        assertTrue(c.app.tratadorDeAtendente().inserirAtendente("AT1", "99988877766").novo());
        assertTrue(c.app.tratadorDeAcervo().inserirLivro("L9", "Novo livro", "Autor").novo());
        assertFalse(c.app.tratadorDeAcervo().inserirLivro("L9", "Outro titulo", "Outro").novo());
        assertThrows(CadastroInvalidoException.class,
                () -> c.app.tratadorDeAcervo().inserirLivro("L10", "", "Autor"));
    }
}

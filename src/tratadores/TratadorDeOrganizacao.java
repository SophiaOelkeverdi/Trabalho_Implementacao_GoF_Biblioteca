package tratadores;

import dominio.RegraDeNegocioException;
import organizacao.GrupoAcervo;

import java.util.LinkedHashMap;
import java.util.Map;

// Tratador da organização do acervo em árvore (Composite). Os grupos são achados pelo nome.
public class TratadorDeOrganizacao {
    private final GrupoAcervo raiz;
    private final Map<String, GrupoAcervo> grupos = new LinkedHashMap<>();

    public TratadorDeOrganizacao(String nomeDaUnidadeRaiz) {
        this.raiz = new GrupoAcervo(nomeDaUnidadeRaiz, GrupoAcervo.Tipo.UNIDADE);
        grupos.put(nomeDaUnidadeRaiz, raiz);
    }

    public synchronized String criarGrupo(String nomeDoPai, String nome, GrupoAcervo.Tipo tipo) {
        if (grupos.containsKey(nome)) {
            throw new RegraDeNegocioException("Ja existe um grupo chamado " + nome);
        }
        GrupoAcervo novo = new GrupoAcervo(nome, tipo);
        grupo(nomeDoPai).adicionar(novo);
        grupos.put(nome, novo);
        return novo.nome();
    }

    public synchronized void incluirLivro(String nomeDoGrupo, String idLivro) {
        grupo(nomeDoGrupo).adicionarLivro(idLivro);
    }

    public synchronized void moverLivro(String idLivro, String nomeDoGrupoDestino) {
        raiz.moverLivro(idLivro, grupo(nomeDoGrupoDestino));
    }

    public synchronized int contarLivros(String nomeDoGrupo) {
        return grupo(nomeDoGrupo).contarLivros();
    }

    public synchronized String desenhar() {
        return raiz.desenhar("");
    }

    private GrupoAcervo grupo(String nome) {
        GrupoAcervo g = grupos.get(nome);
        if (g == null) {
            throw new RegraDeNegocioException("Grupo " + nome + " nao encontrado");
        }
        return g;
    }
}

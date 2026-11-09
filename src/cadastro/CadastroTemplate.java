package cadastro;

import java.util.Optional;

// Padrão Template Method - papel AbstractClass.
// incluir() é o método-modelo: é final (ninguém sobrescreve) e fixa a ordem dos passos:
//   1 buscar existente -> 2 validar -> 3 verificar pré-requisitos -> 4 criar -> 5 armazenar -> 6 apresentar
//
// Passos abstratos (toda subclasse implementa): buscarExistente, validar, criar, armazenar.
// Ganchos (hooks: já têm uma versão padrão, a subclasse sobrescreve se quiser):
//   verificarPreRequisitos e apresentar.
//
// armazenar() é o único passo que grava e vem depois dos pré-requisitos,
// então se o pré-requisito falhar nada foi gravado.
public abstract class CadastroTemplate<D, T> {   // D = dados de entrada, T = entidade

    public final ResultadoCadastro<T> incluir(D dados) {
        Optional<T> existente = buscarExistente(dados);
        if (existente.isPresent()) {
            return apresentar(existente.get(), false);   // duplicado: devolve o que já existe
        }
        validar(dados);
        verificarPreRequisitos(dados);
        T nova = criar(dados);
        armazenar(nova);
        return apresentar(nova, true);
    }

    protected abstract Optional<T> buscarExistente(D dados);

    protected abstract void validar(D dados);

    // gancho: por padrão não tem pré-requisito
    protected void verificarPreRequisitos(D dados) {
    }

    protected abstract T criar(D dados);

    protected abstract void armazenar(T entidade);

    // gancho: por padrão só monta a mensagem
    protected ResultadoCadastro<T> apresentar(T entidade, boolean novo) {
        return new ResultadoCadastro<>(entidade, novo,
                (novo ? "Cadastro incluido: " : "Cadastro ja existente: ") + entidade);
    }

    // auxiliares pras subclasses
    protected static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new CadastroInvalidoException(mensagem);
        }
    }

    protected static boolean preenchido(String s) {
        return s != null && !s.isBlank();
    }
}

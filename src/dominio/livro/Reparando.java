package dominio.livro;

// Padrão State - estado concreto REPARANDO (do / fazerReparo)
public final class Reparando extends EstadoLivro {
    public static final String NOME = "REPARANDO";
    public static final Reparando INSTANCIA = new Reparando();

    private Reparando() {
    }

    @Override
    public String nome() { return NOME; }

    // avaliarReparo [consertado == true] -> LIVRE
    @Override
    public void concluirReparo(Livro livro) {
        livro.alterarEstado(Livre.INSTANCIA);
    }

    // laudo irrecuperável -> INCINERADO
    @Override
    public void registrarLaudoIrrecuperavel(Livro livro) {
        livro.alterarEstado(Incinerado.INSTANCIA);
    }
}

package dominio.livro;

// Padrão State - estado concreto INCINERADO. Estado final: recusa tudo.
public final class Incinerado extends EstadoLivro {
    public static final String NOME = "INCINERADO";
    public static final Incinerado INSTANCIA = new Incinerado();

    private Incinerado() {
    }

    @Override
    public String nome() { return NOME; }
}

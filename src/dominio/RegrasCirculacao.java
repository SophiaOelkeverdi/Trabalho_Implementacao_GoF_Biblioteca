package dominio;

// Constantes das regras de circulação (premissas do Trabalho 02)
public final class RegrasCirculacao {
    public static final int PRAZO_EMPRESTIMO_DIAS = 14;
    public static final int DIAS_POR_RENOVACAO = 30;
    public static final int LIMITE_RENOVACOES = 2;     // guarda: numRenovacoes < 2
    public static final int VALIDADE_RESERVA_DIAS = 3;

    private RegrasCirculacao() {
    }
}

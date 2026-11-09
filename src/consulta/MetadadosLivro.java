package consulta;

// Só os METADADOS do livro (coisas que quase nunca mudam).
// De propósito NÃO tem o estado: assim o cache nunca tem como influenciar a reserva.
public record MetadadosLivro(String idLivro, String titulo, String autor) {
}

package dominio.pessoa;

// Papel que uma pessoa desempenha ("é um"): Estudante ou Atendente
public abstract class Papel {
    private final String id;
    private final Pessoa pessoa;

    protected Papel(String id, Pessoa pessoa) {
        this.id = id;
        this.pessoa = pessoa;
    }

    public String getId() { return id; }
    public Pessoa getPessoa() { return pessoa; }
    public String getCpf() { return pessoa.getCpf(); }
    public String getNome() { return pessoa.getNomeCompleto(); }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + id + " - " + pessoa;
    }
}

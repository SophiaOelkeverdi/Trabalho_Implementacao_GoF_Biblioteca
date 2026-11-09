package dominio.pessoa;

import java.util.Objects;

// Pessoa cadastrada no RH. Estudante e Atendente são papéis de uma Pessoa.
public class Pessoa {
    private final String cpf;
    private String nome;
    private String sobrenome;
    private String email;

    public Pessoa(String nome, String sobrenome, String cpf, String email) {
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.cpf = cpf;
        this.email = email;
    }

    public void atualizar(String nome, String sobrenome, String email) {
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.email = email;
    }

    public String getCpf() { return cpf; }
    public String getNome() { return nome; }
    public String getSobrenome() { return sobrenome; }
    public String getEmail() { return email; }
    public String getNomeCompleto() { return nome + " " + sobrenome; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Pessoa p && p.cpf.equals(cpf);
    }

    @Override
    public int hashCode() { return Objects.hash(cpf); }

    @Override
    public String toString() { return getNomeCompleto() + " (CPF " + cpf + ")"; }
}

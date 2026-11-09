package persistencia;

// Padrão Abstract Factory - produto abstrato "unidade de trabalho".
// Representa uma transação: os repositórios criados com a mesma unidade gravam juntos
// (ou tudo é confirmado, ou tudo é desfeito).
public interface UnidadeDeTrabalho {

    String familia();   // "memoria" ou "mysql"

    void iniciar();

    void confirmar();

    void desfazer();

    boolean ativa();
}

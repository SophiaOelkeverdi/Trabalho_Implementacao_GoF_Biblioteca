package devolucao;

// Padrão Observer - papel Observer.
// Pra criar uma reação nova à devolução: implementar esta interface e registrar no
// PublicadorDevolucao (na classe Aplicacao). O tratador não muda.
public interface OuvinteDevolucao {

    String nome();

    void aoRegistrarDevolucao(EventoDevolucao evento);
}

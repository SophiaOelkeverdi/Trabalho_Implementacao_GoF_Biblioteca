package devolucao;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// Evento publicado DEPOIS que a devolução foi confirmada.
// Já leva tudo que os ouvintes precisam (vencimento, data de devolução e estudante), assim nenhum
// ouvinte precisa ir no banco depois, quando os dados já poderiam ter mudado.
public record EventoDevolucao(String idDevolucao, String idLivro, String tituloLivro, String idEstudante,
                              String cpfEstudante, String nomeEstudante, String emailEstudante,
                              LocalDate vencimento, LocalDate dataDevolucao) {

    public long diasAtraso() {
        return Math.max(0, ChronoUnit.DAYS.between(vencimento, dataDevolucao));
    }

    public boolean atrasada() {
        return diasAtraso() > 0;
    }
}

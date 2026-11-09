package devolucao;

// Indicadores que as devoluções alimentam
public class IndicadoresCirculacao {

    public record Retrato(long totalDevolucoes, long devolucoesAtrasadas, long diasDeAtrasoAcumulados) { }

    private long totalDevolucoes;
    private long devolucoesAtrasadas;
    private long diasDeAtrasoAcumulados;

    public synchronized void registrar(EventoDevolucao evento) {
        totalDevolucoes++;
        if (evento.atrasada()) {
            devolucoesAtrasadas++;
            diasDeAtrasoAcumulados += evento.diasAtraso();
        }
    }

    public synchronized Retrato retrato() {
        return new Retrato(totalDevolucoes, devolucoesAtrasadas, diasDeAtrasoAcumulados);
    }
}

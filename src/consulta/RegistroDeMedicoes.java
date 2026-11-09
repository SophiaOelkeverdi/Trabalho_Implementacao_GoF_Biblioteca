package consulta;

import java.util.ArrayList;
import java.util.List;

// Guarda as medições feitas pelo ConsultaComMedicao
public class RegistroDeMedicoes {

    public record Medicao(String operacao, long duracaoNanos) { }

    private final List<Medicao> medicoes = new ArrayList<>();

    public synchronized void registrar(String operacao, long duracaoNanos) {
        medicoes.add(new Medicao(operacao, duracaoNanos));
    }

    public synchronized int quantidade() {
        return medicoes.size();
    }

    public synchronized List<Medicao> todas() {
        return List.copyOf(medicoes);
    }
}

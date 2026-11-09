package config;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

// "Hoje" do sistema. Permite avançar dias na demonstração (devolução atrasada)
// e fixar a data nos testes, sem o domínio chamar LocalDate.now() direto.
public class Relogio {
    private final Clock base;
    private long deslocamentoDias;

    public Relogio() {
        this(Clock.systemDefaultZone());
    }

    public Relogio(Clock base) {
        this.base = base;
    }

    public synchronized LocalDate hoje() {
        return LocalDate.now(base).plusDays(deslocamentoDias);
    }

    public synchronized Instant agora() {
        return base.instant();
    }

    public synchronized void avancarDias(long dias) {
        deslocamentoDias += dias;
    }
}

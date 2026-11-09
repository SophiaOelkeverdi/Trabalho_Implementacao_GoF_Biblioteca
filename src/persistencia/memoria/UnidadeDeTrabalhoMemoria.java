package persistencia.memoria;

import persistencia.PersistenciaException;
import persistencia.UnidadeDeTrabalho;

// Padrão Abstract Factory - produto concreto (família em memória).
// iniciar(): trava a base e tira uma "foto" das tabelas.
// desfazer(): volta a foto. confirmar(): só descarta a foto.
public class UnidadeDeTrabalhoMemoria implements UnidadeDeTrabalho {
    private final BaseEmMemoria base;
    private BaseEmMemoria.Tabelas fotografia;

    UnidadeDeTrabalhoMemoria(BaseEmMemoria base) {
        this.base = base;
    }

    BaseEmMemoria base() {
        return base;
    }

    @Override
    public String familia() {
        return FabricaPersistenciaMemoria.FAMILIA;
    }

    @Override
    public void iniciar() {
        if (ativa()) {
            throw new PersistenciaException("Unidade de trabalho ja iniciada");
        }
        base.trava().lock();
        fotografia = base.fotografar();
    }

    @Override
    public void confirmar() {
        exigirAtiva();
        fotografia = null;
        base.trava().unlock();
    }

    @Override
    public void desfazer() {
        if (!ativa()) {
            return;
        }
        base.restaurar(fotografia);
        fotografia = null;
        base.trava().unlock();
    }

    @Override
    public boolean ativa() {
        return fotografia != null;
    }

    void exigirAtiva() {
        if (!ativa()) {
            throw new PersistenciaException("Operacao de dados fora de uma unidade de trabalho");
        }
    }
}

package persistencia;

import persistencia.jdbc.FabricaPersistenciaJdbc;
import persistencia.memoria.FabricaPersistenciaMemoria;

import java.util.Properties;

// Único lugar onde a família de persistência é escolhida (uma vez, ao iniciar o sistema)
public final class PerfilExecucao {
    public static final String TESTE = "teste";
    public static final String PRODUCAO = "producao";

    private PerfilExecucao() {
    }

    public static FabricaPersistencia escolherFabrica(Properties config) {
        String perfil = config.getProperty("perfil", TESTE);
        return switch (perfil) {
            case TESTE -> new FabricaPersistenciaMemoria();
            case PRODUCAO -> {
                FabricaPersistenciaJdbc f = new FabricaPersistenciaJdbc(
                        config.getProperty("jdbc.url"),
                        config.getProperty("jdbc.usuario"),
                        config.getProperty("jdbc.senha"));
                if (Boolean.parseBoolean(config.getProperty("jdbc.criarEsquema", "true"))) {
                    f.criarEsquemaSeNecessario();
                }
                yield f;
            }
            default -> throw new IllegalArgumentException("Perfil desconhecido: " + perfil);
        };
    }
}

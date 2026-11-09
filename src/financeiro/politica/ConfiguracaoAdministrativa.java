package financeiro.politica;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

// Configuração administrativa: lista de políticas disponíveis + a política vigente.
// O ServicoDeMultas pergunta a vigente na hora de emitir cada multa.
public class ConfiguracaoAdministrativa {
    private final Map<String, PoliticaMulta> politicas = new LinkedHashMap<>();
    private volatile PoliticaMulta vigente;

    public ConfiguracaoAdministrativa registrar(PoliticaMulta politica) {
        politicas.put(politica.nome(), politica);
        if (vigente == null) {
            vigente = politica;
        }
        return this;
    }

    public void definirPoliticaVigente(String nome) {
        PoliticaMulta p = politicas.get(nome);
        if (p == null) {
            throw new IllegalArgumentException("Politica de multa desconhecida: " + nome
                    + ". Disponiveis: " + politicas.keySet());
        }
        vigente = p;
    }

    public PoliticaMulta politicaVigente() {
        return vigente;
    }

    public Set<String> politicasDisponiveis() {
        return Collections.unmodifiableSet(politicas.keySet());
    }
}

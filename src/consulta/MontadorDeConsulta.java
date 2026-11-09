package consulta;

// Monta a corrente de decoradores conforme a instalação (biblioteca.properties).
// Ordem (de fora pra dentro): Medicao -> Cache -> Original
//  - assim TODA consulta é medida, até as que o cache respondeu
//  - se fosse Cache -> Medicao -> Original, os acertos de cache não seriam medidos
public final class MontadorDeConsulta {
    private MontadorDeConsulta() {
    }

    public static ConsultaAcervo montar(ConsultaAcervo original, boolean comCache, boolean comMedicao,
                                        RegistroDeMedicoes medicoes) {
        ConsultaAcervo consulta = original;
        if (comCache) {
            consulta = new ConsultaComCache(consulta);
        }
        if (comMedicao) {
            consulta = new ConsultaComMedicao(consulta, medicoes);
        }
        return consulta;
    }
}

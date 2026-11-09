package devolucao.ouvintes;

import devolucao.EventoDevolucao;
import devolucao.IndicadoresCirculacao;
import devolucao.OuvinteDevolucao;

// Padrão Observer - observador concreto: atualiza os indicadores
public class OuvinteIndicadores implements OuvinteDevolucao {
    private final IndicadoresCirculacao indicadores;

    public OuvinteIndicadores(IndicadoresCirculacao indicadores) {
        this.indicadores = indicadores;
    }

    @Override
    public String nome() {
        return "indicadores";
    }

    @Override
    public void aoRegistrarDevolucao(EventoDevolucao evento) {
        indicadores.registrar(evento);
    }
}

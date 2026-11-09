package consulta;

import java.util.List;
import java.util.Optional;

// Padrão Decorator - papel Component (contrato da consulta ao acervo)
public interface ConsultaAcervo {

    Optional<MetadadosLivro> consultarMetadados(String idLivro);

    List<MetadadosLivro> pesquisarPorTitulo(String trecho);
}

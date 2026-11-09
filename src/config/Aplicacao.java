package config;

import acesso.ExecutorTransacional;
import circulacao.FachadaCirculacao;
import circulacao.comando.ExecutorDeSolicitacoes;
import consulta.ConsultaAcervo;
import consulta.ConsultaAcervoOriginal;
import consulta.MontadorDeConsulta;
import consulta.RegistroDeMedicoes;
import devolucao.FilaDeInspecao;
import devolucao.IndicadoresCirculacao;
import devolucao.PublicadorDevolucao;
import devolucao.ServicoEmail;
import devolucao.ouvintes.OuvinteEmail;
import devolucao.ouvintes.OuvinteIndicadores;
import devolucao.ouvintes.OuvinteInspecao;
import devolucao.ouvintes.OuvinteMulta;
import externo.BancoAApi;
import externo.BancoBApi;
import externo.SimuladorDePagamentos;
import financeiro.ServicoDeMultas;
import financeiro.cobranca.AdaptadorBancoA;
import financeiro.cobranca.AdaptadorBancoB;
import financeiro.cobranca.ICobranca;
import financeiro.politica.ConfiguracaoAdministrativa;
import financeiro.politica.PoliticaComTeto;
import financeiro.politica.PoliticaComTolerancia;
import financeiro.politica.PoliticaOriginal;
import persistencia.FabricaPersistencia;
import persistencia.PerfilExecucao;
import tratadores.TratadorDeAcervo;
import tratadores.TratadorDeAtendente;
import tratadores.TratadorDeDevolucao;
import tratadores.TratadorDeEmprestimo;
import tratadores.TratadorDeEstudante;
import tratadores.TratadorDeOrganizacao;
import tratadores.TratadorDePessoa;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

// Classe que monta o sistema inteiro. É o único lugar que conhece as classes concretas:
//  - escolhe a família de persistência (Abstract Factory), uma vez só, pelo perfil
//  - escolhe o adaptador do banco (Adapter) pela propriedade "banco"
//  - registra as políticas de multa e define a vigente (Strategy)
//  - registra os ouvintes da devolução (Observer)
//  - monta a corrente de decoradores da consulta (Decorator)
public class Aplicacao {
    private final Properties configuracao;
    private final Relogio relogio;
    private final FabricaPersistencia fabrica;
    private final ExecutorTransacional transacao;
    private final ConfiguracaoAdministrativa configuracaoAdministrativa;
    private final BancoAApi bancoA = new BancoAApi();
    private final BancoBApi bancoB = new BancoBApi();
    private final ServicoDeMultas servicoDeMultas;
    private final PublicadorDevolucao publicador = new PublicadorDevolucao();
    private final FilaDeInspecao filaDeInspecao = new FilaDeInspecao();
    private final IndicadoresCirculacao indicadores = new IndicadoresCirculacao();
    private final RegistroDeMedicoes medicoes = new RegistroDeMedicoes();
    private final FachadaCirculacao fachada;
    private final ExecutorDeSolicitacoes executorDeSolicitacoes;
    private final ConsultaAcervo consulta;

    private final TratadorDeAcervo tratadorDeAcervo;
    private final TratadorDePessoa tratadorDePessoa;
    private final TratadorDeEstudante tratadorDeEstudante;
    private final TratadorDeAtendente tratadorDeAtendente;
    private final TratadorDeEmprestimo tratadorDeEmprestimo;
    private final TratadorDeDevolucao tratadorDeDevolucao;
    private final TratadorDeOrganizacao tratadorDeOrganizacao;

    private Aplicacao(Properties cfg, Relogio relogio, ServicoEmail email) {
        this.configuracao = cfg;
        this.relogio = relogio;

        // Abstract Factory
        this.fabrica = PerfilExecucao.escolherFabrica(cfg);
        this.transacao = new ExecutorTransacional(fabrica);

        // Strategy (pra adicionar uma política nova é só registrar aqui)
        this.configuracaoAdministrativa = new ConfiguracaoAdministrativa()
                .registrar(new PoliticaOriginal())
                .registrar(new PoliticaComTeto())
                .registrar(new PoliticaComTolerancia());
        configuracaoAdministrativa.definirPoliticaVigente(cfg.getProperty("politica.multa", PoliticaOriginal.NOME));

        // Adapter
        this.servicoDeMultas = new ServicoDeMultas(transacao, configuracaoAdministrativa, escolherCobranca(cfg),
                relogio);

        // Observer (pra adicionar um ouvinte novo é só registrar aqui)
        publicador.registrar(new OuvinteInspecao(filaDeInspecao));
        publicador.registrar(new OuvinteMulta(servicoDeMultas));
        publicador.registrar(new OuvinteIndicadores(indicadores));
        publicador.registrar(new OuvinteEmail(email));

        // Facade + Command
        this.fachada = new FachadaCirculacao(transacao, publicador, filaDeInspecao, relogio);
        this.executorDeSolicitacoes = new ExecutorDeSolicitacoes(fachada);

        // Decorator
        this.consulta = MontadorDeConsulta.montar(new ConsultaAcervoOriginal(transacao),
                Boolean.parseBoolean(cfg.getProperty("consulta.cache", "true")),
                Boolean.parseBoolean(cfg.getProperty("consulta.medicao", "true")),
                medicoes);

        // Tratadores (a interface só conversa com eles)
        this.tratadorDeAcervo = new TratadorDeAcervo(transacao, consulta);
        this.tratadorDePessoa = new TratadorDePessoa(transacao);
        this.tratadorDeEstudante = new TratadorDeEstudante(transacao);
        this.tratadorDeAtendente = new TratadorDeAtendente(transacao);
        this.tratadorDeEmprestimo = new TratadorDeEmprestimo(fachada, executorDeSolicitacoes);
        this.tratadorDeDevolucao = new TratadorDeDevolucao(fachada, servicoDeMultas, filaDeInspecao, indicadores);
        this.tratadorDeOrganizacao = new TratadorDeOrganizacao(cfg.getProperty("acervo.unidade", "Central"));
    }

    // único lugar com "if por banco" do sistema
    private ICobranca escolherCobranca(Properties cfg) {
        String banco = cfg.getProperty("banco", "A");
        return switch (banco) {
            case "A" -> new AdaptadorBancoA(bancoA);
            case "B" -> new AdaptadorBancoB(bancoB);
            default -> throw new IllegalArgumentException("Banco desconhecido: " + banco);
        };
    }

    public static Aplicacao montar(Properties cfg, Relogio relogio, ServicoEmail email) {
        return new Aplicacao(cfg, relogio, email);
    }

    // lê o biblioteca.properties; o que vier com -D na linha de comando tem prioridade
    public static Properties carregarConfiguracao() {
        Properties p = new Properties();
        try (InputStream in = Aplicacao.class.getResourceAsStream("/biblioteca.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        for (String chave : System.getProperties().stringPropertyNames()) {
            if (p.containsKey(chave)) {
                p.setProperty(chave, System.getProperty(chave));
            }
        }
        return p;
    }

    public Properties configuracao() { return configuracao; }
    public Relogio relogio() { return relogio; }
    public FabricaPersistencia fabrica() { return fabrica; }
    public ExecutorTransacional transacao() { return transacao; }
    public ConfiguracaoAdministrativa configuracaoAdministrativa() { return configuracaoAdministrativa; }
    public ServicoDeMultas servicoDeMultas() { return servicoDeMultas; }
    public PublicadorDevolucao publicador() { return publicador; }
    public FilaDeInspecao filaDeInspecao() { return filaDeInspecao; }
    public IndicadoresCirculacao indicadores() { return indicadores; }
    public RegistroDeMedicoes medicoes() { return medicoes; }
    public FachadaCirculacao fachada() { return fachada; }
    public ExecutorDeSolicitacoes executorDeSolicitacoes() { return executorDeSolicitacoes; }
    public ConsultaAcervo consulta() { return consulta; }
    public BancoAApi bancoA() { return bancoA; }
    public BancoBApi bancoB() { return bancoB; }
    public SimuladorDePagamentos simuladorDePagamentos() { return new SimuladorDePagamentos(bancoA, bancoB); }

    public TratadorDeAcervo tratadorDeAcervo() { return tratadorDeAcervo; }
    public TratadorDePessoa tratadorDePessoa() { return tratadorDePessoa; }
    public TratadorDeEstudante tratadorDeEstudante() { return tratadorDeEstudante; }
    public TratadorDeAtendente tratadorDeAtendente() { return tratadorDeAtendente; }
    public TratadorDeEmprestimo tratadorDeEmprestimo() { return tratadorDeEmprestimo; }
    public TratadorDeDevolucao tratadorDeDevolucao() { return tratadorDeDevolucao; }
    public TratadorDeOrganizacao tratadorDeOrganizacao() { return tratadorDeOrganizacao; }
}

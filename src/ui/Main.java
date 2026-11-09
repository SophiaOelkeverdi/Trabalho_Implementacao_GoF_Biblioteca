package ui;

import circulacao.Comprovantes.ComprovanteDevolucao;
import circulacao.comando.ExecutorDeSolicitacoes.Resultado;
import circulacao.comando.SolicitacaoCirculacao;
import circulacao.comando.SolicitacaoRenovacao;
import circulacao.comando.SolicitacaoReserva;
import config.Aplicacao;
import config.Relogio;
import devolucao.ServicoEmailSimulado;
import dominio.RegraDeNegocioException;
import externo.SimuladorDePagamentos;
import organizacao.GrupoAcervo;
import tratadores.TratadorDeAcervo;
import tratadores.TratadorDeAtendente;
import tratadores.TratadorDeDevolucao;
import tratadores.TratadorDeEmprestimo;
import tratadores.TratadorDeEstudante;
import tratadores.TratadorDeOrganizacao;
import tratadores.TratadorDePessoa;

import java.util.Properties;
import java.util.Scanner;

// Interface de linha de comando.
// Só lê os dados, chama os TRATADORES e mostra os comprovantes. Nenhuma regra de negócio aqui.
// As opções marcadas com [SIMULACAO] fazem o papel do mundo externo (passar o tempo e o banco).
public class Main {
    private final Aplicacao app;
    private final Scanner in;
    private final TratadorDePessoa pessoas;
    private final TratadorDeEstudante estudantes;
    private final TratadorDeAtendente atendentes;
    private final TratadorDeAcervo acervo;
    private final TratadorDeEmprestimo emprestimos;
    private final TratadorDeDevolucao devolucoes;
    private final TratadorDeOrganizacao organizacao;
    private final SimuladorDePagamentos banco;
    private final Relogio relogio;
    private SolicitacaoCirculacao<?> ultimaSolicitacao;

    Main(Aplicacao app, Scanner in) {
        this.app = app;
        this.in = in;
        this.pessoas = app.tratadorDePessoa();
        this.estudantes = app.tratadorDeEstudante();
        this.atendentes = app.tratadorDeAtendente();
        this.acervo = app.tratadorDeAcervo();
        this.emprestimos = app.tratadorDeEmprestimo();
        this.devolucoes = app.tratadorDeDevolucao();
        this.organizacao = app.tratadorDeOrganizacao();
        this.banco = app.simuladorDePagamentos();
        this.relogio = app.relogio();
    }

    public static void main(String[] args) {
        Properties cfg = Aplicacao.carregarConfiguracao();
        Aplicacao app = Aplicacao.montar(cfg, new Relogio(), new ServicoEmailSimulado());
        new Main(app, new Scanner(System.in)).executar();
    }

    void executar() {
        while (true) {
            mostrarMenu();
            String opcao = ler("Opcao");
            if (opcao.equals("0")) {
                System.out.println("Ate logo.");
                return;
            }
            try {
                tratar(opcao);
            } catch (RegraDeNegocioException e) {
                System.out.println("  RECUSADO: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("  ERRO: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
        }
    }

    private void mostrarMenu() {
        Properties c = app.configuracao();
        System.out.println();
        System.out.println("=== Biblioteca | perfil " + c.getProperty("perfil") + " | banco " + c.getProperty("banco")
                + " | politica " + app.configuracaoAdministrativa().politicaVigente().nome()
                + " | hoje " + relogio.hoje() + " ===");
        System.out.println(" 1 Carregar dados de demonstracao      20 FLUXO COMPLETO automatico");
        System.out.println(" -- Cadastros:  2 Pessoa  3 Estudante  4 Atendente  5 Livro  6 Listar livros");
        System.out.println(" -- Circulacao: 7 Reservar  8 Emprestar  9 Renovar  10 Devolver  11 Cancelar reserva");
        System.out.println(" -- Inspecao:   12 Inspecionar  13 Concluir reparo  14 Laudo irrecuperavel  15 Fila");
        System.out.println(" -- Financeiro: 16 Emitir boleto  17 Listar multas  18 [SIMULACAO] Banco paga boleto");
        System.out.println(" -- Outros:     19 Consultar metadados  21 Organizacao do acervo  22 Indicadores");
        System.out.println("                23 [SIMULACAO] Avancar relogio  24 Repetir ultima solicitacao  0 Sair");
    }

    private void tratar(String opcao) {
        switch (opcao) {
            case "1" -> carregarDemonstracao();
            case "2" -> System.out.println("  " + pessoas.inserirPessoa(ler("Nome"), ler("Sobrenome"), ler("CPF"),
                    ler("E-mail")).mensagem());
            case "3" -> System.out.println("  " + estudantes.inserirEstudante(ler("Matricula"), ler("CPF da pessoa"))
                    .mensagem());
            case "4" -> System.out.println("  " + atendentes.inserirAtendente(ler("Id do atendente"),
                    ler("CPF da pessoa")).mensagem());
            case "5" -> System.out.println("  " + acervo.inserirLivro(ler("Id do livro"), ler("Titulo"), ler("Autor"))
                    .mensagem());
            case "6" -> acervo.listarLivros().forEach(l -> System.out.println("  " + l));
            case "7" -> {
                // a TELA cria a solicitação (Command) e manda pro tratador -> executor
                SolicitacaoReserva s = SolicitacaoReserva.nova(ler("Autor (atendente ou estudante)"),
                        ler("Id do livro"), ler("Matricula do estudante"));
                submeter(s);
            }
            case "8" -> System.out.println("  " + emprestimos.realizarEmprestimo(ler("Id do livro"), ler("Matricula")));
            case "9" -> {
                SolicitacaoRenovacao s = SolicitacaoRenovacao.nova(ler("Autor"), ler("Id do livro"), ler("Matricula"));
                submeter(s);
            }
            case "10" -> imprimirDevolucao(devolucoes.registrarDevolucao(ler("Id do livro"), ler("Matricula")));
            case "11" -> System.out.println("  " + emprestimos.cancelarReserva(ler("Id do livro"), ler("Matricula")));
            case "12" -> System.out.println("  " + devolucoes.inspecionarLivro(ler("Id do livro"),
                    ler("Bom estado? (s/n)").equalsIgnoreCase("s")));
            case "13" -> System.out.println("  " + devolucoes.concluirReparo(ler("Id do livro")));
            case "14" -> System.out.println("  " + devolucoes.registrarLaudoIrrecuperavel(ler("Id do livro")));
            case "15" -> System.out.println("  Aguardando inspecao: " + devolucoes.livrosAguardandoInspecao());
            case "16" -> System.out.println("  " + devolucoes.emitirBoletoMulta(ler("Id do livro"), ler("Matricula")));
            case "17" -> devolucoes.listarMultas().forEach(m -> System.out.println("  " + m));
            case "18" -> System.out.println("  " + banco.pagar(ler("Id da cobranca (ex.: A-100001)"), relogio.hoje()));
            case "19" -> System.out.println("  " + acervo.consultarMetadados(ler("Id do livro"))
                    + "  (medicoes registradas: " + app.medicoes().quantidade() + ")");
            case "20" -> fluxoCompleto();
            case "21" -> System.out.print(organizacao.desenhar());
            case "22" -> System.out.println("  " + devolucoes.indicadores());
            case "23" -> {
                relogio.avancarDias(Long.parseLong(ler("Quantos dias")));
                System.out.println("  Hoje agora e " + relogio.hoje());
            }
            case "24" -> repetirUltimaSolicitacao();
            default -> System.out.println("  Opcao invalida");
        }
    }

    private <R> void submeter(SolicitacaoCirculacao<R> s) {
        ultimaSolicitacao = s;
        Resultado<R> r = emprestimos.submeter(s);
        System.out.println("  Solicitacao " + s.getId() + (r.repetida() ? " (REPETIDA - nao executada de novo)" : ""));
        System.out.println("  " + r.comprovante());
    }

    // manda a MESMA solicitação de novo (mesmo id) pra mostrar que o executor não repete
    private void repetirUltimaSolicitacao() {
        if (ultimaSolicitacao == null) {
            System.out.println("  Nenhuma solicitacao enviada ainda.");
            return;
        }
        submeter(ultimaSolicitacao);
    }

    private void imprimirDevolucao(ComprovanteDevolucao c) {
        System.out.println("  " + c);
        if (c.diasAtraso() > 0) {
            System.out.println("  Devolucao com " + c.diasAtraso() + " dia(s) de atraso: multa emitida (opcao 17).");
        }
        if (!c.falhasNasReacoes().isEmpty()) {
            System.out.println("  Atencao, reacoes que falharam (a devolucao foi mantida): " + c.falhasNasReacoes());
        }
    }

    // ---- demonstração ----

    private void carregarDemonstracao() {
        System.out.println("  " + pessoas.inserirPessoa("Ana", "Souza", "111.222.333-44", "ana@pucpr.edu.br").mensagem());
        System.out.println("  " + pessoas.inserirPessoa("Bruno", "Lima", "555.666.777-88", "bruno@pucpr.br").mensagem());
        System.out.println("  " + estudantes.inserirEstudante("E1", "11122233344").mensagem());
        System.out.println("  " + atendentes.inserirAtendente("AT1", "55566677788").mensagem());
        String[][] livros = {
                {"L1", "Padroes de Projeto", "Gamma et al."},
                {"L2", "Engenharia de Software", "Sommerville"},
                {"L3", "UML Essencial", "Fowler"},
                {"L4", "Codigo Limpo", "Martin"},
                {"L5", "Refatoracao", "Fowler"}};
        for (String[] l : livros) {
            System.out.println("  " + acervo.inserirLivro(l[0], l[1], l[2]).mensagem());
        }
        try {
            organizacao.criarGrupo("Central", "Computacao", GrupoAcervo.Tipo.SECAO);
            organizacao.criarGrupo("Central", "Engenharia", GrupoAcervo.Tipo.SECAO);
            for (String id : new String[]{"L1", "L2", "L3"}) {
                organizacao.incluirLivro("Computacao", id);
            }
            organizacao.incluirLivro("Engenharia", "L4");
            organizacao.incluirLivro("Engenharia", "L5");
        } catch (RegraDeNegocioException jaCarregada) {
            // já tinha carregado antes
        }
        System.out.print(organizacao.desenhar());
    }

    // reserva -> empréstimo -> renovação -> devolução atrasada -> multa -> inspeção -> pagamento
    private void fluxoCompleto() {
        String livro = "L1";
        String est = "E1";
        try {
            acervo.consultarLivro(livro);
        } catch (RegraDeNegocioException naoCarregado) {
            carregarDemonstracao();
        }
        passo("1. Reserva (Command -> Facade -> State)");
        submeter(SolicitacaoReserva.nova("AT1", livro, est));
        passo("2. Emprestimo (Facade: valida reserva, remove, cria emprestimo, atualiza Livro)");
        System.out.println("  " + emprestimos.realizarEmprestimo(livro, est));
        passo("3. Renovacao (+30 dias; guarda numRenovacoes < 2)");
        submeter(SolicitacaoRenovacao.nova(est, livro, est));
        passo("4. [SIMULACAO] passam 49 dias (14 de prazo + 30 da renovacao + 5 de atraso)");
        relogio.avancarDias(49);
        System.out.println("  Hoje: " + relogio.hoje());
        passo("5. Devolucao atrasada (Observer: inspecao, multa, indicadores, e-mail)");
        imprimirDevolucao(devolucoes.registrarDevolucao(livro, est));
        System.out.println("  Fila de inspecao: " + devolucoes.livrosAguardandoInspecao());
        System.out.println("  " + devolucoes.indicadores());
        passo("6. Multa (Strategy) e boleto (Adapter)");
        System.out.println("  " + devolucoes.emitirBoletoMulta(livro, est));
        passo("7. Inspecao: livro em bom estado -> LIVRE");
        System.out.println("  " + devolucoes.inspecionarLivro(livro, true));
        passo("8. [SIMULACAO] estudante paga o boleto; o banco notifica (inclusive em duplicidade)");
        String ultima = devolucoes.listarMultas().get(devolucoes.listarMultas().size() - 1);
        String idCobranca = ultima.substring(ultima.indexOf("cobranca=") + "cobranca=".length()).trim();
        System.out.println("  " + banco.pagar(idCobranca, relogio.hoje()));
        System.out.println("  " + banco.reenviarUltimaNotificacao() + " -> ignorada, nao ha segunda quitacao");
        devolucoes.listarMultas().forEach(m -> System.out.println("  " + m));
    }

    private static void passo(String titulo) {
        System.out.println();
        System.out.println(">> " + titulo);
    }

    private String ler(String rotulo) {
        System.out.print("  " + rotulo + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : "0";
    }
}

# Guia de arguição

Respostas às perguntas listadas em "Na arguição, saiba explicar" de cada requisito, com o lugar
exato do código. Lembrete: **todos** os integrantes respondem sobre **qualquer** parte. Estudem
este guia junto com o código, não no lugar dele.

---

## R1 · State

**Onde está a guarda da renovação e por que é avaliada antes de alterar o vencimento?**
Em `Emprestado.renovar` e `Renovado.renovar`: `if (!emprestimo.podeRenovar()) throw ...` e só depois
`emprestimo.estenderVencimento(30)`. `podeRenovar()` é `numRenovacoes < 2` (`Emprestimo`). Se a ação
viesse antes, a terceira renovação mudaria o vencimento e só então seria recusada, o que deixaria o
empréstimo inconsistente. Guarda falsa = transição não disparada = nenhum efeito.

**O que acontece quando um evento chega a um estado que não o aceita?**
A classe base `EstadoLivro` implementa todos os eventos lançando `TransicaoRecusadaException`. Cada
estado concreto só sobrescreve os eventos que aceita. O livro não muda, e a fachada roda dentro de
uma unidade de trabalho, então qualquer gravação anterior na mesma operação também é desfeita.

**Como o estado é gravado e reconstituído na persistência?**
Gravado como texto (`Livro.getEstado()` → `estado.nome()`) na coluna `estado` (MySQL) ou no registro
`LivroReg` (memória). Na leitura, `Livro.reconstituir(..., nomeEstado)` chama `EstadoLivro.porNome`,
o único `switch` que converte texto em objeto de estado. Os estados são singletons sem dados.

**E se trocássemos por um `if (estado.equals("LIVRE"))` no tratador?**
Cada novo estado ou transição exigiria mudar todos os lugares com esses `if`. Com State, a regra fica
num único método da classe do estado de origem, alinhado à máquina de estados.

---

## R2 · Strategy

**Como uma nova política é incluída sem alterar o serviço de multas?**
Cria-se uma classe que implementa `PoliticaMulta` (`nome()` e `calcular(dias)`) e registra-se em
`config/Aplicacao` (`.registrar(new MinhaPolitica())`). O `ServicoDeMultas` só chama
`configuracao.politicaVigente().calcular(dias)`.
*Extensão ao vivo:* copiar `PoliticaComTeto`, mudar nome e cálculo, registrar, rodar com
`-Dpolitica.multa=NOVO_NOME`.

**Por que uma troca de política não recalcula multas já emitidas?**
A `Multa` grava o **valor** e o **nome da política** no momento da emissão (`ServicoDeMultas.emitirMulta`).
Ninguém recalcula multa existente: `emitirMulta` é idempotente por devolução e, se a multa já existe,
devolve a gravada. Teste: `R02_trocaDePoliticaNaoRecalculaMultaJaEmitida`.

**Como são calculados os dias de atraso e os valores?**
`EventoDevolucao.diasAtraso()` (e `Devolucao.diasAtraso()`): `ChronoUnit.DAYS.between(vencimento,
devolucao)`, com mínimo 0. Valores em `BigDecimal`, escala 2, `RoundingMode.HALF_EVEN`, nunca `double`
(0,1 + 0,2 ≠ 0,3 em ponto flutuante).

---

## R3 · Adapter

**Onde ficam as conversões de cada banco?**
Somente em `AdaptadorBancoA` (centavos `long`, CPF só dígitos, data `AAAAMMDD`, código numérico de
situação) e `AdaptadorBancoB` (decimal com 2 casas, CPF `000.000.000-00`, data ISO, situação textual).
A tradução das notificações está nos métodos `traduzir(...)` de cada adaptador.

**Como o banco da instalação é escolhido?**
Propriedade `banco` em `biblioteca.properties`, lida uma vez em `Aplicacao.escolherCobranca`. É o
único `switch` por banco do sistema. O `ServicoDeMultas` recebe um `ICobranca` e não sabe qual é.

**Como a quitação é protegida contra notificações repetidas?**
`ServicoDeMultas.registrarPagamento` busca a multa pela cobrança e chama `Multa.quitar()`, que retorna
`false` se a multa já estava `QUITADA`. Só grava quando a quitação é nova. No MySQL, para
notificações simultâneas, a melhoria seria um `UPDATE multa SET situacao='QUITADA' WHERE id=? AND
situacao='EMITIDA'` (o número de linhas afetadas diz quem quitou).

---

## R4 · Facade

**Quais classes a fachada coordena e em que ordem?**
`FachadaCirculacao.efetivarEmprestimo` segue o diagrama de sequência "Emprestar Livro":
`Movimentacao.obterReserva` (valida) → `Acervo.obterLivro` → `Livro.emprestar` (estado cria o
empréstimo) → `Acervo.atualizar` → `Movimentacao.removerReserva` → `Movimentacao.inserirEmprestimo`.
`reservar` usa também `Relacionamento` (estudante cadastrado).

**O que é desfeito se um passo falhar no meio?**
Tudo. Cada método da fachada roda em `ExecutorTransacional.executar`, que cria **uma** unidade de
trabalho para os três pontos de acesso: se qualquer passo lança exceção, `desfazer()` é chamado
(rollback no MySQL; restauração da fotografia na memória). Teste:
`R04_falhaNoMeioDaOperacaoDesfazTodosOsPassos`.

**Por que a interface não acessa Movimentacao diretamente?**
Para não espalhar regras e ordem de passos pela interface, não expor coleções do domínio e manter
transação e consistência num único lugar. A interface só conhece os tratadores, e os tratadores só
conhecem a fachada. Os comprovantes são `record`s com dados simples.

---

## R5 · Observer

**Como um novo ouvinte é incluído sem alterar o tratador?**
Cria-se uma classe que implementa `OuvinteDevolucao` e registra-se em `Aplicacao`
(`publicador.registrar(...)`). Nem `TratadorDeDevolucao` nem `FachadaCirculacao` mudam.
Teste: `R05_novoOuvinteEhIncluidoSemAlterarOTratador`.

**O que acontece quando o envio de e-mail falha?**
`PublicadorDevolucao.publicar` chama cada ouvinte num `try/catch` separado. A falha vira uma linha em
`falhasNasReacoes` do comprovante; os outros ouvintes rodam normalmente. A devolução já tinha sido
**confirmada** (commit) antes da publicação, então nada é desfeito. Teste:
`R05_falhaDeUmOuvinteNaoAfetaOsDemaisNemDesfazADevolucao`.

**Por que o evento leva os dados em vez de o ouvinte consultá-los depois?**
O evento descreve o fato no instante em que aconteceu (vencimento, data de devolução, estudante).
Se o ouvinte consultasse depois, poderia ler dados já alterados (ex.: uma nova renovação, outro
empréstimo do mesmo livro), além de acoplar cada ouvinte aos repositórios e gerar consultas extras.

---

## R6 · Command

**Quem cria, quem invoca e quem executa?**
Cria: a tela (`ui/Main`, opções 7 e 9: `SolicitacaoReserva.nova(...)`). Encaminha: `TratadorDeEmprestimo.submeter`.
Invoca: `ExecutorDeSolicitacoes.executar`. Executa as regras: o receptor `FachadaCirculacao` e o `Livro`
(estado). As regras continuam no domínio; o comando só sabe qual operação pedir.

**Como a repetição é detectada mesmo com duas submissões simultâneas?**
`ExecutorDeSolicitacoes.executar` é `synchronized`: só uma thread entra por vez. A primeira executa e
guarda `id → comprovante` no mapa `executadas`; a segunda espera, encontra o id no mapa e só recebe o
mesmo comprovante (`repetida = true`), sem executar de novo. Se a execução der erro, o id não é
guardado, então dá pra tentar de novo.
Teste com 8 threads: `R06_submissoesSimultaneasComOMesmoIdentificadorExecutamUmaUnicaVez`.
Limitação conhecida: o registro está em memória. Com vários servidores, seria uma tabela com chave
única no id da solicitação.

**Por que não há operação de desfazer?**
Reserva e renovação são fatos de negócio com efeitos para terceiros (fila, prazo, multa). Reverter
é outra operação do domínio, com regras próprias (ex.: `cancelarReserva`), e não um "undo" técnico.
Um undo de renovação reabriria a possibilidade de burlar o limite de duas renovações.

---

## R7 · Template Method

**Quais passos são fixos e quais variam?**
Fixo: a ordem em `CadastroTemplate.incluir` (método `final`): buscar existente → validar → verificar
pré-requisitos → criar → armazenar → apresentar. Variam (abstratos): `buscarExistente`, `validar`,
`criar`, `armazenar`, implementados por `CadastroLivro`, `CadastroPessoa`, `CadastroEstudante` e
`CadastroAtendente`.

**O que é um gancho e onde é usado?**
Método com implementação padrão (vazia ou trivial) que a subclasse **pode** sobrescrever:
`verificarPreRequisitos` (sobrescrito por Estudante e Atendente: a pessoa precisa existir no RH) e
`apresentar` (padrão: monta a mensagem do resultado).

**Por que a falha do pré-requisito não permite gravação parcial?**
Porque `armazenar` é o único passo que grava e vem depois de `verificarPreRequisitos`, numa ordem
que a subclasse não pode mudar (`final`). Além disso, o tratador executa `incluir` dentro de uma
unidade de trabalho. Teste: o contador de gravações não muda (`R07_novoEstudanteComCpf...`).

---

## R8 · Abstract Factory

**Onde a família é escolhida?**
`PerfilExecucao.escolherFabrica(config)`, chamado uma única vez em `Aplicacao` (propriedade `perfil`:
`teste` → memória, `producao` → MySQL).

**Como os repositórios compartilham a mesma transação?**
`ExecutorTransacional.executar` cria uma `UnidadeDeTrabalho` e passa **a mesma** instância para as
três fábricas de repositório. Na família JDBC, a unidade guarda uma única `Connection` com
`autoCommit=false`, usada pelos três repositórios. Na memória, a unidade trava a base e tira uma
fotografia para o rollback.

**O que impede misturar repositório em memória com relacional?**
Cada fábrica concreta verifica o tipo da unidade recebida (`daFamilia(...)`) e lança
`FamiliaIncompativelException` se for de outra família. Além disso, a escolha é feita num ponto só,
e o resto do sistema só conhece as interfaces. Teste: `R08_naoEhPossivelMisturar...`.

**E o Oracle?** É réplica: recebe dados pela replicação do MySQL. `FabricaPersistenciaJdbc` recusa
URL `jdbc:oracle`, então a aplicação nunca escreve nele.

---

## R9 · Decorator

**Como a cadeia é montada e por que a ordem importa?**
`MontadorDeConsulta.montar`: `Medicao → Cache → Original` (de fora para dentro), conforme
`consulta.cache` e `consulta.medicao`. Com a medição por fora, toda consulta é medida, inclusive os
acertos de cache. Na ordem inversa (`Cache → Medicao → Original`), só as consultas que passam pelo
cache sem acerto seriam medidas.

**O que acontece se o cache falhar?**
`ConsultaComCache` captura a exceção ao ler ou gravar, conta a falha e segue para o componente
interno: a consulta continua funcionando, só mais lenta. Teste: `R09_falhaDoCacheNaoImpedeAConsulta`.

**Por que o cache não participa da decisão de reserva?**
O cache só guarda `MetadadosLivro` (id, título, autor), um `record` **sem** estado. A reserva
(`FachadaCirculacao.reservar`) lê o `Livro` atual pelo `Acervo` dentro da transação e quem decide é o
objeto de estado. Um dado antigo do cache não tem como autorizar nada. Teste:
`R09_resultadoAntigoDoCacheNunca...`.

---

## R10 · Composite

**Como a contagem percorre a árvore?**
`GrupoAcervo.contarLivros()` soma `filho.contarLivros()` de cada filho (recursão). `ItemLivro`
devolve 1. O cliente chama a mesma operação sem saber se é grupo ou item (`NoAcervo`).

**Quais regras impedem ciclos e livros em dois grupos?**
Em `GrupoAcervo.adicionar`: (1) o filho não pode ser o próprio grupo nem um ancestral (`ehAncestral`
sobe pelos pais); (2) o filho não pode já ter pai; (3) nenhum livro do filho pode já estar na
árvore (`raiz().listarLivros()`).

**Por que mover um item não toca no Livro nem na Movimentacao?**
`ItemLivro` guarda só o **id** do livro, não o objeto. `moverLivro` troca o pai do item na árvore
(e desfaz a remoção se a inclusão no destino falhar). Id, estado e empréstimo ficam onde estão.
Teste: `R10_moverLivroEntreSecoes...`.

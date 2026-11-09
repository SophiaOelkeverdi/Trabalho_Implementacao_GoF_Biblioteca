# Trabalho 3 - Biblioteca com padrões GoF

PUCPR - Modelagem de Sistemas Computacionais - Prof. Edson Emílio Scalabrin - 2º semestre/2026

**Equipe:** _(colocar os nomes aqui)_

Continuação do Trabalho 02: implementamos o sistema de cadastro, reserva, empréstimo, renovação e
devolução de livros usando os 10 padrões pedidos (State, Strategy, Adapter, Facade, Observer,
Command, Template Method, Abstract Factory, Decorator e Composite).

> O que ainda falta fazer está em **[O_QUE_FALTA.md](O_QUE_FALTA.md)**.

## Como rodar

Precisa só do **Java 17** (ou mais novo). O Maven não precisa estar instalado: o projeto tem o
Maven Wrapper (`mvnw.cmd` no Windows, `./mvnw` no Linux/Mac), que baixa o Maven sozinho na primeira vez.

| O que | Comando (Windows) |
|---|---|
| Compilar e rodar os testes | `mvnw.cmd test` |
| Rodar só os testes de um requisito | `mvnw.cmd test -Dtest=R06*` |
| Abrir o sistema (em memória) | `mvnw.cmd -q compile exec:java` |
| Abrir o sistema com MySQL | `mvnw.cmd -q compile exec:java -Dperfil=producao` |

No Linux/Mac é igual, trocando `mvnw.cmd` por `./mvnw`.

Dá pra mudar a configuração na linha de comando, por exemplo:
`mvnw.cmd -q compile exec:java -Dbanco=B -Dpolitica.multa=TOLERANCIA_2_DIAS`.
Todas as opções estão em `resources/biblioteca.properties`.

### Roteiro da demonstração

1. `mvnw.cmd test`: os 44 testes passam (os cenários de verificação são os `R01_` até `R10_`).
2. `mvnw.cmd -q compile exec:java` e escolher a opção **20** (fluxo completo): reserva → empréstimo →
   renovação → passam 49 dias → devolução atrasada → multa de R$ 5,00 → boleto no banco A →
   inspeção → pagamento → o banco manda a notificação duas vezes e a segunda é ignorada.
3. Opção **24**: manda de novo a última solicitação (mesmo id) e o sistema não executa outra vez.
4. Fluxo manual: opção 1 (carrega dados) e depois 7, 8, 9, 23 (avançar dias), 10, 17, 12, 18.

### MySQL (perfil produção)

```sql
CREATE DATABASE biblioteca;
CREATE USER 'biblioteca'@'localhost' IDENTIFIED BY 'biblioteca';
GRANT ALL ON biblioteca.* TO 'biblioteca'@'localhost';
```

As tabelas são criadas sozinhas a partir de `resources/schema.sql`. Usuário, senha e URL estão em
`resources/biblioteca.properties`. O Oracle é só réplica do MySQL, então o sistema nunca grava nele
(a fábrica recusa URL do Oracle). Os testes não precisam de MySQL: a parte JDBC é testada com o H2
em modo MySQL.

## Estrutura

```
src/          código
test/         testes JUnit (R01 a R10)
resources/    biblioteca.properties e schema.sql
docs/         rastreabilidade e guia de arguição
```

| Pasta (dentro de `src/`) | O que tem | Padrão |
|---|---|---|
| `dominio/livro` | `Livro` e as 7 classes de estado | State |
| `dominio/movimentacao`, `dominio/pessoa` | Reserva, Empréstimo, Devolução, Multa, Pessoa, Estudante, Atendente | - |
| `acesso` | Acervo, Movimentacao e Relacionamento (pontos de acesso do T02) | - |
| `persistencia` (+ `memoria`, `jdbc`) | fábricas, unidade de trabalho e repositórios | Abstract Factory |
| `circulacao` | fachada de circulação e comprovantes | Facade |
| `circulacao/comando` | solicitações de reserva e renovação | Command |
| `devolucao` (+ `ouvintes`) | publicador e ouvintes da devolução | Observer |
| `financeiro/politica` | políticas de multa | Strategy |
| `financeiro/cobranca` | contrato de cobrança e adaptadores dos bancos | Adapter |
| `externo` | simulação dos bancos A e B (sistemas externos) | - |
| `cadastro` | rotina comum de cadastro | Template Method |
| `consulta` | consulta ao acervo com cache e medição | Decorator |
| `organizacao` | unidades, seções e livros em árvore | Composite |
| `tratadores` | ILivro, IPessoa, IEstudante, IAtendente, IEmprestimo, IDevolucao e os tratadores | - |
| `config` | `Aplicacao` (monta tudo) e `Relogio` | - |
| `ui` | menu de linha de comando (só chama os tratadores) | - |

Como as camadas conversam:

```
ui.Main -> tratadores -> ExecutorDeSolicitacoes (Command) -> FachadaCirculacao (Facade)
                                                             -> Acervo / Movimentacao / Relacionamento
                                                             -> Livro -> EstadoLivro (State)
                                                             -> PublicadorDevolucao -> ouvintes (Observer)
ServicoDeMultas -> PoliticaMulta (Strategy) e ICobranca -> AdaptadorBancoA/B (Adapter)
```

Mais detalhes:
- [docs/RASTREABILIDADE.md](docs/RASTREABILIDADE.md): requisito → padrão → classes de cada papel → teste.
- [docs/GUIA_ARGUICAO.md](docs/GUIA_ARGUICAO.md): respostas das perguntas de arguição de cada requisito.

## Premissas que adotamos

- Prazo do empréstimo de 14 dias (o enunciado não define). Renovação de +30 dias, no máximo 2
  (`numRenovacoes < 2`). Reserva vale 3 dias. Tudo isso fica em `dominio/RegrasCirculacao.java`.
- Dias de atraso = dias corridos entre o vencimento e a devolução (nunca negativo).
- Dinheiro sempre em `BigDecimal` com 2 casas.
- Livro devolvido fica `DEVOLVIDO` até a inspeção, que manda pra `LIVRE` ou `REPARANDO`. Em
  `REPARANDO`, o reparo concluído volta pra `LIVRE` e o laudo irrecuperável leva a `INCINERADO`.
- A multa é gerada por um ouvinte da devolução (Observer), e não dentro da transição de estado.
- Atraso dentro da tolerância (política com tolerância) não gera multa.

## Uso de LLM

| Ferramenta / modelo | Onde usamos |
|---|---|
| Claude Code (Claude Opus 5.5, Anthropic) | estrutura do projeto, código dos 10 padrões, testes R01-R10, menu de linha de comando e rascunho da documentação |
| _(outras ferramentas que a equipe usar)_ | |

Os pedidos feitos e o que a equipe aceitou, corrigiu ou descartou estão em [REGISTRO_LLM.md](REGISTRO_LLM.md).

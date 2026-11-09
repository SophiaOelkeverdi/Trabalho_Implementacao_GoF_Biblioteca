# Registro de uso de LLM

Ferramenta: **Claude Code** (extensão do VS Code), modelo **Claude Opus 5.5** (Anthropic).

> A equipe deve completar as colunas "Decisão da equipe" e "O que foi corrigido" à medida que
> revisar e alterar o código. Código que nenhum integrante consegue explicar conta como não implementado.

| # | Data | Pedido feito ao modelo (resumo) | O que o modelo produziu | Decisão da equipe (aceito / corrigido / descartado) | O que foi corrigido e por quê |
|---|---|---|---|---|---|
| 1 | 09/11/2026 | Iniciar o trabalho a partir do enunciado e dos diagramas do Trabalho 02, com orientações para continuar | Projeto Maven (Java 17) com wrapper; domínio (Livro, transações, pessoas); os 10 padrões; persistência em memória e JDBC/MySQL; CLI; 44 testes JUnit (`R01`–`R10`); README, rastreabilidade, guia de arguição e lista de ajustes de UML | _a preencher_ | _a preencher_ |
| 2 | 09/11/2026 | Tirar as pastas que só tinham outra pasta dentro, deixar o código mais simples e listar o que falta | Estrutura `src/` + `test/` + `resources/` sem o pacote `br.pucpr.biblioteca`; bancos simulados numa pasta só; comentários mais curtos; executor do Command simplificado (`synchronized` + `HashMap`); teste do R04 sem reflexão; `O_QUE_FALTA.md` | _a preencher_ | _a preencher_ |
| 3 | | | | | |

## Decisões de projeto tomadas na geração inicial (para a equipe validar)

* Guarda de renovação `numRenovacoes < 2` (o diagrama do T02 tinha `== 0` e `<= 2`).
* Prazo inicial de 14 dias (não definido no enunciado); renovação de +30 dias.
* Multa emitida por um ouvinte (Observer), não como ação da transição de devolução.
* Identificadores de livro e estudante como `String` (o T02 misturava `int`, `long` e `String`).
* Bancos A e B simulados em `externo/` (API em centavos × API em decimal).
* Família relacional testada com H2 em modo MySQL, para que os testes não dependam de servidor.
* Registro de solicitações executadas (Command) mantido em memória; limitação documentada.
* Estrutura de pastas fora do padrão do Maven (`src/`, `test/`, `resources/`), configurada no `pom.xml`.

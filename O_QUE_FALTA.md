# O que falta fazer

**Entrega:** repositório até **27/11/2026, 09h40**. Apresentação no mesmo dia (5 min por equipe).

**Já está pronto:** código dos 10 padrões, 44 testes passando (`mvnw.cmd test`), menu de linha de
comando com o fluxo completo (opção 20), README, rastreabilidade e guia de arguição.

Marquem `[x]` conforme forem terminando.

---

## 1. Diagramas UML no Astah (vale 15% da nota) - PRIORIDADE ALTA

Tem que entregar o `.asta` atualizado **e** exportar em PDF.

### 1.1 Máquina de estados do Livro

O diagrama atual não bate com o código nem com o enunciado. Corrigir:

- [ ] Renomear o estado **"Renovar"** para **"Renovado"**.
- [ ] Guarda da renovação: trocar `[numRenovacoes == 0]` e `[numRenovacoes <= 2]` por
      **`[numRenovacoes < 2]`** nas duas transições (do jeito que está, daria pra renovar 3 vezes).
- [ ] Reparando → Incinerado: trocar `avaliarReparo [consertado == false]` pelo evento **`laudoIrrecuperavel`**.
- [ ] Reparando → Livre: pode continuar `avaliarReparo [consertado == true]` ou renomear para
      `concluirReparo` (é o nome do método no código).
- [ ] Devolução: deixar só **`registrarDevolucao [emprestimo registrado]`** saindo de Emprestado e de
      Renovado para Devolvido. Tirar as transições `[atraso] / emitirBoletoMulta`, porque a multa é
      gerada pelo ouvinte (Observer) e não pela transição.
- [ ] Remover `registrarDano / descartarLivro` (Livre → Incinerado): não foi implementado. O descarte
      passa por Reparando + laudo.
- [ ] Trocar as ações de `entry` por ações nas transições (o enunciado pede guardas e ações no
      método da transição):
  - `reservarLivro [estudante cadastrado] / registrarVencimentoReserva`
  - `emprestarLivro [mesmo estudante da reserva] / calcularDataDevolucao`
  - `registrarDevolucao [emprestimo registrado] / registrarDataDevolucao`
- [ ] Conferir que continuam: `inserirLivro` (início → Livre), `removerReserva` (Reservado → Livre),
      `inspecionarLivro [bom estado]` (Devolvido → Livre), `inspecionarLivro [danificado]`
      (Devolvido → Reparando), `registrarDano` (Livre → Reparando), `excluirLivro` (Livre → final).

### 1.2 Um diagrama de classes por padrão (10 diagramas)

Usar as classes da [tabela de rastreabilidade](docs/RASTREABILIDADE.md) e colocar o papel de cada
classe em estereótipo ou nota (ex.: `«Context» Livro`, `«State» EstadoLivro`).

- [ ] State: `Livro`, `EstadoLivro`, `Livre`, `Reservado`, `Emprestado`, `Renovado`, `Devolvido`, `Reparando`, `Incinerado`
- [ ] Strategy: `PoliticaMulta`, `PoliticaOriginal`, `PoliticaComTeto`, `PoliticaComTolerancia`, `ServicoDeMultas`, `ConfiguracaoAdministrativa`
- [ ] Adapter: `ICobranca`, `AdaptadorBancoA`, `AdaptadorBancoB`, `BancoAApi`, `BancoBApi`, `ServicoDeMultas`
- [ ] Facade: `FachadaCirculacao`, `Acervo`, `Movimentacao`, `Relacionamento`, `Livro`, `TratadorDeEmprestimo`, `TratadorDeDevolucao`
- [ ] Observer: `PublicadorDevolucao`, `OuvinteDevolucao`, `OuvinteInspecao`, `OuvinteMulta`, `OuvinteIndicadores`, `OuvinteEmail`, `EventoDevolucao`
- [ ] Command: `SolicitacaoCirculacao`, `SolicitacaoReserva`, `SolicitacaoRenovacao`, `ExecutorDeSolicitacoes`, `FachadaCirculacao`, `Main`
- [ ] Template Method: `CadastroTemplate`, `CadastroLivro`, `CadastroPessoa`, `CadastroEstudante`, `CadastroAtendente`
- [ ] Abstract Factory: `FabricaPersistencia`, `FabricaPersistenciaMemoria`, `FabricaPersistenciaJdbc`, `UnidadeDeTrabalho`, os 3 repositórios e as versões `*Memoria` / `*Jdbc`
- [ ] Decorator: `ConsultaAcervo`, `ConsultaAcervoOriginal`, `DecoradorConsulta`, `ConsultaComMedicao`, `ConsultaComCache`
- [ ] Composite: `NoAcervo`, `GrupoAcervo`, `ItemLivro`

### 1.3 Diagrama de classes do domínio

- [ ] `Livro.idLivro` como **String** (no diagrama aparece `int` em um lugar e `String` em outro).
- [ ] `Livro` com associação para `EstadoLivro` no lugar do atributo `estado : String`.
- [ ] `Emprestimo`: acrescentar `ativo`, `podeRenovar()`, `estenderVencimento(dias)`, `encerrar()`.
- [ ] Acrescentar a classe `Multa` (valor, política aplicada, situação, idCobranca).
- [ ] `IEmprestimo` retorna comprovantes em vez de `void`; `ILivro.inserirLivro` recebe `autor`.
- [ ] Acrescentar `FachadaCirculacao`, `ExecutorDeSolicitacoes`, `ServicoDeMultas` e `ICobranca`.

### 1.4 Diagramas de sequência

Atualizar os que já existem:
- [ ] **Emprestar Livro (f3):** colocar a `FachadaCirculacao` entre o `TratadorDeEmprestimo` e
      Movimentacao/Acervo, e a chamada `Livro.emprestar()` → `Reservado.emprestar()`. A ordem já
      está certa: consultarReserva → buscarLivro → criarEmprestimo → atualizarEstado →
      removerReserva → inserirEmprestimo.
- [ ] **Renovar (f4):** o atual repete `atualizarVencimento` e `incrementarRenovacoes` duas vezes
      (passos 4 a 7). Trocar por `Livro.renovar(emp)` → `EstadoLivro.renovar` → `[podeRenovar]` →
      `emp.estenderVencimento(30)`.
- [ ] **Registrar Devolução (f5):** acrescentar o `PublicadorDevolucao` **depois do commit** e os 4 ouvintes.
- [ ] **Emitir Boleto (f6):** `TratadorDeDevolucao` → `ServicoDeMultas` → `PoliticaMulta.calcular` →
      `ICobranca.emitir` → `AdaptadorBancoA` → `BancoAApi`.

Criar um para cada cenário de verificação (dá pra seguir o código do teste correspondente):
- [ ] R01 terceira renovação recusada (`test/R01_StateTest.java`)
- [ ] R02 multa de 5 dias nas políticas original e com tolerância
- [ ] R03 a mesma cobrança de R$ 7,00 nos bancos A e B
- [ ] R04 efetivar empréstimo pela fachada
- [ ] R05 devolução atrasada avisando os ouvintes
- [ ] R06 a mesma solicitação de renovação enviada duas vezes
- [ ] R07 estudante com CPF não cadastrado
- [ ] R08 perfil de produção criando os repositórios MySQL
- [ ] R09 consulta com cache + tentativa de reserva
- [ ] R10 contar livros e mover um livro entre seções

---

## 2. Testar com MySQL de verdade

Ainda não foi testado com um servidor MySQL, só com o H2 imitando o MySQL.

- [ ] Instalar o MySQL 8 (ou usar o do laboratório).
- [ ] Rodar o script de criação do banco que está no README.
- [ ] `mvnw.cmd -q compile exec:java -Dperfil=producao` e fazer o fluxo da opção 20.
- [ ] Fechar e abrir o sistema de novo e conferir que os livros continuam lá (opção 6). Isso mostra
      o estado sendo gravado e reconstituído (pergunta da arguição do R01).
- [ ] Se usuário/senha forem diferentes, mudar em `resources/biblioteca.properties` ou passar
      `-Djdbc.usuario=... -Djdbc.senha=...`.

---

## 3. Documentação

- [ ] Colocar os nomes da equipe no README.
- [ ] **REGISTRO_LLM.md:** preencher as colunas "Decisão da equipe" e "O que foi corrigido", e
      acrescentar uma linha para cada pedido novo que fizerem a qualquer LLM.
- [ ] Se usarem outra ferramenta além do Claude, acrescentar na tabela "Uso de LLM" do README.
- [ ] Revisar o [guia de arguição](docs/GUIA_ARGUICAO.md) e reescrever com as palavras de vocês o
      que acharem necessário.

---

## 4. Estudar para a arguição (a nota individual depende disso)

O professor escolhe **quem** responde e **qual** trecho. Nota da pessoa = nota da equipe × fator
(1,0 explica com segurança; 0,7 com lacunas; 0,4 só a própria parte; 0 não explica).

- [ ] Cada integrante lê o código dos **10** padrões, não só o que fez.
- [ ] Cada um responde em voz alta as perguntas do guia, sem olhar a resposta.
- [ ] Um pergunta pro outro: abrir um arquivo aleatório, apontar uma linha e perguntar "o que faz?
      qual o papel no padrão? e se eu mudar isso?".
- [ ] Saber rodar os testes de um requisito só (`mvnw.cmd test -Dtest=R03*`).

### Treinar a alteração ao vivo

**Nova política de multa** (ex.: R$ 2,00 por dia):
1. Criar `src/financeiro/politica/PoliticaDobrada.java` copiando `PoliticaOriginal`, com outro
   `NOME` e outro cálculo.
2. Em `src/config/Aplicacao.java`, acrescentar `.registrar(new PoliticaDobrada())`.
3. Rodar com `-Dpolitica.multa=DOBRADA`.
4. Explicar: o `ServicoDeMultas` não mudou, porque só conhece a interface `PoliticaMulta`.

**Novo ouvinte da devolução** (ex.: log no console):
1. Criar `src/devolucao/ouvintes/OuvinteLog.java` implementando `OuvinteDevolucao` (copiar o
   `OuvinteIndicadores` e trocar o corpo por um `System.out.println`).
2. Em `Aplicacao`, acrescentar `publicador.registrar(new OuvinteLog());`.
3. Explicar: a `FachadaCirculacao` e o `TratadorDeDevolucao` não mudaram.

Outras que o professor pode pedir: um novo decorador da consulta (ex.: um que conte as chamadas),
um novo banco C (novo adaptador implementando `ICobranca` + um `case` em `escolherCobranca`) ou uma
nova regra de estado.

---

## 5. Ensaiar a apresentação (5 minutos)

- [ ] Testar o roteiro do README (testes → opção 20 → opção 24) num computador limpo, de
      preferência o que vai ser usado na apresentação.
- [ ] Rodar `mvnw.cmd test` uma vez antes (com internet) para o Maven e as dependências ficarem
      baixados. Depois funciona offline.
- [ ] Cronometrar.

---

## 6. Repositório e entrega

- [ ] Iniciar o git e fazer o primeiro commit (`git init`, `git add .` e `git commit -m "..."`).
- [ ] Criar o repositório no GitHub/GitLab, dar `git push` e adicionar o professor, se ele pedir.
- [ ] Colocar o `.asta` e os PDFs dos diagramas no repositório (ex.: pasta `uml/`).
- [ ] Conferir que `target/` não foi enviado (já está no `.gitignore`).

---

## 7. Decisões que a equipe precisa confirmar

Foram assumidas na hora de implementar. Se discordarem, é só mudar:

- [ ] Prazo do empréstimo de **14 dias** (`dominio/RegrasCirculacao.java`).
- [ ] A multa é gerada pelo **ouvinte** da devolução, e não como ação da transição de estado.
- [ ] Identificadores de livro e estudante como **String**.
- [ ] Interface por **linha de comando** (o enunciado aceita web ou linha de comando).

---

## 8. Melhorias opcionais (se sobrar tempo)

- A validade da reserva (3 dias) é gravada, mas o sistema não cancela reservas vencidas sozinho.
- O registro de solicitações já executadas (Command) fica em memória e some quando o sistema
  fecha. O certo seria uma tabela no banco com o id como chave.
- Quitação no MySQL com duas notificações chegando juntas: dá pra garantir com
  `UPDATE multa SET situacao='QUITADA' WHERE id=? AND situacao='EMITIDA'`.
- O cache de metadados não é limpo quando o livro é editado (`atualizarLivro`).

---

## Dica: VS Code mostrando erro vermelho

A estrutura de pastas mudou (`src/`, `test/`, `resources/`). Se o VS Code mostrar erros que o
`mvnw.cmd test` não mostra, abra o `pom.xml` e aceite "recarregar o projeto", ou rode o comando
**"Java: Clean Java Language Server Workspace"** (Ctrl+Shift+P).

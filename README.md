# Minha Refeição — Implementação do cenário "Assinar Plano de Refeições"

Implementação em **Java 17+** (console) do caso de uso *Assinar Plano de Refeições* do serviço de assinatura de marmitas **Minha Refeição**, traduzindo os **diagramas de sequência** e o **diagrama de classes de projeto** para código.
Cobre o **fluxo principal** e os **fluxos alternativos FA1** (código inválido), **FA2** (quantidade acima do plano) e **FA3** (pagamento não autorizado).

> Documentação de projeto (diagramas UML, correspondência UML ↔ código e roteiro do vídeo): veja a **Wiki** do repositório. A pasta `wiki/` deste projeto tem as mesmas páginas, prontas para colar.

## Arquitetura (Fronteira – Controle – Entidade)

| Camada | Pacote | Classes |
|---|---|---|
| **Fronteira** (interface com o Assinante) | `fronteira` | `TelaAssinatura` — um método por "tela" do protótipo (Telas 1 a 11) |
| **Controle** | `controle` | `AssinaturaController` — recebe os estímulos do ator e coordena os objetos |
| **Entidades (domínio)** | `entidade` | `Assinante`, `PreferenciaAlimentar`, `PlanoAssinatura`, `Assinatura`, `Pedido`, `ItemPedido`, `Prato`, `EnderecoEntrega`, `Pagamento`, `CartaoCredito`, enums `StatusAssinatura` e `CategoriaPrato`, `ResumoAssinatura` |
| **Catálogos / persistência (CSV)** | `catalogo` | `CatalogoAssinantes`, `CatalogoPlanos`, `Cardapio`, `RepositorioAssinaturas`, `CsvUtil` |
| **Serviços** | `servico` | `ServicoSMS` (simulado), `GeradorProtocolo`, `OperadoraCartaoCredito` (simulada) |
| **Exceções** | `excecao` | `QuantidadeExcedidaException` (FA2) |

## Persistência (arquivos CSV, separador `;`, UTF-8)

Pasta `data/`:

| Arquivo | Conteúdo | Origem |
|---|---|---|
| `planos.csv` | planos disponíveis | versionado (dados-base) |
| `pratos.csv` | cardápio (pratos, categoria, perfis alimentares) | versionado (dados-base) |
| `assinantes.csv` | assinantes e suas preferências | gerado na execução |
| `assinaturas.csv` | assinaturas ATIVAS e ENCERRADAS (protocolo, endereço, valor, cartão mascarado) | gerado na execução |
| `itens_pedido.csv` | itens do pedido de cada assinatura | gerado na execução |
| `protocolo.txt` | último protocolo gerado | gerado na execução |

O número do cartão **nunca** é gravado por completo (apenas `**** **** **** 1234`).

## Como executar

Requisito: **JDK 17 ou superior** (`java -version` e `javac -version`).

**Linux / macOS / Git Bash**
```bash
./build.sh      # compila para ./out
./run.sh        # executa o sistema (interativo)
```

**Windows (Prompt de Comando)**
```bat
build.bat
run.bat
```

**Sem os scripts (Linux/macOS)**
```bash
mkdir -p out
javac --release 17 -encoding UTF-8 -d out $(find src -name "*.java")
java -Dfile.encoding=UTF-8 -Ddata.dir=data -cp out br.com.minharefeicao.Main
```

Também funciona abrindo a pasta no IntelliJ, Eclipse ou VS Code: marque `src` como *source root* e execute `br.com.minharefeicao.Main`, com o diretório de trabalho na raiz do projeto (para achar a pasta `data/`).

> No Windows, se os acentos aparecerem quebrados, use `chcp 65001` antes de executar (o `run.bat` já faz isso).

### Roteiro de uso (interativo)

No menu escolha **1 – Assinar plano de refeições** e siga as telas:

1. **Celular** (ex.: `11987654321`). O "SMS" aparece no console com o código (simulado).
2. **Código** recebido.
3. **Plano** (1, 2 ou 3).
4. **Preferências alimentares** (ex.: `1,3`; ENTER = nenhuma).
5. **Pratos principais**: números separados por vírgula; repita um número para mais de uma unidade. A cota é a quantidade de refeições do plano.
6. **Acompanhamentos e sobremesas** (opcionais).
7. **Resumo do pedido**.
8. **Endereço**.
9. **Cartão** → 10. **Autorização** → 11. **Confirmação** com número de protocolo.

A opção **2 – Ver assinaturas gravadas** lê o `assinaturas.csv`.

### Como provocar cada fluxo alternativo

| Fluxo | Como provocar |
|---|---|
| **FA1** – código inválido | Na Tela 2 digite códigos errados. Após 3 erros o código é cancelado e um novo SMS é enviado. |
| **FA2** – quantidade acima do plano | Na Tela 5 escolha mais pratos principais do que o plano permite (ex.: 6 no plano Semanal, que permite 5). O sistema informa o limite e pede novo ajuste. |
| **FA3** – pagamento não autorizado | Use um cartão que **termina em `0000`**, com validade vencida ou com número diferente de 16 dígitos. Escolha 1 (tentar de novo) ou 2 (desistir → assinatura `ENCERRADA`). |

Cartão de teste aprovado: `4111111111111111`, validade `12/28`.

## Demonstração automática (cenários pré-gravados)

```bash
./demo.sh        # roda todos os cenários de demo/*.txt e mostra os CSV gerados
./demo.sh 03     # roda só o cenário FA2
```

| Arquivo | Cenário |
|---|---|
| `demo/01_fluxo_principal.txt` | Fluxo principal completo |
| `demo/02_fa1_codigo_invalido.txt` | FA1 |
| `demo/03_fa2_quantidade_excedida.txt` | FA2 |
| `demo/04_fa3_pagamento_recusado_retry.txt` | FA3: recusado, tenta outro cartão e conclui |
| `demo/05_fa3_pagamento_recusado_desistir.txt` | FA3: recusado e desiste |

O `demo.sh` usa uma **cópia temporária** dos dados e fixa o código SMS em `1234` (`-Dsms.codigo=1234`), sem alterar a pasta `data/`. A saída completa de uma execução está em `demo/saida_exemplo.txt`.

No Windows (PowerShell):
```powershell
Get-Content demo\01_fluxo_principal.txt | java -Dfile.encoding=UTF-8 -Dsms.codigo=1234 -cp out br.com.minharefeicao.Main
```

## Regras simuladas

- **SMS**: o código de 4 dígitos é "enviado" ao console. `-Dsms.codigo=XXXX` fixa o código (usado nas demos).
- **Operadora de cartão**: aprova se o número tem 16 dígitos, a validade `MM/AA` não está vencida e o cartão **não** termina em `0000`.
- **Cota do plano**: conta apenas os pratos **principais**; acompanhamentos e sobremesas não consomem cota.
- **Cardápio**: filtrado pelas preferências do assinante (sem preferência = todos os pratos).
- **Primeira entrega**: próxima segunda-feira após a contratação.

## Estrutura do repositório

```
minha-refeicao/
├── src/br/com/minharefeicao/   # código-fonte (fronteira, controle, entidade, catalogo, servico, excecao)
├── data/                       # CSVs de dados (planos.csv e pratos.csv versionados)
├── demo/                       # entradas dos cenários + saida_exemplo.txt
├── wiki/                       # páginas da Wiki (diagramas, correspondência UML↔código, roteiro do vídeo)
├── build.sh / run.sh / demo.sh # scripts Linux/macOS
├── build.bat / run.bat         # scripts Windows
└── README.md
```

## Autoria

Atividade desenvolvida por **Beatriz Savazzi Del Bianco** — Disciplina de Projeto de Software.

# ⚡ VoltMonitor - #G13

## Discentes: 

- Emiliano Rodrigues Feliciano
- Gabriel Henrique Oliveira Silva 
- Elberty Borges de Oliveira

---

## Visão Geral

O VoltMonitor é um sistema java para monitoração de tensão elétrica em tempo real, com comunicação via WebSocket com placa ESP32 (Arduino) e sensor de tensão AC ZMPT101B. Inclui gestão de usuários, clientes, equipamentos, departamentos e geração automática de ordem de serviço para manutenção.

Desde 2012, com a publicação da Resolução Normativa nº 482 pela ANEEL, que permitiu aos consumidores instalarem pequenos sistemas e compensarem o excedente de energia na rede, observamos aumento significativo de estações fotovoltaicas de geração de energia elétrica. Normalmente o sistema é gerenciado diretamente pelo cliente (sem conhecimento técnico) ou por técnico especializado no local de instalação. Para que o sistema não seja sobrecarregado com tensão fora do padrão (diferente de 220V), é necessário que a tensão elétrica se mantenha dentro de limites pré-definidos. Neste projeto, gerenciaremos remotamente a tensão elétrica no sistema simulado de placa fotovoltaica.

Com o objetivo de facilitar este monitoramento, será implementado software e hardware para medição de tensão de geração simulada e transmissão via wireless para central de processamento. Será considerada tensão dentro do padrão quando a mesma se manter entre 219V (mínimo) e 221V (máximo).

---

## Requisitos

O servidor contará com as seguintes funcionalidades:

a) Incluir e excluir cadastro de usuários (com nome, sobrenome, CPF, matrícula, função, login e senha pessoal que deverá ser digitada antes de entrar no sistema de monitoração. Somente usuários com função de "Administrador" poderá incluir e excluir qualquer cadastro. Usuários com a função de "Monitor" poderão visualizar as informações da tela principal de monitoração de clientes além de abrir e fechar Ordem de Serviço de 1º nível (reestabelecimento remoto). Usuários com a função de "Técnico" poderão visualizar as informações da tela principal de monitoração de clientes e abrir e fechar Ordem de Serviço de 2º nível (reestabelecimento presencial).

b) Incluir e excluir cadastro de clientes (com identificação interna - ID, CPF para pessoa físíca ou CNPJ para pessoa jurídica (com validação de CPF ou CNPJ digitado corretamente), nome e sobrenome para pessoa física ou razão social e nome fantasia para pessoa jurídica, endereço (incluindo logradouro, número, bairro, cidade, estado, CEP), telefone (incluindo DDD com 2 dígitos numéricos e número com 9 dígitos dígitos numéricos. Deverá aceitar apenas números e limitar a quantidade de números do DDD para até 2 dígitos e número para até 9 dígitos), IP do cliente com formato padrão.

c) Incluir e excluir cadastro de equipamento (com marca, modelo e tensão nominal. Marca e modelo poderão conter letras, números e caracteres especiais e tensão contendo apenas números maior ou igual 1 e menor ou igual a 380).

d) Incluir ou excluir cliente da lista de monitoração.

e) Incluir e excluir cadastro de departamentos da empresa. Inicialmente será incluídos os seguintes departamentos: Administração, Monitoração, Suporte Técnico, Vendas e Financeiro.

f) Relatórios de Usuários, Clientes, Equipamentos, Ordens de Serviço e Disponibilidade da Tensão elétrica do cliente de até 90 dias.

---

## Operação

A tela principal do sistema de monitoração terá layout de grade com colunas "CLIENTE", "EQUIPAMENTO", "ESTADO DA REDE", "TENSÃO (V)", "MAIOR TENSÃO (V)", "MENOR TENSÃO (V)", "SITUAÇÃO DA REDE" e "DISPONIBILIDADE", além de atalhos para sair do sistema "ESC", atualizar dados de monitoração "F5", pausar dados monitoração "F8", ordens de serviço "F9" e data e hora local.

Na coluna "CLIENTE", deverá mostrar nome e ID ou razão social e ID do cliente.

Na coluna "EQUIPAMENTO", deverá mostrar o modelo do equipamento do cliente.

Na coluna "ESTADO DA REDE", deverá mostrar mensagem de:
 a) "ATIVO" na cor verde caso o servidor receba dados da medição de tensão da placa ESP32 a cada 500 milisegundos e tensão medida maior que 0 volts,
 b) "INATIVO" na cor vermelho claro caso o servidor receba dados da medição de tensão da placa ESP32 a cada 500 milisegundos e tensão medida pela placa ESP32 igual a 0 volts,
 c) "SEM COMUNICAÇÃO" na cor vermelho escuro caso o servidor não receba dados da tensão a cada 2500 milisegundos.

Na coluna "TENSÃO (V)", deverá mostrar dados da medida feita pela placa ESP32 e recebida pelo servidor a cada 500 milisegundos.

Na coluna "MENOR TENSÃO (V)", deverá mostrar o menor valor de tensão do cliente recebida pelo servidor desde o início da sessão de monitoração do usuário logado. 

Na coluna "MAIOR TENSÃO (V)", deverá mostrar o maior valor de tensão do cliente recebida pelo servidor desde o início da sessão de monitoração do usuário logado.

Na coluna "SITUAÇÃO DA REDE", deverá mostrar mensagem da situação da rede:
 a) "NORMAL" na cor verde caso a medida de tensão recebida pelo servidor seja (maior ou igual a [("TENSAO_NOMINAL"-1) volts e ("TENSAO_NOMINAL"+1) volts], 
 b) "ALERTA" na cor amarelo caso a medida de tensão recebida pelo servidor seja entre [maior ou igual a ("TENSAO_NOMINAL"-2) volts e menor ou igual a ("TENSAO_NOMINAL"+2) volts] e [maior ou igual a ("TENSAO_NOMINAL"-3) volts] e [menor ou igual a ("TENSAO_NOMINAL"+3) volts],
 c) "CRÍTICO" na cor vermelho e emitir alerta sonoro caso a medida de tensão recebida pelo servidor seja entre [maior ou igual a 0 volts e menor ou igual a ("TENSAO_NOMINAL"-4) volts] ou [maior ou igual a ("TENSAO_NOMINAL"+4) volts].
	
O sistema também deverá abrir Ordem de Serviço (OS) automaticamente caso medida da tensão do cliente recebida pelo servidor apresentar "ESTADO DA REDE" igual a "INATIVO" ou "SEM COMUNICAÇÃO" por mais de 5000 milisegundos, ou valores da "SITUAÇÃO DA REDE" apresentar "MENOR TENSÃO (V)" ou "MAIOR TENSÃO (V)" mais que 3 mensagens de "ALERTA" ou "CRÍTICO". A Ordem de Serviço deve ser encaminhada para usuário "Monitor" de 1º nível para reestabelecimento remoto. Caso não seja possível, deverá ser encaminhada para área de Suporte Técnico e tratada pelo usuário "Suporte" de 2º nível para reestabelecimento presencial. Deverá ser fechada com a descrição da solução e especificar se ponto de falha encontrada foi no cliente ou no serviço de monitoração.
	
---

## Arquitetura do Sistema

```
---
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                    VISÃO GERAL                                        │
└─────────────────────────────────────────────────────────────────────────────────────┘

                                    ┌─────────────────┐
                                    │   ESP32 Device  │
                                    │   (Sensor AC)   │
                                    └────────┬────────┘
                                             │ HTTP POST (JSON)
                                             │ /api/voltage
                                             ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              CAMADA DE APRESENTAÇÃO (VIEW)                           │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌────────────┐ │
│  │TelaLogin     │ │TelaPrincipal │ │TelaMonitora- │ │TelaClientes  │ │TelaOrdens- │ │
│  │              │ │              │ │mento         │ │              │ │Servico      │ │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘ └────────────┘ │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌────────────┐ │
│  │TelaEstoque   │ │TelaFinanceiro│ │TelaRH        │ │TelaOrcamento │ │TelaRelato- │ │
│  │              │ │              │ │              │ │              │ │rios        │ │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘ └────────────┘ │
│                                                                                     │
│  Framework: Java Swing (JFrame, JPanel, JTable, JTabbedPane, JMenuBar)            │
│  Comunicação entre telas: Callbacks, Listeners, Timer para atualizações periódicas │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                              │
                                              │ Chamadas de métodos
                                              ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              CAMADA DE CONTROLE (CONTROLLER)                         │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                         MonitoramentoService                                  │   │
│  │  • Gerencia timers de verificação por cliente                                 │   │
│  │  • Processa medições recebidas do ESP32                                       │   │
│  │  • Controla estado PAUSADO/ATIVO                                              │   │
│  │  • Calcula disponibilidade e estatísticas                                     │   │
│  │  • Dispara callback para abertura automática de OS                            │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                         OrdemServicoService                                   │   │
│  │  • Gerencia filas de OS por nível (1º, 2º, 3º)                                │   │
│  │  • Implementa OrdemServicoCallback para OS automáticas                        │   │
│  │  • Escala OS entre níveis                                                     │   │
│  │  • Notifica listeners sobre criação/escalonamento/fechamento de OS           │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                         WebSocketServer / HttpApiServer                       │   │
│  │  • Servidor HTTP embutido (porta 8080/8081)                                   │   │
│  │  • Endpoint POST /api/voltage para receber medições do ESP32                  │   │
│  │  • Endpoint GET /api/health para health check                                 │   │
│  │  • Mapeamento IP → Cliente para identificação automática                      │   │
│  │  • Modo de simulação via console quando porta está em uso                     │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                              │
                                              │ Operações CRUD
                                              ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              CAMADA DE MODEL (MODEL)                                 │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                              DAOs (Data Access Objects)                       │   │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐        │   │
│  │  │ClienteDAO    │ │EquipamentoDAO│ │OrdemServico- │ │MedicaoTensao-│        │   │
│  │  │              │ │              │ │DAO           │ │DAO           │        │   │
│  │  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘        │   │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐        │   │
│  │  │AtendimentoDAO│ │ProdutoEstoque│ │Transacao-    │ │UsuarioDAO    │        │   │
│  │  │              │ │DAO           │ │FinanceiraDAO │ │              │        │   │
│  │  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘        │   │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐                          │   │
│  │  │Departamento- │ │FuncaoDAO     │ │ParcelaMonito-│                          │   │
│  │  │DAO           │ │              │ │ramentoDAO    │                          │   │
│  │  └──────────────┘ └──────────────┘ └──────────────┘                          │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                           ENTITIES (POJOs)                                    │   │
│  │  Cliente, Equipamento, OrdemServico, Atendimento, MedicaoTensao, Usuario,    │   │
│  │  Departamento, Funcao, ProdutoEstoque, MovimentacaoEstoque,                  │   │
│  │  TransacaoFinanceira, ParcelaMonitoramento, InstalacaoProduto, ReservaProduto│   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                              │
                                              │ Leitura/Escrita JSON
                                              ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              CAMADA DE PERSISTÊNCIA (DATA)                           │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                         DatabaseConnection (Util)                            │   │
│  │  • Gerenciamento de arquivos JSON via Jackson ObjectMapper                    │   │
│  │  • Geração automática de IDs sequenciais                                      │   │
│  │  • Criação automática do diretório data/ e arquivos vazios                    │   │
│  │  • Serialização/Deserialização com suporte a Java 8 Date/Time                 │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
│  📁 Diretório: ./data/                                                              │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │  clientes.json │ equipamentos.json │ ordens_servico.json │ medicoes.json    │   │
│  │  atendimentos.json │ usuarios.json │ departamentos.json │ funcoes.json      │   │
│  │  produtos_estoque.json │ movimentacoes_estoque.json                           │   │
│  │  transacoes_financeiras.json │ parcelas_monitoramento.json                   │   │
│  │  instalacao_produtos.json │ reservas_produtos.json │ orcamentos.json         │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘

---
```

## Estrutura do Projeto

```
---
VOLTMONITOR/
│
├── pom.xml                                   # Dependências Maven (Jackson)
│
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── monitoramento/
│                   │
│                   ├── Main.java             # Ponto de entrada principal
│                   │
│                   ├── model/                # ENTIDADES (POJOs)
│                   │   ├── Atendimento.java
│                   │   ├── Cliente.java
│                   │   ├── Departamento.java
│                   │   ├── Equipamento.java
│                   │   ├── Funcao.java
│                   │   ├── InstalacaoProduto.java
│                   │   ├── MedicaoTensao.java
│                   │   ├── MovimentacaoEstoque.java
│                   │   ├── OrdemServico.java
│                   │   ├── ParcelaMonitoramento.java
│                   │   ├── ProdutoEstoque.java
│                   │   ├── ReservaProduto.java
│                   │   ├── TransacaoFinanceira.java
│                   │   └── Usuario.java
│                   │
│                   ├── dao/                  # ACESSO A DADOS (JSON)
│                   │   ├── AtendimentoDAO.java
│                   │   ├── ClienteDAO.java
│                   │   ├── DepartamentoDAO.java
│                   │   ├── EquipamentoDAO.java
│                   │   ├── FuncaoDAO.java
│                   │   ├── InstalacaoProdutoDAO.java
│                   │   ├── MedicaoTensaoDAO.java
│                   │   ├── MovimentacaoEstoqueDAO.java
│                   │   ├── OrdemServicoDAO.java
│                   │   ├── ParcelaMonitoramentoDAO.java
│                   │   ├── ProdutoEstoqueDAO.java
│                   │   ├── ReservaProdutoDAO.java
│                   │   ├── TransacaoFinanceiraDAO.java
│                   │   └── UsuarioDAO.java
│                   │
│                   ├── service/              # LÓGICA DE NEGÓCIO
│                   │   ├── HttpApiServer.java       # API REST para ESP32 (porta 8081)
│                   │   ├── MonitoramentoService.java # Processa medições, controla pausa
│                   │   ├── OrdemServicoService.java  # Gerencia filas de OS e callbacks
│                   │   └── WebSocketServer.java     # Servidor HTTP/WebSocket (porta 8080)
│                   │
│                   ├── ui/                   # INTERFACES GRÁFICAS (Swing)
│                   │   ├── TelaLogin.java
│                   │   ├── TelaPrincipal.java
│                   │   ├── TelaMonitoramento.java
│                   │   ├── TelaClientes.java
│                   │   ├── TelaEquipamentos.java
│                   │   ├── TelaOrdensServico.java
│                   │   ├── TelaOrdemInstalacao.java
│                   │   ├── TelaOrcamento.java
│                   │   ├── TelaAtendimento.java
│                   │   ├── TelaEstoque.java
│                   │   ├── TelaEstoqueBaixo.java
│                   │   ├── TelaMovimentacoesEstoque.java
│                   │   ├── TelaFinanceiro.java
│                   │   ├── TelaRH.java
│                   │   ├── TelaDepartamentos.java
│                   │   ├── TelaFuncoes.java
│                   │   ├── TelaRelatorios.java
│                   │   ├── TelaTI.java
│                   │   ├── TelaUsuarios.java
│                   │   └── TelaVisualizarOS.java
│                   │
│                   └── util/                 # UTILITÁRIOS
│                       ├── DatabaseConnection.java   # Gerenciamento de arquivos JSON
│                       └── ValidadorDocumento.java   # Validação de CPF/CNPJ
│
├── data/                                      # ARQUIVOS DE PERSISTÊNCIA (criados automaticamente)
│   ├── clientes.json
│   ├── equipamentos.json
│   ├── medicoes.json
│   ├── ordens_servico.json
│   ├── atendimentos.json
│   ├── usuarios.json
│   ├── departamentos.json
│   ├── funcoes.json
│   ├── produtos_estoque.json
│   ├── movimentacoes_estoque.json
│   ├── transacoes_financeiras.json
│   ├── parcelas_monitoramento.json
│   ├── instalacao_produtos.json
│   ├── reservas_produtos.json
│   └── orcamentos.json
│
└── README.md                                  # Documentação do projeto

---
```

## Diagrama de Classes

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                              SISTEMA VOLTMONITOR                                             │
│                                              Diagrama de Classes (UML)                                      │
└─────────────────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                          CAMADA DE SERVIÇOS (SERVICE)                                        │
└─────────────────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────┐       ┌─────────────────────────────────────────┐
│      MonitoramentoService               │       │      OrdemServicoService                │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ - clienteDAO: ClienteDAO                │       │ - ordemServicoDAO: OrdemServicoDAO      │
│ - medicaoDAO: MedicaoTensaoDAO          │       │ - clienteDAO: ClienteDAO                │
│ - dadosMonitoramento: Map<Int,Dados>    │       │ - filaOSPrimeiroNivel: Queue<OS>         │
│ - timersCliente: Map<Int,Timer>         │       │ - filaOSSegundoNivel: Queue<OS>          │
│ - pausado: boolean                      │       │ - filaOSTerceiroNivel: Queue<OS>         │
│ - ordemServicoCallback: OrdemServico... │       │ - listeners: List<OSListener>            │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ + iniciarMonitoramentoCliente(id)       │       │ + abrirOSAtendimento(...): OS            │
│ + pararMonitoramentoCliente(id)         │       │ + abrirOSSuporteTecnico(...): OS         │
│ + receberMedicao(id, tensao)            │       │ + abrirOSTI(...): OS                     │
│ + setPausado(pausado)                   │       │ + abrirOSInstalacao(...): OS             │
│ + calcularDisponibilidade(id): double   │       │ + abrirOrdemServicoManual(...): OS       │
│ + getDadosMonitoramento(id): Dados      │       │ + escalarParaSegundoNivel(id): boolean   │
│ + reiniciarMonitoramentoCliente(id)     │       │ + escalarParaTerceiroNivel(id): boolean  │
│ + reinicializarTodosClientes()          │       │ + fecharOrdemServico(...): boolean       │
└─────────────────────────────────────────┘       │ + listarTodasOS(): List<OS>              │
                    ▲                             │ + getProximaOSPrimeiroNivel(): OS        │
                    │ implements                  │ + addOSListener(listener)                │
                    │                             └─────────────────────────────────────────┘
┌─────────────────────────────────────────┐                    ▲
│ <<interface>>                           │                    │ implements
│ OrdemServicoCallback                    │                    │
├─────────────────────────────────────────┤       ┌─────────────────────────────────────────┐
│ + onAbrirOSAutomatica(idCliente, motivo)│       │ <<interface>>                           │
└─────────────────────────────────────────┘       │ OSListener                               │
                                                   ├─────────────────────────────────────────┤
                                                   │ + onOSCriada(os)                        │
                                                   │ + onOSEscalada(os)                      │
                                                   │ + onOSFechada(os)                       │
                                                   └─────────────────────────────────────────┘

┌─────────────────────────────────────────┐       ┌─────────────────────────────────────────┐
│      WebSocketServer                    │       │      HttpApiServer                      │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ - monitoramentoService: Monitoramento...│       │ - monitoramentoService: Monitoramento...│
│ - clienteDAO: ClienteDAO                │       │ - clienteDAO: ClienteDAO                │
│ - ipClienteMap: Map<String,Integer>     │       │ - server: HttpServer                    │
│ - running: boolean                      │       │ - running: boolean                      │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ + start()                               │       │ + start()                               │
│ + pararServidor()                       │       │ + stop()                                │
│ + atualizarMapeamentoIPs()              │       │ + isRunning(): boolean                  │
│ + processarMedicaoRecebida(ip,tensao,ts)│       │                                         │
└─────────────────────────────────────────┘       └─────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                             CAMADA DE MODELO (MODEL)                                         │
└─────────────────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────┐       ┌─────────────────────────────────────────┐
│           Cliente                       │       │           Equipamento                   │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ - id: int                               │       │ - id: int                               │
│ - tipo: String                          │       │ - marca: String                         │
│ - documento: String                     │       │ - modelo: String                        │
│ - nome: String                          │       │ - tensaoNominal: double                 │
│ - sobrenome: String                     │       │ - idCliente: int                        │
│ - razaoSocial: String                   │       ├─────────────────────────────────────────┤
│ - nomeFantasia: String                  │       │ + Equipamento()                         │
│ - logradouro: String                    │       │ + getMarca() / setMarca()               │
│ - numero: String                        │       │ + getModelo() / setModelo()             │
│ - bairro: String                        │       │ + getTensaoNominal() / setTensaoNominal()│
│ - cidade: String                        │       └─────────────────────────────────────────┘
│ - estado: String                        │                   1
│ - cep: String                           │                   │
│ - telefoneDDD: String                   │                   │ has
│ - telefoneNumero: String                │                   │
│ - ipCliente: String                     │                   ▼
│ - emMonitoramento: boolean              │       ┌─────────────────────────────────────────┐
│ - prioridadeAtendimento: String         │       │           OrdemServico                   │
│ - dataCadastro: Date                    │       ├─────────────────────────────────────────┤
│ - equipamento: Equipamento              │       │ - id: int                               │
├─────────────────────────────────────────┤       │ - idCliente: int                        │
│ + Cliente()                             │       │ - idEquipamento: int                    │
│ + getNomeExibicao(): String             │       │ - tipoNivel: String                     │
│ + getEnderecoCompleto(): String         │       │ - status: String                        │
│ + getTelefoneCompleto(): String         │       │ - motivo: String                        │
│ + mesmoDocumentoEEndereco(Cliente): bool│       │ - descricaoSolucao: String              │
│ + getChaveDocumentoEndereco(): String   │       │ - falhaIdentificada: String             │
└─────────────────────────────────────────┘       │ - dataAbertura: Date                    │
                   1                              │ - dataFechamento: Date                  │
                   │                              │ - idUsuarioAbertura: int                │
                   │ has                          │ - idUsuarioFechamento: int              │
                   │                              │ - tipoOs: String                        │
                   ▼                              │ - tipoOrdem: String                     │
┌─────────────────────────────────────────┐       │ - valorTotal: double                    │
│           Atendimento                   │       │ - enderecoInstalacao: String            │
├─────────────────────────────────────────┤       │ - dataAgendamento: Date                 │
│ - id: int                               │       │ - prioridade: String                    │
│ - idCliente: int                        │       │ - produtosInstalacao: List<Instalacao...>│
│ - tipo: String                          │       ├─────────────────────────────────────────┤
│ - status: String                        │       │ + OrdemServico()                        │
│ - assunto: String                       │       │ + isInstalacao(): boolean               │
│ - descricao: String                     │       │ + isOrcamento(): boolean                │
│ - prioridade: String                    │       │ + isInformacao(): boolean               │
│ - dataAbertura: Date                    │       │ + isReparo(): boolean                   │
│ - dataConclusao: Date                   │       │ + getPrioridadeExibicao(): String       │
│ - idUsuarioAbertura: int                │       │ + adicionarProdutoInstalacao(Instalacao)│
│ - idUsuarioAtendimento: int             │       │ + recalcularValorTotal()                │
│ - idOrdemServicoVinculada: int          │       └─────────────────────────────────────────┘
├─────────────────────────────────────────┤                   1
│ + Atendimento()                         │                   │
└─────────────────────────────────────────┘                   │ contains
                                                              ▼
┌─────────────────────────────────────────┐       ┌─────────────────────────────────────────┐
│           MedicaoTensao                 │       │      InstalacaoProduto                  │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ - id: int                               │       │ - id: int                               │
│ - idCliente: int                        │       │ - idOrdemInstalacao: int                │
│ - tensao: double                        │       │ - idProduto: int                        │
│ - dataHora: Date                        │       │ - nomeProduto: String                   │
│ - estadoRede: String                    │       │ - quantidade: int                       │
│ - situacaoRede: String                  │       │ - precoUnitario: double                 │
├─────────────────────────────────────────┤       │ - subtotal: double                      │
│ + MedicaoTensao()                       │       ├─────────────────────────────────────────┤
└─────────────────────────────────────────┘       │ + InstalacaoProduto()                   │
                                                  └─────────────────────────────────────────┘

┌─────────────────────────────────────────┐       ┌─────────────────────────────────────────┐
│           Usuario                       │       │           ProdutoEstoque                │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ - id: int                               │       │ - id: int                               │
│ - nome: String                          │       │ - codigo: String                        │
│ - sobrenome: String                     │       │ - nome: String                          │
│ - cpf: String                           │       │ - categoria: String                     │
│ - matricula: String                     │       │ - fabricante: String                    │
│ - funcao: String                        │       │ - modelo: String                        │
│ - departamento: String                  │       │ - quantidade: int                       │
│ - login: String                         │       │ - quantidadeMinima: int                 │
│ - senha: String                         │       │ - precoCusto: double                    │
│ - dataCadastro: Date                    │       │ - precoVenda: double                    │
│ - nivelAtendimento: String              │       │ - unidadeMedida: String                 │
├─────────────────────────────────────────┤       │ - localizacao: String                   │
│ + Usuario()                             │       │ - ativo: boolean                        │
│ + getNomeCompleto(): String             │       ├─────────────────────────────────────────┤
│ + podeVisualizarNivel(nivelOS): boolean │       │ + getNomeCompleto(): String             │
└─────────────────────────────────────────┘       │ + isEstoqueBaixo(): boolean             │
                                                  └─────────────────────────────────────────┘

┌─────────────────────────────────────────┐       ┌─────────────────────────────────────────┐
│      TransacaoFinanceira                │       │      MovimentacaoEstoque                │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ - id: int                               │       │ - id: int                               │
│ - tipo: String                          │       │ - idProduto: int                        │
│ - natureza: String                      │       │ - tipo: String (ENTRADA/SAIDA)          │
│ - formaPagamento: String                │       │ - motivo: String                        │
│ - valor: double                         │       │ - quantidade: int                       │
│ - descricao: String                     │       │ - precoUnitario: double                 │
│ - status: String                        │       │ - valorTotal: double                    │
│ - dataVencimento: Date                  │       │ - dataMovimento: Date                   │
│ - dataPagamento: Date                   │       │ - idUsuario: int                        │
│ - dataRegistro: Date                    │       │ - idReferencia: int                     │
│ - documentoReferencia: String           │       │ - observacao: String                    │
│ - idCliente: int                        │       ├─────────────────────────────────────────┤
│ - idOrdemServico: int                   │       │ + MovimentacaoEstoque()                 │
│ - idUsuarioRegistro: int                │       └─────────────────────────────────────────┘
├─────────────────────────────────────────┤
│ + TransacaoFinanceira()                 │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐       ┌─────────────────────────────────────────┐
│      ParcelaMonitoramento               │       │           ReservaProduto                │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ - id: int                               │       │ - id: int                               │
│ - idOrdemServico: int                   │       │ - idProduto: int                        │
│ - numeroParcela: int                    │       │ - nomeProduto: String                   │
│ - valor: double                         │       │ - quantidade: int                       │
│ - dataVencimento: Date                  │       │ - idOrcamento: int                      │
│ - status: String                        │       │ - idOrdemInstalacao: int                │
│ - dataPagamento: Date                   │       │ - status: String                        │
├─────────────────────────────────────────┤       │ - dataReserva: Date                     │
│ + ParcelaMonitoramento()                │       │ - dataConsumo: Date                     │
└─────────────────────────────────────────┘       │ - observacao: String                    │
                                                  ├─────────────────────────────────────────┤
                                                  │ + ReservaProduto()                      │
                                                  └─────────────────────────────────────────┘

┌─────────────────────────────────────────┐       ┌─────────────────────────────────────────┐
│           Departamento                  │       │            Funcao                        │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ - id: int                               │       │ - id: int                               │
│ - nome: String                          │       │ - nome: String                          │
│ - descricao: String                     │       │ - descricao: String                     │
│ - responsavel: String                   │       │ - departamento: String                  │
│ - telefone: String                      │       ├─────────────────────────────────────────┤
│ - email: String                         │       │ + Funcao()                              │
├─────────────────────────────────────────┤       └─────────────────────────────────────────┘
│ + Departamento()                        │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                           CAMADA DE ACESSO A DADOS (DAO)                                     │
└─────────────────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────┐       ┌─────────────────────────────────────────┐
│           ClienteDAO                    │       │        OrdemServicoDAO                 │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ - FILE_NAME: String = "clientes.json"   │       │ - FILE_NAME: String = "ordens_servico..."│
│ - clientes: List<Cliente>               │       │ - ordens: List<OrdemServico>            │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ + ClienteDAO()                          │       │ + OrdemServicoDAO()                     │
│ + inserir(Cliente): boolean             │       │ + inserir(OS): boolean                  │
│ + atualizar(Cliente): boolean           │       │ + atualizar(OS): boolean                │
│ + excluir(id): boolean                  │       │ + excluir(id): boolean                  │
│ + buscarPorId(id): Cliente              │       │ + buscarPorId(id): OS                   │
│ + listarTodos(): List<Cliente>          │       │ + listarTodas(): List<OS>               │
│ + buscarPorDocumento(doc): Cliente      │       │ + buscarPorCliente(id): List<OS>        │
│ + listarPorMonitoramento(monitorando)   │       │ + buscarPorStatus(status): List<OS>     │
│ + listarPorMonitoramento(boolean)       │       │ + listarOsInstalacao(): List<OS>        │
│ + adicionarMonitoramento(id): boolean   │       │ + listarOsInformacao(): List<OS>        │
│ + reiniciarMonitoramento(id): boolean   │       │ + listarOsReparo(): List<OS>            │
└─────────────────────────────────────────┘       │ + listarOsOrcamento(): List<OS>         │
                                                  │ + clientePossuiOSAbertaPorTipo(...): bool│
┌─────────────────────────────────────────┐       │ + fecharOrdemServico(...): boolean      │
│        MonitoramentoService.Dados       │       └─────────────────────────────────────────┘
│        Monitoramento (Inner Class)      │
├─────────────────────────────────────────┤       ┌─────────────────────────────────────────┐
│ - idCliente: int                        │       │           UsuarioDAO                    │
│ - equipamento: Equipamento              │       ├─────────────────────────────────────────┤
│ - tensaoNominal: double                 │       │ - FILE_NAME: String = "usuarios.json"   │
│ - ultimaMedicao: Double                 │       │ - usuarios: List<Usuario>               │
│ - ultimoRecebimento: Date               │       ├─────────────────────────────────────────┤
│ - menorTensao: double                   │       │ + UsuarioDAO()                          │
│ - maiorTensao: double                   │       │ + inserir(Usuario): boolean             │
│ - estadoRedeAtual: String               │       │ + autenticar(login,senha): Usuario      │
│ - contadorMedicoes: int                 │       │ + buscarPorLogin(login): Usuario        │
│ - contadorAtivo: int                    │       │ + buscarPorCpf(cpf): Usuario            │
│ - contadorInativo: int                  │       │ + listarPorFuncao(funcao): List<Usuario>│
│ - contadorAlertasConsecutivos: int      │       │ + listarPorNivel(nivel): List<Usuario>  │
│ - contadorCriticosConsecutivos: int     │       └─────────────────────────────────────────┘
│ - inicioEstadoInativo: Date             │
│ - notificouInativo: boolean             │       ┌─────────────────────────────────────────┐
│ - osAbertaInativo: boolean              │       │       ProdutoEstoqueDAO                 │
│ - osAbertaAlerta: boolean               │       ├─────────────────────────────────────────┤
│ - osAbertaCritico: boolean              │       │ - FILE_NAME: String = "produtos_estoq..."│
├─────────────────────────────────────────┤       │ - produtos: List<ProdutoEstoque>        │
│ + incrementarContadorMedicoes()         │       ├─────────────────────────────────────────┤
│ + incrementarContadorAtivo()            │       │ + darBaixaEstoque(id,qtd,motivo,ref,usr)│
│ + resetarContadorAlertasConsecutivos()  │       │ + adicionarEstoque(id,qtd,motivo,ref,usr)│
└─────────────────────────────────────────┘       │ + listarEstoqueBaixo(): List<Produto>    │
                                                  └─────────────────────────────────────────┘

┌─────────────────────────────────────────┐       ┌─────────────────────────────────────────┐
│      TransacaoFinanceiraDAO             │       │         AtendimentoDAO                  │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ - FILE_NAME: String = "transacoes_fi..."│       │ - FILE_NAME: String = "atendimentos.json"│
│ - transacoes: List<TransacaoFinanceira> │       │ - atendimentos: List<Atendimento>       │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ + calcularSaldo(): double               │       │ + listarPorCliente(id): List<Atendimento>│
│ + listarContasPagar(): List<Transacao>  │       │ + listarPorTipo(tipo): List<Atendimento>│
│ + listarContasReceber(): List<Transacao>│       │ + listarPorStatus(status): List<Atend.> │
│ + getResumoPorNatureza(dataIni,dataFim) │       │ + concluirAtendimento(id,desc,usr): bool│
└─────────────────────────────────────────┘       │ + listarPendentes(): List<Atendimento>  │
                                                  └─────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                             CAMADA DE UTILITÁRIOS (UTIL)                                     │
└─────────────────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────┐       ┌─────────────────────────────────────────┐
│      DatabaseConnection                 │       │        ValidadorDocumento               │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ - DATA_DIR: String = "data"             │       │ + validar(documento): boolean           │
│ - objectMapper: ObjectMapper (static)   │       │ + validarCPF(cpf): boolean              │
├─────────────────────────────────────────┤       │ + validarCNPJ(cnpj): boolean            │
│ + carregarLista(fileName, class): List<T>│      │ + formatarCPF(cpf): String              │
│ + salvarLista(fileName, list)           │       │ + formatarCNPJ(cnpj): String            │
│ + carregarObjeto(fileName, class): T    │       │ + formatar(documento): String           │
│ + salvarObjeto(fileName, objeto)        │       └─────────────────────────────────────────┘
│ + gerarNovoId(lista): int               │
│ + inicializarArquivos()                 │
│ + recriarArquivo(fileName)              │
│ + limparTodosDados()                    │
│ + testConnection()                      │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                             CAMADA DE INTERFACE (UI) - PARCIAIS                               │
└─────────────────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────┐       ┌─────────────────────────────────────────┐
│           TelaPrincipal (JFrame)        │       │         TelaMonitoramento (JPanel)      │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ - usuarioLogado: Usuario                │       │ - tabelaMonitoramento: JTable           │
│ - tabbedPane: JTabbedPane               │       │ - monitoramentoService: Monitoramento...│
│ - abasAbertas: Map<String,JPanel>       │       │ - ordemServicoService: OrdemServico...  │
├─────────────────────────────────────────┤       │ - pausado: boolean                      │
│ + TelaPrincipal(usuario)                │       │ - timerAtualizacao: Timer               │
│ - adicionarAba(titulo, painel)          │       ├─────────────────────────────────────────┤
│ - configurarMenu()                      │       │ + carregarClientesMonitorados()         │
│ - logout()                              │       │ + abrirOSManual()                       │
│ - abrirMonitoramento()                  │       │ + fecharOSManual()                      │
│ - abrirAtendimento()                    │       │ - togglePausa()                         │
│ - abrirOrdemServicoST()                 │       │ - iniciarMonitoramento()                │
│ - abrirTI()                             │       └─────────────────────────────────────────┘
│ - abrirOrcamento()                      │
│ - abrirOrdemInstalacao()                │       ┌─────────────────────────────────────────┐
│ - abrirClientes()                       │       │         TelaOrdensServico (JPanel)      │
│ - abrirRH()                             │       ├─────────────────────────────────────────┤
└─────────────────────────────────────────┘       │ - tabelaOrdens: JTable                  │
                                                  │ - osService: OrdemServicoService        │
                                                  │ - ordemServicoDAO: OrdemServicoDAO      │
                                                  │ - usuarioLogado: Usuario                │
                                                  ├─────────────────────────────────────────┤
                                                  │ + carregarOrdens()                      │
                                                  │ + abrirOSManual()                       │
                                                  │ + escalarOrdem()                        │
                                                  │ + fecharOrdem()                         │
                                                  │ + onOSCriada(os)  (implements OSListener)│
                                                  └─────────────────────────────────────────┘

┌─────────────────────────────────────────┐       ┌─────────────────────────────────────────┐
│         TelaClientes (JPanel)           │       │         TelaEstoque (JPanel)            │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ - tabelaClientes: JTable                │       │ - produtoDAO: ProdutoEstoqueDAO         │
│ - clienteDAO: ClienteDAO                │       │ - tabelaProdutos: JTable                │
│ - txtDocumento, txtNome, txtEndereco... │       │ - listenersEstoqueBaixo: static List    │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ + carregarClientes()                    │       │ + carregarProdutos()                    │
│ + inserirCliente()                      │       │ + inserirProduto()                      │
│ + atualizarCliente()                    │       │ + atualizarProduto()                    │
│ + excluirCliente()                      │       │ + notificarListenersEstoqueBaixo()      │
│ - validarCampos(): boolean              │       │ + addEstoqueBaixoListener(listener)     │
└─────────────────────────────────────────┘       └─────────────────────────────────────────┘

┌─────────────────────────────────────────┐       ┌─────────────────────────────────────────┐
│      TelaEstoqueBaixo (JPanel)          │       │         TelaFinanceiro (JPanel)         │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ - produtoDAO: ProdutoEstoqueDAO         │       │ - transacaoDAO: TransacaoFinanceiraDAO  │
│ - tabelaProdutos: JTable                │       │ - tabelaTransacoes: JTable              │
│ - timerAtualizacao: Timer               │       │ - lblSaldo, lblTotalEntradas, etc.      │
├─────────────────────────────────────────┤       ├─────────────────────────────────────────┤
│ + atualizarLista()  (chamado pelo estoque)│     │ + carregarTransacoes()                  │
│ - carregarProdutosEstoqueBaixo()        │       │ + registrarTransacao()                  │
│ - solicitarCompra()                     │       │ + atualizarResumo()                     │
│ - verDetalhesProduto()                  │       │ + criarPainelContasPagar()              │
└─────────────────────────────────────────┘       │ + criarPainelContasReceber()            │
                                                  └─────────────────────────────────────────┘

### Legenda de Relacionamentos

| Símbolo | Significado |
|---------|-------------|
| `─────►` | Associação / Dependência |
| `▷─────` | Herança / Implementação de interface |
| `◆─────` | Composição (parte-todo forte) |
| `◇─────` | Agregação (parte-todo fraca) |
| `1` / `*` | Multiplicidade (um / muitos) |

### Observações

1. **MonitoramentoService.DadosMonitoramento** é uma classe interna (inner class) que encapsula os dados de monitoramento de cada cliente em tempo real.

2. **OrdemServicoService** implementa a interface `OrdemServicoCallback` para receber callbacks do `MonitoramentoService` quando uma OS precisa ser aberta automaticamente.

3. **TelaOrdensServico** implementa a interface `OSListener` para ser notificada quando uma OS é criada, escalada ou fechada.

4. **TelaEstoque** mantém uma lista estática de `TelaEstoqueBaixo` listeners, permitindo que múltiplas instâncias da tela de estoque baixo sejam atualizadas automaticamente quando o estoque é modificado.

5. **DatabaseConnection** é uma classe utilitária estática que gerencia toda a persistência via Jackson ObjectMapper.

6. **ValidadorDocumento** é uma classe utilitária estática para validação e formatação de CPF e CNPJ.

---
```

## Principais Fluxos

### Fluxo de Dados Principal

```
---
┌──────────┐    HTTP POST     ┌─────────────────┐    receberMedicao()    ┌─────────────────────┐
│   ESP32   │ ──────────────► │  HttpApiServer   │ ────────────────────► │ MonitoramentoService │
│  (Sensor) │  /api/voltage   │  (porta 8081)    │                        │                     │
└──────────┘                  └─────────────────┘                        └──────────┬──────────┘
                                                                                     │
                                    ┌────────────────────────────────────────────────┼────────────────────────────────────────────────┐
                                    │                                                │                                                │
                                    ▼                                                ▼                                                ▼
                          ┌─────────────────┐                              ┌─────────────────┐                              ┌─────────────────┐
                          │  WebSocketServer │                              │  MedicaoTensaoDAO│                              │OrdemServicoService│
                          │   (porta 8080)   │                              │  (salvar JSON)   │                              │ (callback OS)   │
                          └─────────────────┘                              └─────────────────┘                              └─────────────────┘

---
```

### Fluxo de Monitoramento

```
---
┌─────────────────────────────────────────────────────────────────────────────────────────────┐
│                              Processo de Monitoramento Contínuo                              │
└─────────────────────────────────────────────────────────────────────────────────────────────┘

1. ESP32 envia medição a cada 500ms via HTTP POST para /api/voltage
   ↓
2. HttpApiServer/WebSocketServer recebe e identifica o cliente pelo IP
   ↓
3. MonitoramentoService.receberMedicao(idCliente, tensao)
   ↓
4. Atualiza DadosMonitoramento (última tensão, contadores, maior/menor)
   ↓
5. Salva MedicaoTensao no JSON
   ↓
6. Se NÃO estiver pausado, verifica condições para abrir OS automática:
   - Tensão == 0 por >5s → abre OS REPARO
   - 3 medições consecutivas em ALERTA (diferença >1V) → abre OS INFORMAÇÃO
   - 3 medições consecutivas em CRÍTICO (diferença >3V) → abre OS REPARO
   - Timeout sem comunicação >10s → abre OS REPARO
   ↓
7. TelaMonitoramento atualiza a cada 1 segundo (se não pausado)

Modos de Operação
Modo	   Atualização Tela   	Abertura OS Automática	Abertura OS Manual
PAUSADO	  ❌ Não	            ❌ Não	                ✅ Sim
ATIVO	  ✅ Sim (1s)	        ✅ Sim	                ✅ Sim

---
```

### Fluxo de Login e Navegação

```
---
┌─────────────────┐
│   TelaLogin     │
│  login/senha    │
└────────┬────────┘
         │ autenticar()
         ▼
┌─────────────────┐
│  UsuarioDAO     │ → busca no usuarios.json
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           TelaPrincipal                                     │
│  JMenuBar com menus: Monitoração | Atendimento | Suporte Técnico | TI      │
│                       Vendas | Estoque | Cadastros | Financeiro | Relatórios│
└─────────────────────────────────────────────────────────────────────────────┘
         │
         ├──► TelaMonitoramento (monitoramento em tempo real)
         ├──► TelaAtendimento (registrar atendimentos)
         ├──► TelaOrdensServico (OS para Suporte Técnico)
         ├──► TelaTI (OS para TI)
         ├──► TelaOrcamento (criar/aprovar orçamentos)
         ├──► TelaOrdemInstalacao (converter orçamento em instalação)
         ├──► TelaClientes / TelaEquipamentos / TelaRH
         ├──► TelaEstoque / TelaMovimentacoesEstoque / TelaEstoqueBaixo
         ├──► TelaFinanceiro (transações, contas a pagar/receber)
         └──► TelaRelatorios (múltiplas abas de relatórios)

---
```

## Estrutura de Arquivos Json

```
---

Todos os dados são persistidos no diretório `data/` na raiz do projeto:

| Arquivo                         | Descrição                                      |
|---------------------------------|------------------------------------------------|
| `clientes.json`                 | Cadastro de clientes (PF/PJ)                   |
| `equipamentos.json`             | Equipamentos cadastrados                       |
| `medicoes.json`                 | Histórico de medições de tensão                |
| `ordens_servico.json`           | Ordens de Serviço (todos os tipos)             |
| `atendimentos.json`             | Atendimentos/Solicitações                      |
| `usuarios.json`                 | Usuários do sistema (login, senha, nível)      |
| `departamentos.json`            | Departamentos da empresa                       |
| `funcoes.json`                  | Funções/cargos                                 |
| `produtos_estoque.json`         | Produtos em estoque                            |
| `movimentacoes_estoque.json`    | Movimentações (entrada/saída/reserva)          |
| `transacoes_financeiras.json`   | Transações financeiras                         |
| `parcelas_monitoramento.json`   | Parcelas de mensalidades de monitoramento      |
| `instalacao_produtos.json`      | Produtos utilizados em ordens de instalação    |
| `reservas_produtos.json`        | Reservas de produtos para orçamentos aprovados |
| `orcamentos.json`               | Orçamentos gerados (criado pela TelaOrcamento) |

**Exemplo de `clientes.json`:**
```json
[
  {
    "id": 1,
    "tipo": "FISICA",
    "documento": "12345678900",
    "nome": "João",
    "sobrenome": "Silva",
    "logradouro": "Rua A",
    "numero": "100",
    "cidade": "São Paulo",
    "estado": "SP",
    "ipCliente": "192.168.43.201",
    "emMonitoramento": true,
    "prioridadeAtendimento": "ALTA",
    "equipamento": { "id": 1, "marca": "Intelbras", "modelo": "ZMPT101B", "tensaoNominal": 220.0 }
  }
]

---
```

## Funcionalidades

```
---

Módulo de Monitoramento
    Recebimento de medições via HTTP (ESP32)
    Cálculo de disponibilidade do cliente (%)
    Abertura automática de OS baseada em regras
    Modo pausa/retomada (F8)
    Indicadores visuais coloridos (ATIVO/INATIVO/SEM COMUNICAÇÃO)

Módulo de Ordens de Serviço
    Tipos: INFORMAÇÃO, REPARO, INSTALAÇÃO, ORÇAMENTO
    Níveis: 1º, 2º e 3º Nível (escalonamento)
    Prioridades: BAIXA, MÉDIA, ALTA (herdada do cliente)
    Fechamento com registro de falha e solução
    Integração com monitoramento para abertura automática

Módulo de Atendimento
    Registro de atendimentos (INFORMAÇÃO/SUGESTÃO/SOLICITAÇÃO/RECLAMAÇÃO)
    Conversão de SOLICITAÇÃO para OS INFORMAÇÃO
    Conclusão e cancelamento com registro de motivo

Módulo de Vendas/Orçamentos
    Criação de orçamentos com produtos do estoque
    Aprovação com reserva de produtos
    Conversão em Ordem de Instalação (baixa no estoque)
    Geração de mensalidades de monitoramento e contas a receber

Módulo de Estoque
    CRUD de produtos
    Movimentações (entrada/saída/reserva/cancelamento)
    Alerta de estoque baixo (atualização automática entre telas via listener)
    Solicitação de compra integrada

Módulo Financeiro
    Transações (ENTRADA/SAÍDA)
    Contas a pagar e a receber
    Parcelamento de mensalidades de monitoramento
    Resumo por período e saldo geral

Módulo de RH
    Cadastro de funcionários (nome, CPF, matrícula)
    Definição de nível de atendimento (define visibilidade de OS)
    Associação a departamento e função

Módulo de Relatórios
    Listagem de OS por status/nível
    Histórico de medições por cliente/período
    Disponibilidade por cliente
    Estatísticas gerais
    Listas (usuários, clientes, equipamentos, departamentos, funções)
    Exportação para CSV

---
```

## Banco de Dados Persistente

```
---
O sistema NÃO utiliza banco de dados relacional (MySQL/PostgreSQL). Em vez disso, utiliza armazenamento em arquivos JSON através da classe DatabaseConnection, que gerencia:
    Leitura/escrita de listas de objetos via Jackson (JSON)
    Geração automática de IDs sequenciais
    Criação automática do diretório data/ e arquivos vazios na primeira execução

Vantagens da abordagem:
    ✅ Leve, sem necessidade de instalar SGBD
    ✅ Portável (copia o diretório data/ para outro computador)
    ✅ Fácil debugging (arquivos legíveis por humanos)

Limitações:
    ❌ Sem concorrência avançada (escrita simultânea)
    ❌ Sem índices otimizados para grandes volumes (funciona bem para até milhares de registros)

---
```

## Pré-requisitos

```
---

- **Java** (JDK)
- **Maven**
- Placa **ESP32** com sensor de tensão ZMPT101B.
- **Fonte** de tensão 220V
- **Roteador** A930H
- Computador - **Servidor Local**

---
```

## Credenciais Padrão

```
---

| Campo    | Valor        |
|----------|--------------|
| Login    | `admin`     |
| Senha    | `admin123`   |
| Função   | Administrador|

---
```

## Funcionalidades

### Controle de Acesso

|-----------------|------------------------------------------------------------|
| Função          | Permissões                                                 |
|-----------------|------------------------------------------------------------|
| Administrador   | Acesso total: cadastros, monitoração e suporte             |
| Monitor         | Visualização da tela de monitoração e suporte de 1º nível  |
| Técnico         | Visualização da tela de monitoração e suporte de 2º nível  |
|-----------------|------------------------------------------------------------|

### Tela Principal de Monitoração

**Grade em tempo real com as seguintes colunas:**
|-------------------|--------------------------------------------------|
| Coluna            | Descrição                                        |
|-------------------|--------------------------------------------------|
| CLIENTE           | Nome do cliente                                  |
| EQUIPAMENTO       | Modelo do equipamento                            |
| ESTADO DA REDE    | ATIVO / INATIVO / SEM COMUNICAÇÃO                |
| TENSÃO (V)        | Tensão atual recebida do ESP32                   |
| MAIOR TENSÃO (V)  | Maior valor desde início da sessão               |
| MENOR TENSÃO (V)  | Menor valor desde início da sessão               |
| SITUAÇÃO DA REDE  | NORMAL / ALERTA / CRÍTICO                        |
|-------------------|--------------------------------------------------|

**Atalhos de Teclado:**

- `ESC` → Sair do sistema
- `F5`  → Atualizar dados manualmente
- `F8`  → Pausar / Retomar monitoração
- `F9`  → OS / Ordens de Serviço

### Lógica de Estados

**Estados da Rede:**

- `● ATIVO` (verde) — ESP32 enviando dados + tensão > 0V
- `⛔ INATIVO` (vermelho claro) — tensão = 0V
- `⛔ SEM COMUNICAÇÃO` (vermelho escuro) — sem dados por > 2.5 segundos

**Situação da Rede:** (baseada na tensão nominal do equipamento):

- `✔ NORMAL` — tensão dentro de ±1V da nominal
- `⚠ ALERTA` — tensão entre ±2V e ±3V da nominal
- `✖ CRÍTICO` — tensão além de ±4V nominal (+ alerta sonoro)

### Ordem de Serviço Automática

```
---

OS é aberta automaticamente para o departamento de Monitoração, para reestabalecimento remoto. Caso não seja possível, deverá ser encaminhada para departamento de Suporte Técnico. Dados de monitoração deve apresentar pelo menos 1 dos 2 casos abaixo:

- Estado da Rede = "INATIVO" ou "SEM COMUNICAÇÃO" por mais de 5 segundos
- Mais de 3 ocorrências de "ALERTA" ou "CRÍTICO"

---
```

### Validações

```
---
- **CPF**: algoritmo oficial com dígitos verificadores
- **CNPJ**: algoritmo oficial com dígitos verificadores
- **IP**: formato IPv4 padrão (xxx.xxx.xxx.xxx)
- **Telefone**: DDD com exatamente 2 dígitos numéricos, número com exatamente 9 dígitos
- **Tensão nominal**: entre 1V e 1500V

---
```

## Configuração do ESP32

### Hardware

```
---

Para medir tensões acima de 3.3V com o ADC do ESP32:

Fonte 19V ──┬── R1 (150kΩ) ──┬── GND
            │                │
            │              GPIO34 (ADC)
            │                │
            └── R2 (27kΩ) ───┘

Fator de divisão: `(R1+R2)/R2 = 177k/27k ≈ 6.556`
Tensão máxima medida: `3.3V × 6.556 ≈ 21.6V`

---
```

### Bibliotecas Arduíno Necessárias

```
---

- `Arduino IDE` (Gerenciador de Bibliotecas):
- `ArduinoWebsockets`
- `ArduinoJson`

---
```

### Configuração do Hardware

```
---

Editar o arquivo `esp32/VoltMonitor_ESP32.ino`:
cpp
const char* WIFI_SSID     = "POO-G13";
const char* WIFI_PASSWORD = "12345678";
const char* SERVER_IP     = "192.168.0.100";
const int   SERVER_PORT   = 8765;

---
```

### Protocolo de Comunicação

```
---

O ESP32 envia JSON via WebSocket a cada 500ms:
```json
{"ip": "192.168.1.105", "tensao": 220,00}

O servidor identifica o cliente pelo campo `"ip"`, que deve coincidir
com o **IP Local** cadastrado no registro do cliente.

---
```

## Banco de Dados Persistente

```
---

- Json 
- Exclusão **lógica** (campo `ativo`)

---
```

## Dependências Mavem

```
---

|----------------------|---------|------------------------------|
| Biblioteca           | Versão  | Uso                          |
|----------------------|---------|------------------------------|
| Java-WebSocket       | 1.5.4   | Servidor WebSocket           |
| gson                 | 2.10.1  | Parsing JSON do ESP32        |
| slf4j-simple         | 2.0.12  | Logging                      |
| jackson-annotations  | 2.21    | Data                         |
| jackson-core         | 2.21.2  | Data                         |
| jackson-databind     | 2.21.2  | Data                         |
| jackson-datatype     | 2.21.2  | Data                         |
| jdk-26_windows       | x64_bin | Data                         |
|----------------------|---------|------------------------------|

---
```

## Segurança

```
---

- Autenticação obrigatória via login/senha
- Controle de acesso por função (Administrador / Monitor)
- Sessão encerrada ao fechar o sistema

---
```

## Cronograma

```
---

**16/03** - Definição do tema do projeto: VoltMonitor - Sistema de Monitoração de Tensão Elétrica de Fonte de 19V, simulando medição de placa fotovoltáica com ESP32 e envio de dados via Wi-Fi.

**23/03** - Definição do cronograma. 

**30/03** - Estrutura base do programa: Configuração inicial no ESP32 e criação da base do código no Eclipse.

**06/04** - Definições de objetos e classes. Definição das estruturas do sistema: Classe de leitura de tensão, classe de comunicação Wi-Fi, classe de envio de dados ao servidor.

**13/04** - Aprimoramento das características dos objetos: Ajuste da leitura e calibração da tensão. Melhoria na estrutura do código e organização das funções.

**20/04** - Implementação da leitura de tensão: Leitura da fonte de 19V pelo ESP32 e conversão para valores reais. Testes no monitor.

**27/04** - Desenvolvimento do envio de dados (Wi-Fi): Testes de comunicação. Conexão com roteador A930H e envio de dados via HTTP ou MQTT.

**04/05** - Criação da lógica de monitoramento (simulação de falhas): Definir limites de tensão e detectar quedas ou valores anormais. Simular falhas na fonte.

**11/05** - Testes de validação de fluxo e correção de bugs: Testar leitura-envio-recepção-monitoração e corrigir erros de comunicação. Validar estabilidade.

**18/05** - Formatação da saída de dados: Exibição no servidor (interface) e organização dos dados recebidos. Apresentação das tensões e alertas.

**25/05** - Revisão final: Revisar todo o sistema, organizar código e slides para apresentação do funcionamento. 

**01/06** - Seminário.

---
```

## Finalidade

```
---

Projeto desenvolvido para fins educacionais na disciplina de POO - Programação Orientada a Objetos - 2026-1.

---
```

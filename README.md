# ⚡ VoltMonitor - #G13

Discentes: 

- Emiliano Rodrigues Feliciano
- Gabriel Henrique Oliveira Silva 
- Elberty Borges de Oliveira

---

## Visão Geral

O VoltMonitor é um sistema java para monitoração de tensão elétrica em tempo real, com comunicação via WebSocket com placa ESP32 (Arduino). Inclui gestão de usuários, clientes, equipamentos, departamentos e geração automática de ordem de serviço para manutenção.

Com o objetivo de facilitar este monitoramento, será implementado software e hardware para medição de tensão de geração simulada e transmissão via wireless para central de processamento. Será considerada tensão dentro do padrão quando a mesma se manter entre 18V (mínimo) e 20V (máximo).

Desde 2012, com a publicação da Resolução Normativa nº 482 pela ANEEL, que permitiu aos consumidores instalarem pequenos sistemas e compensarem o excedente de energia na rede, observamos aumento significativo de estações fotovoltaicas de geração de energia elétrica. Normalmente o sistema é gerenciado diretamente pelo cliente (sem conhecimento técnico) ou por técnico especializado no local de instalação. Para que o sistema não seja sobrecarregado com tensão fora do padrão (diferente de 19V), é necessário que a tensão elétrica se mantenha dentro de limites pré-definidos. Neste projeto, gerenciaremos remotamente a tensão elétrica no sistema simulado de placa fotovoltaica.

---

## Requisitos

O servidor contará com as seguintes funcionalidades:

a) Incluir e excluir cadastro de usuários (com nome, sobrenome, CPF, matrícula, função, login e senha pessoal que deverá ser digitada antes de entrar no sistema de monitoração. Somente usuários com função de "Administrador" poderá incluir e excluir qualquer cadastro. Usuários com a função de "Monitor" poderão visualizar as informações da tela principal de monitoração de clientes além de abrir e fechar Ordem de Serviço de 1º nível (reestabelecimento remoto). Usuários com a função de "Técnico" poderão visualizar as informações da tela principal de monitoração de clientes e abrir e fechar Ordem de Serviço de 2º nível (reestabelecimento presencial).

b) Incluir e excluir cadastro de clientes (com identificação interna - ID, CPF para pessoa físíca ou CNPJ para pessoa jurídica (com validação de CPF ou CNPJ digitado corretamente), nome e sobrenome para pessoa física ou razão social e nome fantasia para pessoa jurídica, endereço (incluindo logradouro, número, bairro, cidade, estado, CEP), telefone (incluindo DDD com 2 dígitos numéricos e número com 9 dígitos dígitos numéricos. Aceitar apenas números e limitar a quantidade de números do DDD para até 2 dígitos e número para até 9 dígitos), IP do cliente com formato padrão.

c) Incluir e excluir cadastro de equipamento (com marca, modelo e tensão nominal. Marca e modelo poderão conter letras, números e caracteres especiais e tensão contendo apenas números maior ou igual 1 e menor ou igual a 1500).

d) Incluir ou excluir cliente da lista de monitoração.

e) Incluir e excluir cadastro de departamentos da empresa. Inicialmente será incluídos os seguintes departamentos: Administração, Monitoração, Suporte Técnico.

f) Relatórios de Usuários, Clientes, Equipamentos, Ordens de Serviço e Disponibilidade da Tensão elétrica do cliente de até 90 dias.

---

## Operação

A tela principal do sistema de monitoração terá layout de grade com colunas "CLIENTE", "EQUIPAMENTO", "ESTADO DA REDE", "TENSÃO (V)", "MAIOR TENSÃO (V)", "MENOR TENSÃO (V)", "SITUAÇÃO DA REDE", além de atalhos para sair do sistema "ESC", atualizar dados de monitoração "F5", pausar dados monitoração "F8", ordens de serviço "F9" e data e hora local.

Na coluna "CLIENTE", deverá mostrar nome e ID ou razão social e ID do cliente.

Na coluna "EQUIPAMENTO", deverá mostrar o modelo do equipamento do cliente.

Na coluna "ESTADO DA REDE", deverá mostrar mensagem de:
 a) "ATIVO" na cor verde caso o servidor receba dados da medição de tensão da placa ESP32 a cada 500 milisegundos e tensão medida maior que 0 volts,
 b) "INATIVO" na cor vermelho claro caso o servidor receba dados da medição de tensão da placa ESP32 a cada 500 milisegundos e tensão medida pela placa ESP32 igual a 0 volts,
 c) "SEM COMUNICAÇÃO" na cor vermelho escuro caso o servidor não receba dados da tensão a cada 2,5 segundos.

Na coluna "TENSÃO (V)", deverá mostrar dados da medida feita pela placa ESP32 e recebida pelo servidor a cada 500 milisegundos.

Na coluna "MENOR TENSÃO (V)", deverá mostrar o menor valor de tensão do cliente recebida pelo servidor desde o início da sessão de monitoração do usuário logado. 

Na coluna "MAIOR TENSÃO (V)", deverá mostrar o maior valor de tensão do cliente recebida pelo servidor desde o início da sessão de monitoração do usuário logado.

Na coluna "SITUAÇÃO DA REDE", deverá mostrar mensagem da situação da rede:
 a) "NORMAL" na cor verde caso a medida de tensão recebida pelo servidor seja (maior ou igual a [("TENSÃO_NOMINAL"-1) volts e ("TENSÃO_NOMINAL"+1) volts], 
 b) "ALERTA" na cor amarelo caso a medida de tensão recebida pelo servidor seja entre [maior ou igual a ("TENSÃO_NOMINAL"-2) volts e menor ou igual a ("TENSÃO_NOMINAL"+2) volts] e [maior ou igual a ("TENSÃO_NOMINAL"-3) volts] e [menor ou igual a ("TENSÃO_NOMINAL"+3) volts],
 c) "CRÍTICO" na cor vermelho e emitir alerta sonoro caso a medida de tensão recebida pelo servidor seja entre [maior ou igual a 0 volts e menor ou igual a ("TENSÃO_NOMINAL"-4) volts] ou [maior ou igual a ("TENSÃO_NOMINAL"+4) volts].
	
O sistema também deverá abrir Ordem de Serviço (OS) automaticamente caso medida da tensão do cliente recebida pelo servidor apresentar "ESTADO DA REDE" igual a "INATIVO" ou "SEM COMUNICAÇÃO" por mais de 5 segundos, ou valores da "SITUAÇÃO DA REDE" apresentar "MENOR TENSÃO (V)" ou "MAIOR TENSÃO (V)" mais que 3 mensagens de "ALERTA" ou "CRÍTICO". A Ordem de Serviço deve ser encaminhada para usuário "Monitor" de 1º nível para reestabelecimento remoto. Caso não seja possível, deverá ser encaminhada para área de Suporte Técnico e tratada pelo usuário "Suporte" de 2º nível para reestabelecimento presencial. Deverá ser fechada com a descrição da solução e especificar se ponto de falha encontrada foi no cliente ou no serviço de monitoração.
	
---

## Arquitetura do Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│              VOLTMONITOR - SISTEMA DE MONITORAMENTO             │
│                      DE TENSÃO ELÉTRICA                         │
└─────────────────────────────────────────────────────────────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                        │                        │
        ▼                        ▼                        ▼
┌───────────────┐      ┌─────────────────┐      ┌───────────────┐
│  UI LAYER     │      │  SERVICE LAYER  │      │  DATA LAYER   │
│  (Swing)      │◄────►│  (Business)     │◄────►│  (DAO/JSON)   │
└───────────────┘      └─────────────────┘      └───────────────┘
        │                        │                        │
        ▼                        ▼                        ▼
┌───────────────┐      ┌─────────────────┐      ┌────────────────┐
│ TelaLogin     │      │Monitoramento    │      │DatabaseConn    │
│ TelaPrincipal │      │Service          │      │ClienteDAO      │
│ TelaClientes  │      │OrdemServico     │      │EquipamentoDAO  │
│ TelaEquipam.  │      │Service          │      │UsuarioDAO      │
│ TelaOrdensOS  │      │WebSocketServer  │      │OrdemServicoDAO │
│ TelaRelatorios│      │                 │      │MedicaoTensaoDAO│
│ TelaUsuarios  │      │                 │      │                │
└───────────────┘      └─────────────────┘      └────────────────┘
```
---

## Estrutura do Projeto

```
com.monitoramento/
│
├── Main.java                          # Ponto de entrada principal
│
├── model/                             # MODELOS (Entidades/DTOs)
│   ├── Cliente.java                   # Cliente (PF/PJ)
│   ├── Equipamento.java               # Equipamento monitorado
│   ├── MedicaoTensao.java             # Medições de tensão
│   ├── OrdemServico.java              # Ordem de serviço
│   ├── Usuario.java                   # Usuário do sistema
│   └── Departamento.java              # Departamento
│
├── dao/                               # DATA ACCESS OBJECTS
│   ├── ClienteDAO.java                # CRUD Cliente
│   ├── EquipamentoDAO.java            # CRUD Equipamento
│   ├── MedicaoTensaoDAO.java          # CRUD Medições
│   ├── OrdemServicoDAO.java           # CRUD Ordens Serviço
│   ├── UsuarioDAO.java                # CRUD Usuário + Autenticação
│   └── DepartamentoDAO.java           # CRUD Departamento
│
├── service/                           # CAMADA DE SERVIÇOS
│   ├── MonitoramentoService.java      # Lógica monitoramento tensão
│   ├── OrdemServicoService.java       # Gerenciamento OS + filas
│   └── WebSocketServer.java           # Servidor WebSocket (ESP32)
│
├── ui/                                # INTERFACE GRÁFICA
│   ├── TelaLogin.java                 # Tela de autenticação
│   ├── TelaPrincipal.java             # Dashboard monitoramento
│   ├── TelaClientes.java              # CRUD Clientes
│   ├── TelaEquipamentos.java          # CRUD Equipamentos
│   ├── TelaOrdensServico.java         # Gerenciamento OS
│   ├── TelaRelatorios.java            # Relatórios e estatísticas
│   └── TelaUsuarios.java              # CRUD Usuários (Admin)
│
└── util/                              # UTILIDADES
    ├── DatabaseConnection.java        # Persistência JSON
    ├── ValidadorCPF.java              # Validação CPF
    └── ValidadorCNPJ.java             # Validação CNPJ
```

---

## Estrutura de Arquivos Json

```
data/
├── clientes.json          # Lista<Cliente>
├── equipamentos.json      # Lista<Equipamento>
├── medicoes.json          # Lista<MedicaoTensao>
├── ordens_servico.json    # Lista<OrdemServico>
├── usuarios.json          # Lista<Usuario>
└── departamentos.json     # Lista<Departamento>
```
---

## Fluxo de Dados Principal

```
┌────────────────────────────────────────────────────────────────────┐
│                          EXTERNAL ACTORS                           │
├───────────────────┬────────────────────┬───────────────────────────┤
│   ESP32 Device    │    User (Swing)    │      Administrator        │
│   (WebSocket)     │    (Monitor)       │      (Manager)            │
└─────────┬─────────┴──────────┬─────────┴─────────────┬─────────────┘
          │                    │                       │
          ▼                    ▼                       ▼
┌────────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                            │
│ ┌────────────────────────────────────────────────────────────────┐ │
│ │  TelaPrincipal (Dashboard)  │  TelaClientes  │ TelaEquipamentos│ │
│ │  TelaOrdensServico          │  TelaRelatorios│ TelaUsuarios    │ │ 
│ └────────────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────┘
          │                    │                       │
          ▼                    ▼                       ▼
┌────────────────────────────────────────────────────────────────────┐
│                      BUSINESS LAYER (SERVICES)                     │
│  ┌─────────────────────────┐    ┌─────────────────────────────┐    │
│  │  MonitoramentoService   │◄──►│  OrdemServicoService        │    │
│  │  - receberMedicao()     │    │  - abrirOrdemServico()      │    │
│  │  - calcularDisp()       │    │  - escalarParaSegundoNivel()│    │
│  │  - verificarTimeout()   │    │  - fecharOrdemServico()     │    │
│  └─────────────────────────┘    └─────────────────────────────┘    │
│              ▲                              ▲                      │
│              │                              │                      │
│              └──────────┬───────────────────┘                      │
│                         │                                          │
│              ┌──────────┴──────────┐                               │
│              │  WebSocketServer    │                               │
│              │  - processarMedicao │                               │
│              └─────────────────────┘                               │
└────────────────────────────────────────────────────────────────────┘
          │                    │                       │
          ▼                    ▼                       ▼
┌────────────────────────────────────────────────────────────────────┐
│                        DATA ACCESS LAYER (DAO)                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │ClienteDAO│ │EquipDAO  │ │MedicaoDAO│ │OrdemDAO  │ │UsuarioDAO│  │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘  │
│       └────────────┴────────────┴────────────┴────────────┘        │
│                              │                                     │
│                              ▼                                     │
│              ┌─────────────────────────────┐                       │
│              │   DatabaseConnection        │                       │
│              │   (Jackson JSON persistence)│                       │
│              └─────────────┬───────────────┘                       │
└────────────────────────────┼───────────────────────────────────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │   data/         │
                    │   *.json files  │
                    └─────────────────┘
```

---
## Diagrama de Classes

```
┌─────────────────────────────────────────────────────────────────────┐
│                              MAIN                                   │
│  + main()                                                           │
│  - inicializarUsuarioAdmin()                                        │
│  - iniciarWebSocketServer()                                         │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
┌───────────────────────┐ ┌─────────────────┐ ┌─────────────────────┐
│    TelaLogin          │ │ Monitoramento   │ │ WebSocketServer     │
│  - realizarLogin()    │ │ Service         │ │ - start()           │
│  - usuarioDAO         │ │ - receberMedicao│ │ - processarMedicao()│
└───────────────────────┘ │ - calcularDisp()│ └─────────────────────┘
                          └─────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
┌───────────────────────┐ ┌─────────────────┐ ┌─────────────────────┐
│   ClienteDAO          │ │ EquipamentoDAO  │ │ OrdemServicoService │
│  + inserir()          │ │ + inserir()     │ │ + abrirOS()         │
│  + atualizar()        │ │ + atualizar()   │ │ + escalarOS()       │
│  + excluir()          │ │ + excluir()     │ │ + fecharOS()        │
│  + buscarPorId()      │ │ + buscarPorId() │ │ + getProximaOS()    │
└───────────────────────┘ └─────────────────┘ └─────────────────────┘
            │                       │                      │
            └───────────────────────┼──────────────────────┘
                                    ▼
                    ┌─────────────────────────────────┐
                    │      DatabaseConnection         │
                    │  + carregarLista()              │
                    │  + salvarLista()                │
                    │  + gerarNovoId()                │
                    │  + inicializarArquivos()        │
                    └─────────────────────────────────┘
                                    │
                                    ▼
                          ┌────────────────────┐
                          │   data/            │
                          │  clientes.json     │
                          │ equipamentos.json  │
                          │ medicoes.json      │
                          │ ordens_servico.json│
                          │ usuarios.json      │
                          └────────────────────┘
```

---
## Principais Fluxos

### Fluxo de Monitoramento

```
ESP32 → WebSocketServer → MonitoramentoService → MedicaoTensaoDAO → medicoes.json
                              │
                              ▼
                    Verificar anomalias
                              │
                              ▼
                    OrdemServicoService.onAbrirOSAutomatica()
                              │
                              ▼
                    TelaOrdensServico (notificação UI)
```
### Fluxo de Login e Navegação

```
TelaLogin → UsuarioDAO.autenticar() → TelaPrincipal
                                          │
                    ┌─────────────────────┼─────────────────────┐
                    ▼                     ▼                     ▼
              TelaClientes          TelaEquipamentos     TelaOrdensServico
```

---

## Pré-requisitos

- **Java** (JDK)
- **Maven**
- Placa **ESP32** com sensor de tensão.
- **Fonte** de tensão 19V
- **Roteador** A930H
- Computador - **Servidor Local**

---

## Credenciais Padrão

| Campo    | Valor        |
|----------|--------------|
| Login    | `admin`     |
| Senha    | `admin123`   |
| Função   | Administrador|

---

## Funcionalidades

### Controle de Acesso

| Função          | Permissões                                                 |
|-----------------|------------------------------------------------------------|
| Administrador   | Acesso total: cadastros, monitoração e suporte             |
| Monitor         | Visualização da tela de monitoração e suporte de 1º nível  |
| Técnico         | Visualização da tela de monitoração e suporte de 2º nível  |

### Tela Principal de Monitoração

**Grade em tempo real com as seguintes colunas:**

| Coluna            | Descrição                                        |
|-------------------|--------------------------------------------------|
| CLIENTE           | Nome do cliente                                  |
| EQUIPAMENTO       | Modelo do equipamento                            |
| ESTADO DA REDE    | ATIVO / INATIVO / SEM COMUNICAÇÃO                |
| TENSÃO (V)        | Tensão atual recebida do ESP32                   |
| MAIOR TENSÃO (V)  | Maior valor desde início da sessão               |
| MENOR TENSÃO (V)  | Menor valor desde início da sessão               |
| SITUAÇÃO DA REDE  | NORMAL / ALERTA / CRÍTICO                        |

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

OS é aberta automaticamente para o departamento de Monitoração, para reestabalecimento remoto. Caso não seja possível, deverá ser encaminhada para departamento de Suporte Técnico. Dados de monitoração deve apresentar pelo menos 1 dos 2 casos abaixo:

- Estado da Rede = "INATIVO" ou "SEM COMUNICAÇÃO" por mais de 5 segundos
- Mais de 3 ocorrências de "ALERTA" ou "CRÍTICO"

### Validações

- **CPF**: algoritmo oficial com dígitos verificadores
- **CNPJ**: algoritmo oficial com dígitos verificadores
- **IP**: formato IPv4 padrão (xxx.xxx.xxx.xxx)
- **Telefone**: DDD com exatamente 2 dígitos numéricos, número com exatamente 9 dígitos
- **Tensão nominal**: entre 1V e 1500V

---

## Configuração do ESP32

### Hardware

Para medir tensões acima de 3.3V com o ADC do ESP32:

```
Fonte 19V ──┬── R1 (150kΩ) ──┬── GND
            │                │
            │              GPIO34 (ADC)
            │                │
            └── R2 (27kΩ) ───┘
```

Fator de divisão: `(R1+R2)/R2 = 177k/27k ≈ 6.556`
Tensão máxima medida: `3.3V × 6.556 ≈ 21.6V`

### Bibliotecas Arduíno Necessárias

- `Arduino IDE` (Gerenciador de Bibliotecas):
- `ArduinoWebsockets`
- `ArduinoJson`

### Configuração do Hardware

Editar o arquivo `esp32/VoltMonitor_ESP32.ino`:
```cpp
const char* WIFI_SSID     = "POO-G13";
const char* WIFI_PASSWORD = "12345678";
const char* SERVER_IP     = "192.168.0.100";
const int   SERVER_PORT   = 8765;
```

### Protocolo de Comunicação

O ESP32 envia JSON via WebSocket a cada 500ms:
```json
{"ip": "192.168.1.105", "tensao": 19.05}
```

O servidor identifica o cliente pelo campo `"ip"`, que deve coincidir
com o **IP Local** cadastrado no registro do cliente.

---

## Banco de Dados Persistente

- Json 
- Exclusão **lógica** (campo `ativo`)

---

## Dependências Mavem

```
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
```

---

## Segurança

- Autenticação obrigatória via login/senha
- Controle de acesso por função (Administrador / Monitor)
- Sessão encerrada ao fechar o sistema

---

## Cronograma

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

## Finalidade
Projeto desenvolvido para fins educacionais na disciplina de POO - Programação Orientada a Objetos - 2026-1.















## Conceitos Aplicados ao Projeto - Avaliação 20/04/2026

## Abstração:

    Classe Cliente: Representa uma abstração de um cliente do sistema, contendo apenas atributos e métodos relevantes para o domínio (nome, documento, endereço, etc.).

    Classe Equipamento: Abstrai um equipamento com suas características essenciais (marca, modelo, tensão nominal).

    Classe OrdemServico: Abstrai uma ordem de serviço com seus atributos (status, motivo, datas).

    Classe MedicaoTensao: Abstrai uma medição de tensão realizada.

    Interface MonitoramentoService.OrdemServicoCallback (linha 237): Define uma abstração para callback de abertura automática de OS.

    Interface OrdemServicoService.OSListener (linha 262): Abstrai eventos relacionados a ordens de serviço.

##Agregação:

    TelaClientes contém ClienteDAO (linha 13): A tela usa o DAO, mas o DAO pode existir independentemente.

    TelaEquipamentos contém EquipamentoDAO e ClienteDAO (linhas 18-19): Agrega os DAOs para operações.

    TelaOrdensServico contém OrdemServicoService, ClienteDAO, EquipamentoDAO, UsuarioDAO (linhas 20-23): Agrega serviços e DAOs.

    TelaRelatorios contém MonitoramentoService e múltiplos DAOs (linhas 18-23): Agrega os componentes necessários para relatórios.

## Associação:

    Cliente associado a Equipamento: Um cliente pode ter vários equipamentos (via idCliente em Equipamento).

    Cliente associado a OrdemServico: Através do atributo idCliente na classe OrdemServico.

    Equipamento associado a OrdemServico: Via atributo idEquipamento em OrdemServico.

    Usuario associado a OrdemServico: Via idUsuarioAbertura e idUsuarioFechamento.

    MonitoramentoService associado a OrdemServicoService: O OrdemServicoService registra um callback no MonitoramentoService (linha 35-37 do OrdemServicoService.java).

## Atribuição:

    ClienteDAO.java linha 22: cliente.setId(DatabaseConnection.gerarNovoId(clientes));

    EquipamentoDAO.java linha 37: equipamento.setId(DatabaseConnection.gerarNovoId(equipamentos));

    UsuarioDAO.java linha 29: usuario.setId(DatabaseConnection.gerarNovoId(usuarios));

    TelaClientes.java linha 271: cliente.setTipo((String) comboTipo.getSelectedItem());

    OrdemServicoService.java linha 87-97: Atribuição de todos os atributos da nova OrdemServico.

## Classes Principais:

    Main (Main.java): Classe principal que inicializa o sistema, serviços, WebSocket e interface gráfica.

    MonitoramentoService (MonitoramentoService.java): Gerencia o monitoramento de tensão em tempo real.

    OrdemServicoService (OrdemServicoService.java): Gerencia ordens de serviço e filas.

    WebSocketServer (WebSocketServer.java): Servidor para receber medições de dispositivos.

    DatabaseConnection (DatabaseConnection.java): Gerencia persistência em JSON.

    TelaLogin (TelaLogin.java): Tela de autenticação.

    TelaPrincipal (TelaPrincipal.java): Tela principal com monitoramento em tempo real.

    Cliente (Cliente.java): Modelo de dados do cliente.

    OrdemServico (OrdemServico.java): Modelo de ordem de serviço.

    Usuario (Usuario.java): Modelo de usuário do sistema.

## Composição:

    Main compõe WebSocketServer (linha 12): A Main cria e gerencia o ciclo de vida do servidor WebSocket.

    MonitoramentoService compõe ClienteDAO, EquipamentoDAO, MedicaoTensaoDAO (linhas 16-18): O service cria e gerencia seus DAOs internamente.

    OrdemServicoService compõe OrdemServicoDAO, ClienteDAO, EquipamentoDAO, UsuarioDAO (linhas 18-21): Composição dos DAOs necessários.

    TelaPrincipal compõe MonitoramentoService (linha 22): A tela principal recebe e mantém referência ao service.

    DatabaseConnection compõe ObjectMapper (linha 15): O objectMapper é criado e gerenciado estaticamente.

## Encapsulamento:

    Todas as classes modelo: Atributos são private com getters/setters públicos (ex: Cliente.java linhas 4-47).

    Cliente.java linha 72: private int id; encapsulado por getId() e setId().

    MonitoramentoService: Atributos como dadosMonitoramento, timersCliente, contadorSemComunicacao são private (linhas 23-26).

    DatabaseConnection: Construtor privado implícito (classe utilitária), métodos estáticos públicos.

    WebSocketServer: running, serverThread, serverSocket são private (linhas 14-17).

## Herança:

    Uso de herança da biblioteca Java:

    TelaClientes extends JFrame (linha 12)

    TelaEquipamentos extends JFrame (linha 13)

    TelaLogin extends JFrame (linha 10)

    TelaOrdensServico extends JFrame (linha 14)

    TelaPrincipal extends JFrame (linha 16)

    TelaRelatorios extends JFrame (linha 15)

    TelaUsuarios extends JFrame (linha 12)

## Modificadores de Acesso:

   private:

        Atributos de classe: private int id; (Cliente.java linha 4)

        Métodos auxiliares: private void carregarClientes() (ClienteDAO.java linha 16)

        Classes internas: private class EstadoRedeRenderer (TelaPrincipal.java linha 197)

   public:

        Construtores: public Cliente() (Cliente.java linha 20)

        Métodos de serviço: public void receberMedicao() (MonitoramentoService.java linha 127)

        Classes principais: public class MonitoramentoService

    protected: Nenhum uso explícito nos arquivos.

    static:

        private static final String FILE_NAME em DAOs (ex: ClienteDAO.java linha 6)

        private static ObjectMapper objectMapper (DatabaseConnection.java linha 14)

        public static void main(String[] args) (Main.java linha 12)

## Objetos:

    Cliente: Cliente cliente = new Cliente(); (TelaClientes.java linha 271)

    OrdemServico: OrdemServico os = new OrdemServico(); (OrdemServicoService.java linha 87)

    Equipamento: Equipamento equipamento = new Equipamento(); (TelaEquipamentos.java linha 144)

    Usuario: Usuario admin = new Usuario(); (Main.java linha 56)

    MedicaoTensao: MedicaoTensao medicao = new MedicaoTensao(); (MonitoramentoService.java linha 153)

    Departamento: Departamento dept = new Departamento(); (DepartamentoDAO.java linha 82)

    Timer: Timer timer = new Timer(true); (MonitoramentoService.java linha 85)

    ArrayList: clientes = new ArrayList<>(); (ClienteDAO.java linha 19)

## Polimorfismo:

   Polimorfismo de subtipo (herança de JFrame):

        TelaLogin, TelaPrincipal, TelaClientes etc. são tratados como JFrame.

   Polimorfismo paramétrico (Generics):

        DatabaseConnection.carregarLista(FILE_NAME, Cliente.class) (ClienteDAO.java linha 17)

        DatabaseConnection.salvarLista(FILE_NAME, clientes) (ClienteDAO.java linha 27)

        List<Cliente>, List<Equipamento>, List<OrdemServico> em todos os DAOs.

        ConcurrentHashMap<Integer, DadosMonitoramento> (MonitoramentoService.java linha 25)

   Polimorfismo de inclusão (interface):

        OrdemServicoService implementa MonitoramentoService.OrdemServicoCallback (linha 17 do OrdemServicoService.java):
        java

		public class OrdemServicoService implements MonitoramentoService.OrdemServicoCallback

		TelaOrdensServico implementa OrdemServicoService.OSListener (linha 14):

		public class TelaOrdensServico extends JFrame implements OrdemServicoService.OSListener

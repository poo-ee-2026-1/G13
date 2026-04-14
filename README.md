# ⚡ VoltMonitor - #G13

Discentes: 

- Emiliano Rodrigues Feliciano
- Gabriel Henrique Oliveira Silva 
- Elberty Borges de Oliveira

---

## Visão Geral

O VoltMonitor é um sistema java para monitoração de tensão elétrica em tempo real, com comunicação via WebSocket com placa ESP32 (Arduino). Inclui gestão de usuários, clientes, equipamentos, departamentos e geração automática de ordem de serviço para manutenção.

Com o objetivo de facilitar este monitoramento, será implementado software e hardware para medição de tensão da geração simulada e transmissão por wireless para central de processamento. Será considerada tensão dentro do padrão quando a mesma se manter entre 18V (mínimo) e 20V (máximo).

Desde 2012, com a publicação da Resolução Normativa nº 482 pela ANEEL, que permitiu aos consumidores instalarem pequenos sistemas e compensarem o excedente de energia na rede, observamos aumento significativo de estações fotovoltaicas de geração de energia elétrica. Normalmente o sistema é gerenciado diretamente pelo cliente (sem conhecimento técnico) ou por técnico especializado no local de instalação. Para que o sistema não seja sobrecarregado com tensão fora do padrão (19V), é necessário que a tensão elétrica se mantenha dentro de limites pré-definidos. Neste projeto, gerenciaremos apenas a tensão elétrica no sistema simulado de placa fotovoltaica.

---

## Requisitos

O servidor deverá contar com as seguintes funcionalidades:

a) Incluir e excluir cadastro de usuários (com nome, sobrenome, CPF, matrícula, função, login e senha pessoal que deverá ser digitada antes de entrar no sistema de monitoração. Somente usuários com função de "Administrador" poderá incluir e excluir qualquer cadastro. Usuários com a função de "Monitor", poderão somente visualizar as informações da tela principal de monitoração de clientes). Deverá também contar usuários com a função de "Suporte" para tratamento das ordens de serviço de manutenção abertas manual ou automaticamente.

b) Incluir e excluir cadastro de clientes (com CPF para pessoa físíca ou CNPJ para pessoa jurídica (com validação de CPF ou CNPJ digitado corretamente), nome e sobrenome para pessoa física ou razão social e nome fantasia para pessoa jurídica, endereço (incluindo logradouro, número, bairro, cidade, estado, CEP), telefone (incluindo DDD com 2 dígitos numéricos e número com 9 dígitos dígitos numéricos. Aceitar apenas números e limitar a quantidade de números do DDD para até 2 dígitos e número para até 9 dígitos), IP do cliente com formato padrão.

c) Incluir ou excluir cliente da lista de monitoração.

d) Incluir e excluir cadastro de equipamento (com marca, modelo e tensão nominal. Marca e modelo poderão conter letras, números e caracteres especiais e tensão contendo apenas números maior ou igual 1 e menor ou igual a 1500).

e) Incluir e excluir cadastro de departamentos da empresa.

---

## Operação

A tela principal do sistema de monitoração deverá ser em layout de grade monstrando: "CLIENTE", "EQUIPAMENTO", "ESTADO DA REDE", "TENSÃO (V)", "MAIOR TENSÃO (V)", "MENOR TENSÃO (V)", "SITUAÇÃO DA REDE", além de atalhos para sair do sistema "ESC", atualizar dados de monitoração "F5", pausar dados monitoração "F8" e data e hora local.

Na coluna "CLIENTE", deverá mostrar nome e sobrenome do cliente.

Na coluna "EQUIPAMENTO", deverá mostrar o modelo do equipamento do cliente.

Na coluna "ESTADO DA REDE", deverá mostrar mensagem de:
 a) "ATIVO" na cor verde caso o servidor receba dados da medição de tensão da placa ESP32 a cada 500 milisegundos e tensão medida maior que 0 volts,
 b) "INATIVO" na cor vermelho claro caso o servidor receba dados da medição de tensão da placa ESP32 a cada 500 milisegundos e tensão medida pela placa ESP32 igual a 0 volts,
 c) "SEM COMUNICAÇÃO" na cor vermelho escuro caso o servidor não receba dados da tensão a cada 2,5 segundos.

Na coluna "TENSÃO (V)", deverá mostrar dados da medida feita pela placa ESP32 e recebida pelo servidor a cada 500 milisegundos.

Na coluna "MENOR TENSÃO (V)", deverá mostrar o menor valor de tensão do cliente recebida pelo servidor desde o início da sessão de monitoração do usuário logado. 

Na coluna "MAIOR TENSÃO (V)", deverá mostrar o maior valor de tensão do cliente recebida pelo servidor desde o início da sessão de monitoração do usuário logado.

Na coluna "SITUAÇÃO DA REDE", deverá mostrar mensagem da situação da rede:
 a) "NORMAL" na cor verde caso a medida de tensão recebida pelo servidor seja (maior ou igual a ("VALOR_TENSÃO_NOMINAL"-1) volts e ("VALOR_TENSÃO_NOMINAL"+1) volts, 
 b) "ALERTA" na cor amarelo caso a medida de tensão recebida pelo servidor seja entre maior ou igual a ("VALOR_TENSÃO_NOMINAL"-3) volts e menor ou igual a ("VALOR_TENSÃO_NOMINAL"-2) volts) e (maior ou igual a ("VALOR_TENSÃO_NOMINAL"+2) volts e menor ou igual a ("VALOR_TENSÃO_NOMINAL"+3) volts,
 c) "CRÍTICO" na cor vermelho e emitir alerta sonoro caso a medida de tensão recebida pelo servidor seja entre maior ou igual a 0 volts e menor ou igual a ("VALOR_TENSÃO_NOMINAL"-4) volts e maior ou igual a ("VALOR_TENSÃO_NOMINAL"+4) volts.
	
O sistema também deverá abrir Ordem de Serviço (OS) automaticamente caso medida do cliente recebida pelo servidor apresentar "ESTADO DA REDE" igual a "INATIVO" ou "SEM COMUNICAÇÃO", ou valores da "SITUAÇÃO DA REDE" apresentar "MENOR TENSÃO (V)" ou "MAIOR TENSÃO (V)" mais que 3 mensagens de "ALERTA" ou "CRÍTICO". A Ordem de Serviço deve ser encaminhada para área de Suporte Técnico e tratada pelo usuário "Suporte" e fechada com a descrição da solução e especificar se ponto de falha encontrada foi no cliente ou no servio de monitoração.
	
---

## Arquitetura do Sistema

```
[ESP32 + Sensor de Tensão]
         │  WiFi / WebSocket (porta 8765)
         ▼
[Servidor Java - VoltMonitor]
    ├── WebSocket Server (recebe dados ESP32)
    ├── MonitoramentoService (lógica de negócio)
    ├── DatabaseManager (SQLite - persistência)
    └── Interface Swing (tela principal + cadastros)
```

---

## Estrutura do Projeto

```
VoltMonitor/
├── pom.xml                              # Build Maven
├── esp32/
│   └── VoltMonitor_ESP32.ino            # Firmware ESP32
└── src/main/java/com/voltmonitor/
    ├── Main.java                        # Ponto de entrada
    ├── model/
    │   ├── Usuario.java
    │   ├── Cliente.java
    │   ├── Equipamento.java
    │   ├── Departamento.java
    │   ├── OrdemServico.java
    │   └── MedicaoTensao.java
    ├── repository/
    │   └── DatabaseManager.java          # SQLite + BCrypt
    ├── service/
    │   └── MonitoramentoService.java     # Lógica central
    ├── websocket/
    │   └── VoltWebSocketServer.java      # Servidor WS
    ├── ui/
    │   ├── LoginDialog.java              # Tela de login
    │   ├── TelaPrincipal.java            # Grade de monitoração
    │   ├── TelaUsuarios.java             # CRUD Usuários
    │   ├── TelaClientes.java             # CRUD Clientes
    │   └── CadastroPanels.java           # Equipamentos/Dep./OS
    └── util/
        └── Validador.java                # CPF, CNPJ, IP, etc.
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
| Função          | Permissões                                          |
|-----------------|-----------------------------------------------------|
| Administrador   | Acesso total: cadastros + monitoração               |
| Monitor         | Somente visualização da tela de monitoração         |

### Tela Principal de Monitoração

**Grade em tempo real com as seguintes colunas:**

| Coluna            | Descrição                                        |
|-------------------|--------------------------------------------------|
| CLIENTE           | Nome do cliente                                  |
| EQUIPAMENTO       | Modelo do equipamento                            |
| ESTADO DA REDE    | ATIVO / INATIVO / SEM COMUNICAÇÃO               |
| TENSÃO (V)        | Tensão atual recebida do ESP32                   |
| MAIOR TENSÃO (V)  | Maior valor desde início da sessão               |
| MENOR TENSÃO (V)  | Menor valor desde início da sessão               |
| SITUAÇÃO DA REDE  | NORMAL / ALERTA / CRÍTICO                        |

**Atalhos de Teclado:**

- `ESC` → Sair do sistema
- `F5`  → Atualizar dados manualmente
- `F8`  → Pausar / Retomar monitoração

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

OS é aberta automaticamente para o Suporte Técnico quando:

- Estado da Rede = "INATIVO" ou "SEM COMUNICAÇÃO"
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

## Banco de Dados

- **SQLite** — arquivo `voltmonitor.db` gerado automaticamente
- Senhas armazenadas com hash **BCrypt**
- Exclusão lógica (campo `ativo`)

---

## Dependências Mavem

| Biblioteca           | Versão  | Uso                          |
|----------------------|---------|------------------------------|
| Java-WebSocket       | 1.5.4   | Servidor WebSocket           |
| sqlite-jdbc          | 3.45.1  | Banco de dados local         |
| gson                 | 2.10.1  | Parsing JSON do ESP32        |
| jbcrypt              | 0.4     | Hash de senhas               |
| slf4j-simple         | 2.0.12  | Logging                      |

---

## Segurança

- Autenticação obrigatória via login/senha
- Senhas hasheadas com BCrypt (salt aleatório)
- Controle de acesso por função (Administrador / Monitor)
- Sessão encerrada ao fechar o sistema

---

## Cronograma

**16/03** - Definição do tema do projeto: Sistema de Monitoração de Tensão Elétrica (VoltMonitor) de fonte de 19V, simulando medição de placa fotovoltáica com ESP32 e envio de dados via Wi-Fi.

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

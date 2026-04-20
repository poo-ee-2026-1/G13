# ⚡ VoltMonitor - #G13

Discentes: 

- Emiliano Rodrigues Feliciano
- Gabriel Henrique Oliveira Silva 
- Elberty Borges de Oliveira

---

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

## Sobrescrita (Override):

    getTableCellRendererComponent em EstadoRedeRenderer e SituacaoRedeRenderer (TelaPrincipal.java)

    run() em TimerTask anônimo (MonitoramentoService.java linha 88)

    isCellEditable() sobrescrito em múltiplos DefaultTableModel anônimos
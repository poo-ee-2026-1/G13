// TelaRelatorios.java (Convertido para JPanel)
package com.monitoramento.ui;

import com.monitoramento.dao.*;
import com.monitoramento.model.*;
import com.monitoramento.service.MonitoramentoService;
import com.monitoramento.service.OrdemServicoService;
import com.monitoramento.Main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class TelaRelatorios extends JPanel {
    private Usuario usuarioLogado;
    private MonitoramentoService monitoramentoService;
    private OrdemServicoService osService;
    private ClienteDAO clienteDAO;
    private EquipamentoDAO equipamentoDAO;
    private MedicaoTensaoDAO medicaoDAO;
    private OrdemServicoDAO ordemServicoDAO;
    private UsuarioDAO usuarioDAO;
    private DepartamentoDAO departamentoDAO;
    private FuncaoDAO funcaoDAO;
    
    private JTabbedPane tabbedPane;
    private JComboBox<String> comboCliente;
    private JTextField txtDataInicio, txtDataFim;
    private JTable tabelaMedicoes;
    private DefaultTableModel tableModelMedicoes;
    
    private JLabel lblTotalMedicoes;
    private JLabel lblTensaoMedia;
    private JLabel lblTensaoMin;
    private JLabel lblTensaoMax;
    private JLabel lblAlertas;
    private JLabel lblCriticos;
    
    public TelaRelatorios(Usuario usuario, MonitoramentoService monitoramentoService) {
        this.usuarioLogado = usuario;
        this.monitoramentoService = monitoramentoService;
        this.osService = Main.getOrdemServicoService();
        this.clienteDAO = new ClienteDAO();
        this.equipamentoDAO = new EquipamentoDAO();
        this.medicaoDAO = new MedicaoTensaoDAO();
        this.ordemServicoDAO = new OrdemServicoDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.departamentoDAO = new DepartamentoDAO();
        this.funcaoDAO = new FuncaoDAO();
        
        initComponents();
        carregarClientesCombo();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Ordens de Serviço", criarPainelOrdens());
        tabbedPane.addTab("Medição de Tensão", criarPainelMedicoes());
        tabbedPane.addTab("Disponibilidade", criarPainelDisponibilidade());
        tabbedPane.addTab("Estatísticas Gerais", criarPainelEstatisticas());
        tabbedPane.addTab("Listas", criarPainelListas());
        tabbedPane.addTab("Atendimento", criarPainelDepartamento("ATENDIMENTO"));
        tabbedPane.addTab("Vendas", criarPainelDepartamento("VENDAS"));
        tabbedPane.addTab("Financeiro", criarPainelDepartamento("FINANCEIRO"));
        tabbedPane.addTab("TI", criarPainelDepartamento("TI"));
        
        JPanel painelBotoes = new JPanel(new FlowLayout());
        JButton btnExportar = new JButton("Exportar Relatório (CSV)");
        
        btnExportar.addActionListener(e -> exportarRelatorio());
        
        painelBotoes.add(btnExportar);
        
        add(tabbedPane, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);
        
        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.addActionListener(e -> {
            carregarClientesCombo();
        });
        
        toolBar.add(btnAtualizar);
        toolBar.add(Box.createHorizontalGlue());
        
        add(toolBar, BorderLayout.NORTH);
    }
    
    private JPanel criarPainelMedicoes() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel painelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros"));
        
        painelFiltros.add(new JLabel("Cliente:"));
        comboCliente = new JComboBox<>();
        comboCliente.setPreferredSize(new Dimension(250, 25));
        
        painelFiltros.add(new JLabel("Data Início:"));
        txtDataInicio = new JTextField(10);
        txtDataInicio.setToolTipText("dd/MM/yyyy");
        
        painelFiltros.add(new JLabel("Data Fim:"));
        txtDataFim = new JTextField(10);
        txtDataFim.setToolTipText("dd/MM/yyyy");
        
        JButton btnBuscar = new JButton("Buscar");
        JButton btnLimpar = new JButton("Limpar");
        
        btnBuscar.addActionListener(e -> buscarMedicoes());
        btnLimpar.addActionListener(e -> {
            txtDataInicio.setText("");
            txtDataFim.setText("");
            tableModelMedicoes.setRowCount(0);
            atualizarLabelsResumo(0, 0, 0, 0, 0, 0);
        });
        
        painelFiltros.add(comboCliente);
        painelFiltros.add(txtDataInicio);
        painelFiltros.add(txtDataFim);
        painelFiltros.add(btnBuscar);
        painelFiltros.add(btnLimpar);
        
        String[] colunas = {"ID", "Data/Hora", "Tensão (V)", "Estado da Rede", "Situação"};
        tableModelMedicoes = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaMedicoes = new JTable(tableModelMedicoes);
        tabelaMedicoes.setRowHeight(25);
        
        JScrollPane scrollTabela = new JScrollPane(tabelaMedicoes);
        
        JPanel painelResumo = new JPanel(new GridLayout(2, 3, 10, 10));
        painelResumo.setBorder(BorderFactory.createTitledBorder("Resumo"));
        
        lblTotalMedicoes = new JLabel("Total: 0");
        lblTensaoMedia = new JLabel("Média: 0 V");
        lblTensaoMin = new JLabel("Mínima: 0 V");
        lblTensaoMax = new JLabel("Máxima: 0 V");
        lblAlertas = new JLabel("Alertas: 0");
        lblCriticos = new JLabel("Críticos: 0");
        
        painelResumo.add(lblTotalMedicoes);
        painelResumo.add(lblTensaoMedia);
        painelResumo.add(lblTensaoMin);
        painelResumo.add(lblTensaoMax);
        painelResumo.add(lblAlertas);
        painelResumo.add(lblCriticos);
        
        panel.add(painelFiltros, BorderLayout.NORTH);
        panel.add(scrollTabela, BorderLayout.CENTER);
        panel.add(painelResumo, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void atualizarLabelsResumo(int total, double media, double min, double max, int alertas, int criticos) {
        if (lblTotalMedicoes != null) lblTotalMedicoes.setText("Total: " + total);
        if (lblTensaoMedia != null) lblTensaoMedia.setText(String.format("Média: %.2f V", media));
        if (lblTensaoMin != null) lblTensaoMin.setText(String.format("Mínima: %.2f V", min));
        if (lblTensaoMax != null) lblTensaoMax.setText(String.format("Máxima: %.2f V", max));
        if (lblAlertas != null) lblAlertas.setText("Alertas: " + alertas);
        if (lblCriticos != null) lblCriticos.setText("Críticos: " + criticos);
    }
    
    private void carregarClientesCombo() {
        comboCliente.removeAllItems();
        List<Cliente> clientes = clienteDAO.listarTodos();
        
        for (Cliente c : clientes) {
            comboCliente.addItem(c.getId() + " - " + c.getNomeExibicao());
        }
    }
    
    private void buscarMedicoes() {
        if (comboCliente.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente!");
            return;
        }
        
        int idCliente = Integer.parseInt(((String) comboCliente.getSelectedItem()).split(" - ")[0]);
        
        Date dataInicio;
        Date dataFim;
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
            if (txtDataInicio.getText().trim().isEmpty()) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, -30);
                dataInicio = cal.getTime();
            } else {
                dataInicio = sdf.parse(txtDataInicio.getText().trim());
            }
            
            if (txtDataFim.getText().trim().isEmpty()) {
                dataFim = new Date();
            } else {
                dataFim = sdf.parse(txtDataFim.getText().trim());
                Calendar cal = Calendar.getInstance();
                cal.setTime(dataFim);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                dataFim = cal.getTime();
            }
            
            List<MedicaoTensao> medicoes = medicaoDAO.buscarPorCliente(idCliente, dataInicio, dataFim);
            
            tableModelMedicoes.setRowCount(0);
            SimpleDateFormat sdfHora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            
            double soma = 0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            int alertas = 0;
            int criticos = 0;
            
            for (MedicaoTensao m : medicoes) {
                Object[] row = {
                    m.getId(),
                    sdfHora.format(m.getDataHora()),
                    String.format("%.2f", m.getTensao()),
                    m.getEstadoRede(),
                    m.getSituacaoRede()
                };
                tableModelMedicoes.addRow(row);
                
                soma += m.getTensao();
                if (m.getTensao() < min) min = m.getTensao();
                if (m.getTensao() > max) max = m.getTensao();
                if ("ALERTA".equals(m.getSituacaoRede())) alertas++;
                if ("CRÍTICO".equals(m.getSituacaoRede())) criticos++;
            }
            
            int total = medicoes.size();
            atualizarLabelsResumo(
                total,
                total > 0 ? soma / total : 0,
                min != Double.MAX_VALUE ? min : 0,
                max != Double.MIN_VALUE ? max : 0,
                alertas,
                criticos
            );
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar medições: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private JPanel criarPainelOrdens() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel painelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros"));
        
        JComboBox<String> comboStatus = new JComboBox<>(new String[]{"TODAS", "ABERTA", "FECHADA"});
        JComboBox<String> comboNivel = new JComboBox<>(new String[]{"TODOS", "1º Nível", "2º Nível"});
        JButton btnBuscarOS = new JButton("Buscar");
        
        painelFiltros.add(new JLabel("Status:"));
        painelFiltros.add(comboStatus);
        painelFiltros.add(new JLabel("Nível:"));
        painelFiltros.add(comboNivel);
        painelFiltros.add(btnBuscarOS);
        
        String[] colunas = {"ID", "Cliente", "Motivo", "Nível", "Status", "Abertura", "Fechamento", "Falha"};
        DefaultTableModel tableModelOS = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable tabelaOS = new JTable(tableModelOS);
        JScrollPane scrollTabela = new JScrollPane(tabelaOS);
        
        btnBuscarOS.addActionListener(e -> {
            String status = (String) comboStatus.getSelectedItem();
            String nivel = (String) comboNivel.getSelectedItem();
            
            List<OrdemServico> ordens;
            
            if ("TODAS".equals(status)) {
                ordens = ordemServicoDAO.listarTodas();
            } else {
                ordens = ordemServicoDAO.buscarPorStatus(status);
            }
            
            if (!"TODOS".equals(nivel)) {
                ordens = ordens.stream().filter(os -> os.getTipoNivel().equals(nivel)).toList();
            }
            
            tableModelOS.setRowCount(0);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            for (OrdemServico os : ordens) {
                Cliente c = clienteDAO.buscarPorId(os.getIdCliente());
                String nomeCliente = c != null ? c.getNomeExibicao() : "N/A";
                
                Object[] row = {
                    os.getId(),
                    nomeCliente,
                    os.getMotivo() != null ? (os.getMotivo().length() > 40 ? os.getMotivo().substring(0, 40) + "..." : os.getMotivo()) : "",
                    os.getTipoNivel(),
                    os.getStatus(),
                    os.getDataAbertura() != null ? sdf.format(os.getDataAbertura()) : "",
                    os.getDataFechamento() != null ? sdf.format(os.getDataFechamento()) : "",
                    os.getFalhaIdentificada() != null ? os.getFalhaIdentificada() : ""
                };
                tableModelOS.addRow(row);
            }
        });
        
        panel.add(painelFiltros, BorderLayout.NORTH);
        panel.add(scrollTabela, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel criarPainelDisponibilidade() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel painelTopo = new JPanel(new GridLayout(1, 2, 10, 10));
        
        JPanel painelClientes = new JPanel(new BorderLayout());
        painelClientes.setBorder(BorderFactory.createTitledBorder("Clientes em Monitoramento"));
        
        String[] colunasClientes = {"ID", "Cliente", "Disponibilidade", "Medições", "Ativo", "Inativo"};
        DefaultTableModel tableModelClientes = new DefaultTableModel(colunasClientes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable tabelaClientesDisp = new JTable(tableModelClientes);
        JScrollPane scrollClientes = new JScrollPane(tabelaClientesDisp);
        painelClientes.add(scrollClientes, BorderLayout.CENTER);
        
        JButton btnAtualizarDisp = new JButton("Atualizar Disponibilidade");
        
        JPanel painelGrafico = new JPanel();
        painelGrafico.setBorder(BorderFactory.createTitledBorder("Resumo Geral"));
        painelGrafico.setLayout(new BoxLayout(painelGrafico, BoxLayout.Y_AXIS));
        
        JLabel lblMediaGeral = new JLabel("Disponibilidade Média Geral: --");
        JLabel lblTotalClientes = new JLabel("Total Clientes Monitorados: --");
        JLabel lblMediaAtivo = new JLabel("Média de Medições Ativas: --");
        
        painelGrafico.add(Box.createVerticalStrut(20));
        painelGrafico.add(lblMediaGeral);
        painelGrafico.add(Box.createVerticalStrut(10));
        painelGrafico.add(lblTotalClientes);
        painelGrafico.add(Box.createVerticalStrut(10));
        painelGrafico.add(lblMediaAtivo);
        
        btnAtualizarDisp.addActionListener(e -> {
            tableModelClientes.setRowCount(0);
            var dadosList = monitoramentoService.listarTodosDadosMonitoramento();
            double somaDisp = 0;
            int totalMedicoesAtivas = 0;
            int totalMedicoes = 0;
            
            for (var dados : dadosList) {
                Cliente c = clienteDAO.buscarPorId(dados.getIdCliente());
                if (c != null) {
                    double disp = monitoramentoService.calcularDisponibilidade(dados.getIdCliente());
                    somaDisp += disp;
                    totalMedicoesAtivas += dados.getContadorAtivo();
                    totalMedicoes += dados.getContadorMedicoes();
                    
                    Object[] row = {
                        dados.getIdCliente(),
                        c.getNomeExibicao(),
                        String.format("%.2f%%", disp),
                        dados.getContadorMedicoes(),
                        dados.getContadorAtivo(),
                        dados.getContadorInativo()
                    };
                    tableModelClientes.addRow(row);
                }
            }
            
            double mediaDisp = dadosList.isEmpty() ? 0 : somaDisp / dadosList.size();
            lblMediaGeral.setText(String.format("Disponibilidade Média Geral: %.2f%%", mediaDisp));
            lblTotalClientes.setText("Total Clientes Monitorados: " + dadosList.size());
            lblMediaAtivo.setText(String.format("Média de Medições Ativas: %.1f%%", 
                totalMedicoes > 0 ? (totalMedicoesAtivas * 100.0 / totalMedicoes) : 0));
        });
        
        painelTopo.add(painelClientes);
        painelTopo.add(painelGrafico);
        
        panel.add(painelTopo, BorderLayout.CENTER);
        panel.add(btnAtualizarDisp, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel criarPainelEstatisticas() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel painelOS = new JPanel(new BorderLayout());
        painelOS.setBorder(BorderFactory.createTitledBorder("Estatísticas de OS"));
        
        JTextArea txtEstatisticasOS = new JTextArea();
        txtEstatisticasOS.setEditable(false);
        JScrollPane scrollOS = new JScrollPane(txtEstatisticasOS);
        painelOS.add(scrollOS, BorderLayout.CENTER);
        
        JPanel painelMedicoes = new JPanel(new BorderLayout());
        painelMedicoes.setBorder(BorderFactory.createTitledBorder("Estatísticas de Medições"));
        
        JTextArea txtEstatisticasMed = new JTextArea();
        txtEstatisticasMed.setEditable(false);
        JScrollPane scrollMed = new JScrollPane(txtEstatisticasMed);
        painelMedicoes.add(scrollMed, BorderLayout.CENTER);
        
        JButton btnCarregarStats = new JButton("Carregar Estatísticas");
        btnCarregarStats.addActionListener(e -> {
            List<OrdemServico> todasOS = ordemServicoDAO.listarTodas();
            List<OrdemServico> abertas = ordemServicoDAO.buscarPorStatus("ABERTA");
            List<OrdemServico> fechadas = ordemServicoDAO.buscarPorStatus("FECHADA");
            
            long totalCliente = 0;
            long totalMonitoramento = 0;
            for (OrdemServico os : fechadas) {
                if ("CLIENTE".equals(os.getFalhaIdentificada())) totalCliente++;
                if ("MONITORAMENTO".equals(os.getFalhaIdentificada())) totalMonitoramento++;
            }
            
            StringBuilder sbOS = new StringBuilder();
            sbOS.append("=== ORDENS DE SERVIÇO ===\n\n");
            sbOS.append("Total de OS: ").append(todasOS.size()).append("\n");
            sbOS.append("OS Abertas: ").append(abertas.size()).append("\n");
            sbOS.append("OS Fechadas: ").append(fechadas.size()).append("\n");
            sbOS.append("Falhas identificadas:\n");
            sbOS.append("  - Cliente: ").append(totalCliente).append("\n");
            sbOS.append("  - Monitoramento: ").append(totalMonitoramento).append("\n");
            
            double tempoMedio = ordemServicoDAO.calcularTempoMedioResolucao(
                new Date(System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000),
                new Date()
            );
            sbOS.append("\nTempo médio de resolução (90 dias): ");
            sbOS.append(String.format("%.1f horas", tempoMedio));
            
            txtEstatisticasOS.setText(sbOS.toString());
            
            List<Cliente> clientes = clienteDAO.listarTodos();
            int totalEquipamentos = equipamentoDAO.listarTodos().size();
            
            StringBuilder sbMed = new StringBuilder();
            sbMed.append("=== MEDIÇÕES ===\n\n");
            sbMed.append("Total de Clientes: ").append(clientes.size()).append("\n");
            sbMed.append("Clientes em Monitoramento: ");
            sbMed.append(clienteDAO.listarPorMonitoramento(true).size()).append("\n");
            sbMed.append("Total de Equipamentos: ").append(totalEquipamentos).append("\n");
            
            var dadosMonitorados = monitoramentoService.listarTodosDadosMonitoramento();
            int totalAtivos = 0;
            for (var dados : dadosMonitorados) {
                if ("ATIVO".equals(dados.getEstadoRedeAtual())) totalAtivos++;
            }
            sbMed.append("Clientes Ativos no momento: ").append(totalAtivos).append("\n");
            
            txtEstatisticasMed.setText(sbMed.toString());
        });
        
        JPanel painelBotoesStats = new JPanel();
        painelBotoesStats.add(btnCarregarStats);
        
        panel.add(painelOS);
        panel.add(painelMedicoes);
        panel.add(painelBotoesStats);
        
        return panel;
    }
    
    private JPanel criarPainelListas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTabbedPane listasPane = new JTabbedPane();
        
        listasPane.addTab("Usuários", criarTabelaUsuarios());
        listasPane.addTab("Clientes", criarTabelaClientes());
        listasPane.addTab("Equipamentos", criarTabelaEquipamentos());
        listasPane.addTab("Departamentos", criarTabelaDepartamentos());
        listasPane.addTab("Funções", criarTabelaFuncoes());
        
        panel.add(listasPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JScrollPane criarTabelaUsuarios() {
        String[] colunas = {"ID", "Nome Completo", "CPF", "Matrícula", "Função", "Login", "Data Cadastro"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        
        List<Usuario> usuarios = usuarioDAO.listarTodos();
        for (Usuario u : usuarios) {
            Object[] row = {
                u.getId(),
                u.getNomeCompleto(),
                u.getCpf(),
                u.getMatricula(),
                u.getFuncao(),
                u.getLogin(),
                u.getDataCadastro() != null ? sdf.format(u.getDataCadastro()) : ""
            };
            model.addRow(row);
        }
        
        return new JScrollPane(table);
    }
    
    private JScrollPane criarTabelaClientes() {
        String[] colunas = {"ID", "Tipo", "Documento", "Nome/Razão Social", "Telefone", "Monitoramento", "IP", "Data Cadastro"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        
        List<Cliente> clientes = clienteDAO.listarTodos();
        for (Cliente c : clientes) {
            String nomeExibicao = c.getTipo().equals("FISICA") ? 
                c.getNome() + " " + (c.getSobrenome() != null ? c.getSobrenome() : "") : 
                (c.getRazaoSocial() != null ? c.getRazaoSocial() : "");
            
            Object[] row = {
                c.getId(),
                c.getTipo(),
                c.getDocumento(),
                nomeExibicao,
                c.getTelefoneCompleto(),
                c.isEmMonitoramento() ? "SIM" : "NÃO",
                c.getIpCliente(),
                c.getDataCadastro() != null ? sdf.format(c.getDataCadastro()) : ""
            };
            model.addRow(row);
        }
        
        return new JScrollPane(table);
    }
    
    private JScrollPane criarTabelaEquipamentos() {
        String[] colunas = {"ID", "Marca", "Modelo", "Tensão Nominal (V)", "ID Cliente", "Cliente"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        
        List<Equipamento> equipamentos = equipamentoDAO.listarTodos();
        for (Equipamento e : equipamentos) {
            Cliente c = clienteDAO.buscarPorId(e.getIdCliente());
            String nomeCliente = c != null ? c.getNomeExibicao() : "N/A";
            
            Object[] row = {
                e.getId(),
                e.getMarca(),
                e.getModelo(),
                String.format("%.2f", e.getTensaoNominal()),
                e.getIdCliente(),
                nomeCliente
            };
            model.addRow(row);
        }
        
        return new JScrollPane(table);
    }
    
    private JScrollPane criarTabelaDepartamentos() {
        String[] colunas = {"ID", "Nome", "Descrição", "Responsável", "Telefone", "Email"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        
        List<Departamento> departamentos = departamentoDAO.listarTodos();
        for (Departamento d : departamentos) {
            Object[] row = {
                d.getId(),
                d.getNome(),
                d.getDescricao() != null ? d.getDescricao() : "",
                d.getResponsavel() != null ? d.getResponsavel() : "",
                d.getTelefone() != null ? d.getTelefone() : "",
                d.getEmail() != null ? d.getEmail() : ""
            };
            model.addRow(row);
        }
        
        return new JScrollPane(table);
    }
    
    private JScrollPane criarTabelaFuncoes() {
        String[] colunas = {"ID", "Nome", "Descrição", "Departamento"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        
        List<Funcao> funcoes = funcaoDAO.listarTodos();
        for (Funcao f : funcoes) {
            Object[] row = {
                f.getId(),
                f.getNome(),
                f.getDescricao() != null ? f.getDescricao() : "",
                f.getDepartamento() != null ? f.getDepartamento() : ""
            };
            model.addRow(row);
        }
        
        return new JScrollPane(table);
    }
    
    private JPanel criarPainelDepartamento(String nomeDepartamento) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        Departamento dept = departamentoDAO.buscarPorNome(nomeDepartamento);
        
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informações do Departamento"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(new JLabel("Departamento:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(nomeDepartamento), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        infoPanel.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(dept != null && dept.getDescricao() != null ? dept.getDescricao() : "---"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        infoPanel.add(new JLabel("Responsável:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(dept != null && dept.getResponsavel() != null ? dept.getResponsavel() : "---"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        infoPanel.add(new JLabel("Telefone:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(dept != null && dept.getTelefone() != null ? dept.getTelefone() : "---"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        infoPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(dept != null && dept.getEmail() != null ? dept.getEmail() : "---"), gbc);
        
        JPanel funcoesPanel = new JPanel(new BorderLayout());
        funcoesPanel.setBorder(BorderFactory.createTitledBorder("Funções do Departamento"));
        
        String[] colunasFuncoes = {"ID", "Função", "Descrição"};
        DefaultTableModel modelFuncoes = new DefaultTableModel(colunasFuncoes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable tabelaFuncoes = new JTable(modelFuncoes);
        
        List<Funcao> funcoes = funcaoDAO.buscarPorDepartamento(nomeDepartamento);
        for (Funcao f : funcoes) {
            Object[] row = {f.getId(), f.getNome(), f.getDescricao() != null ? f.getDescricao() : ""};
            modelFuncoes.addRow(row);
        }
        
        funcoesPanel.add(new JScrollPane(tabelaFuncoes), BorderLayout.CENTER);
        
        JButton btnEditar = new JButton("Editar Departamento");
        btnEditar.addActionListener(e -> {
            if (dept != null) {
                editarDepartamento(dept);
            } else {
                JOptionPane.showMessageDialog(this, "Departamento não encontrado!");
            }
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnEditar);
        
        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(funcoesPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void editarDepartamento(Departamento dept) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nome:"), gbc);
        JTextField txtNome = new JTextField(dept.getNome(), 20);
        txtNome.setEditable(false);
        gbc.gridx = 1;
        panel.add(txtNome, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Descrição:"), gbc);
        JTextArea txtDescricao = new JTextArea(dept.getDescricao(), 3, 20);
        txtDescricao.setLineWrap(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescricao);
        gbc.gridx = 1;
        panel.add(scrollDesc, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Responsável:"), gbc);
        JTextField txtResponsavel = new JTextField(dept.getResponsavel(), 20);
        gbc.gridx = 1;
        panel.add(txtResponsavel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Telefone:"), gbc);
        JTextField txtTelefone = new JTextField(dept.getTelefone(), 15);
        gbc.gridx = 1;
        panel.add(txtTelefone, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Email:"), gbc);
        JTextField txtEmail = new JTextField(dept.getEmail(), 25);
        gbc.gridx = 1;
        panel.add(txtEmail, gbc);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Editar Departamento: " + dept.getNome(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            dept.setDescricao(txtDescricao.getText().trim());
            dept.setResponsavel(txtResponsavel.getText().trim());
            dept.setTelefone(txtTelefone.getText().trim());
            dept.setEmail(txtEmail.getText().trim());
            
            if (departamentoDAO.atualizar(dept)) {
                JOptionPane.showMessageDialog(this, "Departamento atualizado com sucesso!");
                // Recarregar o painel
                tabbedPane.removeTabAt(tabbedPane.getSelectedIndex());
                tabbedPane.insertTab(dept.getNome(), null, criarPainelDepartamento(dept.getNome()), null, tabbedPane.getSelectedIndex());
                tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex());
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar departamento!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportarRelatorio() {
        int abaSelecionada = tabbedPane.getSelectedIndex();
        String nomeArquivo = JOptionPane.showInputDialog(this, "Nome do arquivo (sem extensão):", 
            "relatorio_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        
        if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) return;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo + ".csv"))) {
            
            if (abaSelecionada == 1) {
                writer.println("ID,Data/Hora,Tensão (V),Estado da Rede,Situação");
                for (int i = 0; i < tableModelMedicoes.getRowCount(); i++) {
                    writer.printf("%s,%s,%s,%s,%s%n",
                        tableModelMedicoes.getValueAt(i, 0),
                        tableModelMedicoes.getValueAt(i, 1),
                        tableModelMedicoes.getValueAt(i, 2),
                        tableModelMedicoes.getValueAt(i, 3),
                        tableModelMedicoes.getValueAt(i, 4));
                }
            }
            
            JOptionPane.showMessageDialog(this, "Relatório exportado com sucesso!\nArquivo: " + nomeArquivo + ".csv");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao exportar relatório: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Métodos para navegação entre abas
    public int getTabCount() {
        return tabbedPane.getTabCount();
    }
    
    public void setSelectedTab(int index) {
        if (index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }
}
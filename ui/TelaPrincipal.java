package com.monitoramento.ui;

import com.monitoramento.model.Usuario;
import com.monitoramento.service.MonitoramentoService;
import com.monitoramento.Main;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;

public class TelaPrincipal extends JFrame {
    private JTable tabelaMonitoramento;
    private DefaultTableModel tableModel;
    private JLabel lblDataHora;
    private JLabel lblStatusConexao;
    private Timer timerAtualizacao;
    private Timer timerRelogio;
    private MonitoramentoService monitoramentoService;
    private Usuario usuarioLogado;
    private boolean pausado = false;
    private Map<Integer, Double> menorTensao = new HashMap<>();
    private Map<Integer, Double> maiorTensao = new HashMap<>();
    private Map<Integer, Integer> contadorAlertas = new HashMap<>();
    private Map<Integer, Integer> contadorCriticos = new HashMap<>();
    
    public TelaPrincipal(Usuario usuario) {
        this.usuarioLogado = usuario;
        this.monitoramentoService = Main.getMonitoramentoService();
        initComponents();
        configurarMenu();
        configurarAtalhos();
        iniciarMonitoramento();
        iniciarRelogio();
    }
    
    private void initComponents() {
        setTitle("Sistema de Monitoramento de Tensão Elétrica - Usuário: " + usuarioLogado.getLogin());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Painel Superior
        JPanel painelSuperior = new JPanel(new BorderLayout());
        painelSuperior.setBackground(Color.LIGHT_GRAY);
        
        JLabel titulo = new JLabel("MONITORAMENTO DE TENSÃO ELÉTRICA - 19V", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(Color.BLUE);
        
        lblDataHora = new JLabel("", SwingConstants.RIGHT);
        lblDataHora.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        lblStatusConexao = new JLabel("● CONECTADO", SwingConstants.LEFT);
        lblStatusConexao.setFont(new Font("Arial", Font.BOLD, 12));
        lblStatusConexao.setForeground(Color.GREEN);
        
        painelSuperior.add(titulo, BorderLayout.CENTER);
        painelSuperior.add(lblDataHora, BorderLayout.EAST);
        painelSuperior.add(lblStatusConexao, BorderLayout.WEST);
        
        // Tabela
        String[] colunas = {"CLIENTE", "EQUIPAMENTO", "ESTADO DA REDE", "TENSÃO (V)", 
                           "MAIOR TENSÃO (V)", "MENOR TENSÃO (V)", "SITUAÇÃO DA REDE", 
                           "DISPONIBILIDADE"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaMonitoramento = new JTable(tableModel);
        tabelaMonitoramento.setRowHeight(30);
        tabelaMonitoramento.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Configurar renderizadores de cor para cada coluna específica
        tabelaMonitoramento.getColumnModel().getColumn(2).setCellRenderer(new EstadoRedeRenderer());
        tabelaMonitoramento.getColumnModel().getColumn(6).setCellRenderer(new SituacaoRedeRenderer());
        
        // Renderizador padrão para as outras colunas
        tabelaMonitoramento.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tabelaMonitoramento);
        
        // Painel de Botões
        JPanel painelBotoes = new JPanel();
        painelBotoes.setLayout(new GridLayout(1, 5, 10, 10));
        
        JButton btnAtualizar = new JButton("Atualizar (F5)");
        JButton btnPausar = new JButton("Pausar (F8)");
        JButton btnOS = new JButton("Ordens de Serviço (F9)");
        JButton btnRelatorios = new JButton("Relatórios");
        JButton btnSair = new JButton("Sair (ESC)");
        
        btnAtualizar.addActionListener(e -> atualizarDados());
        btnPausar.addActionListener(e -> togglePausa());
        btnOS.addActionListener(e -> abrirOrdensServico());
        btnRelatorios.addActionListener(e -> abrirRelatorios());
        btnSair.addActionListener(e -> sairSistema());
        
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnPausar);
        painelBotoes.add(btnOS);
        painelBotoes.add(btnRelatorios);
        painelBotoes.add(btnSair);
        
        // Painel inferior
        JPanel painelInferior = new JPanel();
        painelInferior.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel lblInfo = new JLabel("Atalhos: F2=Abrir OS | F4=Fechar OS | ESC=Sair | F5=Atualizar | F8=Pausar | F9=OS");
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 10));
        painelInferior.add(lblInfo);
        
        add(painelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);
        add(painelInferior, BorderLayout.PAGE_END);
        
        // Carregar clientes em monitoramento
        carregarClientesMonitorados();
    }
    
    // Renderizador para coluna "Estado da Rede"
    private class EstadoRedeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected && value != null) {
                String estado = value.toString();
                
                if ("ATIVO".equals(estado)) {
                    c.setBackground(new Color(144, 238, 144)); // Verde claro
                    c.setForeground(Color.BLACK);
                } else if ("INATIVO".equals(estado)) {
                    c.setBackground(new Color(255, 182, 193)); // Rosa claro
                    c.setForeground(Color.BLACK);
                } else if ("SEM COMUNICAÇÃO".equals(estado)) {
                    c.setBackground(new Color(139, 0, 0)); // Vermelho escuro
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
            } else if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }
            
            return c;
        }
    }
    
    // Renderizador para coluna "Situação da Rede"
    private class SituacaoRedeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected && value != null) {
                String situacao = value.toString();
                
                if (situacao.contains("NORMAL")) {
                    c.setBackground(Color.GREEN);
                    c.setForeground(Color.BLACK);
                } else if (situacao.contains("ALERTA")) {
                    c.setBackground(Color.YELLOW);
                    c.setForeground(Color.BLACK);
                } else if (situacao.contains("CRÍTICO")) {
                    c.setBackground(Color.RED);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
            } else if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }
            
            return c;
        }
    }
    
    private void configurarMenu() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu menuCadastro = new JMenu("Cadastros");
        JMenuItem menuClientes = new JMenuItem("Clientes");
        JMenuItem menuEquipamentos = new JMenuItem("Equipamentos");
        
        menuClientes.addActionListener(e -> abrirClientes());
        menuEquipamentos.addActionListener(e -> abrirEquipamentos());
        
        menuCadastro.add(menuClientes);
        menuCadastro.add(menuEquipamentos);
        
        if (usuarioLogado.getFuncao().equals("Administrador")) {
            JMenuItem menuUsuarios = new JMenuItem("Usuários");
            menuUsuarios.addActionListener(e -> abrirUsuarios());
            menuCadastro.add(menuUsuarios);
        }
        
        JMenu menuOS = new JMenu("Ordens de Serviço");
        JMenuItem menuListarOS = new JMenuItem("Listar Ordens");
        menuListarOS.addActionListener(e -> abrirOrdensServico());
        menuOS.add(menuListarOS);
        
        JMenu menuRelatorios = new JMenu("Relatórios");
        JMenuItem menuRelClientes = new JMenuItem("Relatórios Gerais");
        menuRelClientes.addActionListener(e -> abrirRelatorios());
        menuRelatorios.add(menuRelClientes);
        
        menuBar.add(menuCadastro);
        menuBar.add(menuOS);
        menuBar.add(menuRelatorios);
        
        setJMenuBar(menuBar);
    }
    
    private void configurarAtalhos() {
        getRootPane().registerKeyboardAction(e -> sairSistema(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        getRootPane().registerKeyboardAction(e -> abrirOSManual(),
            KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        getRootPane().registerKeyboardAction(e -> fecharOSManual(),
            KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        getRootPane().registerKeyboardAction(e -> atualizarDados(),
            KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        getRootPane().registerKeyboardAction(e -> togglePausa(),
            KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        getRootPane().registerKeyboardAction(e -> abrirOrdensServico(),
            KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    private void iniciarMonitoramento() {
        timerAtualizacao = new Timer();
        timerAtualizacao.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!pausado) {
                    SwingUtilities.invokeLater(() -> atualizarDados());
                }
            }
        }, 0, 500);
    }
    
    private void iniciarRelogio() {
        timerRelogio = new Timer();
        timerRelogio.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    lblDataHora.setText("Data/Hora: " + sdf.format(new Date()));
                });
            }
        }, 0, 1000);
    }
    
    private void carregarClientesMonitorados() {
        tableModel.setRowCount(0);
        
        try {
            var dadosMonitoramento = monitoramentoService.listarTodosDadosMonitoramento();
            for (var dados : dadosMonitoramento) {
                var cliente = new com.monitoramento.dao.ClienteDAO().buscarPorId(dados.getIdCliente());
                if (cliente != null) {
                    double disponibilidade = monitoramentoService.calcularDisponibilidade(dados.getIdCliente());
                    Object[] row = {
                        cliente.getNomeExibicao(),
                        dados.getEquipamento().getMarca() + " " + dados.getEquipamento().getModelo(),
                        dados.getEstadoRedeAtual() != null ? dados.getEstadoRedeAtual() : "AGUARDANDO",
                        dados.getUltimaMedicao() != null ? String.format("%.2f", dados.getUltimaMedicao()) : "---",
                        dados.getMaiorTensao() != Double.MIN_VALUE ? String.format("%.2f", dados.getMaiorTensao()) : "---",
                        dados.getMenorTensao() != Double.MAX_VALUE ? String.format("%.2f", dados.getMenorTensao()) : "---",
                        "NORMAL",
                        String.format("%.2f%%", disponibilidade)
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar clientes monitorados: " + e.getMessage());
            e.printStackTrace();
        }
        
        if (tableModel.getRowCount() == 0) {
            Object[] row = {"Nenhum cliente em monitoramento", "-", "-", "-", "-", "-", "-", "-"};
            tableModel.addRow(row);
        }
    }
    
    private void atualizarDados() {
        carregarClientesMonitorados();
    }
    
    private void tocarAlertaSonoro() {
        Toolkit.getDefaultToolkit().beep();
    }
    
    private void abrirOSAutomatica(int clienteId, String motivo) {
        System.out.println("Abrindo OS automática para cliente " + clienteId + " - Motivo: " + motivo);
    }
    
    private void abrirOSManual() {
        int row = tabelaMonitoramento.getSelectedRow();
        if (row >= 0) {
            String cliente = (String) tableModel.getValueAt(row, 0);
            String motivo = JOptionPane.showInputDialog(this, "Motivo da Ordem de Serviço:");
            if (motivo != null && !motivo.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "OS aberta para " + cliente + "\nMotivo: " + motivo);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um cliente primeiro!");
        }
    }
    
    private void fecharOSManual() {
        int row = tabelaMonitoramento.getSelectedRow();
        if (row >= 0) {
            String cliente = (String) tableModel.getValueAt(row, 0);
            String solucao = JOptionPane.showInputDialog(this, "Descrição da solução:");
            if (solucao != null && !solucao.trim().isEmpty()) {
                String[] options = {"Cliente", "Serviço de Monitoramento"};
                int falha = JOptionPane.showOptionDialog(this, "Onde foi identificada a falha?", 
                    "Identificação da Falha", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
                
                if (falha >= 0) {
                    JOptionPane.showMessageDialog(this, "OS fechada para " + cliente + 
                        "\nSolução: " + solucao + "\nFalha: " + options[falha]);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um cliente primeiro!");
        }
    }
    
    private void togglePausa() {
        pausado = !pausado;
        lblStatusConexao.setText(pausado ? "⏸ PAUSADO" : "● CONECTADO");
        lblStatusConexao.setForeground(pausado ? Color.ORANGE : Color.GREEN);
    }
    
    private void abrirOrdensServico() {
        try {
            TelaOrdensServico telaOS = new TelaOrdensServico(usuarioLogado, Main.getOrdemServicoService());
            telaOS.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir Ordens de Serviço: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void abrirRelatorios() {
        try {
            TelaRelatorios telaRel = new TelaRelatorios(usuarioLogado, monitoramentoService);
            telaRel.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir Relatórios: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void abrirClientes() {
        try {
            TelaClientes telaCli = new TelaClientes(usuarioLogado);
            telaCli.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir Clientes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void abrirEquipamentos() {
        try {
            TelaEquipamentos telaEq = new TelaEquipamentos(usuarioLogado);
            telaEq.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir Equipamentos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void abrirUsuarios() {
        try {
            TelaUsuarios telaUsu = new TelaUsuarios(usuarioLogado);
            telaUsu.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir Usuários: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void sairSistema() {
        int confirm = JOptionPane.showConfirmDialog(this, "Deseja realmente sair?", 
            "Confirmar saída", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (timerAtualizacao != null) {
                timerAtualizacao.cancel();
            }
            if (timerRelogio != null) {
                timerRelogio.cancel();
            }
            dispose();
            new TelaLogin().setVisible(true);
        }
    }
}
// TelaPrincipal.java
package com.monitoramento.ui;

import com.monitoramento.model.Usuario;
import com.monitoramento.service.MonitoramentoService;
import com.monitoramento.Main;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class TelaPrincipal extends JFrame {
    private Usuario usuarioLogado;
    private MonitoramentoService monitoramentoService;
    private JTabbedPane tabbedPane;
    
    private Map<String, JPanel> abasAbertas;
    
    public TelaPrincipal(Usuario usuario) {
        this.usuarioLogado = usuario;
        this.monitoramentoService = Main.getMonitoramentoService();
        this.abasAbertas = new HashMap<>();
        
        initComponents();
        configurarMenu();
        
        // CORREÇÃO: Maximizar a tela e centralizar
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("VOLTMONITOR - Sistema de Monitoramento de Tensão Elétrica - Usuário: " + usuarioLogado.getLogin());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // REMOVIDO setSize(1400, 750) - agora será maximizado
        setLayout(new BorderLayout());
        
        getContentPane().setBackground(new Color(240, 240, 240));
        
        JPanel painelSuperior = new JPanel(new BorderLayout());
        painelSuperior.setBackground(new Color(240, 240, 240));
        painelSuperior.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
        
        JLabel titulo = new JLabel("VOLTMONITOR - SISTEMA DE MONITORAMENTO DE TENSÃO ELÉTRICA", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(new Color(0, 0, 139));
        
        JLabel lblDataHora = new JLabel("", SwingConstants.RIGHT);
        lblDataHora.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        painelSuperior.add(titulo, BorderLayout.CENTER);
        painelSuperior.add(lblDataHora, BorderLayout.EAST);
        
        new Timer(1000, e -> {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            lblDataHora.setText("Data/Hora: " + sdf.format(new java.util.Date()));
        }).start();
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setBackground(new Color(240, 240, 240));
        
        tabbedPane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                UIManager.put("TabbedPane.focus", new Color(240, 240, 240));
                UIManager.put("TabbedPane.selected", new Color(240, 240, 240));
                UIManager.put("TabbedPane.shadow", new Color(200, 200, 200));
                UIManager.put("TabbedPane.darkShadow", new Color(180, 180, 180));
                UIManager.put("TabbedPane.highlight", new Color(255, 255, 255));
                UIManager.put("TabbedPane.lightHighlight", new Color(220, 220, 220));
            }
            
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                g.setColor(new Color(200, 200, 200));
                if (isSelected) {
                    g.drawLine(x, y + h - 1, x + w, y + h - 1);
                } else {
                    g.drawRect(x, y, w, h - 1);
                }
            }
            
            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            }
        });
        
        JPanel painelBemVindo = criarPainelBemVindo();
        tabbedPane.addTab("Início", painelBemVindo);
        
        int inicioIndex = tabbedPane.indexOfComponent(painelBemVindo);
        tabbedPane.setTabComponentAt(inicioIndex, criarTabHeader("Início", inicioIndex, false));
        
        add(painelSuperior, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        
        JPanel painelInferior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelInferior.setBackground(new Color(240, 240, 240));
        painelInferior.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));
        JLabel lblInfo = new JLabel("Sistema de Monitoramento de Tensão - Versão 1.0");
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 10));
        painelInferior.add(lblInfo);
        add(painelInferior, BorderLayout.SOUTH);
    }
    
    private JPanel criarPainelBemVindo() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel lblWelcome = new JLabel("BEM-VINDO AO VOLTMONITOR", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 28));
        lblWelcome.setForeground(new Color(0, 100, 0));
        
        JLabel lblUser = new JLabel("Usuário: " + usuarioLogado.getNomeCompleto(), SwingConstants.CENTER);
        lblUser.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JLabel lblFuncao = new JLabel("Função: " + usuarioLogado.getFuncao(), SwingConstants.CENTER);
        lblFuncao.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JLabel lblInstrucao = new JLabel("Selecione uma opção no menu superior para iniciar", SwingConstants.CENTER);
        lblInstrucao.setFont(new Font("Arial", Font.ITALIC, 14));
        lblInstrucao.setForeground(Color.GRAY);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(lblWelcome, gbc);
        gbc.gridy = 1;
        panel.add(lblUser, gbc);
        gbc.gridy = 2;
        panel.add(lblFuncao, gbc);
        gbc.gridy = 3;
        panel.add(lblInstrucao, gbc);
        
        return panel;
    }
    
    private void adicionarAba(String titulo, JPanel painel) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(titulo)) {
                tabbedPane.setSelectedIndex(i);
                return;
            }
        }
        
        tabbedPane.addTab(titulo, painel);
        int index = tabbedPane.indexOfComponent(painel);
        
        JPanel tabHeader = criarTabHeader(titulo, index, true);
        tabbedPane.setTabComponentAt(index, tabHeader);
        
        tabbedPane.setSelectedIndex(index);
    }
    
    private JPanel criarTabHeader(String titulo, int index, boolean showCloseButton) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.NONE;
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        panel.add(lblTitulo, gbc);
        
        if (showCloseButton) {
            JButton btnFechar = new JButton("X");
            btnFechar.setFont(new Font("Arial", Font.BOLD, 10));
            btnFechar.setPreferredSize(new Dimension(16, 16));
            btnFechar.setMinimumSize(new Dimension(16, 16));
            btnFechar.setMaximumSize(new Dimension(16, 16));
            btnFechar.setMargin(new Insets(0, 0, 0, 0));
            btnFechar.setFocusPainted(false);
            btnFechar.setBorderPainted(false);
            btnFechar.setContentAreaFilled(false);
            btnFechar.setOpaque(false);
            btnFechar.setToolTipText("Fechar aba");
            btnFechar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnFechar.setForeground(new Color(100, 100, 100));
            
            btnFechar.addActionListener(e -> fecharAba(index));
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 0;
            gbc.insets = new Insets(0, 5, 0, 0);
            panel.add(btnFechar, gbc);
        }
        
        return panel;
    }
    
    private void fecharAba(int index) {
        if (index >= 0 && index < tabbedPane.getTabCount()) {
            String titulo = tabbedPane.getTitleAt(index);
            if (!"Início".equals(titulo)) {
                Component component = tabbedPane.getComponentAt(index);
                if (component instanceof JPanel) {
                }
                tabbedPane.removeTabAt(index);
            }
        }
    }
    
    private void configurarMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(240, 240, 240));
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
        
        // ===== Menu Monitoração (1ª posição) =====
        JMenu menuMonitoracao = new JMenu("Monitoração");
        JMenuItem menuMonitorarClientes = new JMenuItem("Monitorar Clientes");
        menuMonitorarClientes.addActionListener(e -> abrirMonitoramento());
        menuMonitoracao.add(menuMonitorarClientes);
        menuBar.add(menuMonitoracao);
        
        // ===== Menu Atendimento (2ª posição) =====
        JMenu menuAtendimento = new JMenu("Atendimento");
        JMenuItem menuRegistrarSolicitacao = new JMenuItem("Registrar Atendimento");
        menuRegistrarSolicitacao.addActionListener(e -> abrirAtendimento());
        menuAtendimento.add(menuRegistrarSolicitacao);
        menuBar.add(menuAtendimento);
        
        // ===== Menu Suporte Técnico (3ª posição) =====
        JMenu menuSuporte = new JMenu("Suporte Técnico");
        JMenuItem menuOrdemServicoST = new JMenuItem("Ordem de Serviço (ST)");
        menuOrdemServicoST.addActionListener(e -> abrirOrdemServicoST());
        menuSuporte.add(menuOrdemServicoST);
        menuBar.add(menuSuporte);
        
        // ===== Menu TI (4ª posição) =====
        JMenu menuTI = new JMenu("TI");
        JMenuItem menuOrdemServicoTI = new JMenuItem("Ordem de Serviço (TI)");
        menuOrdemServicoTI.addActionListener(e -> abrirOrdemServicoTI());
        menuTI.add(menuOrdemServicoTI);
        menuBar.add(menuTI);
        
        // ===== Menu Vendas (5ª posição) =====
        JMenu menuVendas = new JMenu("Vendas");
        JMenuItem menuRegistrarOrcamento = new JMenuItem("Registrar Orçamento");
        JMenuItem menuOrdemInstalacao = new JMenuItem("Ordem de Instalação (ST)");
        menuRegistrarOrcamento.addActionListener(e -> abrirOrcamento());
        menuOrdemInstalacao.addActionListener(e -> abrirOrdemInstalacao());
        menuVendas.add(menuRegistrarOrcamento);
        menuVendas.add(menuOrdemInstalacao);
        menuBar.add(menuVendas);
        
        // ===== Menu Estoque (6ª posição) =====
        JMenu menuEstoque = new JMenu("Estoque");
        JMenuItem menuProdutosEstoque = new JMenuItem("Produtos");
        JMenuItem menuMovimentacoesEstoque = new JMenuItem("Movimentações");
        JMenuItem menuEstoqueBaixo = new JMenuItem("Estoque Baixo");
        
        menuProdutosEstoque.addActionListener(e -> abrirEstoque());
        menuMovimentacoesEstoque.addActionListener(e -> abrirMovimentacoesEstoque());
        menuEstoqueBaixo.addActionListener(e -> abrirEstoqueBaixo());
        
        menuEstoque.add(menuProdutosEstoque);
        menuEstoque.add(menuMovimentacoesEstoque);
        menuEstoque.addSeparator();
        menuEstoque.add(menuEstoqueBaixo);
        menuBar.add(menuEstoque);
        
        // ===== Menu Cadastros (7ª posição) =====
        JMenu menuCadastros = new JMenu("Cadastros");
        
        JMenuItem menuClientes = new JMenuItem("Clientes");
        JMenuItem menuEquipamentos = new JMenuItem("Equipamentos");
        JMenuItem menuProdutos = new JMenuItem("Produtos");
        
        JMenuItem menuDepartamentos = new JMenuItem("Departamentos");
        JMenuItem menuFuncoes = new JMenuItem("Funções");
        JMenuItem menuFuncionarios = new JMenuItem("Funcionários");
        
        menuClientes.addActionListener(e -> abrirClientes());
        menuEquipamentos.addActionListener(e -> abrirEquipamentos());
        menuProdutos.addActionListener(e -> abrirEstoque());
        
        menuDepartamentos.addActionListener(e -> abrirDepartamentos());
        menuFuncoes.addActionListener(e -> abrirFuncoes());
        menuFuncionarios.addActionListener(e -> abrirRH());
        
        menuCadastros.add(menuClientes);
        menuCadastros.add(menuEquipamentos);
        menuCadastros.add(menuProdutos);
        menuCadastros.addSeparator();
        menuCadastros.add(menuDepartamentos);
        menuCadastros.add(menuFuncoes);
        menuCadastros.add(menuFuncionarios);
        
        menuBar.add(menuCadastros);
        
        // ===== Menu Financeiro (8ª posição) =====
        JMenu menuFinanceiro = new JMenu("Financeiro");
        JMenuItem menuTransacoes = new JMenuItem("Transações");
        JMenuItem menuContasPagar = new JMenuItem("Contas à Pagar");
        JMenuItem menuContasReceber = new JMenuItem("Contas à Receber");
        JMenuItem menuResumoFinanceiro = new JMenuItem("Resumo Financeiro");
        JMenuItem menuNovaTransacao = new JMenuItem("Nova Transação");
        
        menuTransacoes.addActionListener(e -> abrirFinanceiro(0));
        menuContasPagar.addActionListener(e -> abrirFinanceiro(1));
        menuContasReceber.addActionListener(e -> abrirFinanceiro(2));
        menuResumoFinanceiro.addActionListener(e -> abrirFinanceiro(3));
        menuNovaTransacao.addActionListener(e -> abrirFinanceiro(4));
        
        menuFinanceiro.add(menuTransacoes);
        menuFinanceiro.add(menuContasPagar);
        menuFinanceiro.add(menuContasReceber);
        menuFinanceiro.add(menuResumoFinanceiro);
        menuFinanceiro.add(menuNovaTransacao);
        menuBar.add(menuFinanceiro);
        
        // ===== Menu Relatórios (9ª posição) =====
        JMenu menuRelatorios = new JMenu("Relatórios");
        
        JMenuItem menuRelOrdensServico = new JMenuItem("Ordens de Serviço");
        JMenuItem menuRelMedicaoTensao = new JMenuItem("Medição de Tensão");
        JMenuItem menuRelDisponibilidade = new JMenuItem("Disponibilidade");
        JMenuItem menuRelEstatisticas = new JMenuItem("Estatísticas Gerais");
        JMenuItem menuRelListas = new JMenuItem("Listas");
        JMenuItem menuRelAtendimento = new JMenuItem("Atendimento");
        JMenuItem menuRelVendas = new JMenuItem("Vendas");
        JMenuItem menuRelFinanceiro = new JMenuItem("Financeiro");
        JMenuItem menuRelTI = new JMenuItem("TI");
        
        menuRelOrdensServico.addActionListener(e -> abrirRelatorio(0));
        menuRelMedicaoTensao.addActionListener(e -> abrirRelatorio(1));
        menuRelDisponibilidade.addActionListener(e -> abrirRelatorio(2));
        menuRelEstatisticas.addActionListener(e -> abrirRelatorio(3));
        menuRelListas.addActionListener(e -> abrirRelatorio(4));
        menuRelAtendimento.addActionListener(e -> abrirRelatorio(5));
        menuRelVendas.addActionListener(e -> abrirRelatorio(6));
        menuRelFinanceiro.addActionListener(e -> abrirRelatorio(7));
        menuRelTI.addActionListener(e -> abrirRelatorio(8));
        
        menuRelatorios.add(menuRelOrdensServico);
        menuRelatorios.add(menuRelMedicaoTensao);
        menuRelatorios.add(menuRelDisponibilidade);
        menuRelatorios.add(menuRelEstatisticas);
        menuRelatorios.add(menuRelListas);
        menuRelatorios.addSeparator();
        menuRelatorios.add(menuRelAtendimento);
        menuRelatorios.add(menuRelVendas);
        menuRelatorios.add(menuRelFinanceiro);
        menuRelatorios.add(menuRelTI);
        menuBar.add(menuRelatorios);
        
        // ===== Menu Sistema (10ª posição) =====
        JMenu menuSistema = new JMenu("Sistema");
        JMenuItem menuLogout = new JMenuItem("Logout");
        JMenuItem menuSairSistema = new JMenuItem("Sair");
        menuLogout.addActionListener(e -> logout());
        menuSairSistema.addActionListener(e -> System.exit(0));
        menuSistema.add(menuLogout);
        menuSistema.addSeparator();
        menuSistema.add(menuSairSistema);
        menuBar.add(menuSistema);
        
        setJMenuBar(menuBar);
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Deseja realmente sair do sistema?", 
            "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new TelaLogin().setVisible(true);
            dispose();
        }
    }
    
    // ===== MÉTODOS PARA ABRIR TELAS =====
    
    private void abrirMonitoramento() {
        TelaMonitoramento painel = new TelaMonitoramento(usuarioLogado);
        adicionarAba("Monitorar Clientes", painel);
    }
    
    private void abrirAtendimento() {
        TelaAtendimento painel = new TelaAtendimento(usuarioLogado);
        adicionarAba("Registrar Atendimento", painel);
    }
    
    private void abrirOrdemServicoST() {
        TelaOrdensServico painel = new TelaOrdensServico(usuarioLogado, Main.getOrdemServicoService());
        adicionarAba("Ordem de Serviço (ST)", painel);
    }
    
    private void abrirOrdemServicoTI() {
        TelaTI painel = new TelaTI(usuarioLogado);
        adicionarAba("Ordem de Serviço (TI)", painel);
    }
    
    private void abrirOrcamento() {
        TelaOrcamento painel = new TelaOrcamento(usuarioLogado);
        adicionarAba("Registrar Orçamento", painel);
    }
    
    private void abrirOrdemInstalacao() {
        TelaOrdemInstalacao painel = new TelaOrdemInstalacao(usuarioLogado);
        adicionarAba("Ordem de Instalação (ST)", painel);
    }
    
    private void abrirClientes() {
        TelaClientes painel = new TelaClientes(usuarioLogado);
        adicionarAba("Clientes", painel);
    }
    
    private void abrirEquipamentos() {
        TelaEquipamentos painel = new TelaEquipamentos(usuarioLogado);
        adicionarAba("Equipamentos", painel);
    }
    
    private void abrirDepartamentos() {
        TelaDepartamentos painel = new TelaDepartamentos(usuarioLogado);
        adicionarAba("Departamentos", painel);
    }
    
    private void abrirFuncoes() {
        TelaFuncoes painel = new TelaFuncoes(usuarioLogado);
        adicionarAba("Funções", painel);
    }
    
    private void abrirRH() {
        TelaRH painel = new TelaRH(usuarioLogado);
        adicionarAba("Funcionários", painel);
    }
    
    private void abrirEstoque() {
        TelaEstoque painel = new TelaEstoque(usuarioLogado);
        adicionarAba("Produtos", painel);
    }
    
    private void abrirMovimentacoesEstoque() {
        TelaMovimentacoesEstoque painel = new TelaMovimentacoesEstoque(usuarioLogado);
        adicionarAba("Movimentações de Estoque", painel);
    }
    
    private void abrirEstoqueBaixo() {
        TelaEstoqueBaixo painel = new TelaEstoqueBaixo(usuarioLogado);
        adicionarAba("Estoque Baixo", painel);
    }
    
    private void abrirFinanceiro(int aba) {
        TelaFinanceiro painel = new TelaFinanceiro(usuarioLogado);
        if (aba >= 0 && aba < painel.getTabCount()) {
            painel.setSelectedTab(aba);
        }
        adicionarAba("Financeiro", painel);
    }
    
    private void abrirRelatorio(int aba) {
        TelaRelatorios painel = new TelaRelatorios(usuarioLogado, monitoramentoService);
        if (aba >= 0 && aba < painel.getTabCount()) {
            painel.setSelectedTab(aba);
        }
        adicionarAba("Relatórios", painel);
    }
}
package com.voltmonitor.ui;

import com.voltmonitor.model.*;
import com.voltmonitor.repository.DatabaseManager;
import com.voltmonitor.service.MonitoramentoService;
import com.voltmonitor.websocket.VoltWebSocketServer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

public class TelaPrincipal extends JFrame {

    private final Usuario usuarioLogado;
    private final MonitoramentoService monitoramentoService;
    private VoltWebSocketServer wsServer;

    // UI Components
    private JTable tabelaMonitoracao;
    private DefaultTableModel modeloTabela;
    private JLabel lblDataHora;
    private JLabel lblStatusServer;
    private JLabel lblUsuario;
    private JLabel lblPausado;

    // Colunas da tabela
    private static final String[] COLUNAS = {
        "CLIENTE", "EQUIPAMENTO", "ESTADO DA REDE",
        "TENSÃO (V)", "MAIOR TENSÃO (V)", "MENOR TENSÃO (V)", "SITUAÇÃO DA REDE"
    };

    private static final Color COR_BG = new Color(15, 23, 35);
    private static final Color COR_HEADER = new Color(20, 30, 45);
    private static final Color COR_ROW1 = new Color(25, 35, 52);
    private static final Color COR_ROW2 = new Color(20, 28, 42);
    private static final Color COR_ATIVO = new Color(0, 210, 120);
    private static final Color COR_INATIVO = new Color(255, 120, 100);
    private static final Color COR_SEM_COM = new Color(180, 30, 30);
    private static final Color COR_NORMAL = new Color(0, 200, 100);
    private static final Color COR_ALERTA = new Color(255, 200, 0);
    private static final Color COR_CRITICO = new Color(220, 30, 30);
    private static final Color COR_TEXTO = new Color(200, 215, 230);

    private Timer timerRelogio;
    private AtomicBoolean pausado = new AtomicBoolean(false);

    public TelaPrincipal(Usuario usuario) {
        this.usuarioLogado = usuario;
        this.monitoramentoService = new MonitoramentoService();
        inicializarUI();
        inicializarServicos();
        configurarAtalhos();
    }

    private void inicializarUI() {
        setTitle("VoltMonitor ⚡ - Sistema de Monitoração de Tensão Elétrica");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        getContentPane().setBackground(COR_BG);
        setLayout(new BorderLayout(0, 0));

        add(criarBarraTopo(), BorderLayout.NORTH);
        add(criarPainelCentral(), BorderLayout.CENTER);
        add(criarBarraStatus(), BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { sair(); }
        });
    }

    private JPanel criarBarraTopo() {
        JPanel painel = new JPanel(new BorderLayout(10, 0));
        painel.setBackground(COR_HEADER);
        painel.setBorder(new EmptyBorder(8, 15, 8, 15));

        // Título
        JLabel titulo = new JLabel("⚡  VOLTMONITOR");
        titulo.setFont(new Font("Monospaced", Font.BOLD, 18));
        titulo.setForeground(new Color(0, 210, 150));

        // Info usuário
        lblUsuario = new JLabel("👤 " + usuarioLogado.getNomeCompleto() + " [" + usuarioLogado.getFuncao() + "]");
        lblUsuario.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblUsuario.setForeground(new Color(150, 180, 200));

        // Data/hora
        lblDataHora = new JLabel();
        lblDataHora.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblDataHora.setForeground(new Color(100, 200, 255));

        // Menu
        JMenuBar menuBar = criarMenuBar();

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        direita.setOpaque(false);
        direita.add(lblUsuario);
        direita.add(lblDataHora);

        painel.add(titulo, BorderLayout.WEST);
        painel.add(menuBar, BorderLayout.CENTER);
        painel.add(direita, BorderLayout.EAST);

        return painel;
    }

    private JMenuBar criarMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(COR_HEADER);
        menuBar.setBorder(BorderFactory.createEmptyBorder());

        // Cadastros (somente Admin)
        if (usuarioLogado.isAdministrador()) {
            JMenu menuCad = criarMenu("⚙ CADASTROS");
            menuCad.add(criarMenuItem("👥 Usuários", () -> new TelaUsuarios(this).setVisible(true)));
            menuCad.add(criarMenuItem("🏢 Clientes", () -> new TelaClientes(this, monitoramentoService).setVisible(true)));
            menuCad.add(criarMenuItem("🔧 Equipamentos", () -> new TelaEquipamentos(this).setVisible(true)));
            menuCad.add(criarMenuItem("🏭 Departamentos", () -> new TelaDepartamentos(this).setVisible(true)));
            menuBar.add(menuCad);
        }

        JMenu menuOS = criarMenu("📋 ORDENS DE SERVIÇO");
        menuOS.add(criarMenuItem("📄 Visualizar OS", () -> new TelaOrdensServico(this).setVisible(true)));
        menuBar.add(menuOS);

        return menuBar;
    }

    private JMenu criarMenu(String texto) {
        JMenu menu = new JMenu(texto);
        menu.setForeground(new Color(180, 200, 220));
        menu.setFont(new Font("SansSerif", Font.BOLD, 11));
        return menu;
    }

    private JMenuItem criarMenuItem(String texto, Runnable acao) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(e -> acao.run());
        return item;
    }

    private JPanel criarPainelCentral() {
        JPanel painel = new JPanel(new BorderLayout(0, 5));
        painel.setBackground(COR_BG);
        painel.setBorder(new EmptyBorder(5, 10, 5, 10));

        // Título da grade
        JLabel tituloGrade = new JLabel("  📊 PAINEL DE MONITORAÇÃO DE TENSÃO ELÉTRICA  |  " +
            (pausado.get() ? "⏸ PAUSADO" : "▶ EM TEMPO REAL"));
        tituloGrade.setFont(new Font("Monospaced", Font.BOLD, 13));
        tituloGrade.setForeground(new Color(0, 200, 150));
        tituloGrade.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Tabela
        modeloTabela = new DefaultTableModel(COLUNAS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tabelaMonitoracao = new JTable(modeloTabela);
        configurarTabela();

        JScrollPane scroll = new JScrollPane(tabelaMonitoracao);
        scroll.setBackground(COR_BG);
        scroll.getViewport().setBackground(COR_BG);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(40, 60, 90), 1));

        painel.add(tituloGrade, BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);

        return painel;
    }

    private void configurarTabela() {
        tabelaMonitoracao.setBackground(COR_ROW1);
        tabelaMonitoracao.setForeground(COR_TEXTO);
        tabelaMonitoracao.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tabelaMonitoracao.setRowHeight(38);
        tabelaMonitoracao.setGridColor(new Color(40, 55, 75));
        tabelaMonitoracao.setSelectionBackground(new Color(0, 80, 120));
        tabelaMonitoracao.setIntercellSpacing(new Dimension(1, 1));
        tabelaMonitoracao.setShowGrid(true);

        // Header
        JTableHeader header = tabelaMonitoracao.getTableHeader();
        header.setBackground(new Color(10, 20, 38));
        header.setForeground(new Color(0, 200, 150));
        header.setFont(new Font("Monospaced", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

        // Larguras de colunas
        int[] larguras = {200, 150, 140, 110, 130, 130, 130};
        for (int i = 0; i < larguras.length; i++) {
            tabelaMonitoracao.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }

        // Renderer colorido
        tabelaMonitoracao.setDefaultRenderer(Object.class, new MonitorTableCellRenderer());
    }

    private JPanel criarBarraStatus() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(new Color(10, 18, 30));
        painel.setBorder(new EmptyBorder(5, 10, 5, 10));

        // Atalhos
        JLabel atalhos = new JLabel(
            "  [ESC] Sair    [F5] Atualizar    [F8] Pausar/Retomar    |  " +
            "🟢 ATIVO   🔴 INATIVO   ⛔ SEM COMUNICAÇÃO"
        );
        atalhos.setFont(new Font("SansSerif", Font.PLAIN, 11));
        atalhos.setForeground(new Color(100, 130, 160));

        lblStatusServer = new JLabel("● Servidor WebSocket: Iniciando...");
        lblStatusServer.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblStatusServer.setForeground(new Color(200, 150, 50));

        lblPausado = new JLabel("");
        lblPausado.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblPausado.setForeground(COR_ALERTA);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        direita.setOpaque(false);
        direita.add(lblPausado);
        direita.add(lblStatusServer);

        painel.add(atalhos, BorderLayout.WEST);
        painel.add(direita, BorderLayout.EAST);

        return painel;
    }

    private void configurarAtalhos() {
        // ESC - Sair
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "sair");
        getRootPane().getActionMap().put("sair", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { sair(); }
        });

        // F5 - Atualizar
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "atualizar");
        getRootPane().getActionMap().put("atualizar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { atualizarTabela(); }
        });

        // F8 - Pausar/Retomar
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "pausar");
        getRootPane().getActionMap().put("pausar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { alternarPausa(); }
        });
    }

    private void inicializarServicos() {
        // Iniciar serviço de monitoramento
        monitoramentoService.setAtualizacaoListener(v -> SwingUtilities.invokeLater(this::atualizarTabela));
        monitoramentoService.iniciar();

        // Iniciar WebSocket
        wsServer = new VoltWebSocketServer(monitoramentoService);
        wsServer.start();
        SwingUtilities.invokeLater(() -> {
            lblStatusServer.setText("● Servidor WebSocket: Porta " + wsServer.getPorta());
            lblStatusServer.setForeground(COR_ATIVO);
        });

        // Relógio
        timerRelogio = new Timer(true);
        timerRelogio.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                String hora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
                SwingUtilities.invokeLater(() -> lblDataHora.setText("📅 " + hora));
            }
        }, 0, 1000);
    }

    private void atualizarTabela() {
        if (pausado.get()) return;

        Map<String, MedicaoTensao> medicoes = monitoramentoService.getMedicoes();
        Map<String, Cliente> clientesPorIp = monitoramentoService.getClientesPorIp();
        Map<Integer, Equipamento> equipamentos = monitoramentoService.getEquipamentos();

        modeloTabela.setRowCount(0);

        List<Map.Entry<String, Cliente>> clientesOrdenados = new ArrayList<>(clientesPorIp.entrySet());
        clientesOrdenados.sort(Comparator.comparing(e -> e.getValue().getNomeExibicao()));

        for (Map.Entry<String, Cliente> entry : clientesOrdenados) {
            String ip = entry.getKey();
            Cliente cliente = entry.getValue();
            MedicaoTensao med = medicoes.getOrDefault(ip, new MedicaoTensao());
            Equipamento eq = equipamentos.get(cliente.getId());

            String nomeCliente = cliente.getNomeExibicao();
            String equipamento = eq != null ? eq.getModelo() : "N/D";
            String estadoRede = med.getEstadoRede() != null ? med.getEstadoRede().name() : "SEM_COMUNICACAO";
            String tensao = med.getUltimaRecepcao() != null ? String.format("%.2f", med.getTensao()) : "--";
            String maiorT = !med.isOsAberta() && med.getMaiorTensao() == 0 ? "--" :
                            String.format("%.2f", med.getMaiorTensao());
            String menorT = !med.isOsAberta() && med.getMenorTensao() == 0 ? "--" :
                            String.format("%.2f", med.getMenorTensao());
            String situacao = med.getSituacaoRede() != null ? med.getSituacaoRede().name() : "INDEFINIDO";

            modeloTabela.addRow(new Object[]{
                nomeCliente, equipamento, estadoRede, tensao, maiorT, menorT, situacao
            });

            // Alerta sonoro para CRÍTICO
            if ("CRITICO".equals(situacao) || "CRITICO_EXCEDIDO".equals(situacao)) {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    private void alternarPausa() {
        pausado.set(!pausado.get());
        monitoramentoService.setPausado(pausado.get());

        if (pausado.get()) {
            lblPausado.setText("⏸ MONITORAÇÃO PAUSADA");
        } else {
            lblPausado.setText("");
            atualizarTabela();
        }
    }

    private void sair() {
        int opt = JOptionPane.showConfirmDialog(this,
            "Deseja realmente sair do VoltMonitor?", "Confirmar Saída",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            monitoramentoService.parar();
            if (timerRelogio != null) timerRelogio.cancel();
            try {
                if (wsServer != null) wsServer.stop();
            } catch (Exception ignored) {}
            System.exit(0);
        }
    }

    // ======================================================
    // Renderer personalizado para a tabela
    // ======================================================
    private class MonitorTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

            label.setHorizontalAlignment(column >= 2 ? SwingConstants.CENTER : SwingConstants.LEFT);
            label.setBorder(new EmptyBorder(0, 8, 0, 8));

            // Cor de fundo alternada
            Color bgBase = (row % 2 == 0) ? COR_ROW1 : COR_ROW2;

            String texto = value != null ? value.toString() : "";
            Color fgColor = COR_TEXTO;

            // Colorização por coluna
            switch (column) {
                case 2 -> { // ESTADO DA REDE
                    switch (texto) {
                        case "ATIVO" -> { fgColor = COR_ATIVO; label.setText("● ATIVO"); }
                        case "INATIVO" -> { fgColor = COR_INATIVO; label.setText("● INATIVO"); }
                        case "SEM_COMUNICACAO" -> { fgColor = new Color(255, 255, 255); bgBase = COR_SEM_COM; label.setText("⛔ SEM COMUNICAÇÃO"); }
                    }
                }
                case 6 -> { // SITUAÇÃO DA REDE
                    switch (texto) {
                        case "NORMAL" -> { fgColor = COR_NORMAL; label.setText("✔ NORMAL"); }
                        case "ALERTA" -> { fgColor = COR_ALERTA; label.setText("⚠ ALERTA"); }
                        case "CRITICO" -> { fgColor = COR_CRITICO; label.setText("✖ CRÍTICO"); bgBase = new Color(60, 10, 10); }
                        case "INDEFINIDO" -> { fgColor = new Color(120, 140, 160); label.setText("— —"); }
                    }
                }
                case 3, 4, 5 -> { // Tensões
                    label.setFont(new Font("Monospaced", Font.BOLD, 14));
                }
            }

            if (isSelected) {
                label.setBackground(new Color(0, 80, 120));
            } else {
                label.setBackground(bgBase);
            }
            label.setForeground(fgColor);
            label.setOpaque(true);

            return label;
        }
    }
}

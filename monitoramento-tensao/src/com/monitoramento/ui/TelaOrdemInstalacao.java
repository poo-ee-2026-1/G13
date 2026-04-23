// TelaOrdemInstalacao.java
package com.monitoramento.ui;

import com.monitoramento.model.OrdemServico;
import com.monitoramento.model.Cliente;
import com.monitoramento.model.Usuario;
import com.monitoramento.model.ProdutoEstoque;
import com.monitoramento.model.InstalacaoProduto;
import com.monitoramento.model.TransacaoFinanceira;
import com.monitoramento.model.ParcelaMonitoramento;
import com.monitoramento.dao.OrdemServicoDAO;
import com.monitoramento.dao.ClienteDAO;
import com.monitoramento.dao.ProdutoEstoqueDAO;
import com.monitoramento.dao.InstalacaoProdutoDAO;
import com.monitoramento.dao.TransacaoFinanceiraDAO;
import com.monitoramento.dao.ParcelaMonitoramentoDAO;
import com.monitoramento.dao.EquipamentoDAO;
import com.monitoramento.service.OrdemServicoService;
import com.monitoramento.Main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TelaOrdemInstalacao extends JPanel {
    private OrdemServicoDAO ordemServicoDAO;
    private ClienteDAO clienteDAO;
    private ProdutoEstoqueDAO produtoDAO;
    private InstalacaoProdutoDAO instalacaoProdutoDAO;
    private TransacaoFinanceiraDAO transacaoDAO;
    private ParcelaMonitoramentoDAO parcelaDAO;
    private EquipamentoDAO equipamentoDAO;
    private OrdemServicoService osService;
    private Usuario usuarioLogado;
    
    private JTable tabelaOrdensInstalacao;
    private DefaultTableModel modelOrdensInstalacao;
    private JComboBox<String> comboStatusInstalacao, comboNivelInstalacao, comboTipoOsInstalacao, comboClienteInstalacao;
    private JButton btnAbrirInstalacao, btnConcluirInstalacao, btnAtualizarInstalacoes;
    private JPanel painelDetalhes;
    
    private JComboBox<String> comboClienteNovo;
    private JTextArea txtDescricaoInstalacao, txtObservacoesInstalacao;
    private JTextField txtEnderecoInstalacao;
    private JSpinner spDataAgendamento;
    private JTable tabelaProdutosSelecionados;
    private DefaultTableModel modelProdutosSelecionados;
    private List<ProdutoSelecionado> produtosSelecionados;
    private JButton btnSelecionarProdutos;
    private JLabel lblValorTotalInstalacao;
    
    private JLabel lblTotalAbertas, lblTotalFechadas, lblValorTotalInstalacoes, lblProdutosUtilizados;
    private javax.swing.Timer timerAtualizacao;
    
    private static final String[] NIVEL_OPCOES = {"TODOS", "1º Nível", "2º Nível", "3º Nível"};
    private static final String[] STATUS_OPCOES = {"TODAS", "ABERTA", "EM_ATENDIMENTO", "FECHADA"};
    private static final String[] TIPOS_OS = {"TODOS", "INSTALAÇÃO", "REPARO", "INFORMAÇÃO", "ORÇAMENTO"};
    
    private DefaultTableCellRenderer centerRenderer;
    
    public TelaOrdemInstalacao(Usuario usuario) {
        this.usuarioLogado = usuario;
        this.ordemServicoDAO = new OrdemServicoDAO();
        this.clienteDAO = new ClienteDAO();
        this.produtoDAO = new ProdutoEstoqueDAO();
        this.instalacaoProdutoDAO = new InstalacaoProdutoDAO();
        this.transacaoDAO = new TransacaoFinanceiraDAO();
        this.parcelaDAO = new ParcelaMonitoramentoDAO();
        this.equipamentoDAO = new EquipamentoDAO();
        this.osService = Main.getOrdemServicoService();
        this.produtosSelecionados = new ArrayList<>();
        
        centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        initComponents();
        carregarOrdensInstalacao();
        iniciarTimerAtualizacao();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        JPanel painelStatus = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelStatus.setBackground(new Color(240, 248, 255));
        painelStatus.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel lblStatusInfo = new JLabel("Sistema de Ordens de Instalação | Usuário: " + usuarioLogado.getNomeCompleto());
        lblStatusInfo.setFont(new Font("Arial", Font.BOLD, 12));
        lblStatusInfo.setForeground(new Color(0, 100, 0));
        painelStatus.add(lblStatusInfo);
        
        JPanel painelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros"));
        
        painelFiltros.add(new JLabel("Status:"));
        comboStatusInstalacao = new JComboBox<>(STATUS_OPCOES);
        painelFiltros.add(comboStatusInstalacao);
        
        painelFiltros.add(new JLabel("Nível:"));
        comboNivelInstalacao = new JComboBox<>(NIVEL_OPCOES);
        painelFiltros.add(comboNivelInstalacao);
        
        painelFiltros.add(new JLabel("Tipo:"));
        comboTipoOsInstalacao = new JComboBox<>(TIPOS_OS);
        painelFiltros.add(comboTipoOsInstalacao);
        
        painelFiltros.add(new JLabel("Cliente:"));
        comboClienteInstalacao = new JComboBox<>();
        comboClienteInstalacao.setPreferredSize(new Dimension(250, 25));
        carregarClientesCombo(comboClienteInstalacao);
        painelFiltros.add(comboClienteInstalacao);
        
        JButton btnFiltrar = new JButton("Filtrar");
        JButton btnLimparFiltro = new JButton("Limpar Filtros");
        
        btnFiltrar.addActionListener(e -> filtrarOrdensInstalacao());
        btnLimparFiltro.addActionListener(e -> {
            comboStatusInstalacao.setSelectedIndex(0);
            comboNivelInstalacao.setSelectedIndex(0);
            comboTipoOsInstalacao.setSelectedIndex(0);
            comboClienteInstalacao.setSelectedIndex(0);
            carregarOrdensInstalacao();
        });
        
        painelFiltros.add(btnFiltrar);
        painelFiltros.add(btnLimparFiltro);
        
        String[] colunas = {"ID", "Cliente", "Tipo OS", "Nível", "Status", "Data Abertura", "Valor Total (R$)", "Data Agendamento", "Endereço"};
        modelOrdensInstalacao = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaOrdensInstalacao = new JTable(modelOrdensInstalacao);
        tabelaOrdensInstalacao.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaOrdensInstalacao.setRowHeight(25);
        
        for (int i = 0; i < tabelaOrdensInstalacao.getColumnCount(); i++) {
            tabelaOrdensInstalacao.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        tabelaOrdensInstalacao.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabelaOrdensInstalacao.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabelaOrdensInstalacao.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabelaOrdensInstalacao.getColumnModel().getColumn(3).setPreferredWidth(80);
        tabelaOrdensInstalacao.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabelaOrdensInstalacao.getColumnModel().getColumn(5).setPreferredWidth(130);
        tabelaOrdensInstalacao.getColumnModel().getColumn(6).setPreferredWidth(100);
        tabelaOrdensInstalacao.getColumnModel().getColumn(7).setPreferredWidth(100);
        tabelaOrdensInstalacao.getColumnModel().getColumn(8).setPreferredWidth(200);
        
        tabelaOrdensInstalacao.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                atualizarBotoes();
            }
        });
        
        JScrollPane scrollTabela = new JScrollPane(tabelaOrdensInstalacao);
        
        JPanel painelBotoes = new JPanel(new FlowLayout());
        painelBotoes.setBorder(BorderFactory.createTitledBorder("Ações"));
        
        btnAbrirInstalacao = new JButton("Abrir Ordem de Instalação");
        btnConcluirInstalacao = new JButton("Concluir Instalação");
        btnAtualizarInstalacoes = new JButton("Atualizar");
        
        btnAbrirInstalacao.setBackground(new Color(60, 120, 60));
        btnAbrirInstalacao.setForeground(Color.WHITE);
        btnAbrirInstalacao.setFont(new Font("Arial", Font.BOLD, 12));
        
        btnConcluirInstalacao.setBackground(new Color(180, 60, 60));
        btnConcluirInstalacao.setForeground(Color.WHITE);
        btnConcluirInstalacao.setFont(new Font("Arial", Font.BOLD, 12));
        
        btnAtualizarInstalacoes.setBackground(new Color(70, 130, 180));
        btnAtualizarInstalacoes.setForeground(Color.WHITE);
        btnAtualizarInstalacoes.setFont(new Font("Arial", Font.BOLD, 12));
        
        btnAbrirInstalacao.addActionListener(e -> mostrarDialogNovaInstalacao());
        btnConcluirInstalacao.addActionListener(e -> concluirInstalacao());
        btnAtualizarInstalacoes.addActionListener(e -> carregarOrdensInstalacao());
        
        painelBotoes.add(btnAbrirInstalacao);
        painelBotoes.add(btnConcluirInstalacao);
        painelBotoes.add(btnAtualizarInstalacoes);
        
        painelDetalhes = new JPanel(new BorderLayout());
        painelDetalhes.setBorder(BorderFactory.createTitledBorder("Detalhes da Instalação"));
        
        JLabel lblInfoSelecao = new JLabel("Selecione uma ordem de instalação na tabela para visualizar seus detalhes.", SwingConstants.CENTER);
        lblInfoSelecao.setFont(new Font("Arial", Font.ITALIC, 12));
        lblInfoSelecao.setForeground(Color.GRAY);
        painelDetalhes.add(lblInfoSelecao, BorderLayout.CENTER);
        
        JPanel painelStats = criarPainelEstatisticas();
        
        JPanel painelInstrucoes = new JPanel();
        painelInstrucoes.setBackground(new Color(240, 248, 255));
        painelInstrucoes.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel lblInstrucoes = new JLabel("Instruções: Clique em 'Abrir Ordem de Instalação' para criar uma nova OS de instalação.");
        lblInstrucoes.setFont(new Font("Arial", Font.PLAIN, 11));
        lblInstrucoes.setForeground(new Color(100, 100, 100));
        painelInstrucoes.add(lblInstrucoes);
        
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(new Color(240, 240, 240));
        
        JButton btnRefresh = new JButton("Atualizar Tudo");
        btnRefresh.addActionListener(e -> carregarOrdensInstalacao());
        
        toolBar.add(btnRefresh);
        toolBar.add(Box.createHorizontalGlue());
        
        JPanel painelNorte = new JPanel(new BorderLayout());
        painelNorte.add(toolBar, BorderLayout.NORTH);
        painelNorte.add(painelStatus, BorderLayout.CENTER);
        painelNorte.add(painelFiltros, BorderLayout.SOUTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        splitPane.setTopComponent(scrollTabela);
        splitPane.setBottomComponent(painelDetalhes);
        
        JPanel painelCentral = new JPanel(new BorderLayout());
        painelCentral.add(splitPane, BorderLayout.CENTER);
        painelCentral.add(painelStats, BorderLayout.EAST);
        
        JPanel painelSul = new JPanel(new BorderLayout());
        painelSul.add(painelBotoes, BorderLayout.NORTH);
        painelSul.add(painelInstrucoes, BorderLayout.SOUTH);
        
        add(painelNorte, BorderLayout.NORTH);
        add(painelCentral, BorderLayout.CENTER);
        add(painelSul, BorderLayout.SOUTH);
    }
    
    private void carregarClientesCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        combo.addItem("TODOS");
        List<Cliente> clientes = clienteDAO.listarTodos();
        for (Cliente c : clientes) {
            combo.addItem(c.getId() + " - " + c.getNomeExibicao());
        }
    }
    
    private void carregarOrdensInstalacao() {
        modelOrdensInstalacao.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        
        List<OrdemServico> ordensInstalacao = ordemServicoDAO.listarOsInstalacao();
        
        for (OrdemServico os : ordensInstalacao) {
            Cliente cliente = clienteDAO.buscarPorId(os.getIdCliente());
            String nomeCliente = cliente != null ? cliente.getNomeExibicao() : "N/A";
            
            String tipoOs = "INSTALAÇÃO";
            String statusExibicao = formatarStatus(os.getStatus());
            String dataAgendamentoStr = os.getDataAgendamentoFormatada();
            
            Object[] row = {
                os.getId(),
                nomeCliente,
                tipoOs,
                os.getTipoNivel() != null ? os.getTipoNivel() : "1º Nível",
                statusExibicao,
                os.getDataAbertura() != null ? sdf.format(os.getDataAbertura()) : "",
                String.format("%.2f", os.getValorTotal()),
                dataAgendamentoStr != null && !dataAgendamentoStr.isEmpty() ? dataAgendamentoStr : "---",
                os.getEnderecoInstalacao() != null ? (os.getEnderecoInstalacao().length() > 30 ? os.getEnderecoInstalacao().substring(0, 30) + "..." : os.getEnderecoInstalacao()) : ""
            };
            modelOrdensInstalacao.addRow(row);
        }
        
        if (modelOrdensInstalacao.getRowCount() == 0) {
            Object[] row = {0, "Nenhuma ordem de instalação encontrada", "---", "---", "---", "---", "0,00", "---", "---"};
            modelOrdensInstalacao.addRow(row);
        }
        
        atualizarEstatisticas();
    }
    
    private void filtrarOrdensInstalacao() {
        String status = (String) comboStatusInstalacao.getSelectedItem();
        String nivel = (String) comboNivelInstalacao.getSelectedItem();
        String tipoOs = (String) comboTipoOsInstalacao.getSelectedItem();
        String clienteSelecionado = (String) comboClienteInstalacao.getSelectedItem();
        
        List<OrdemServico> ordens = ordemServicoDAO.listarOsInstalacao();
        
        if (!"TODAS".equals(status)) {
            ordens = ordens.stream()
                .filter(os -> os.getStatus() != null && os.getStatus().equals(status))
                .toList();
        }
        
        if (!"TODOS".equals(nivel)) {
            ordens = ordens.stream()
                .filter(os -> os.getTipoNivel() != null && os.getTipoNivel().equals(nivel))
                .toList();
        }
        
        if (clienteSelecionado != null && !"TODOS".equals(clienteSelecionado)) {
            int idCliente = Integer.parseInt(clienteSelecionado.split(" - ")[0]);
            ordens = ordens.stream()
                .filter(os -> os.getIdCliente() == idCliente)
                .toList();
        }
        
        modelOrdensInstalacao.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        
        for (OrdemServico os : ordens) {
            Cliente cliente = clienteDAO.buscarPorId(os.getIdCliente());
            String nomeCliente = cliente != null ? cliente.getNomeExibicao() : "N/A";
            
            String statusExibicao = formatarStatus(os.getStatus());
            String dataAgendamentoStr = os.getDataAgendamentoFormatada();
            
            Object[] row = {
                os.getId(),
                nomeCliente,
                "INSTALAÇÃO",
                os.getTipoNivel(),
                statusExibicao,
                os.getDataAbertura() != null ? sdf.format(os.getDataAbertura()) : "",
                String.format("%.2f", os.getValorTotal()),
                dataAgendamentoStr,
                os.getEnderecoInstalacao() != null ? (os.getEnderecoInstalacao().length() > 30 ? os.getEnderecoInstalacao().substring(0, 30) + "..." : os.getEnderecoInstalacao()) : ""
            };
            modelOrdensInstalacao.addRow(row);
        }
    }
    
    private String formatarStatus(String status) {
        if (status == null) return "---";
        switch (status) {
            case "ABERTA": return "ABERTA";
            case "EM_ATENDIMENTO": return "EM ATENDIMENTO";
            case "FECHADA": return "FECHADA";
            default: return status;
        }
    }
    
    private void atualizarBotoes() {
        int row = tabelaOrdensInstalacao.getSelectedRow();
        boolean selecionado = row >= 0 && !"Nenhuma ordem de instalação encontrada".equals(modelOrdensInstalacao.getValueAt(row, 1));
        
        if (selecionado) {
            int idOS = (int) modelOrdensInstalacao.getValueAt(row, 0);
            String status = (String) modelOrdensInstalacao.getValueAt(row, 4);
            btnConcluirInstalacao.setEnabled(!"FECHADA".equals(status));
            btnAbrirInstalacao.setEnabled(true);
            carregarDetalhesInstalacao(idOS);
        } else {
            btnConcluirInstalacao.setEnabled(false);
            btnAbrirInstalacao.setEnabled(true);
            
            painelDetalhes.removeAll();
            JLabel lblInfoSelecao = new JLabel("Selecione uma ordem de instalação na tabela para visualizar seus detalhes.", SwingConstants.CENTER);
            lblInfoSelecao.setFont(new Font("Arial", Font.ITALIC, 12));
            lblInfoSelecao.setForeground(Color.GRAY);
            painelDetalhes.add(lblInfoSelecao, BorderLayout.CENTER);
            painelDetalhes.revalidate();
            painelDetalhes.repaint();
        }
    }
    
    private void carregarDetalhesInstalacao(int idOS) {
        OrdemServico os = ordemServicoDAO.buscarPorId(idOS);
        if (os == null) return;
        
        painelDetalhes.removeAll();
        
        JPanel painelInfo = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfHora = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        gbc.gridx = 0; gbc.gridy = 0;
        painelInfo.add(new JLabel("Cliente:"), gbc);
        Cliente cliente = clienteDAO.buscarPorId(os.getIdCliente());
        gbc.gridx = 1;
        painelInfo.add(new JLabel(cliente != null ? cliente.getNomeExibicao() : "N/A"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        painelInfo.add(new JLabel("Endereço:"), gbc);
        gbc.gridx = 1;
        painelInfo.add(new JLabel(os.getEnderecoInstalacao() != null ? os.getEnderecoInstalacao() : "---"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        painelInfo.add(new JLabel("Data Agendamento:"), gbc);
        gbc.gridx = 1;
        painelInfo.add(new JLabel(os.getDataAgendamentoFormatada()), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        painelInfo.add(new JLabel("Valor Total:"), gbc);
        gbc.gridx = 1;
        painelInfo.add(new JLabel(String.format("R$ %.2f", os.getValorTotal())), gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        painelInfo.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        JLabel lblStatus = new JLabel(formatarStatus(os.getStatus()));
        lblStatus.setForeground("ABERTA".equals(os.getStatus()) ? new Color(0, 100, 0) : 
                               ("FECHADA".equals(os.getStatus()) ? Color.GRAY : Color.BLUE));
        painelInfo.add(lblStatus, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        painelInfo.add(new JLabel("Data Abertura:"), gbc);
        gbc.gridx = 1;
        painelInfo.add(new JLabel(os.getDataAbertura() != null ? sdfHora.format(os.getDataAbertura()) : "---"), gbc);
        
        if (os.getDataFechamento() != null) {
            gbc.gridx = 0; gbc.gridy = 6;
            painelInfo.add(new JLabel("Data Conclusão:"), gbc);
            gbc.gridx = 1;
            painelInfo.add(new JLabel(sdfHora.format(os.getDataFechamento())), gbc);
        }
        
        if (os.getDescricaoSolucao() != null && !os.getDescricaoSolucao().isEmpty()) {
            gbc.gridx = 0; gbc.gridy = 7;
            painelInfo.add(new JLabel("Descrição:"), gbc);
            gbc.gridx = 1;
            JTextArea txtDesc = new JTextArea(os.getDescricaoSolucao(), 3, 40);
            txtDesc.setEditable(false);
            txtDesc.setLineWrap(true);
            txtDesc.setBackground(new Color(250, 250, 250));
            painelInfo.add(new JScrollPane(txtDesc), gbc);
        }
        
        gbc.gridx = 0; gbc.gridy = 8;
        painelInfo.add(new JLabel("Produtos:"), gbc);
        
        List<InstalacaoProduto> itens = instalacaoProdutoDAO.listarPorOrdemInstalacao(idOS);
        String[] colunasProdutos = {"Produto", "Quantidade", "Preço Unitário", "Subtotal"};
        DefaultTableModel modelProdutos = new DefaultTableModel(colunasProdutos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        for (InstalacaoProduto item : itens) {
            modelProdutos.addRow(new Object[]{
                item.getNomeProduto(),
                item.getQuantidade(),
                String.format("R$ %.2f", item.getPrecoUnitario()),
                String.format("R$ %.2f", item.getSubtotal())
            });
        }
        
        JTable tabelaProdutos = new JTable(modelProdutos);
        tabelaProdutos.setRowHeight(25);
        for (int i = 0; i < tabelaProdutos.getColumnCount(); i++) {
            tabelaProdutos.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        JScrollPane scrollProdutos = new JScrollPane(tabelaProdutos);
        scrollProdutos.setPreferredSize(new Dimension(0, 120));
        
        gbc.gridx = 1; gbc.gridy = 8;
        painelInfo.add(scrollProdutos, gbc);
        
        // Adicionar informações de mensalidades de monitoramento
        gbc.gridx = 0; gbc.gridy = 9;
        painelInfo.add(new JLabel("Mensalidades:"), gbc);
        
        List<ParcelaMonitoramento> parcelas = parcelaDAO.listarPorOrdemServico(idOS);
        if (!parcelas.isEmpty()) {
            String[] colunasParcelas = {"Parcela", "Vencimento", "Valor (R$)", "Status"};
            DefaultTableModel modelParcelas = new DefaultTableModel(colunasParcelas, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            for (ParcelaMonitoramento p : parcelas) {
                modelParcelas.addRow(new Object[]{
                    p.getNumeroParcela(),
                    p.getDataVencimento() != null ? new SimpleDateFormat("dd/MM/yyyy").format(p.getDataVencimento()) : "---",
                    String.format("%.2f", p.getValor()),
                    p.getStatus()
                });
            }
            
            JTable tabelaParcelas = new JTable(modelParcelas);
            tabelaParcelas.setRowHeight(25);
            for (int i = 0; i < tabelaParcelas.getColumnCount(); i++) {
                tabelaParcelas.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
            JScrollPane scrollParcelas = new JScrollPane(tabelaParcelas);
            scrollParcelas.setPreferredSize(new Dimension(0, 100));
            
            gbc.gridx = 1; gbc.gridy = 9;
            painelInfo.add(scrollParcelas, gbc);
        } else {
            gbc.gridx = 1; gbc.gridy = 9;
            painelInfo.add(new JLabel("Nenhuma mensalidade gerada"), gbc);
        }
        
        JScrollPane scrollInfo = new JScrollPane(painelInfo);
        scrollInfo.setBorder(null);
        painelDetalhes.add(scrollInfo, BorderLayout.CENTER);
        
        painelDetalhes.revalidate();
        painelDetalhes.repaint();
    }
    
    private JPanel criarPainelEstatisticas() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Estatísticas"));
        panel.setPreferredSize(new Dimension(250, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Instalações Abertas:"), gbc);
        lblTotalAbertas = new JLabel("0");
        lblTotalAbertas.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalAbertas.setForeground(new Color(0, 100, 0));
        gbc.gridy = 1;
        panel.add(lblTotalAbertas, gbc);
        
        gbc.gridy = 2;
        panel.add(new JLabel("Instalações Concluídas:"), gbc);
        lblTotalFechadas = new JLabel("0");
        lblTotalFechadas.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalFechadas.setForeground(new Color(0, 100, 0));
        gbc.gridy = 3;
        panel.add(lblTotalFechadas, gbc);
        
        gbc.gridy = 4;
        panel.add(new JLabel("Valor Total Instalações:"), gbc);
        lblValorTotalInstalacoes = new JLabel("R$ 0,00");
        lblValorTotalInstalacoes.setFont(new Font("Arial", Font.BOLD, 14));
        lblValorTotalInstalacoes.setForeground(new Color(0, 0, 139));
        gbc.gridy = 5;
        panel.add(lblValorTotalInstalacoes, gbc);
        
        gbc.gridy = 6;
        panel.add(new JLabel("Produtos Utilizados:"), gbc);
        lblProdutosUtilizados = new JLabel("0");
        lblProdutosUtilizados.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 7;
        panel.add(lblProdutosUtilizados, gbc);
        
        return panel;
    }
    
    private void iniciarTimerAtualizacao() {
        timerAtualizacao = new javax.swing.Timer(10000, e -> atualizarEstatisticas());
        timerAtualizacao.start();
    }
    
    private void atualizarEstatisticas() {
        List<OrdemServico> ordens = ordemServicoDAO.listarOsInstalacao();
        
        long abertas = ordens.stream().filter(o -> "ABERTA".equals(o.getStatus()) || "EM_ATENDIMENTO".equals(o.getStatus())).count();
        long fechadas = ordens.stream().filter(o -> "FECHADA".equals(o.getStatus())).count();
        double valorTotal = ordens.stream().mapToDouble(OrdemServico::getValorTotal).sum();
        
        int totalProdutos = 0;
        for (OrdemServico os : ordens) {
            List<InstalacaoProduto> itens = instalacaoProdutoDAO.listarPorOrdemInstalacao(os.getId());
            totalProdutos += itens.stream().mapToInt(InstalacaoProduto::getQuantidade).sum();
        }
        
        lblTotalAbertas.setText(String.valueOf(abertas));
        lblTotalFechadas.setText(String.valueOf(fechadas));
        lblValorTotalInstalacoes.setText(String.format("R$ %.2f", valorTotal));
        lblProdutosUtilizados.setText(String.valueOf(totalProdutos));
    }
    
    private void mostrarDialogNovaInstalacao() {
        produtosSelecionados.clear();
        
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Nova Ordem de Instalação", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(750, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panelCampos.add(new JLabel("Cliente:*"), gbc);
        comboClienteNovo = new JComboBox<>();
        comboClienteNovo.setPreferredSize(new Dimension(300, 25));
        carregarClientesCombo(comboClienteNovo);
        gbc.gridx = 1; gbc.gridwidth = 3;
        panelCampos.add(comboClienteNovo, gbc);
        gbc.gridwidth = 1;
        
        gbc.gridx = 0; gbc.gridy = 1;
        panelCampos.add(new JLabel("Endereço de Instalação:*"), gbc);
        txtEnderecoInstalacao = new JTextField(40);
        gbc.gridx = 1; gbc.gridwidth = 3;
        panelCampos.add(txtEnderecoInstalacao, gbc);
        gbc.gridwidth = 1;
        
        gbc.gridx = 0; gbc.gridy = 2;
        panelCampos.add(new JLabel("Data Agendamento:*"), gbc);
        spDataAgendamento = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorData = new JSpinner.DateEditor(spDataAgendamento, "dd/MM/yyyy");
        spDataAgendamento.setEditor(editorData);
        spDataAgendamento.setValue(new Date());
        gbc.gridx = 1;
        panelCampos.add(spDataAgendamento, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panelCampos.add(new JLabel("Descrição do Serviço:"), gbc);
        txtDescricaoInstalacao = new JTextArea(3, 50);
        txtDescricaoInstalacao.setLineWrap(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescricaoInstalacao);
        gbc.gridx = 1; gbc.gridwidth = 3;
        panelCampos.add(scrollDesc, gbc);
        gbc.gridwidth = 1;
        
        gbc.gridx = 0; gbc.gridy = 4;
        panelCampos.add(new JLabel("Observações:"), gbc);
        txtObservacoesInstalacao = new JTextArea(2, 50);
        txtObservacoesInstalacao.setLineWrap(true);
        JScrollPane scrollObs = new JScrollPane(txtObservacoesInstalacao);
        gbc.gridx = 1; gbc.gridwidth = 3;
        panelCampos.add(scrollObs, gbc);
        gbc.gridwidth = 1;
        
        JPanel painelProdutos = new JPanel(new BorderLayout());
        painelProdutos.setBorder(BorderFactory.createTitledBorder("Produtos para Instalação"));
        
        String[] colunasProdutos = {"Produto", "Quantidade", "Preço Unitário (R$)", "Subtotal (R$)"};
        modelProdutosSelecionados = new DefaultTableModel(colunasProdutos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaProdutosSelecionados = new JTable(modelProdutosSelecionados);
        tabelaProdutosSelecionados.setRowHeight(25);
        for (int i = 0; i < tabelaProdutosSelecionados.getColumnCount(); i++) {
            tabelaProdutosSelecionados.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        JScrollPane scrollProdutos = new JScrollPane(tabelaProdutosSelecionados);
        scrollProdutos.setPreferredSize(new Dimension(0, 120));
        
        btnSelecionarProdutos = new JButton("Adicionar Produtos");
        btnSelecionarProdutos.setBackground(new Color(70, 130, 180));
        btnSelecionarProdutos.setForeground(Color.WHITE);
        btnSelecionarProdutos.setFont(new Font("Arial", Font.BOLD, 11));
        btnSelecionarProdutos.addActionListener(e -> selecionarProdutos(dialog));
        
        JPanel panelBotoesProdutos = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotoesProdutos.add(btnSelecionarProdutos);
        
        painelProdutos.add(scrollProdutos, BorderLayout.CENTER);
        painelProdutos.add(panelBotoesProdutos, BorderLayout.SOUTH);
        
        JPanel painelValor = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelValor.add(new JLabel("Valor Total da Instalação (R$):"));
        lblValorTotalInstalacao = new JLabel("R$ 0,00");
        lblValorTotalInstalacao.setFont(new Font("Arial", Font.BOLD, 14));
        lblValorTotalInstalacao.setForeground(new Color(0, 100, 0));
        painelValor.add(lblValorTotalInstalacao);
        
        JPanel painelBotoesDialog = new JPanel(new FlowLayout());
        JButton btnSalvar = new JButton("Criar Ordem de Instalação");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnSalvar.setBackground(new Color(60, 120, 60));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(new Font("Arial", Font.BOLD, 14));
        btnSalvar.setPreferredSize(new Dimension(200, 35));
        
        btnSalvar.addActionListener(e -> criarOrdemInstalacao(dialog));
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        painelBotoesDialog.add(btnSalvar);
        painelBotoesDialog.add(btnCancelar);
        
        JPanel painelConteudo = new JPanel(new BorderLayout());
        painelConteudo.add(panelCampos, BorderLayout.NORTH);
        painelConteudo.add(painelProdutos, BorderLayout.CENTER);
        painelConteudo.add(painelValor, BorderLayout.SOUTH);
        
        dialog.add(painelConteudo, BorderLayout.CENTER);
        dialog.add(painelBotoesDialog, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void criarOrdemInstalacao(JDialog dialog) {
        if (comboClienteNovo.getSelectedIndex() == -1 || "TODOS".equals(comboClienteNovo.getSelectedItem())) {
            JOptionPane.showMessageDialog(dialog, "Selecione um cliente válido!");
            return;
        }
        
        String endereco = txtEnderecoInstalacao.getText().trim();
        if (endereco.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Endereço de instalação é obrigatório!");
            return;
        }
        
        Date dataAgendamentoDate = (Date) spDataAgendamento.getValue();
        if (dataAgendamentoDate == null) {
            JOptionPane.showMessageDialog(dialog, "Data de agendamento é obrigatória!");
            return;
        }
        
        int idCliente = Integer.parseInt(((String) comboClienteNovo.getSelectedItem()).split(" - ")[0]);
        
        double valorTotal = 0;
        for (ProdutoSelecionado ps : produtosSelecionados) {
            valorTotal += ps.getQuantidade() * ps.getProduto().getPrecoVenda();
        }
        
        String descricaoServico = txtDescricaoInstalacao.getText().trim();
        String observacoes = txtObservacoesInstalacao.getText().trim();
        String motivo = "ORDEM DE INSTALAÇÃO" + (descricaoServico.isEmpty() ? "" : " - " + descricaoServico);
        
        OrdemServico os = new OrdemServico();
        os.setIdCliente(idCliente);
        os.setIdEquipamento(0);
        os.setTipoNivel("1º Nível");
        os.setStatus("ABERTA");
        os.setMotivo(motivo);
        os.setDataAbertura(new Date());
        os.setIdUsuarioAbertura(usuarioLogado.getId());
        
        os.setTipoOrdem("INSTALACAO");
        os.setTipoOs("INSTALACAO");
        os.setDepartamentoOrigem("VENDAS");
        
        os.setValorTotal(valorTotal);
        os.setEnderecoInstalacao(endereco);
        os.setDataAgendamento(dataAgendamentoDate);
        os.setObservacoes(observacoes);
        os.setDescricaoSolucao(descricaoServico);
        
        if (ordemServicoDAO.inserir(os)) {
            for (ProdutoSelecionado ps : produtosSelecionados) {
                InstalacaoProduto item = new InstalacaoProduto();
                item.setIdOrdemInstalacao(os.getId());
                item.setIdProduto(ps.getProduto().getId());
                item.setNomeProduto(ps.getProduto().getNome());
                item.setQuantidade(ps.getQuantidade());
                item.setPrecoUnitario(ps.getProduto().getPrecoVenda());
                item.setSubtotal(ps.getQuantidade() * ps.getProduto().getPrecoVenda());
                instalacaoProdutoDAO.inserir(item);
                
                produtoDAO.darBaixaEstoque(ps.getProduto().getId(), ps.getQuantidade(), 
                    "INSTALACAO", os.getId(), usuarioLogado.getId());
            }
            
            JOptionPane.showMessageDialog(dialog, 
                "✅ Ordem de Instalação #" + os.getId() + " criada com sucesso!\n\n" +
                "Cliente: " + comboClienteNovo.getSelectedItem() + "\n" +
                "Valor Total: R$ " + String.format("%.2f", valorTotal) + "\n" +
                "Data Agendamento: " + new SimpleDateFormat("dd/MM/yyyy").format(dataAgendamentoDate) + "\n" +
                "Produtos: " + produtosSelecionados.size() + " itens",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
            dialog.dispose();
            carregarOrdensInstalacao();
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Deseja registrar uma transação financeira para esta instalação agora?",
                "Registrar Transação", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                registrarTransacaoParaInstalacao(os.getId(), idCliente, valorTotal);
            }
        } else {
            JOptionPane.showMessageDialog(dialog, "Erro ao criar ordem de instalação!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void selecionarProdutos(JDialog parentDialog) {
        List<ProdutoEstoque> produtos = produtoDAO.listarAtivos();
        
        if (produtos.isEmpty()) {
            JOptionPane.showMessageDialog(parentDialog, "Nenhum produto cadastrado no estoque!\nCadastre produtos primeiro.", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog(parentDialog, "Selecionar Produtos", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(parentDialog);
        dialog.setLayout(new BorderLayout());
        
        String[] colunas = {"ID", "Código", "Nome", "Preço Venda (R$)", "Estoque", "Selecionar"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
        };
        
        for (ProdutoEstoque p : produtos) {
            Object[] row = {
                p.getId(),
                p.getCodigo(),
                p.getNome(),
                String.format("%.2f", p.getPrecoVenda()),
                p.getQuantidade(),
                Boolean.FALSE
            };
            model.addRow(row);
        }
        
        JTable tabelaProdutos = new JTable(model);
        tabelaProdutos.setRowHeight(25);
        for (int i = 0; i < tabelaProdutos.getColumnCount(); i++) {
            if (i != 5) {
                tabelaProdutos.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
        JScrollPane scroll = new JScrollPane(tabelaProdutos);
        
        JPanel painelBotoes = new JPanel();
        JButton btnAdicionar = new JButton("Adicionar Selecionados");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnAdicionar.addActionListener(e -> {
            for (int i = 0; i < model.getRowCount(); i++) {
                Boolean selecionado = (Boolean) model.getValueAt(i, 5);
                if (selecionado != null && selecionado) {
                    int idProduto = (int) model.getValueAt(i, 0);
                    ProdutoEstoque produto = produtoDAO.buscarPorId(idProduto);
                    
                    String quantidadeStr = JOptionPane.showInputDialog(dialog, 
                        "Quantidade de " + produto.getNome() + ":\nEstoque disponível: " + produto.getQuantidade(),
                        "Quantidade", JOptionPane.QUESTION_MESSAGE);
                    
                    if (quantidadeStr != null && !quantidadeStr.trim().isEmpty()) {
                        try {
                            int quantidade = Integer.parseInt(quantidadeStr.trim());
                            if (quantidade <= 0) {
                                JOptionPane.showMessageDialog(dialog, "Quantidade deve ser maior que zero!");
                                continue;
                            }
                            if (quantidade > produto.getQuantidade()) {
                                JOptionPane.showMessageDialog(dialog, "Quantidade insuficiente em estoque!\nDisponível: " + produto.getQuantidade());
                                continue;
                            }
                            
                            boolean encontrado = false;
                            for (ProdutoSelecionado ps : produtosSelecionados) {
                                if (ps.getProduto().getId() == idProduto) {
                                    ps.setQuantidade(ps.getQuantidade() + quantidade);
                                    encontrado = true;
                                    break;
                                }
                            }
                            
                            if (!encontrado) {
                                produtosSelecionados.add(new ProdutoSelecionado(produto, quantidade));
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(dialog, "Quantidade inválida!");
                        }
                    }
                }
            }
            
            atualizarListaProdutosSelecionados();
            dialog.dispose();
        });
        
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        painelBotoes.add(btnAdicionar);
        painelBotoes.add(btnCancelar);
        
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(painelBotoes, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void atualizarListaProdutosSelecionados() {
        modelProdutosSelecionados.setRowCount(0);
        double total = 0;
        
        for (ProdutoSelecionado ps : produtosSelecionados) {
            double subtotal = ps.getQuantidade() * ps.getProduto().getPrecoVenda();
            total += subtotal;
            
            Object[] row = {
                ps.getProduto().getNome(),
                ps.getQuantidade(),
                String.format("%.2f", ps.getProduto().getPrecoVenda()),
                String.format("%.2f", subtotal)
            };
            modelProdutosSelecionados.addRow(row);
        }
        
        lblValorTotalInstalacao.setText(String.format("R$ %.2f", total));
    }
    
    private void registrarTransacaoParaInstalacao(int idOS, int idCliente, double valorTotal) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Valor (R$):"), gbc);
        JTextField txtValor = new JTextField(String.format("%.2f", valorTotal), 15);
        gbc.gridx = 1;
        panel.add(txtValor, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Forma de Pagamento:*"), gbc);
        JComboBox<String> comboForma = new JComboBox<>(new String[]{"DINHEIRO", "PIX", "CARTAO_DEBITO", "CARTAO_CREDITO", "TRANSFERENCIA", "BOLETO"});
        gbc.gridx = 1;
        panel.add(comboForma, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Status:"), gbc);
        JComboBox<String> comboStatus = new JComboBox<>(new String[]{"PENDENTE", "RECEBIDO"});
        gbc.gridx = 1;
        panel.add(comboStatus, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Data Vencimento:"), gbc);
        JSpinner spDataVencimento = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spDataVencimento, "dd/MM/yyyy");
        spDataVencimento.setEditor(editor);
        spDataVencimento.setValue(new Date());
        gbc.gridx = 1;
        panel.add(spDataVencimento, gbc);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Registrar Transação - Instalação #" + idOS, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                double valor = Double.parseDouble(txtValor.getText().trim().replace(",", "."));
                if (valor <= 0) {
                    JOptionPane.showMessageDialog(this, "Valor deve ser maior que zero!");
                    return;
                }
                
                TransacaoFinanceira transacao = new TransacaoFinanceira();
                transacao.setTipo("ENTRADA");
                transacao.setNatureza("Instalação");
                transacao.setFormaPagamento((String) comboForma.getSelectedItem());
                transacao.setValor(valor);
                transacao.setDescricao("Instalação - OS #" + idOS);
                transacao.setStatus((String) comboStatus.getSelectedItem());
                transacao.setDataVencimento((Date) spDataVencimento.getValue());
                transacao.setIdCliente(idCliente);
                transacao.setIdOrdemServico(idOS);
                transacao.setIdUsuarioRegistro(usuarioLogado.getId());
                
                if ("RECEBIDO".equals(transacao.getStatus())) {
                    transacao.setDataPagamento(new Date());
                }
                
                if (transacaoDAO.inserir(transacao)) {
                    JOptionPane.showMessageDialog(this, "Transação registrada com sucesso!");
                } else {
                    JOptionPane.showMessageDialog(this, "Erro ao registrar transação!", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Valor inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // ================================================================
    // MÉTODO CONCLUIR INSTALACAO COM ORDEM CORRETA DOS CAMPOS
    // ORDEM: 1. Observações -> 2. Mensalidades -> 3. Conta Instalação
    // ================================================================
    
    private void concluirInstalacao() {
        int row = tabelaOrdensInstalacao.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma ordem de instalação para concluir!");
            return;
        }
        
        if ("Nenhuma ordem de instalação encontrada".equals(modelOrdensInstalacao.getValueAt(row, 1))) {
            JOptionPane.showMessageDialog(this, "Nenhuma ordem de instalação disponível para concluir!");
            return;
        }
        
        int idOS = (int) modelOrdensInstalacao.getValueAt(row, 0);
        String status = (String) modelOrdensInstalacao.getValueAt(row, 4);
        
        if ("FECHADA".equals(status)) {
            JOptionPane.showMessageDialog(this, "Esta ordem de instalação já está concluída!");
            return;
        }
        
        OrdemServico os = ordemServicoDAO.buscarPorId(idOS);
        if (os == null) {
            JOptionPane.showMessageDialog(this, "Ordem de serviço não encontrada!");
            return;
        }
        
        // ================================================================
        // PAINEL DE CONCLUSÃO - ORDEM CORRETA DOS CAMPOS
        // 1. Observações de conclusão
        // 2. Configuração de Mensalidades de Monitoramento
        // 3. Conta a Receber da Instalação (produtos e serviços)
        // ================================================================
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // ========== 1. OBSERVAÇÕES DE CONCLUSÃO ==========
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Observações sobre a conclusão:"), gbc);
        JTextArea txtObsConclusao = new JTextArea(3, 40);
        txtObsConclusao.setLineWrap(true);
        JScrollPane scrollObs = new JScrollPane(txtObsConclusao);
        gbc.gridy = 1;
        panel.add(scrollObs, gbc);
        
        // Separador
        gbc.gridy = 2;
        panel.add(new JSeparator(), gbc);
        
        // ========== 2. MENSALIDADES DE MONITORAMENTO ==========
        gbc.gridy = 3;
        JLabel lblTituloMonitoramento = new JLabel("=== MENSALIDADES DE MONITORAMENTO ===");
        lblTituloMonitoramento.setFont(new Font("Arial", Font.BOLD, 12));
        lblTituloMonitoramento.setForeground(new Color(0, 100, 0));
        panel.add(lblTituloMonitoramento, gbc);
        
        gbc.gridy = 4;
        JCheckBox chkGerarMensalidades = new JCheckBox("Gerar mensalidades de monitoramento", false);
        chkGerarMensalidades.setFont(new Font("Arial", Font.BOLD, 11));
        chkGerarMensalidades.setForeground(new Color(0, 0, 139));
        panel.add(chkGerarMensalidades, gbc);
        
        // Painel de configuração de mensalidades (inicialmente desabilitado)
        JPanel painelMensalidades = new JPanel(new GridBagLayout());
        painelMensalidades.setBorder(BorderFactory.createTitledBorder("Configuração das Mensalidades"));
        painelMensalidades.setEnabled(false);
        
        GridBagConstraints gbcMsg = new GridBagConstraints();
        gbcMsg.insets = new Insets(5, 5, 5, 5);
        gbcMsg.fill = GridBagConstraints.HORIZONTAL;
        
        // Número de mensalidades (1 a 12)
        gbcMsg.gridx = 0; gbcMsg.gridy = 0;
        painelMensalidades.add(new JLabel("Número de Mensalidades (1-12):"), gbcMsg);
        JSpinner spNumMensalidades = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        spNumMensalidades.setPreferredSize(new Dimension(80, 25));
        gbcMsg.gridx = 1;
        painelMensalidades.add(spNumMensalidades, gbcMsg);
        
        // Valor por mensalidade
        gbcMsg.gridx = 0; gbcMsg.gridy = 1;
        painelMensalidades.add(new JLabel("Valor por Mensalidade (R$):"), gbcMsg);
        JTextField txtValorMensalidade = new JTextField(10);
        txtValorMensalidade.setToolTipText("Valor fixo de cada parcela de monitoramento (mínimo R$ 0,01)");
        gbcMsg.gridx = 1;
        painelMensalidades.add(txtValorMensalidade, gbcMsg);
        
        // Data da primeira mensalidade
        gbcMsg.gridx = 0; gbcMsg.gridy = 2;
        painelMensalidades.add(new JLabel("Data 1ª Mensalidade:"), gbcMsg);
        JSpinner spDataPrimeiraMensalidade = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorData = new JSpinner.DateEditor(spDataPrimeiraMensalidade, "dd/MM/yyyy");
        spDataPrimeiraMensalidade.setEditor(editorData);
        
        // Definir data padrão como 30 dias após hoje
        Calendar calPadrao = Calendar.getInstance();
        calPadrao.add(Calendar.DAY_OF_MONTH, 30);
        spDataPrimeiraMensalidade.setValue(calPadrao.getTime());
        
        gbcMsg.gridx = 1;
        painelMensalidades.add(spDataPrimeiraMensalidade, gbcMsg);
        
        // Info adicional
        gbcMsg.gridx = 0; gbcMsg.gridy = 3;
        gbcMsg.gridwidth = 2;
        JLabel lblInfo = new JLabel("As demais parcelas vencerão a cada 30 dias da primeira.");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 10));
        lblInfo.setForeground(Color.GRAY);
        painelMensalidades.add(lblInfo, gbcMsg);
        gbcMsg.gridwidth = 1;
        
        // Adicionar painel de mensalidades
        gbc.gridy = 5;
        panel.add(painelMensalidades, gbc);
        
        // Separador
        gbc.gridy = 6;
        panel.add(new JSeparator(), gbc);
        
        // ========== 3. CONTA A RECEBER DA INSTALAÇÃO ==========
        gbc.gridy = 7;
        JCheckBox chkGerarContaInstalacao = new JCheckBox("Gerar conta a receber da instalação (R$ " + String.format("%.2f", os.getValorTotal()) + ")", true);
        chkGerarContaInstalacao.setFont(new Font("Arial", Font.BOLD, 11));
        chkGerarContaInstalacao.setForeground(new Color(0, 100, 0));
        panel.add(chkGerarContaInstalacao, gbc);
        
        gbc.gridy = 8;
        JLabel lblInfoInstalacao = new JLabel("Valor referente aos produtos e serviços da instalação.");
        lblInfoInstalacao.setFont(new Font("Arial", Font.ITALIC, 10));
        lblInfoInstalacao.setForeground(Color.GRAY);
        panel.add(lblInfoInstalacao, gbc);
        
        // Habilitar/desabilitar painel de mensalidades conforme checkbox
        chkGerarMensalidades.addActionListener(e -> {
            boolean enabled = chkGerarMensalidades.isSelected();
            painelMensalidades.setEnabled(enabled);
            for (Component c : painelMensalidades.getComponents()) {
                c.setEnabled(enabled);
            }
        });
        
        // Inicialmente desabilitado
        painelMensalidades.setEnabled(false);
        for (Component c : painelMensalidades.getComponents()) {
            c.setEnabled(false);
        }
        
        // Botões do diálogo
        JButton btnConfirmar = new JButton("Concluir Instalação");
        JButton btnCancelarDialog = new JButton("Cancelar");
        
        btnConfirmar.setBackground(new Color(60, 120, 60));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFont(new Font("Arial", Font.BOLD, 12));
        
        btnCancelarDialog.setBackground(Color.GRAY);
        btnCancelarDialog.setForeground(Color.WHITE);
        
        JPanel painelBotoesDialog = new JPanel(new FlowLayout());
        painelBotoesDialog.add(btnConfirmar);
        painelBotoesDialog.add(btnCancelarDialog);
        
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Concluir Instalação - OS #" + idOS, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(panel), BorderLayout.CENTER);
        dialog.add(painelBotoesDialog, BorderLayout.SOUTH);
        dialog.setSize(600, 650);
        dialog.setLocationRelativeTo(this);
        
        // Ação do botão confirmar com validações
        btnConfirmar.addActionListener(e -> {
            // ================================================================
            // VALIDAÇÕES DAS MENSALIDADES
            // ================================================================
            
            boolean mensalidadesValidas = true;
            String erroMensagem = "";
            
            if (chkGerarMensalidades.isSelected()) {
                // Validar número de mensalidades
                int numMensalidades = (int) spNumMensalidades.getValue();
                if (numMensalidades < 1 || numMensalidades > 12) {
                    mensalidadesValidas = false;
                    erroMensagem = "❌ NÚMERO DE MENSALIDADES INVÁLIDO!\n\n" +
                                   "O número de mensalidades deve ser entre 1 e 12.\n" +
                                   "Valor informado: " + numMensalidades;
                }
                
                // Validar valor da mensalidade
                String valorStr = txtValorMensalidade.getText().trim();
                if (mensalidadesValidas && (valorStr == null || valorStr.isEmpty())) {
                    mensalidadesValidas = false;
                    erroMensagem = "❌ VALOR DA MENSALIDADE É OBRIGATÓRIO!\n\n" +
                                   "Informe um valor maior que zero.";
                }
                
                double valorMensalidade = 0;
                if (mensalidadesValidas) {
                    try {
                        valorMensalidade = Double.parseDouble(valorStr.replace(",", "."));
                        if (valorMensalidade <= 0) {
                            mensalidadesValidas = false;
                            erroMensagem = "❌ VALOR DA MENSALIDADE INVÁLIDO!\n\n" +
                                           "O valor deve ser maior que zero.\n" +
                                           "Valor informado: R$ " + String.format("%.2f", valorMensalidade);
                        } else if (valorMensalidade > 10000) {
                            mensalidadesValidas = false;
                            erroMensagem = "❌ VALOR DA MENSALIDADE MUITO ALTO!\n\n" +
                                           "Valor máximo permitido: R$ 10.000,00\n" +
                                           "Valor informado: R$ " + String.format("%.2f", valorMensalidade);
                        }
                    } catch (NumberFormatException ex) {
                        mensalidadesValidas = false;
                        erroMensagem = "❌ VALOR DA MENSALIDADE INVÁLIDO!\n\n" +
                                       "Use formato numérico válido (ex: 99,90 ou 99.90)\n" +
                                       "Valor informado: " + valorStr;
                    }
                }
                
                // Validar data da primeira mensalidade
                if (mensalidadesValidas) {
                    Date dataPrimeira = (Date) spDataPrimeiraMensalidade.getValue();
                    Date hoje = new Date();
                    
                    // Remover horas/minutos/segundos para comparação apenas de data
                    Calendar calHoje = Calendar.getInstance();
                    calHoje.setTime(hoje);
                    calHoje.set(Calendar.HOUR_OF_DAY, 0);
                    calHoje.set(Calendar.MINUTE, 0);
                    calHoje.set(Calendar.SECOND, 0);
                    calHoje.set(Calendar.MILLISECOND, 0);
                    
                    Calendar calData = Calendar.getInstance();
                    calData.setTime(dataPrimeira);
                    calData.set(Calendar.HOUR_OF_DAY, 0);
                    calData.set(Calendar.MINUTE, 0);
                    calData.set(Calendar.SECOND, 0);
                    calData.set(Calendar.MILLISECOND, 0);
                    
                    if (calData.getTime().before(calHoje.getTime())) {
                        mensalidadesValidas = false;
                        erroMensagem = "❌ DATA DA PRIMEIRA MENSALIDADE INVÁLIDA!\n\n" +
                                       "A data não pode ser anterior à data atual.\n" +
                                       "Data atual: " + new SimpleDateFormat("dd/MM/yyyy").format(hoje) + "\n" +
                                       "Data informada: " + new SimpleDateFormat("dd/MM/yyyy").format(dataPrimeira);
                    }
                }
                
                // Se alguma validação falhou, mostrar erro e NÃO permitir continuar
                if (!mensalidadesValidas) {
                    JOptionPane.showMessageDialog(dialog, 
                        erroMensagem + "\n\nCorrija os dados e tente novamente.", 
                        "Dados de Mensalidade Inválidos", 
                        JOptionPane.ERROR_MESSAGE);
                    return; // IMPEDE O FECHAMENTO DA OS
                }
            }
            
            // Se chegou aqui, os dados são válidos (ou não há mensalidades para gerar)
            
            String observacoes = txtObsConclusao.getText().trim();
            String observacoesCompletas = observacoes.isEmpty() ? 
                "Instalação concluída com sucesso." : 
                "Instalação concluída. Observações: " + observacoes;
            
            boolean concluido = ordemServicoDAO.concluirInstalacao(idOS, observacoesCompletas, usuarioLogado.getId());
            
            if (concluido) {
                JOptionPane.showMessageDialog(dialog, "✅ Ordem de Instalação #" + idOS + " concluída com sucesso!");
                
                // GERAR MENSALIDADES DE MONITORAMENTO (PRIMEIRO)
                if (chkGerarMensalidades.isSelected()) {
                    try {
                        int numMensalidades = (int) spNumMensalidades.getValue();
                        double valorMensalidade = Double.parseDouble(txtValorMensalidade.getText().trim().replace(",", "."));
                        Date dataPrimeira = (Date) spDataPrimeiraMensalidade.getValue();
                        
                        gerarMensalidadesMonitoramento(idOS, os.getIdCliente(), numMensalidades, valorMensalidade, dataPrimeira);
                        
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog, 
                            "Erro ao gerar mensalidades: valor inválido!\nAs mensalidades não foram geradas.", 
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
                
                // GERAR CONTA A RECEBER DA INSTALAÇÃO (DEPOIS)
                if (chkGerarContaInstalacao.isSelected() && os.getValorTotal() > 0) {
                    gerarContaReceberAoConcluir(os);
                } else if (os.getValorTotal() > 0) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Lembre-se de registrar manualmente a conta a receber da instalação no módulo Financeiro!\n" +
                        "Valor: R$ " + String.format("%.2f", os.getValorTotal()),
                        "Conta a Receber não Gerada", JOptionPane.INFORMATION_MESSAGE);
                }
                
                dialog.dispose();
                carregarOrdensInstalacao();
                atualizarBotoes();
            } else {
                JOptionPane.showMessageDialog(dialog, "Erro ao concluir instalação!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnCancelarDialog.addActionListener(e -> dialog.dispose());
        
        dialog.setVisible(true);
    }
    
    private void gerarContaReceberAoConcluir(OrdemServico os) {
        List<TransacaoFinanceira> transacoesExistentes = transacaoDAO.listarTodas().stream()
            .filter(t -> t.getIdOrdemServico() == os.getId())
            .toList();
        
        if (!transacoesExistentes.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Nota: Já existe uma transação registrada para esta instalação.\n" +
                "Conta a receber não foi gerada automaticamente.",
                "Informação", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        Date dataVencimento = cal.getTime();
        
        TransacaoFinanceira transacao = new TransacaoFinanceira();
        transacao.setTipo("ENTRADA");
        transacao.setNatureza("Instalação");
        transacao.setFormaPagamento("A_DEFINIR");
        transacao.setValor(os.getValorTotal());
        transacao.setDescricao("Instalação concluída - OS #" + os.getId());
        transacao.setStatus("PENDENTE");
        transacao.setDataVencimento(dataVencimento);
        transacao.setIdCliente(os.getIdCliente());
        transacao.setIdOrdemServico(os.getId());
        transacao.setIdUsuarioRegistro(usuarioLogado.getId());
        
        if (transacaoDAO.inserir(transacao)) {
            Cliente cliente = clienteDAO.buscarPorId(os.getIdCliente());
            String nomeCliente = cliente != null ? cliente.getNomeExibicao() : "Cliente ID: " + os.getIdCliente();
            
            JOptionPane.showMessageDialog(this, 
                "✅ Conta a receber gerada automaticamente!\n\n" +
                "OS #" + os.getId() + " - Instalação concluída\n" +
                "Cliente: " + nomeCliente + "\n" +
                "Valor: R$ " + String.format("%.2f", os.getValorTotal()) + "\n" +
                "Vencimento: " + new SimpleDateFormat("dd/MM/yyyy").format(dataVencimento),
                "Conta a Receber Gerada", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Gera parcelas de mensalidade de monitoramento para uma instalação concluída
     */
    private void gerarMensalidadesMonitoramento(int idOS, int idCliente, int numMensalidades, double valorMensalidade, Date dataPrimeira) {
        ParcelaMonitoramentoDAO parcelaDAO = new ParcelaMonitoramentoDAO();
        List<ParcelaMonitoramento> parcelasGeradas = new ArrayList<>();
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(dataPrimeira);
        
        for (int i = 1; i <= numMensalidades; i++) {
            Date dataVencimento = (Date) cal.getTime().clone();
            
            ParcelaMonitoramento parcela = new ParcelaMonitoramento();
            parcela.setIdOrdemServico(idOS);
            parcela.setNumeroParcela(i);
            parcela.setValor(valorMensalidade);
            parcela.setDataVencimento(dataVencimento);
            parcela.setStatus("PENDENTE");
            
            parcelasGeradas.add(parcela);
            
            // Próxima parcela: adicionar 30 dias
            cal.add(Calendar.DAY_OF_MONTH, 30);
            
            System.out.println("Parcela #" + i + " gerada - Vencimento: " + 
                new SimpleDateFormat("dd/MM/yyyy").format(dataVencimento) + 
                " - Valor: R$ " + String.format("%.2f", valorMensalidade));
        }
        
        // Salvar todas as parcelas
        if (parcelaDAO.inserirLista(parcelasGeradas)) {
            // Também criar transações financeiras para cada parcela
            Cliente cliente = clienteDAO.buscarPorId(idCliente);
            String nomeCliente = cliente != null ? cliente.getNomeExibicao() : "Cliente ID: " + idCliente;
            
            for (ParcelaMonitoramento parcela : parcelasGeradas) {
                TransacaoFinanceira transacao = new TransacaoFinanceira();
                transacao.setTipo("ENTRADA");
                transacao.setNatureza("Mensalidade Monitoramento");
                transacao.setFormaPagamento("A_DEFINIR");
                transacao.setValor(parcela.getValor());
                transacao.setDescricao("Mensalidade de Monitoramento - OS Instalação #" + idOS + 
                    " - Cliente: " + nomeCliente + " - Parcela " + parcela.getNumeroParcela() + "/" + numMensalidades);
                transacao.setStatus("PENDENTE");
                transacao.setDataVencimento(parcela.getDataVencimento());
                transacao.setIdCliente(idCliente);
                transacao.setIdOrdemServico(idOS);
                transacao.setIdUsuarioRegistro(usuarioLogado.getId());
                
                // Adicionar referência à parcela na observação
                transacao.setDocumentoReferencia("PARCELA_" + parcela.getNumeroParcela() + "_MENSALIDADE");
                
                transacaoDAO.inserir(transacao);
            }
            
            StringBuilder mensagem = new StringBuilder();
            mensagem.append("✅ ").append(numMensalidades).append(" mensalidades de monitoramento geradas com sucesso!\n\n");
            mensagem.append("Valor por parcela: R$ ").append(String.format("%.2f", valorMensalidade)).append("\n");
            mensagem.append("Total: R$ ").append(String.format("%.2f", valorMensalidade * numMensalidades)).append("\n");
            mensagem.append("Primeiro vencimento: ").append(new SimpleDateFormat("dd/MM/yyyy").format(dataPrimeira)).append("\n\n");
            mensagem.append("As parcelas foram registradas no módulo Financeiro como 'Mensalidade Monitoramento'.");
            
            JOptionPane.showMessageDialog(this, mensagem.toString(), "Mensalidades Geradas", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Erro ao gerar mensalidades de monitoramento!", 
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (timerAtualizacao != null) {
            timerAtualizacao.stop();
        }
    }
    
    private class ProdutoSelecionado {
        private ProdutoEstoque produto;
        private int quantidade;
        
        public ProdutoSelecionado(ProdutoEstoque produto, int quantidade) {
            this.produto = produto;
            this.quantidade = quantidade;
        }
        
        public ProdutoEstoque getProduto() { return produto; }
        public int getQuantidade() { return quantidade; }
        public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    }
}
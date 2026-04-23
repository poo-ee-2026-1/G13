// TelaAtendimento.java - CORRIGIDO: Fecha APENAS a OS de origem
package com.monitoramento.ui;

import com.monitoramento.model.Atendimento;
import com.monitoramento.model.Cliente;
import com.monitoramento.model.Usuario;
import com.monitoramento.model.OrdemServico;
import com.monitoramento.dao.AtendimentoDAO;
import com.monitoramento.dao.ClienteDAO;
import com.monitoramento.dao.OrdemServicoDAO;
import com.monitoramento.dao.EquipamentoDAO;
import com.monitoramento.Main;
import com.monitoramento.service.OrdemServicoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TelaAtendimento extends JPanel {
    private JTable tabelaAtendimentos;
    private DefaultTableModel tableModel;
    private AtendimentoDAO atendimentoDAO;
    private ClienteDAO clienteDAO;
    private OrdemServicoDAO ordemServicoDAO;
    private EquipamentoDAO equipamentoDAO;
    private Usuario usuarioLogado;
    
    private JComboBox<String> comboTipo, comboFiltroStatus, comboFiltroNivel, comboCliente;
    private JTextField txtAssunto;
    private JTextArea txtDescricao;
    private JComboBox<String> comboPrioridade;
    private JButton btnRegistrar, btnConcluir, btnCancelar, btnConverterOS, btnAtualizar;
    private JComboBox<String> comboNovoCliente;
    
    private DefaultTableCellRenderer centerRenderer;
    
    private static final String[] TIPOS_ATENDIMENTO = {"INFORMACAO", "SUGESTAO", "SOLICITACAO", "RECLAMACAO"};
    private static final String[] NIVEL_OPCOES = {"TODOS", "1º Nível", "2º Nível", "3º Nível"};
    
    private static final java.util.Map<String, String> TIPO_EXIBICAO = new java.util.HashMap<>();
    static {
        TIPO_EXIBICAO.put("INFORMACAO", "INFORMAÇÃO");
        TIPO_EXIBICAO.put("SUGESTAO", "SUGESTÃO");
        TIPO_EXIBICAO.put("SOLICITACAO", "SOLICITAÇÃO");
        TIPO_EXIBICAO.put("RECLAMACAO", "RECLAMAÇÃO");
    }
    
    public TelaAtendimento(Usuario usuario) {
        this.usuarioLogado = usuario;
        this.atendimentoDAO = new AtendimentoDAO();
        this.clienteDAO = new ClienteDAO();
        this.ordemServicoDAO = new OrdemServicoDAO();
        this.equipamentoDAO = new EquipamentoDAO();
        
        centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        initComponents();
        carregarAtendimentos();
        carregarClientesCombo();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        JPanel painelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros"));
        
        painelFiltros.add(new JLabel("Status:"));
        comboFiltroStatus = new JComboBox<>(new String[]{"TODOS", "PENDENTE", "CONCLUIDO", "CANCELADO"});
        painelFiltros.add(comboFiltroStatus);
        
        painelFiltros.add(new JLabel("Nível:"));
        comboFiltroNivel = new JComboBox<>(NIVEL_OPCOES);
        painelFiltros.add(comboFiltroNivel);
        
        painelFiltros.add(new JLabel("Tipo:"));
        JComboBox<String> comboTipoFiltro = new JComboBox<>(new String[]{"TODOS", "INFORMACAO", "SUGESTAO", "SOLICITACAO", "RECLAMACAO"});
        
        comboTipoFiltro.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String val = (String) value;
                if (val != null && TIPO_EXIBICAO.containsKey(val)) {
                    val = TIPO_EXIBICAO.get(val);
                } else if ("TODOS".equals(val)) {
                    val = "TODOS";
                }
                return super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
            }
        });
        painelFiltros.add(comboTipoFiltro);
        
        painelFiltros.add(new JLabel("Cliente:"));
        comboCliente = new JComboBox<>();
        comboCliente.setPreferredSize(new Dimension(250, 25));
        painelFiltros.add(comboCliente);
        
        JButton btnFiltrar = new JButton("Filtrar");
        JButton btnLimpar = new JButton("Limpar");
        
        btnFiltrar.addActionListener(e -> filtrarAtendimentos(comboTipoFiltro));
        btnLimpar.addActionListener(e -> {
            comboFiltroStatus.setSelectedIndex(0);
            comboFiltroNivel.setSelectedIndex(0);
            comboTipoFiltro.setSelectedIndex(0);
            comboCliente.setSelectedIndex(0);
            carregarAtendimentos();
        });
        
        painelFiltros.add(btnFiltrar);
        painelFiltros.add(btnLimpar);
        
        String[] colunas = {"ID", "Cliente", "Tipo", "Assunto", "Prioridade", "Status", "Data Abertura", "Data Conclusão", "OS Vinculada"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaAtendimentos = new JTable(tableModel);
        tabelaAtendimentos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaAtendimentos.setRowHeight(25);
        tabelaAtendimentos.setFont(new Font("Arial", Font.PLAIN, 12));
        
        for (int i = 0; i < tabelaAtendimentos.getColumnCount(); i++) {
            tabelaAtendimentos.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        tabelaAtendimentos.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabelaAtendimentos.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabelaAtendimentos.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabelaAtendimentos.getColumnModel().getColumn(3).setPreferredWidth(200);
        tabelaAtendimentos.getColumnModel().getColumn(4).setPreferredWidth(80);
        tabelaAtendimentos.getColumnModel().getColumn(5).setPreferredWidth(100);
        tabelaAtendimentos.getColumnModel().getColumn(6).setPreferredWidth(130);
        tabelaAtendimentos.getColumnModel().getColumn(7).setPreferredWidth(130);
        tabelaAtendimentos.getColumnModel().getColumn(8).setPreferredWidth(80);
        
        JScrollPane scrollTabela = new JScrollPane(tabelaAtendimentos);
        
        JPanel painelRegistro = criarPainelRegistro();
        
        JPanel painelBotoes = new JPanel(new FlowLayout());
        btnRegistrar = new JButton("Registrar Atendimento");
        btnConcluir = new JButton("Concluir Atendimento");
        btnCancelar = new JButton("Cancelar Atendimento");
        btnConverterOS = new JButton("Converter para OS");
        btnAtualizar = new JButton("Atualizar");
        
        btnRegistrar.addActionListener(e -> mostrarFormularioRegistro());
        btnConcluir.addActionListener(e -> concluirAtendimento());
        btnCancelar.addActionListener(e -> cancelarAtendimento());
        btnConverterOS.addActionListener(e -> converterParaOS());
        btnAtualizar.addActionListener(e -> carregarAtendimentos());
        
        tabelaAtendimentos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                atualizarBotoes();
            }
        });
        
        painelBotoes.add(btnRegistrar);
        painelBotoes.add(btnConcluir);
        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnConverterOS);
        painelBotoes.add(btnAtualizar);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setTopComponent(scrollTabela);
        splitPane.setBottomComponent(painelRegistro);
        
        add(painelFiltros, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);
    }
    
    private JPanel criarPainelRegistro() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Novo Atendimento"));
        
        JPanel panelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panelCampos.add(new JLabel("Cliente:*"), gbc);
        comboNovoCliente = new JComboBox<>();
        comboNovoCliente.setPreferredSize(new Dimension(250, 25));
        gbc.gridx = 1; gbc.gridwidth = 2;
        panelCampos.add(comboNovoCliente, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        panelCampos.add(new JLabel("Tipo de Atendimento:*"), gbc);
        comboTipo = new JComboBox<>(TIPOS_ATENDIMENTO);
        
        comboTipo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String val = (String) value;
                if (val != null && TIPO_EXIBICAO.containsKey(val)) {
                    val = TIPO_EXIBICAO.get(val);
                }
                return super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
            }
        });
        
        comboTipo.setToolTipText("INFORMACAO: Aviso/Informação | SUGESTAO: Sugestão de melhoria | SOLICITACAO: Requisição de serviço | RECLAMACAO: Insatisfação");
        gbc.gridx = 1;
        panelCampos.add(comboTipo, gbc);
        
        JLabel lblInfoTipo = new JLabel("Apenas SOLICITAÇÃO pode ser convertida em OS");
        lblInfoTipo.setFont(new Font("Arial", Font.ITALIC, 10));
        lblInfoTipo.setForeground(Color.BLUE);
        gbc.gridx = 2;
        panelCampos.add(lblInfoTipo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panelCampos.add(new JLabel("Assunto:*"), gbc);
        txtAssunto = new JTextField(40);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panelCampos.add(txtAssunto, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 1;
        panelCampos.add(new JLabel("Descrição:*"), gbc);
        txtDescricao = new JTextArea(4, 40);
        txtDescricao.setLineWrap(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescricao);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panelCampos.add(scrollDesc, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 1;
        panelCampos.add(new JLabel("Prioridade:*"), gbc);
        comboPrioridade = new JComboBox<>(new String[]{"BAIXA", "MEDIA", "ALTA"});
        gbc.gridx = 1;
        panelCampos.add(comboPrioridade, gbc);
        
        JPanel painelInfo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel lblInfoRegistro = new JLabel("Clique no botão 'Registrar Atendimento' abaixo para salvar");
        lblInfoRegistro.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfoRegistro.setForeground(new Color(100, 100, 100));
        painelInfo.add(lblInfoRegistro);
        panel.add(painelInfo, BorderLayout.SOUTH);
        
        panel.add(panelCampos, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void carregarClientesCombo() {
        comboCliente.removeAllItems();
        comboNovoCliente.removeAllItems();
        
        comboCliente.addItem("TODOS");
        List<Cliente> clientes = clienteDAO.listarTodos();
        for (Cliente c : clientes) {
            String item = c.getId() + " - " + c.getNomeExibicao();
            comboCliente.addItem(item);
            comboNovoCliente.addItem(item);
        }
    }
    
    private void carregarAtendimentos() {
        tableModel.setRowCount(0);
        List<Atendimento> atendimentos = atendimentoDAO.listarTodos();
        adicionarAtendimentosTabela(atendimentos);
    }
    
    private void filtrarAtendimentos(JComboBox<String> comboTipoFiltro) {
        String status = (String) comboFiltroStatus.getSelectedItem();
        String nivel = (String) comboFiltroNivel.getSelectedItem();
        String tipo = (String) comboTipoFiltro.getSelectedItem();
        String clienteSelecionado = (String) comboCliente.getSelectedItem();
        
        List<Atendimento> atendimentos = atendimentoDAO.listarTodos();
        
        if (!"TODOS".equals(status)) {
            atendimentos = atendimentos.stream()
                .filter(a -> a.getStatus().equals(status))
                .toList();
        }
        
        if (!"TODOS".equals(tipo)) {
            atendimentos = atendimentos.stream()
                .filter(a -> a.getTipo() != null && a.getTipo().equals(tipo))
                .toList();
        }
        
        if (clienteSelecionado != null && !"TODOS".equals(clienteSelecionado)) {
            int idCliente = Integer.parseInt(clienteSelecionado.split(" - ")[0]);
            atendimentos = atendimentos.stream()
                .filter(a -> a.getIdCliente() == idCliente)
                .toList();
        }
        
        adicionarAtendimentosTabela(atendimentos);
    }
    
    private void adicionarAtendimentosTabela(List<Atendimento> atendimentos) {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        for (Atendimento a : atendimentos) {
            Cliente c = clienteDAO.buscarPorId(a.getIdCliente());
            String nomeCliente = c != null ? c.getNomeExibicao() : "N/A";
            
            String osVinculada = a.getIdOrdemServicoVinculada() > 0 ? 
                String.valueOf(a.getIdOrdemServicoVinculada()) : "---";
            
            Object[] row = {
                a.getId(),
                nomeCliente,
                getTipoExibicao(a.getTipo()),
                a.getAssunto() != null ? (a.getAssunto().length() > 40 ? a.getAssunto().substring(0, 40) + "..." : a.getAssunto()) : "",
                a.getPrioridade(),
                a.getStatus(),
                a.getDataAbertura() != null ? sdf.format(a.getDataAbertura()) : "",
                a.getDataConclusao() != null ? sdf.format(a.getDataConclusao()) : "",
                osVinculada
            };
            tableModel.addRow(row);
        }
    }
    
    private void atualizarBotoes() {
        int row = tabelaAtendimentos.getSelectedRow();
        boolean selecionado = row >= 0;
        
        if (selecionado) {
            String status = (String) tableModel.getValueAt(row, 5);
            String tipo = (String) tableModel.getValueAt(row, 2);
            String osVinculada = (String) tableModel.getValueAt(row, 8);
            boolean temOSVinculada = !"---".equals(osVinculada);
            
            btnConcluir.setEnabled("PENDENTE".equals(status));
            btnCancelar.setEnabled("PENDENTE".equals(status));
            btnConverterOS.setEnabled("PENDENTE".equals(status) && "SOLICITAÇÃO".equals(tipo) && !temOSVinculada);
            
            if ("SOLICITAÇÃO".equals(tipo) && !temOSVinculada) {
                btnConverterOS.setToolTipText("Converter esta solicitação em Ordem de Serviço (INFORMAÇÃO)");
            } else if (temOSVinculada) {
                btnConverterOS.setToolTipText("Este atendimento já possui uma OS vinculada");
            } else {
                btnConverterOS.setToolTipText("Apenas atendimentos do tipo SOLICITAÇÃO podem ser convertidos em OS");
            }
        } else {
            btnConcluir.setEnabled(false);
            btnCancelar.setEnabled(false);
            btnConverterOS.setEnabled(false);
        }
    }
    
    private void mostrarFormularioRegistro() {
        if (comboNovoCliente.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente!");
            return;
        }
        
        String selected = (String) comboNovoCliente.getSelectedItem();
        int idCliente = Integer.parseInt(selected.split(" - ")[0]);
        String tipo = (String) comboTipo.getSelectedItem();
        String assunto = txtAssunto.getText().trim();
        String descricao = txtDescricao.getText().trim();
        String prioridade = (String) comboPrioridade.getSelectedItem();
        
        if (assunto.isEmpty() || descricao.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Assunto e descrição são obrigatórios!");
            return;
        }
        
        String tipoExibicao = getTipoExibicao(tipo);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Confirmar registro do atendimento?\n\n" +
            "Cliente: " + selected.split(" - ")[1] + "\n" +
            "Tipo: " + tipoExibicao + "\n" +
            "Assunto: " + assunto + "\n" +
            "Prioridade: " + prioridade + "\n\n" +
            (tipo.equals("SOLICITACAO") ? 
                "✓ Este atendimento poderá ser convertido em OS posteriormente." : 
                "✓ Este tipo de atendimento NÃO pode ser convertido em OS."),
            "Registrar Atendimento", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Atendimento a = new Atendimento();
            a.setIdCliente(idCliente);
            a.setTipo(tipo);
            a.setAssunto(assunto);
            a.setDescricao(descricao);
            a.setPrioridade(prioridade);
            a.setIdUsuarioAbertura(usuarioLogado.getId());
            
            if (atendimentoDAO.inserir(a)) {
                JOptionPane.showMessageDialog(this, "Atendimento registrado com sucesso!\n" +
                    "ID: " + a.getId() + "\n" +
                    "Tipo: " + tipoExibicao + "\n" +
                    (tipo.equals("SOLICITACAO") ? 
                        "Este atendimento pode ser convertido em Ordem de Serviço." : 
                        "Este tipo de atendimento não pode ser convertido em OS."));
                
                txtAssunto.setText("");
                txtDescricao.setText("");
                comboTipo.setSelectedIndex(0);
                comboPrioridade.setSelectedIndex(0);
                if (comboNovoCliente.getItemCount() > 0) {
                    comboNovoCliente.setSelectedIndex(0);
                }
                
                carregarAtendimentos();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao registrar atendimento!");
            }
        }
    }
    
    private void concluirAtendimento() {
        int row = tabelaAtendimentos.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um atendimento para concluir!");
            return;
        }
        
        int id = (int) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 5);
        
        if (!"PENDENTE".equals(status)) {
            JOptionPane.showMessageDialog(this, 
                "Apenas atendimentos com status PENDENTE podem ser concluídos!\n" +
                "Status atual: " + status,
                "Conclusão não permitida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String descricaoConclusao = JOptionPane.showInputDialog(this, 
            "Informe a descrição da conclusão do atendimento:\n" +
            "Descreva como o atendimento foi resolvido ou as ações tomadas.", 
            "Concluir Atendimento", JOptionPane.QUESTION_MESSAGE);
        
        if (descricaoConclusao != null && !descricaoConclusao.trim().isEmpty()) {
            if (atendimentoDAO.concluirAtendimento(id, descricaoConclusao, usuarioLogado.getId())) {
                JOptionPane.showMessageDialog(this, "Atendimento concluído com sucesso!");
                carregarAtendimentos();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao concluir atendimento!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void cancelarAtendimento() {
        int row = tabelaAtendimentos.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um atendimento para cancelar!");
            return;
        }
        
        int id = (int) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 5);
        String osVinculada = (String) tableModel.getValueAt(row, 8);
        boolean temOSVinculada = !"---".equals(osVinculada);
        
        if (!"PENDENTE".equals(status)) {
            JOptionPane.showMessageDialog(this, 
                "Apenas atendimentos com status PENDENTE podem ser cancelados!\n" +
                "Status atual: " + status,
                "Cancelamento não permitido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (temOSVinculada) {
            JOptionPane.showMessageDialog(this, 
                "Este atendimento já possui uma OS vinculada (OS #" + osVinculada + ")!\n" +
                "Não é possível cancelar um atendimento que já foi convertido em OS.\n" +
                "Cancele a OS primeiro se necessário.",
                "Cancelamento não permitido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Motivo do Cancelamento:*"), gbc);
        JTextArea txtMotivoCancelamento = new JTextArea(4, 40);
        txtMotivoCancelamento.setLineWrap(true);
        JScrollPane scrollMotivo = new JScrollPane(txtMotivoCancelamento);
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(scrollMotivo, gbc);
        
        gbc.gridy = 2;
        JComboBox<String> comboCategoriaCancelamento = new JComboBox<>(new String[]{
            "CLIENTE DESISTIU", 
            "PROBLEMA RESOLVIDO PELO CLIENTE", 
            "SOLICITAÇÃO DUPLICADA", 
            "INFORMAÇÕES INSUFICIENTES",
            "FORA DO ESCOPO DE ATENDIMENTO",
            "OUTRO"
        });
        panel.add(new JLabel("Categoria:"), gbc);
        gbc.gridy = 3;
        panel.add(comboCategoriaCancelamento, gbc);
        
        int confirm = JOptionPane.showConfirmDialog(this, panel, 
            "Cancelar Atendimento #" + id, 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (confirm == JOptionPane.OK_OPTION) {
            String motivoCancelamento = txtMotivoCancelamento.getText().trim();
            if (motivoCancelamento.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Motivo do cancelamento é obrigatório!");
                return;
            }
            
            String categoria = (String) comboCategoriaCancelamento.getSelectedItem();
            String descricaoCancelamento = "[" + categoria + "] " + motivoCancelamento;
            
            Atendimento a = atendimentoDAO.buscarPorId(id);
            if (a != null) {
                a.setStatus("CANCELADO");
                a.setDataConclusao(new Date());
                a.setIdUsuarioAtendimento(usuarioLogado.getId());
                
                String descricaoOriginal = a.getDescricao();
                a.setDescricao(descricaoOriginal + "\n\n--- CANCELAMENTO ---\n" +
                    "Data: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + "\n" +
                    "Cancelado por: " + usuarioLogado.getNomeCompleto() + "\n" +
                    "Categoria: " + categoria + "\n" +
                    "Motivo: " + motivoCancelamento);
                
                if (atendimentoDAO.atualizar(a)) {
                    JOptionPane.showMessageDialog(this, 
                        "✅ Atendimento #" + id + " cancelado com sucesso!\n\n" +
                        "Categoria: " + categoria + "\n" +
                        "Motivo: " + motivoCancelamento + "\n\n" +
                        "Usuário: " + usuarioLogado.getNomeCompleto(),
                        "Atendimento Cancelado", JOptionPane.INFORMATION_MESSAGE);
                    carregarAtendimentos();
                } else {
                    JOptionPane.showMessageDialog(this, "Erro ao cancelar atendimento!", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    /**
     * MÉTODO CORRIGIDO: Converter Atendimento para OS do tipo INFORMAÇÃO
     * 
     * CORREÇÕES REALIZADAS:
     * 1. Verifica se o cliente possui alguma OS aberta (qualquer tipo)
     * 2. Se existir OS aberta, identifica especificamente qual OS está em conflito
     * 3. Fecha APENAS a OS que está causando o conflito (a OS de origem)
     * 4. Mantém as demais OS do cliente inalteradas
     * 5. Abre a nova OS do tipo INFORMAÇÃO
     * 6. Registra no histórico da OS fechada o motivo da conversão
     */
    private void converterParaOS() {
        int row = tabelaAtendimentos.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um atendimento para converter!");
            return;
        }
        
        int idAtendimento = (int) tableModel.getValueAt(row, 0);
        Atendimento a = atendimentoDAO.buscarPorId(idAtendimento);
        
        if (a == null) return;
        
        if (!"SOLICITACAO".equals(a.getTipo())) {
            JOptionPane.showMessageDialog(this, 
                "Apenas atendimentos do tipo SOLICITAÇÃO podem ser convertidos em Ordem de Serviço!\n" +
                "Tipo atual: " + getTipoExibicao(a.getTipo()),
                "Conversão não permitida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (a.getIdOrdemServicoVinculada() > 0) {
            JOptionPane.showMessageDialog(this, 
                "Este atendimento já possui uma OS vinculada (OS #" + a.getIdOrdemServicoVinculada() + ")!\n" +
                "Não é possível criar outra OS para o mesmo atendimento.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if ("CANCELADO".equals(a.getStatus())) {
            JOptionPane.showMessageDialog(this, 
                "Não é possível converter um atendimento cancelado em Ordem de Serviço!",
                "Conversão não permitida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if ("CONCLUIDO".equals(a.getStatus())) {
            JOptionPane.showMessageDialog(this, 
                "Não é possível converter um atendimento já concluído em Ordem de Serviço!",
                "Conversão não permitida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Cliente cliente = clienteDAO.buscarPorId(a.getIdCliente());
        if (cliente == null) {
            JOptionPane.showMessageDialog(this, "Cliente não encontrado!");
            return;
        }
        
        // ================================================================
        // CORREÇÃO PRINCIPAL: Identificar e fechar APENAS a OS de origem
        // ================================================================
        
        OrdemServicoService osService = Main.getOrdemServicoService();
        OrdemServico osExistente = null;
        
        if (osService != null) {
            osExistente = osService.getOSAbertaPorCliente(a.getIdCliente());
        } else {
            List<OrdemServico> ordensAbertas = ordemServicoDAO.buscarOrdensAbertasPorCliente(a.getIdCliente());
            if (!ordensAbertas.isEmpty()) {
                osExistente = ordensAbertas.get(0);
            }
        }
        
        // Se existe OS aberta, perguntar se deseja fechá-la
        if (osExistente != null && !"FECHADA".equals(osExistente.getStatus())) {
            String tipoOsExistente = osExistente.getTipoOs();
            if (tipoOsExistente == null) tipoOsExistente = "DESCONHECIDA";
            
            String nivelExistente = osExistente.getTipoNivel();
            String dataAbertura = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(osExistente.getDataAbertura());
            
            int resposta = JOptionPane.showConfirmDialog(this, 
                "⚠️ ATENÇÃO: O cliente já possui uma OS aberta que impede a conversão!\n\n" +
                "Cliente: " + cliente.getNomeExibicao() + "\n" +
                "OS #" + osExistente.getId() + " - Tipo: " + tipoOsExistente + "\n" +
                "Nível: " + (nivelExistente != null ? nivelExistente : "N/A") + "\n" +
                "Data Abertura: " + dataAbertura + "\n\n" +
                "Para converter este atendimento em uma nova OS (INFORMAÇÃO),\n" +
                "APENAS esta OS será FECHADA automaticamente.\n" +
                "As demais OS do cliente (se houver) permanecerão inalteradas.\n\n" +
                "Deseja continuar?",
                "OS de Origem Detectada",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (resposta != JOptionPane.YES_OPTION) {
                return;
            }
            
            // Fechar APENAS a OS de origem com motivo de conversão
            String motivoFechamento = "========================================\n" +
                                      "OS FECHADA AUTOMATICAMENTE PELO SISTEMA\n" +
                                      "========================================\n" +
                                      "Motivo: Conversão de atendimento #" + idAtendimento + " para nova OS de INFORMAÇÃO.\n" +
                                      "Atendimento original: " + a.getAssunto() + "\n" +
                                      "Descrição: " + (a.getDescricao().length() > 200 ? a.getDescricao().substring(0, 200) + "..." : a.getDescricao()) + "\n" +
                                      "========================================";
            
            String falhaIdentificada = "CONVERSAO_PARA_INFORMACAO";
            
            boolean fechada;
            if (osService != null) {
                fechada = osService.fecharOrdemServico(osExistente.getId(), motivoFechamento, falhaIdentificada, usuarioLogado.getId());
            } else {
                fechada = ordemServicoDAO.fecharOrdemServico(osExistente.getId(), motivoFechamento, falhaIdentificada, usuarioLogado.getId());
            }
            
            if (fechada) {
                System.out.println("✅ OS de origem #" + osExistente.getId() + " foi fechada para conversão do atendimento #" + idAtendimento);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "❌ Não foi possível fechar a OS de origem #" + osExistente.getId() + "!\n" +
                    "A conversão foi cancelada.",
                    "Erro ao Fechar OS de Origem", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Verificar novamente se o cliente possui OS aberta (após fechar APENAS a de origem)
        boolean possuiOSAberta = false;
        OrdemServico outraOS = null;
        
        if (osService != null) {
            possuiOSAberta = osService.clientePossuiOSAberta(a.getIdCliente());
            if (possuiOSAberta) {
                outraOS = osService.getOSAbertaPorCliente(a.getIdCliente());
            }
        } else {
            List<OrdemServico> abertas = ordemServicoDAO.buscarOrdensAbertasPorCliente(a.getIdCliente());
            possuiOSAberta = !abertas.isEmpty();
            if (!abertas.isEmpty()) {
                outraOS = abertas.get(0);
            }
        }
        
        if (possuiOSAberta && outraOS != null && (osExistente == null || outraOS.getId() != osExistente.getId())) {
            // Se ainda há outra OS aberta (diferente da que fechamos), informar o usuário
            JOptionPane.showMessageDialog(this, 
                "⚠️ ATENÇÃO: O cliente ainda possui outra OS aberta!\n\n" +
                "OS #" + outraOS.getId() + " - Tipo: " + (outraOS.getTipoOs() != null ? outraOS.getTipoOs() : "DESCONHECIDA") + "\n" +
                "Status: " + outraOS.getStatus() + "\n\n" +
                "A OS de origem foi fechada, mas existe outra OS em aberto.\n" +
                "Feche esta OS manualmente antes de tentar novamente.",
                "Outra OS Detectada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Verificar se o cliente possui equipamento
        List<com.monitoramento.model.Equipamento> equipamentos = equipamentoDAO.buscarPorCliente(a.getIdCliente());
        if (equipamentos.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "❌ Cliente não possui equipamento cadastrado!\n\n" +
                "Para criar uma OS, o cliente precisa ter um equipamento cadastrado.\n" +
                "Cadastre um equipamento primeiro e tente novamente.",
                "Equipamento Necessário", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // ================================================================
        // CRIAÇÃO DA NOVA OS (TIPO INFORMAÇÃO)
        // ================================================================
        
        String mensagemHistorico = "";
        if (osExistente != null) {
            mensagemHistorico = "\n\n📋 A OS de origem #" + osExistente.getId() + " foi fechada automaticamente durante esta conversão.";
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Converter Atendimento #" + idAtendimento + " em Ordem de Serviço\n\n" +
            "Dados do Atendimento:\n" +
            "• Cliente: " + cliente.getNomeExibicao() + "\n" +
            "• Tipo: " + getTipoExibicao(a.getTipo()) + "\n" +
            "• Assunto: " + a.getAssunto() + "\n" +
            "• Descrição: " + (a.getDescricao().length() > 100 ? a.getDescricao().substring(0, 100) + "..." : a.getDescricao()) + "\n" +
            "• Prioridade: " + a.getPrioridade() + "\n\n" +
            "A OS será criada com:\n" +
            "• Tipo: INFORMAÇÃO\n" +
            "• Departamento Origem: ATENDIMENTO\n" +
            "• Nível: 1º Nível" +
            mensagemHistorico + "\n\n" +
            "Deseja continuar?", 
            "Converter para OS", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Motivo da OS
        String motivo = "[ATENDIMENTO #" + idAtendimento + " - " + a.getPrioridade() + "] " + 
            a.getAssunto() + " - " + a.getDescricao();
        
        OrdemServico novaOS = null;
        
        if (osService != null) {
            novaOS = osService.abrirOSAtendimento(a.getIdCliente(), motivo, usuarioLogado.getId(), a.getAssunto());
        } else {
            // Fallback: criar diretamente pelo DAO
            novaOS = new OrdemServico();
            novaOS.setIdCliente(a.getIdCliente());
            novaOS.setIdEquipamento(equipamentos.get(0).getId());
            novaOS.setTipoNivel("1º Nível");
            novaOS.setStatus("ABERTA");
            novaOS.setMotivo(motivo);
            novaOS.setDataAbertura(new Date());
            novaOS.setIdUsuarioAbertura(usuarioLogado.getId());
            novaOS.setTipoOs("INFORMACAO");
            novaOS.setDepartamentoOrigem("ATENDIMENTO");
            novaOS.setTipoOrdem("SERVICO");
            
            if (!ordemServicoDAO.inserir(novaOS)) {
                novaOS = null;
            }
        }
        
        if (novaOS != null && novaOS.getId() > 0) {
            // Atualizar o atendimento com a OS vinculada
            a.setIdOrdemServicoVinculada(novaOS.getId());
            a.setStatus("CONCLUIDO");
            a.setDataConclusao(new Date());
            a.setIdUsuarioAtendimento(usuarioLogado.getId());
            
            String conclusaoDesc = "Convertido para OS #" + novaOS.getId() + " (INFORMAÇÃO) - Atendimento encaminhado ao 1º Nível.";
            if (osExistente != null) {
                conclusaoDesc += "\n\nA OS de origem #" + osExistente.getId() + " foi fechada automaticamente durante esta conversão.";
            }
            
            String descricaoOriginal = a.getDescricao();
            a.setDescricao(descricaoOriginal + "\n\n--- CONVERSÃO PARA OS ---\n" + conclusaoDesc);
            atendimentoDAO.atualizar(a);
            
            String mensagemSucesso = "✅ Atendimento #" + idAtendimento + " convertido com sucesso!\n\n" +
                "OS #" + novaOS.getId() + " (INFORMAÇÃO) aberta!\n\n" +
                "Detalhes da OS:\n" +
                "• Cliente: " + cliente.getNomeExibicao() + "\n" +
                "• Tipo: INFORMAÇÃO\n" +
                "• Departamento: ATENDIMENTO\n" +
                "• Nível: 1º Nível";
            
            if (osExistente != null) {
                mensagemSucesso += "\n\n📋 A OS de origem #" + osExistente.getId() + " foi fechada automaticamente.";
            }
            
            JOptionPane.showMessageDialog(this, mensagemSucesso);
            carregarAtendimentos();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Erro ao criar Ordem de Serviço!\n\n" +
                "Verifique se o cliente possui um equipamento cadastrado.", 
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String getTipoExibicao(String tipo) {
        return TIPO_EXIBICAO.getOrDefault(tipo, tipo);
    }
}
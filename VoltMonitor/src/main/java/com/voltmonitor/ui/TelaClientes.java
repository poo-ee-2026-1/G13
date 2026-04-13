package com.voltmonitor.ui;

import com.voltmonitor.model.Cliente;
import com.voltmonitor.model.Equipamento;
import com.voltmonitor.repository.DatabaseManager;
import com.voltmonitor.service.MonitoramentoService;
import com.voltmonitor.util.Validador;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class TelaClientes extends JDialog {

    private JTable tabela;
    private DefaultTableModel modelo;
    private DatabaseManager db;
    private MonitoramentoService monitoramentoService;

    private static final Color BG = new Color(25, 35, 52);
    private static final Color FG = new Color(200, 215, 230);
    private static final Color ACCENT = new Color(0, 180, 120);

    public TelaClientes(Frame parent, MonitoramentoService ms) {
        super(parent, "Cadastro de Clientes", true);
        this.monitoramentoService = ms;
        try { db = DatabaseManager.getInstance(); } catch (SQLException e) { e.printStackTrace(); }
        inicializarUI();
        carregarDados();
        setSize(1100, 600);
        setLocationRelativeTo(parent);
    }

    private void inicializarUI() {
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(5, 5));

        String[] cols = {"ID", "Tipo", "Nome/Razão Social", "Documento", "Cidade/UF", "IP Local", "Monitorado", "Equipamento"};
        modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabela = new JTable(modelo);
        estilizarTabela(tabela);

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.getViewport().setBackground(new Color(20, 30, 45));

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        botoes.setBackground(BG);
        botoes.add(criarBotao("➕ Incluir", this::incluir));
        botoes.add(criarBotao("🗑 Excluir", this::excluir));
        botoes.add(criarBotao("📡 Alterar Monitoramento", this::alterarMonitoramento));

        JLabel titulo = new JLabel("  🏢 GERENCIAMENTO DE CLIENTES");
        titulo.setFont(new Font("Monospaced", Font.BOLD, 14));
        titulo.setForeground(ACCENT);
        titulo.setBorder(new EmptyBorder(10, 10, 5, 10));

        add(titulo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);
    }

    private void carregarDados() {
        try {
            modelo.setRowCount(0);
            List<Cliente> lista = db.listarClientes();
            List<Equipamento> eqs = db.listarEquipamentos();
            for (Cliente c : lista) {
                String eq = eqs.stream()
                    .filter(e -> e.getId() == c.getEquipamentoId())
                    .map(Equipamento::getModelo)
                    .findFirst().orElse("N/D");
                modelo.addRow(new Object[]{
                    c.getId(),
                    "PF".equals(c.getTipoPessoa()) ? "Pessoa Física" : "Pessoa Jurídica",
                    c.getNomeExibicao(),
                    c.getDocumento(),
                    c.getCidade() + "/" + c.getEstado(),
                    c.getIpLocal(),
                    c.isMonitorado() ? "Sim" : "Não",
                    eq
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void incluir() {
        try {
            List<Equipamento> equipamentos = db.listarEquipamentos();
            if (equipamentos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cadastre pelo menos um equipamento antes de incluir clientes.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JTabbedPane tabs = new JTabbedPane();

            // Aba Identificação
            JPanel pId = new JPanel(new GridLayout(0, 2, 5, 8));
            pId.setBorder(new EmptyBorder(10, 10, 10, 10));

            JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Pessoa Física", "Pessoa Jurídica"});
            JTextField txtNome = new JTextField(20);
            JTextField txtSobrenome = new JTextField(20);
            JTextField txtDocumento = new JTextField(20);
            JLabel lblSobrenomeL = new JLabel("Sobrenome*:");
            JLabel lblDocL = new JLabel("CPF*:");

            cmbTipo.addActionListener(e -> {
                boolean pf = "Pessoa Física".equals(cmbTipo.getSelectedItem());
                lblSobrenomeL.setEnabled(pf);
                txtSobrenome.setEnabled(pf);
                lblDocL.setText(pf ? "CPF*:" : "CNPJ*:");
            });

            pId.add(new JLabel("Tipo Pessoa*:")); pId.add(cmbTipo);
            pId.add(new JLabel("Nome / Razão Social*:")); pId.add(txtNome);
            pId.add(lblSobrenomeL); pId.add(txtSobrenome);
            pId.add(lblDocL); pId.add(txtDocumento);
            tabs.addTab("Identificação", pId);

            // Aba Endereço
            JPanel pEnd = new JPanel(new GridLayout(0, 2, 5, 8));
            pEnd.setBorder(new EmptyBorder(10, 10, 10, 10));

            JTextField txtLogradouro = new JTextField(20);
            JTextField txtNumero = new JTextField(10);
            JTextField txtBairro = new JTextField(20);
            JTextField txtCidade = new JTextField(20);
            JTextField txtEstado = new JTextField(5);
            JTextField txtCep = new JTextField(10);

            pEnd.add(new JLabel("Logradouro:")); pEnd.add(txtLogradouro);
            pEnd.add(new JLabel("Número:")); pEnd.add(txtNumero);
            pEnd.add(new JLabel("Bairro:")); pEnd.add(txtBairro);
            pEnd.add(new JLabel("Cidade:")); pEnd.add(txtCidade);
            pEnd.add(new JLabel("Estado (UF):")); pEnd.add(txtEstado);
            pEnd.add(new JLabel("CEP:")); pEnd.add(txtCep);
            tabs.addTab("Endereço", pEnd);

            // Aba Contato/Equipamento
            JPanel pCont = new JPanel(new GridLayout(0, 2, 5, 8));
            pCont.setBorder(new EmptyBorder(10, 10, 10, 10));

            JTextField txtDdd = new JTextField(3);
            JTextField txtTelefone = new JTextField(10);
            JTextField txtIp = new JTextField(15);
            JCheckBox chkMonitorado = new JCheckBox("Incluir na monitoração", true);

            // Filtro apenas números para DDD e telefone
            aplicarFiltroNumerico(txtDdd, 2);
            aplicarFiltroNumerico(txtTelefone, 9);

            String[] equipsNames = equipamentos.stream()
                .map(eq -> eq.getId() + " - " + eq.getMarca() + " " + eq.getModelo() + " (" + eq.getTensaoNominal() + "V)")
                .toArray(String[]::new);
            JComboBox<String> cmbEquip = new JComboBox<>(equipsNames);

            pCont.add(new JLabel("DDD (2 dígitos):")); pCont.add(txtDdd);
            pCont.add(new JLabel("Telefone (9 dígitos):")); pCont.add(txtTelefone);
            pCont.add(new JLabel("IP Local (padrão IPv4):")); pCont.add(txtIp);
            pCont.add(new JLabel("Equipamento*:")); pCont.add(cmbEquip);
            pCont.add(new JLabel("Monitoramento:")); pCont.add(chkMonitorado);
            tabs.addTab("Contato / Equipamento", pCont);

            int res = JOptionPane.showConfirmDialog(this, tabs, "Incluir Cliente",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (res != JOptionPane.OK_OPTION) return;

            boolean pf = "Pessoa Física".equals(cmbTipo.getSelectedItem());
            String documento = txtDocumento.getText().trim();

            if (pf && !Validador.validarCPF(documento)) {
                JOptionPane.showMessageDialog(this, "CPF inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!pf && !Validador.validarCNPJ(documento)) {
                JOptionPane.showMessageDialog(this, "CNPJ inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String ip = txtIp.getText().trim();
            if (!ip.isEmpty() && !Validador.validarIP(ip)) {
                JOptionPane.showMessageDialog(this, "IP inválido! Use formato xxx.xxx.xxx.xxx", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Cliente c = new Cliente();
            c.setTipoPessoa(pf ? "PF" : "PJ");
            c.setNome(txtNome.getText().trim());
            c.setSobrenome(pf ? txtSobrenome.getText().trim() : "");
            c.setDocumento(pf ? Validador.formatarCPF(documento) : Validador.formatarCNPJ(documento));
            c.setLogradouro(txtLogradouro.getText().trim());
            c.setNumero(txtNumero.getText().trim());
            c.setBairro(txtBairro.getText().trim());
            c.setCidade(txtCidade.getText().trim());
            c.setEstado(txtEstado.getText().trim().toUpperCase());
            c.setCep(txtCep.getText().trim());
            c.setDdd(txtDdd.getText().trim());
            c.setTelefone(txtTelefone.getText().trim());
            c.setIpLocal(ip);
            c.setMonitorado(chkMonitorado.isSelected());

            int equIdx = cmbEquip.getSelectedIndex();
            if (equIdx >= 0) c.setEquipamentoId(equipamentos.get(equIdx).getId());
            c.setAtivo(true);

            db.inserirCliente(c);
            monitoramentoService.carregarClientes();
            carregarDados();
            JOptionPane.showMessageDialog(this, "Cliente incluído com sucesso!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluir() {
        int row = tabela.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione um cliente."); return; }
        int id = (int) modelo.getValueAt(row, 0);
        String nome = modelo.getValueAt(row, 2).toString();
        int res = JOptionPane.showConfirmDialog(this, "Excluir cliente: " + nome + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            try {
                db.excluirCliente(id);
                monitoramentoService.carregarClientes();
                carregarDados();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void alterarMonitoramento() {
        int row = tabela.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione um cliente."); return; }
        int id = (int) modelo.getValueAt(row, 0);
        String atual = modelo.getValueAt(row, 6).toString();
        boolean novoEstado = "Não".equals(atual);
        try {
            db.atualizarMonitoramentoCliente(id, novoEstado);
            monitoramentoService.carregarClientes();
            carregarDados();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aplicarFiltroNumerico(JTextField tf, int maxDigitos) {
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String str, AttributeSet attr) throws BadLocationException {
                String nums = str.replaceAll("[^0-9]", "");
                if (fb.getDocument().getLength() + nums.length() <= maxDigitos)
                    super.insertString(fb, offset, nums, attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int len, String str, AttributeSet attrs) throws BadLocationException {
                String nums = str.replaceAll("[^0-9]", "");
                if (fb.getDocument().getLength() - len + nums.length() <= maxDigitos)
                    super.replace(fb, offset, len, nums, attrs);
            }
        });
    }

    private void estilizarTabela(JTable t) {
        t.setBackground(new Color(20, 30, 45));
        t.setForeground(FG);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setRowHeight(28);
        t.setGridColor(new Color(40, 55, 75));
        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(10, 20, 38));
        h.setForeground(ACCENT);
        h.setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    private JButton criarBotao(String texto, Runnable acao) {
        JButton btn = new JButton(texto);
        btn.setBackground(new Color(0, 100, 70));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> acao.run());
        return btn;
    }
}

package com.voltmonitor.ui;

import com.voltmonitor.model.Departamento;
import com.voltmonitor.model.Equipamento;
import com.voltmonitor.model.OrdemServico;
import com.voltmonitor.repository.DatabaseManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

// ============================================================
// TelaEquipamentos
// ============================================================
class TelaEquipamentos extends JDialog {
    private JTable tabela;
    private DefaultTableModel modelo;
    private DatabaseManager db;
    private static final Color BG = new Color(25, 35, 52);
    private static final Color FG = new Color(200, 215, 230);
    private static final Color ACCENT = new Color(0, 180, 120);

    public TelaEquipamentos(Frame parent) {
        super(parent, "Cadastro de Equipamentos", true);
        try { db = DatabaseManager.getInstance(); } catch (SQLException e) { e.printStackTrace(); }
        inicializarUI();
        carregarDados();
        setSize(700, 450);
        setLocationRelativeTo(parent);
    }

    private void inicializarUI() {
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(5, 5));
        String[] cols = {"ID", "Marca", "Modelo", "Tensão Nominal (V)"};
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

        JLabel titulo = new JLabel("  🔧 GERENCIAMENTO DE EQUIPAMENTOS");
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
            for (Equipamento e : db.listarEquipamentos())
                modelo.addRow(new Object[]{e.getId(), e.getMarca(), e.getModelo(), e.getTensaoNominal()});
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void incluir() {
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 8));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        JTextField txtMarca = new JTextField(15);
        JTextField txtModelo = new JTextField(15);
        JTextField txtTensao = new JTextField(10);
        form.add(new JLabel("Marca*:")); form.add(txtMarca);
        form.add(new JLabel("Modelo*:")); form.add(txtModelo);
        form.add(new JLabel("Tensão Nominal (1-1500V)*:")); form.add(txtTensao);

        int res = JOptionPane.showConfirmDialog(this, form, "Incluir Equipamento",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            double tensao = Double.parseDouble(txtTensao.getText().trim());
            if (tensao < 1 || tensao > 1500) {
                JOptionPane.showMessageDialog(this, "Tensão deve ser entre 1 e 1500V!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Equipamento eq = new Equipamento(txtMarca.getText().trim(), txtModelo.getText().trim(), tensao);
            db.inserirEquipamento(eq);
            carregarDados();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Tensão inválida! Use apenas números.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluir() {
        int row = tabela.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione um equipamento."); return; }
        int id = (int) modelo.getValueAt(row, 0);
        int res = JOptionPane.showConfirmDialog(this, "Excluir equipamento selecionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            try { db.excluirEquipamento(id); carregarDados(); }
            catch (SQLException e) { JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage()); }
        }
    }

    private void estilizarTabela(JTable t) {
        t.setBackground(new Color(20, 30, 45)); t.setForeground(FG);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12)); t.setRowHeight(28);
        t.setGridColor(new Color(40, 55, 75));
        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(10, 20, 38)); h.setForeground(ACCENT);
        h.setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    private JButton criarBotao(String texto, Runnable acao) {
        JButton btn = new JButton(texto);
        btn.setBackground(new Color(0, 100, 70)); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(8, 15, 8, 15)); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> acao.run());
        return btn;
    }
}

// ============================================================
// TelaDepartamentos
// ============================================================
class TelaDepartamentos extends JDialog {
    private JTable tabela;
    private DefaultTableModel modelo;
    private DatabaseManager db;
    private static final Color BG = new Color(25, 35, 52);
    private static final Color FG = new Color(200, 215, 230);
    private static final Color ACCENT = new Color(0, 180, 120);

    public TelaDepartamentos(Frame parent) {
        super(parent, "Cadastro de Departamentos", true);
        try { db = DatabaseManager.getInstance(); } catch (SQLException e) { e.printStackTrace(); }
        inicializarUI(); carregarDados();
        setSize(600, 400); setLocationRelativeTo(parent);
    }

    private void inicializarUI() {
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(5, 5));
        String[] cols = {"ID", "Nome do Departamento", "Descrição"};
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

        JLabel titulo = new JLabel("  🏭 GERENCIAMENTO DE DEPARTAMENTOS");
        titulo.setFont(new Font("Monospaced", Font.BOLD, 14));
        titulo.setForeground(ACCENT); titulo.setBorder(new EmptyBorder(10, 10, 5, 10));

        add(titulo, BorderLayout.NORTH); add(scroll, BorderLayout.CENTER); add(botoes, BorderLayout.SOUTH);
    }

    private void carregarDados() {
        try { modelo.setRowCount(0);
            for (Departamento d : db.listarDepartamentos())
                modelo.addRow(new Object[]{d.getId(), d.getNome(), d.getDescricao()});
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void incluir() {
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 8));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        JTextField txtNome = new JTextField(20); JTextField txtDesc = new JTextField(30);
        form.add(new JLabel("Nome*:")); form.add(txtNome);
        form.add(new JLabel("Descrição:")); form.add(txtDesc);
        int res = JOptionPane.showConfirmDialog(this, form, "Incluir Departamento",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        if (txtNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do departamento.", "Erro", JOptionPane.ERROR_MESSAGE); return;
        }
        try { db.inserirDepartamento(new Departamento(txtNome.getText().trim(), txtDesc.getText().trim())); carregarDados(); }
        catch (SQLException e) { JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage()); }
    }

    private void excluir() {
        int row = tabela.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione um departamento."); return; }
        int id = (int) modelo.getValueAt(row, 0);
        int res = JOptionPane.showConfirmDialog(this, "Excluir departamento selecionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            try { db.excluirDepartamento(id); carregarDados(); }
            catch (SQLException e) { JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage()); }
        }
    }

    private void estilizarTabela(JTable t) {
        t.setBackground(new Color(20, 30, 45)); t.setForeground(FG);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12)); t.setRowHeight(28);
        t.setGridColor(new Color(40, 55, 75));
        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(10, 20, 38)); h.setForeground(ACCENT);
        h.setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    private JButton criarBotao(String texto, Runnable acao) {
        JButton btn = new JButton(texto);
        btn.setBackground(new Color(0, 100, 70)); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(8, 15, 8, 15)); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> acao.run());
        return btn;
    }
}

// ============================================================
// TelaOrdensServico
// ============================================================
class TelaOrdensServico extends JDialog {
    private JTable tabela;
    private DefaultTableModel modelo;
    private DatabaseManager db;
    private static final Color BG = new Color(25, 35, 52);
    private static final Color FG = new Color(200, 215, 230);
    private static final Color ACCENT = new Color(0, 180, 120);

    public TelaOrdensServico(Frame parent) {
        super(parent, "Ordens de Serviço", true);
        try { db = DatabaseManager.getInstance(); } catch (SQLException e) { e.printStackTrace(); }
        inicializarUI(); carregarDados();
        setSize(1000, 550); setLocationRelativeTo(parent);
    }

    private void inicializarUI() {
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(5, 5));
        String[] cols = {"OS#", "Cliente", "Equipamento", "Motivo", "Situação", "Tensão (V)", "Status", "Destino", "Abertura"};
        modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabela = new JTable(modelo);
        tabela.setBackground(new Color(20, 30, 45)); tabela.setForeground(FG);
        tabela.setFont(new Font("SansSerif", Font.PLAIN, 11)); tabela.setRowHeight(28);
        tabela.setGridColor(new Color(40, 55, 75));
        JTableHeader h = tabela.getTableHeader();
        h.setBackground(new Color(10, 20, 38)); h.setForeground(ACCENT);
        h.setFont(new Font("SansSerif", Font.BOLD, 11));

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.getViewport().setBackground(new Color(20, 30, 45));

        JLabel titulo = new JLabel("  📋 ORDENS DE SERVIÇO - SUPORTE TÉCNICO");
        titulo.setFont(new Font("Monospaced", Font.BOLD, 14));
        titulo.setForeground(new Color(255, 200, 50)); titulo.setBorder(new EmptyBorder(10, 10, 5, 10));

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        botoes.setBackground(BG);
        JButton btnAtualizar = new JButton("🔄 Atualizar");
        btnAtualizar.addActionListener(e -> carregarDados());
        btnAtualizar.setBackground(new Color(0, 100, 70)); btnAtualizar.setForeground(Color.WHITE);
        btnAtualizar.setBorder(new EmptyBorder(8, 15, 8, 15)); btnAtualizar.setFocusPainted(false);
        botoes.add(btnAtualizar);

        add(titulo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);
    }

    private void carregarDados() {
        try {
            modelo.setRowCount(0);
            List<OrdemServico> lista = db.listarOrdensServico();
            for (OrdemServico os : lista) {
                modelo.addRow(new Object[]{
                    "#" + os.getId(), os.getNomeCliente(), os.getModeloEquipamento(),
                    os.getMotivoDescricao(), os.getSituacaoRede(),
                    String.format("%.2f", os.getUltimaTensao()),
                    os.getStatus().name(), os.getDepartamentoDestino(), os.getDataAberturaFormatada()
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}

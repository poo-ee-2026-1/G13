package com.voltmonitor.ui;

import com.voltmonitor.model.Usuario;
import com.voltmonitor.repository.DatabaseManager;
import com.voltmonitor.util.Validador;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class TelaUsuarios extends JDialog {

    private JTable tabela;
    private DefaultTableModel modelo;
    private DatabaseManager db;

    private static final Color BG = new Color(25, 35, 52);
    private static final Color FG = new Color(200, 215, 230);
    private static final Color ACCENT = new Color(0, 180, 120);

    public TelaUsuarios(Frame parent) {
        super(parent, "Cadastro de Usuários", true);
        try { db = DatabaseManager.getInstance(); } catch (SQLException e) { e.printStackTrace(); }
        inicializarUI();
        carregarDados();
        setSize(900, 550);
        setLocationRelativeTo(parent);
    }

    private void inicializarUI() {
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(5, 5));

        // Tabela
        String[] cols = {"ID", "Nome", "Sobrenome", "CPF", "Matrícula", "Função", "Login"};
        modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabela = new JTable(modelo);
        estilizarTabela(tabela);

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(new Color(20, 30, 45));

        // Botões
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        botoes.setBackground(BG);
        botoes.add(criarBotao("➕ Incluir", this::incluir));
        botoes.add(criarBotao("🗑 Excluir", this::excluir));

        JLabel titulo = new JLabel("  👥 GERENCIAMENTO DE USUÁRIOS");
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
            List<Usuario> lista = db.listarUsuarios();
            for (Usuario u : lista) {
                modelo.addRow(new Object[]{
                    u.getId(), u.getNome(), u.getSobrenome(), u.getCpf(),
                    u.getMatricula(), u.getFuncao(), u.getLogin()
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void incluir() {
        JPanel form = criarFormulario();
        JTextField txtNome = (JTextField) form.getClientProperty("nome");
        JTextField txtSobrenome = (JTextField) form.getClientProperty("sobrenome");
        JTextField txtCpf = (JTextField) form.getClientProperty("cpf");
        JTextField txtMatricula = (JTextField) form.getClientProperty("matricula");
        JComboBox<?> cmbFuncao = (JComboBox<?>) form.getClientProperty("funcao");
        JTextField txtLogin = (JTextField) form.getClientProperty("login");
        JPasswordField txtSenha = (JPasswordField) form.getClientProperty("senha");

        int res = JOptionPane.showConfirmDialog(this, form, "Incluir Usuário",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res != JOptionPane.OK_OPTION) return;

        String cpf = txtCpf.getText().trim();
        if (!Validador.validarCPF(cpf)) {
            JOptionPane.showMessageDialog(this, "CPF inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Usuario u = new Usuario();
        u.setNome(txtNome.getText().trim());
        u.setSobrenome(txtSobrenome.getText().trim());
        u.setCpf(Validador.formatarCPF(cpf));
        u.setMatricula(txtMatricula.getText().trim());
        u.setFuncao(cmbFuncao.getSelectedItem().toString());
        u.setLogin(txtLogin.getText().trim());
        u.setSenhaHash(new String(txtSenha.getPassword()));

        if (u.getNome().isEmpty() || u.getLogin().isEmpty() || u.getSenhaHash().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            db.inserirUsuario(u);
            carregarDados();
            JOptionPane.showMessageDialog(this, "Usuário incluído com sucesso!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluir() {
        int row = tabela.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário para excluir.");
            return;
        }
        int id = (int) modelo.getValueAt(row, 0);
        String nome = modelo.getValueAt(row, 1) + " " + modelo.getValueAt(row, 2);
        int res = JOptionPane.showConfirmDialog(this,
            "Excluir usuário: " + nome + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            try {
                db.excluirUsuario(id);
                carregarDados();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel criarFormulario() {
        JPanel p = new JPanel(new GridLayout(0, 2, 5, 8));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField txtNome = new JTextField(15);
        JTextField txtSobrenome = new JTextField(15);
        JTextField txtCpf = new JTextField(15);
        JTextField txtMatricula = new JTextField(15);
        JComboBox<String> cmbFuncao = new JComboBox<>(new String[]{"Administrador", "Monitor"});
        JTextField txtLogin = new JTextField(15);
        JPasswordField txtSenha = new JPasswordField(15);

        p.add(new JLabel("Nome*:")); p.add(txtNome);
        p.add(new JLabel("Sobrenome*:")); p.add(txtSobrenome);
        p.add(new JLabel("CPF*:")); p.add(txtCpf);
        p.add(new JLabel("Matrícula*:")); p.add(txtMatricula);
        p.add(new JLabel("Função*:")); p.add(cmbFuncao);
        p.add(new JLabel("Login*:")); p.add(txtLogin);
        p.add(new JLabel("Senha*:")); p.add(txtSenha);

        p.putClientProperty("nome", txtNome);
        p.putClientProperty("sobrenome", txtSobrenome);
        p.putClientProperty("cpf", txtCpf);
        p.putClientProperty("matricula", txtMatricula);
        p.putClientProperty("funcao", cmbFuncao);
        p.putClientProperty("login", txtLogin);
        p.putClientProperty("senha", txtSenha);

        return p;
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
        btn.setBackground(new Color(0, 120, 80));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> acao.run());
        return btn;
    }
}

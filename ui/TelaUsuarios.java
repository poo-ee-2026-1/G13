package com.monitoramento.ui;

import com.monitoramento.model.Usuario;
import com.monitoramento.dao.UsuarioDAO;
import com.monitoramento.util.ValidadorCPF;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class TelaUsuarios extends JFrame {
    private JTable tabelaUsuarios;
    private DefaultTableModel tableModel;
    private UsuarioDAO usuarioDAO;
    private Usuario usuarioLogado;
    
    private JTextField txtId, txtNome, txtSobrenome, txtCpf, txtMatricula, txtLogin, txtSenha;
    private JComboBox<String> comboFuncao;
    private JButton btnInserir, btnAtualizar, btnExcluir, btnLimpar;
    
    public TelaUsuarios(Usuario usuario) {
        this.usuarioLogado = usuario;
        this.usuarioDAO = new UsuarioDAO();
        
        if (!usuario.getFuncao().equals("Administrador")) {
            JOptionPane.showMessageDialog(this, "Apenas administradores podem gerenciar usu�rios!");
            dispose();
            return;
        }
        
        initComponents();
        carregarUsuarios();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("Gerenciamento de Usu�rios - Administrador: " + usuarioLogado.getLogin());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLayout(new BorderLayout());
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        
        String[] colunas = {"ID", "Nome", "CPF", "Matrícula", "Função", "Login"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaUsuarios = new JTable(tableModel);
        tabelaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                carregarUsuarioSelecionado();
            }
        });
        
        JScrollPane scrollTabela = new JScrollPane(tabelaUsuarios);
        
        JPanel panelForm = criarPainelFormulario();
        
        splitPane.setTopComponent(scrollTabela);
        splitPane.setBottomComponent(panelForm);
        
        add(splitPane, BorderLayout.CENTER);
        
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton btnNovo = new JButton("Novo");
        btnNovo.addActionListener(e -> limparFormulario());
        
        JButton btnRefresh = new JButton("Atualizar");
        btnRefresh.addActionListener(e -> carregarUsuarios());
        
        JButton btnVoltar = new JButton("Voltar");
        btnVoltar.addActionListener(e -> dispose());
        
        toolBar.add(btnNovo);
        toolBar.add(btnRefresh);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(btnVoltar);
        
        add(toolBar, BorderLayout.NORTH);
    }
    
    private JPanel criarPainelFormulario() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Dados do Usuário"));
        
        JPanel panelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panelCampos.add(new JLabel("ID:"), gbc);
        txtId = new JTextField(10);
        txtId.setEditable(false);
        gbc.gridx = 1;
        panelCampos.add(txtId, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panelCampos.add(new JLabel("Nome:*"), gbc);
        txtNome = new JTextField(20);
        gbc.gridx = 1;
        panelCampos.add(txtNome, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panelCampos.add(new JLabel("Sobrenome:*"), gbc);
        txtSobrenome = new JTextField(20);
        gbc.gridx = 1;
        panelCampos.add(txtSobrenome, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panelCampos.add(new JLabel("CPF:*"), gbc);
        txtCpf = new JTextField(14);
        gbc.gridx = 1;
        panelCampos.add(txtCpf, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        panelCampos.add(new JLabel("Matrícula:*"), gbc);
        txtMatricula = new JTextField(15);
        gbc.gridx = 1;
        panelCampos.add(txtMatricula, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        panelCampos.add(new JLabel("Função:*"), gbc);
        comboFuncao = new JComboBox<>(new String[]{"Administrador", "Monitor", "Técnico"});
        gbc.gridx = 1;
        panelCampos.add(comboFuncao, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        panelCampos.add(new JLabel("Login:*"), gbc);
        txtLogin = new JTextField(15);
        gbc.gridx = 1;
        panelCampos.add(txtLogin, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7;
        panelCampos.add(new JLabel("Senha:*"), gbc);
        txtSenha = new JPasswordField(15);
        gbc.gridx = 1;
        panelCampos.add(txtSenha, gbc);
        
        JPanel panelBotoes = new JPanel(new FlowLayout());
        btnInserir = new JButton("Inserir");
        btnAtualizar = new JButton("Atualizar");
        btnExcluir = new JButton("Excluir");
        btnLimpar = new JButton("Limpar");
        
        btnInserir.addActionListener(e -> inserirUsuario());
        btnAtualizar.addActionListener(e -> atualizarUsuario());
        btnExcluir.addActionListener(e -> excluirUsuario());
        btnLimpar.addActionListener(e -> limparFormulario());
        
        panelBotoes.add(btnInserir);
        panelBotoes.add(btnAtualizar);
        panelBotoes.add(btnExcluir);
        panelBotoes.add(btnLimpar);
        
        panel.add(panelCampos, BorderLayout.CENTER);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void carregarUsuarios() {
        tableModel.setRowCount(0);
        List<Usuario> usuarios = usuarioDAO.listarTodos();
        
        for (Usuario u : usuarios) {
            Object[] row = {
                u.getId(), u.getNomeCompleto(), u.getCpf(), 
                u.getMatricula(), u.getFuncao(), u.getLogin()
            };
            tableModel.addRow(row);
        }
    }
    
    private void carregarUsuarioSelecionado() {
        int row = tabelaUsuarios.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);
            Usuario u = usuarioDAO.buscarPorId(id);
            if (u != null) {
                txtId.setText(String.valueOf(u.getId()));
                txtNome.setText(u.getNome());
                txtSobrenome.setText(u.getSobrenome());
                txtCpf.setText(u.getCpf());
                txtMatricula.setText(u.getMatricula());
                comboFuncao.setSelectedItem(u.getFuncao());
                txtLogin.setText(u.getLogin());
                txtSenha.setText(u.getSenha());
            }
        }
    }
    
    private void inserirUsuario() {
        if (!validarCampos()) return;
        
        Usuario usuario = new Usuario();
        preencherUsuario(usuario);
        
        if (usuarioDAO.inserir(usuario)) {
            JOptionPane.showMessageDialog(this, "Usuário inserido com sucesso!");
            carregarUsuarios();
            limparFormulario();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao inserir usuário!\nVerifique se CPF, matrícula ou login já existem.", 
                                        "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void atualizarUsuario() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um usu�rio para atualizar!");
            return;
        }
        
        if (!validarCampos()) return;
        
        Usuario usuario = new Usuario();
        usuario.setId(Integer.parseInt(txtId.getText()));
        preencherUsuario(usuario);
        
        if (usuarioDAO.atualizar(usuario)) {
            JOptionPane.showMessageDialog(this, "Usuário atualizado com sucesso!");
            carregarUsuarios();
            limparFormulario();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar usuário!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void excluirUsuario() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário para excluir!");
            return;
        }
        
        int id = Integer.parseInt(txtId.getText());
        if (id == usuarioLogado.getId()) {
            JOptionPane.showMessageDialog(this, "Você não pode excluir seu pr�prio usuário!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Deseja realmente excluir este usu�rio?", 
            "Confirmar exclus�o", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (usuarioDAO.excluir(id)) {
                JOptionPane.showMessageDialog(this, "Usuário excluído com sucesso!");
                carregarUsuarios();
                limparFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao excluir usuário!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void preencherUsuario(Usuario usuario) {
        usuario.setNome(txtNome.getText().trim());
        usuario.setSobrenome(txtSobrenome.getText().trim());
        usuario.setCpf(txtCpf.getText().trim());
        usuario.setMatricula(txtMatricula.getText().trim());
        usuario.setFuncao((String) comboFuncao.getSelectedItem());
        usuario.setLogin(txtLogin.getText().trim());
        usuario.setSenha(txtSenha.getText().trim());
    }
    
    private boolean validarCampos() {
        if (txtNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome é obrigatório!");
            return false;
        }
        
        if (txtSobrenome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sobrenome é obrigatório!");
            return false;
        }
        
        String cpf = txtCpf.getText().trim();
        if (!ValidadorCPF.validar(cpf)) {
            JOptionPane.showMessageDialog(this, "CPF inválido!");
            return false;
        }
        
        if (txtMatricula.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Matrícula é obrigatória!");
            return false;
        }
        
        if (txtLogin.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Login é obrigatório!");
            return false;
        }
        
        String senha = txtSenha.getText().trim();
        if (senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Senha é obrigatória!");
            return false;
        }
        
        if (senha.length() < 6) {
            JOptionPane.showMessageDialog(this, "Senha deve ter no mínimo 6 caracteres!");
            return false;
        }
        
        return true;
    }
    
    private void limparFormulario() {
        txtId.setText("");
        txtNome.setText("");
        txtSobrenome.setText("");
        txtCpf.setText("");
        txtMatricula.setText("");
        comboFuncao.setSelectedIndex(0);
        txtLogin.setText("");
        txtSenha.setText("");
        txtNome.requestFocus();
    }
}
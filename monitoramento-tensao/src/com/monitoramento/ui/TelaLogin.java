// TelaLogin.java
package com.monitoramento.ui;

import com.monitoramento.model.Usuario;
import com.monitoramento.dao.UsuarioDAO;  // Corrigido: import do dao package
import javax.swing.*;
import java.awt.*;

public class TelaLogin extends JFrame {
    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private UsuarioDAO usuarioDAO;
    
    public TelaLogin() {
        usuarioDAO = new UsuarioDAO();
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Sistema de Monitoração - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Título
        JLabel lblTitulo = new JLabel("Sistema de Monitoração de Tensão");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(lblTitulo, gbc);
        
        // Login
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Login:"), gbc);
        txtLogin = new JTextField(15);
        gbc.gridx = 1;
        panel.add(txtLogin, gbc);
        
        // Senha
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Senha:"), gbc);
        txtSenha = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(txtSenha, gbc);
        
        // Botões
        JButton btnLogin = new JButton("Entrar");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnLogin.addActionListener(e -> realizarLogin());
        btnCancelar.addActionListener(e -> System.exit(0));
        
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.insets = new Insets(20, 5, 5, 5);
        panel.add(btnLogin, gbc);
        gbc.gridx = 1;
        panel.add(btnCancelar, gbc);
        
        // Enter key
        getRootPane().setDefaultButton(btnLogin);
        
        add(panel);
    }
    
    private void realizarLogin() {
        String login = txtLogin.getText();
        String senha = new String(txtSenha.getPassword());
        
        if (login.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos!");
            return;
        }
        
        Usuario usuario = usuarioDAO.autenticar(login, senha);
        if (usuario != null) {
            JOptionPane.showMessageDialog(this, "Bem-vindo, " + usuario.getNomeCompleto() + "!");
            new TelaPrincipal(usuario).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Login ou senha inválidos!");
            txtLogin.setText("");
            txtSenha.setText("");
            txtLogin.requestFocus();
        }
    }
}
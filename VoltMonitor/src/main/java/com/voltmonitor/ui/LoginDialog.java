package com.voltmonitor.ui;

import com.voltmonitor.model.Usuario;
import com.voltmonitor.repository.DatabaseManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

public class LoginDialog extends JDialog {

    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JButton btnEntrar;
    private JButton btnCancelar;
    private JLabel lblErro;
    private Usuario usuarioLogado;

    public LoginDialog(Frame parent) {
        super(parent, "VoltMonitor - Login", true);
        inicializarUI();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void inicializarUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBackground(new Color(30, 40, 55));
        painel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Cabeçalho
        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5));
        header.setOpaque(false);
        JLabel titulo = new JLabel("⚡ VOLTMONITOR", SwingConstants.CENTER);
        titulo.setFont(new Font("Monospaced", Font.BOLD, 22));
        titulo.setForeground(new Color(0, 200, 150));
        JLabel subtitulo = new JLabel("Sistema de Monitoração de Tensão", SwingConstants.CENTER);
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitulo.setForeground(new Color(150, 170, 190));
        header.add(titulo);
        header.add(subtitulo);

        // Formulário
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        JLabel lblLoginL = criarLabel("Usuário:");
        txtLogin = criarTextField();
        JLabel lblSenhaL = criarLabel("Senha:");
        txtSenha = new JPasswordField(20);
        estilizarCampo(txtSenha);

        lblErro = new JLabel(" ");
        lblErro.setForeground(new Color(255, 80, 80));
        lblErro.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblErro.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(lblLoginL, gbc);
        gbc.gridy = 1;
        form.add(txtLogin, gbc);
        gbc.gridy = 2;
        form.add(lblSenhaL, gbc);
        gbc.gridy = 3;
        form.add(txtSenha, gbc);
        gbc.gridy = 4;
        form.add(lblErro, gbc);

        // Botões
        JPanel botoes = new JPanel(new GridLayout(1, 2, 10, 0));
        botoes.setOpaque(false);
        btnEntrar = criarBotao("ENTRAR", new Color(0, 160, 100));
        btnCancelar = criarBotao("CANCELAR", new Color(120, 50, 50));
        botoes.add(btnEntrar);
        botoes.add(btnCancelar);

        painel.add(header, BorderLayout.NORTH);
        painel.add(form, BorderLayout.CENTER);
        painel.add(botoes, BorderLayout.SOUTH);

        add(painel);

        // Ações
        btnEntrar.addActionListener(e -> autenticar());
        btnCancelar.addActionListener(e -> {
            usuarioLogado = null;
            dispose();
        });

        txtSenha.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) autenticar();
            }
        });

        txtLogin.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) txtSenha.requestFocus();
            }
        });
    }

    private void autenticar() {
        String login = txtLogin.getText().trim();
        String senha = new String(txtSenha.getPassword());

        if (login.isEmpty() || senha.isEmpty()) {
            lblErro.setText("Informe usuário e senha.");
            return;
        }

        try {
            DatabaseManager db = DatabaseManager.getInstance();
            Usuario[] result = new Usuario[1];
            boolean ok = db.autenticarUsuario(login, senha, result);
            if (ok) {
                usuarioLogado = result[0];
                dispose();
            } else {
                lblErro.setText("Usuário ou senha inválidos.");
                txtSenha.setText("");
                txtSenha.requestFocus();
            }
        } catch (SQLException ex) {
            lblErro.setText("Erro ao conectar ao banco de dados.");
            ex.printStackTrace();
        }
    }

    private JLabel criarLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(new Color(180, 200, 220));
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        return l;
    }

    private JTextField criarTextField() {
        JTextField tf = new JTextField(20);
        estilizarCampo(tf);
        return tf;
    }

    private void estilizarCampo(JTextField tf) {
        tf.setBackground(new Color(45, 58, 78));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 100), 1),
            new EmptyBorder(5, 8, 5, 8)
        ));
    }

    private JButton criarBotao(String texto, Color cor) {
        JButton btn = new JButton(texto);
        btn.setBackground(cor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }
}

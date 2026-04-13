package com.voltmonitor;

import com.voltmonitor.model.Usuario;
import com.voltmonitor.ui.LoginDialog;
import com.voltmonitor.ui.TelaPrincipal;

import javax.swing.*;
import java.awt.*;

/**
 * ============================================================
 * VoltMonitor - Sistema de Monitoração de Tensão Elétrica
 * ============================================================
 * Ponto de entrada da aplicação.
 *
 * Usuário padrão inicial:
 *   Login: admin
 *   Senha: admin123
 *   Função: Administrador
 *
 * Servidor WebSocket: porta 8765
 * ESP32 deve enviar JSON: {"ip":"x.x.x.x","tensao":19.05}
 * ============================================================
 */
public class Main {

    public static void main(String[] args) {
        // Configurar Look and Feel do sistema
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            // Personalizar cores globais do Swing
            UIManager.put("Panel.background", new Color(25, 35, 52));
            UIManager.put("OptionPane.background", new Color(25, 35, 52));
            UIManager.put("OptionPane.messageForeground", new Color(200, 215, 230));
            UIManager.put("Button.background", new Color(0, 100, 70));
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("TextField.background", new Color(45, 58, 78));
            UIManager.put("TextField.foreground", Color.WHITE);
            UIManager.put("TextField.caretForeground", Color.WHITE);
            UIManager.put("PasswordField.background", new Color(45, 58, 78));
            UIManager.put("PasswordField.foreground", Color.WHITE);
            UIManager.put("ComboBox.background", new Color(45, 58, 78));
            UIManager.put("ComboBox.foreground", Color.WHITE);
            UIManager.put("Label.foreground", new Color(200, 215, 230));
            UIManager.put("CheckBox.background", new Color(25, 35, 52));
            UIManager.put("CheckBox.foreground", new Color(200, 215, 230));
            UIManager.put("TabbedPane.background", new Color(25, 35, 52));
            UIManager.put("TabbedPane.foreground", new Color(200, 215, 230));
            UIManager.put("TabbedPane.selected", new Color(40, 55, 78));
            UIManager.put("ScrollPane.background", new Color(20, 30, 45));
            UIManager.put("ScrollBar.background", new Color(20, 30, 45));
            UIManager.put("ScrollBar.thumb", new Color(60, 90, 120));
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            // Exibir tela de login
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);

            Usuario usuario = login.getUsuarioLogado();
            if (usuario == null) {
                // Usuário cancelou o login
                System.exit(0);
                return;
            }

            // Iniciar tela principal com usuário autenticado
            TelaPrincipal telaPrincipal = new TelaPrincipal(usuario);
            telaPrincipal.setVisible(true);
        });
    }
}

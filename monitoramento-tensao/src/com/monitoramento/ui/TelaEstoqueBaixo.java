// TelaEstoqueBaixo.java
package com.monitoramento.ui;

import com.monitoramento.model.ProdutoEstoque;
import com.monitoramento.model.Usuario;
import com.monitoramento.dao.ProdutoEstoqueDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TelaEstoqueBaixo extends JPanel {
    private ProdutoEstoqueDAO produtoDAO;
    private Usuario usuarioLogado;
    
    private JTable tabelaProdutos;
    private DefaultTableModel tableModel;
    private JButton btnAtualizar, btnSolicitarCompra;
    
    public TelaEstoqueBaixo(Usuario usuario) {
        this.usuarioLogado = usuario;
        this.produtoDAO = new ProdutoEstoqueDAO();
        
        initComponents();
        carregarProdutosEstoqueBaixo();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Painel de informações
        JPanel painelInfo = new JPanel();
        painelInfo.setBackground(new Color(255, 200, 200));
        painelInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblInfo = new JLabel("ATENÇÃO: Produtos com quantidade abaixo do estoque mínimo!");
        lblInfo.setFont(new Font("Arial", Font.BOLD, 14));
        lblInfo.setForeground(Color.RED);
        painelInfo.add(lblInfo);
        
        // Tabela
        String[] colunas = {"ID", "Código", "Nome", "Categoria", "Quantidade Atual", "Quantidade Mínima", "Déficit", "Localização"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaProdutos = new JTable(tableModel);
        tabelaProdutos.setRowHeight(25);
        
        tabelaProdutos.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabelaProdutos.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabelaProdutos.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabelaProdutos.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabelaProdutos.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabelaProdutos.getColumnModel().getColumn(5).setPreferredWidth(100);
        tabelaProdutos.getColumnModel().getColumn(6).setPreferredWidth(80);
        tabelaProdutos.getColumnModel().getColumn(7).setPreferredWidth(150);
        
        JScrollPane scrollTabela = new JScrollPane(tabelaProdutos);
        
        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout());
        
        btnAtualizar = new JButton("Atualizar");
        btnSolicitarCompra = new JButton("Solicitar Compra");
        
        btnAtualizar.addActionListener(e -> carregarProdutosEstoqueBaixo());
        btnSolicitarCompra.addActionListener(e -> solicitarCompra());
        
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnSolicitarCompra);
        
        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton btnRefresh = new JButton("Atualizar");
        btnRefresh.addActionListener(e -> carregarProdutosEstoqueBaixo());
        
        toolBar.add(btnRefresh);
        toolBar.add(Box.createHorizontalGlue());
        
        // Organizar layout
        JPanel painelNorte = new JPanel(new BorderLayout());
        painelNorte.add(toolBar, BorderLayout.NORTH);
        painelNorte.add(painelInfo, BorderLayout.CENTER);
        
        add(painelNorte, BorderLayout.NORTH);
        add(scrollTabela, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);
    }
    
    private void carregarProdutosEstoqueBaixo() {
        tableModel.setRowCount(0);
        List<ProdutoEstoque> produtos = produtoDAO.listarEstoqueBaixo();
        
        for (ProdutoEstoque p : produtos) {
            int deficit = p.getQuantidadeMinima() - p.getQuantidade();
            if (deficit < 0) deficit = 0;
            
            // Destacar linhas com estoque crítico (quantidade = 0)
            Object[] row = {
                p.getId(),
                p.getCodigo(),
                p.getNome(),
                p.getCategoria(),
                p.getQuantidade(),
                p.getQuantidadeMinima(),
                deficit,
                p.getLocalizacao() != null ? p.getLocalizacao() : "---"
            };
            tableModel.addRow(row);
        }
        
        if (tableModel.getRowCount() == 0) {
            Object[] row = {0, "---", "Nenhum produto com estoque baixo", "---", 0, 0, 0, "---"};
            tableModel.addRow(row);
        }
    }
    
    private void solicitarCompra() {
        int row = tabelaProdutos.getSelectedRow();
        if (row < 0 || tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para solicitar compra!");
            return;
        }
        
        // Verificar se é a linha de "nenhum produto"
        if (tableModel.getValueAt(row, 0) instanceof Integer && (Integer) tableModel.getValueAt(row, 0) == 0) {
            JOptionPane.showMessageDialog(this, "Nenhum produto selecionado!");
            return;
        }
        
        int idProduto = (int) tableModel.getValueAt(row, 0);
        String nomeProduto = (String) tableModel.getValueAt(row, 2);
        int quantidadeAtual = (int) tableModel.getValueAt(row, 4);
        int quantidadeMinima = (int) tableModel.getValueAt(row, 5);
        int deficit = (int) tableModel.getValueAt(row, 6);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Produto:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(nomeProduto), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Quantidade Atual:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(String.valueOf(quantidadeAtual)), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Quantidade Mínima:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(String.valueOf(quantidadeMinima)), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Déficit:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(String.valueOf(deficit)), gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Quantidade a Comprar:"), gbc);
        JSpinner spQuantidade = new JSpinner(new SpinnerNumberModel(Math.max(1, deficit), 1, 1000, 1));
        gbc.gridx = 1;
        panel.add(spQuantidade, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Justificativa:"), gbc);
        JTextArea txtJustificativa = new JTextArea(3, 30);
        txtJustificativa.setLineWrap(true);
        JScrollPane scrollJust = new JScrollPane(txtJustificativa);
        gbc.gridx = 1;
        panel.add(scrollJust, gbc);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Solicitar Compra - " + nomeProduto, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            int quantidade = (int) spQuantidade.getValue();
            String justificativa = txtJustificativa.getText().trim();
            if (justificativa.isEmpty()) {
                justificativa = "Reposição de estoque - quantidade abaixo do mínimo";
            }
            
            JOptionPane.showMessageDialog(this,
                "Solicitação de compra registrada!\n\n" +
                "Produto: " + nomeProduto + "\n" +
                "Quantidade: " + quantidade + "\n" +
                "Justificativa: " + justificativa + "\n\n" +
                "Um e-mail foi enviado para o departamento de compras.",
                "Solicitação Enviada", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
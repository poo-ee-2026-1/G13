// TelaFuncoes.java
package com.monitoramento.ui;

import com.monitoramento.model.Funcao;
import com.monitoramento.model.Usuario;
import com.monitoramento.dao.FuncaoDAO;
import com.monitoramento.dao.DepartamentoDAO;
import com.monitoramento.model.Departamento;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class TelaFuncoes extends JPanel {
    private JTable tabelaFuncoes;
    private DefaultTableModel tableModel;
    private FuncaoDAO funcaoDAO;
    private DepartamentoDAO departamentoDAO;
    private Usuario usuarioLogado;
    
    private JTextField txtId, txtNome, txtDescricao;
    private JComboBox<String> comboDepartamento;
    private JButton btnInserir, btnAtualizar, btnExcluir, btnLimpar;
    
    public TelaFuncoes(Usuario usuario) {
        this.usuarioLogado = usuario;
        this.funcaoDAO = new FuncaoDAO();
        this.departamentoDAO = new DepartamentoDAO();
        
        initComponents();
        carregarFuncoes();
        carregarDepartamentosCombo();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        
        String[] colunas = {"ID", "Nome", "Descrição", "Departamento"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaFuncoes = new JTable(tableModel);
        tabelaFuncoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaFuncoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                carregarFuncaoSelecionada();
            }
        });
        
        JScrollPane scrollTabela = new JScrollPane(tabelaFuncoes);
        
        JPanel panelForm = criarPainelFormulario();
        
        splitPane.setTopComponent(scrollTabela);
        splitPane.setBottomComponent(panelForm);
        
        add(splitPane, BorderLayout.CENTER);
        
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton btnNovo = new JButton("Novo");
        btnNovo.addActionListener(e -> limparFormulario());
        
        JButton btnRefresh = new JButton("Atualizar");
        btnRefresh.addActionListener(e -> carregarFuncoes());
        
        toolBar.add(btnNovo);
        toolBar.add(btnRefresh);
        toolBar.add(Box.createHorizontalGlue());
        
        add(toolBar, BorderLayout.NORTH);
    }
    
    private JPanel criarPainelFormulario() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Dados da Função"));
        
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
        panelCampos.add(new JLabel("Descrição:"), gbc);
        txtDescricao = new JTextField(30);
        gbc.gridx = 1;
        panelCampos.add(txtDescricao, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panelCampos.add(new JLabel("Departamento:"), gbc);
        comboDepartamento = new JComboBox<>();
        comboDepartamento.setPreferredSize(new Dimension(200, 25));
        gbc.gridx = 1;
        panelCampos.add(comboDepartamento, gbc);
        
        JPanel panelBotoes = new JPanel(new FlowLayout());
        btnInserir = new JButton("Inserir");
        btnAtualizar = new JButton("Atualizar");
        btnExcluir = new JButton("Excluir");
        btnLimpar = new JButton("Limpar");
        
        btnInserir.addActionListener(e -> inserirFuncao());
        btnAtualizar.addActionListener(e -> atualizarFuncao());
        btnExcluir.addActionListener(e -> excluirFuncao());
        btnLimpar.addActionListener(e -> limparFormulario());
        
        panelBotoes.add(btnInserir);
        panelBotoes.add(btnAtualizar);
        panelBotoes.add(btnExcluir);
        panelBotoes.add(btnLimpar);
        
        panel.add(panelCampos, BorderLayout.CENTER);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void carregarDepartamentosCombo() {
        comboDepartamento.removeAllItems();
        comboDepartamento.addItem("GERAL");
        List<Departamento> depts = departamentoDAO.listarTodos();
        for (Departamento d : depts) {
            comboDepartamento.addItem(d.getNome());
        }
    }
    
    private void carregarFuncoes() {
        tableModel.setRowCount(0);
        List<Funcao> funcoes = funcaoDAO.listarTodos();
        
        for (Funcao f : funcoes) {
            Object[] row = {
                f.getId(),
                f.getNome(),
                f.getDescricao() != null ? f.getDescricao() : "",
                f.getDepartamento() != null ? f.getDepartamento() : ""
            };
            tableModel.addRow(row);
        }
    }
    
    private void carregarFuncaoSelecionada() {
        int row = tabelaFuncoes.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);
            Funcao f = funcaoDAO.buscarPorId(id);
            if (f != null) {
                txtId.setText(String.valueOf(f.getId()));
                txtNome.setText(f.getNome());
                txtDescricao.setText(f.getDescricao() != null ? f.getDescricao() : "");
                comboDepartamento.setSelectedItem(f.getDepartamento() != null ? f.getDepartamento() : "GERAL");
            }
        }
    }
    
    private void inserirFuncao() {
        if (!validarCampos()) return;
        
        Funcao funcao = new Funcao();
        preencherFuncao(funcao);
        
        if (funcaoDAO.inserir(funcao)) {
            JOptionPane.showMessageDialog(this, "Função inserida com sucesso!");
            carregarFuncoes();
            limparFormulario();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao inserir função!\nVerifique se o nome já está cadastrado.", 
                                        "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void atualizarFuncao() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione uma função para atualizar!");
            return;
        }
        
        if (!validarCampos()) return;
        
        Funcao funcao = new Funcao();
        funcao.setId(Integer.parseInt(txtId.getText()));
        preencherFuncao(funcao);
        
        if (funcaoDAO.atualizar(funcao)) {
            JOptionPane.showMessageDialog(this, "Função atualizada com sucesso!");
            carregarFuncoes();
            limparFormulario();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar função!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void excluirFuncao() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione uma função para excluir!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Deseja realmente excluir esta função?", 
            "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(txtId.getText());
            if (funcaoDAO.excluir(id)) {
                JOptionPane.showMessageDialog(this, "Função excluída com sucesso!");
                carregarFuncoes();
                limparFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao excluir função!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void preencherFuncao(Funcao funcao) {
        funcao.setNome(txtNome.getText().trim());
        funcao.setDescricao(txtDescricao.getText().trim());
        funcao.setDepartamento((String) comboDepartamento.getSelectedItem());
    }
    
    private boolean validarCampos() {
        if (txtNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome é obrigatório!");
            return false;
        }
        return true;
    }
    
    private void limparFormulario() {
        txtId.setText("");
        txtNome.setText("");
        txtDescricao.setText("");
        comboDepartamento.setSelectedIndex(0);
        txtNome.requestFocus();
    }
}
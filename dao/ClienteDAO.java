package com.monitoramento.dao;

import com.monitoramento.model.Cliente;
import com.monitoramento.model.OrdemServico;
import com.monitoramento.util.DatabaseConnection;

import java.util.*;
import java.util.stream.Collectors;

public class ClienteDAO {
    private static final String FILE_NAME = "clientes.json";
    private List<Cliente> clientes;
    
    public ClienteDAO() {
        carregarClientes();
    }
    
    private void carregarClientes() {
        clientes = DatabaseConnection.carregarLista(FILE_NAME, Cliente.class);
        if (clientes == null) {
            clientes = new ArrayList<>();
        }
    }
    
    private void salvarClientes() {
        DatabaseConnection.salvarLista(FILE_NAME, clientes);
    }
    
    public boolean inserir(Cliente cliente) {
        if (buscarPorDocumento(cliente.getDocumento()) != null) {
            return false;
        }
        
        cliente.setId(DatabaseConnection.gerarNovoId(clientes));
        cliente.setDataCadastro(new Date());
        clientes.add(cliente);
        salvarClientes();
        return true;
    }
    
    public boolean atualizar(Cliente cliente) {
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getId() == cliente.getId()) {
                clientes.set(i, cliente);
                salvarClientes();
                return true;
            }
        }
        return false;
    }
    
    public boolean excluir(int id) {
        EquipamentoDAO equipamentoDAO = new EquipamentoDAO();
        if (equipamentoDAO.verificarEquipamentoCliente(id)) {
            System.err.println("Cliente possui equipamentos cadastrados.");
            return false;
        }
        
        OrdemServicoDAO osDAO = new OrdemServicoDAO();
        List<OrdemServico> ordens = osDAO.buscarPorCliente(id);
        if (!ordens.isEmpty()) {
            System.err.println("Cliente possui ordens de serviço associadas.");
            return false;
        }
        
        boolean removido = clientes.removeIf(c -> c.getId() == id);
        if (removido) {
            salvarClientes();
        }
        return removido;
    }
    
    public Cliente buscarPorId(int id) {
        return clientes.stream()
            .filter(c -> c.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public Cliente buscarPorDocumento(String documento) {
        return clientes.stream()
            .filter(c -> c.getDocumento() != null && c.getDocumento().equals(documento))
            .findFirst()
            .orElse(null);
    }
    
    public List<Cliente> listarTodos() {
        return new ArrayList<>(clientes);
    }
    
    public List<Cliente> listarPorMonitoramento(boolean emMonitoramento) {
        return clientes.stream()
            .filter(c -> c.isEmMonitoramento() == emMonitoramento)
            .collect(Collectors.toList());
    }
    
    public boolean adicionarMonitoramento(int id) {
        Cliente cliente = buscarPorId(id);
        if (cliente != null) {
            cliente.setEmMonitoramento(true);
            return atualizar(cliente);
        }
        return false;
    }
    
    public boolean removerMonitoramento(int id) {
        Cliente cliente = buscarPorId(id);
        if (cliente != null) {
            cliente.setEmMonitoramento(false);
            return atualizar(cliente);
        }
        return false;
    }
}
// EquipamentoDAO.java
package com.monitoramento.dao;

import com.monitoramento.model.Equipamento;
import com.monitoramento.util.DatabaseConnection;

import java.util.*;
import java.util.stream.Collectors;

public class EquipamentoDAO {
    private static final String FILE_NAME = "equipamentos.json";
    private static final double TENSAO_MIN = 1;
    private static final double TENSAO_MAX = 1500;
    
    private List<Equipamento> equipamentos;
    
    public EquipamentoDAO() {
        carregarEquipamentos();
    }
    
    private void carregarEquipamentos() {
        equipamentos = DatabaseConnection.carregarLista(FILE_NAME, Equipamento.class);
        if (equipamentos == null) {
            equipamentos = new ArrayList<>();
        }
    }
    
    private void salvarEquipamentos() {
        DatabaseConnection.salvarLista(FILE_NAME, equipamentos);
    }
    
    private boolean validarTensao(double tensao) {
        return tensao >= TENSAO_MIN && tensao <= TENSAO_MAX;
    }
    
    public boolean inserir(Equipamento equipamento) {
        if (!validarTensao(equipamento.getTensaoNominal())) {
            System.err.println("Tensão nominal fora do intervalo permitido");
            return false;
        }
        
        equipamento.setId(DatabaseConnection.gerarNovoId(equipamentos));
        equipamentos.add(equipamento);
        salvarEquipamentos();
        return true;
    }
    
    public boolean atualizar(Equipamento equipamento) {
        if (!validarTensao(equipamento.getTensaoNominal())) {
            System.err.println("Tensão nominal fora do intervalo permitido");
            return false;
        }
        
        for (int i = 0; i < equipamentos.size(); i++) {
            if (equipamentos.get(i).getId() == equipamento.getId()) {
                equipamentos.set(i, equipamento);
                salvarEquipamentos();
                return true;
            }
        }
        return false;
    }
    
    public boolean excluir(int id) {
        boolean removido = equipamentos.removeIf(e -> e.getId() == id);
        if (removido) {
            salvarEquipamentos();
        }
        return removido;
    }
    
    public boolean excluirPorCliente(int idCliente) {
        boolean removido = equipamentos.removeIf(e -> e.getIdCliente() == idCliente);
        if (removido) {
            salvarEquipamentos();
        }
        return removido;
    }
    
    public Equipamento buscarPorId(int id) {
        return equipamentos.stream()
            .filter(e -> e.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public List<Equipamento> buscarPorCliente(int idCliente) {
        return equipamentos.stream()
            .filter(e -> e.getIdCliente() == idCliente)
            .collect(Collectors.toList());
    }
    
    public List<Equipamento> listarTodos() {
        return new ArrayList<>(equipamentos);
    }
    
    public List<Equipamento> buscarPorMarca(String marca) {
        return equipamentos.stream()
            .filter(e -> e.getMarca() != null && e.getMarca().toLowerCase().contains(marca.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    public boolean verificarEquipamentoCliente(int idCliente) {
        return equipamentos.stream().anyMatch(e -> e.getIdCliente() == idCliente);
    }
}
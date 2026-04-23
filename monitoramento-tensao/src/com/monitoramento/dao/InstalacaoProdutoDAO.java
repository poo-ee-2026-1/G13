// InstalacaoProdutoDAO.java
package com.monitoramento.dao;

import com.monitoramento.model.InstalacaoProduto;
import com.monitoramento.util.DatabaseConnection;

import java.util.*;
import java.util.stream.Collectors;

public class InstalacaoProdutoDAO {
    private static final String FILE_NAME = "instalacao_produtos.json";
    private List<InstalacaoProduto> instalacoesProdutos;
    
    public InstalacaoProdutoDAO() {
        carregarInstalacoesProdutos();
    }
    
    private void carregarInstalacoesProdutos() {
        instalacoesProdutos = DatabaseConnection.carregarLista(FILE_NAME, InstalacaoProduto.class);
        if (instalacoesProdutos == null) {
            instalacoesProdutos = new ArrayList<>();
        }
        System.out.println("Carregados " + instalacoesProdutos.size() + " registros de InstalacaoProduto");
    }
    
    private void salvarInstalacoesProdutos() {
        DatabaseConnection.salvarLista(FILE_NAME, instalacoesProdutos);
        System.out.println("Salvos " + instalacoesProdutos.size() + " registros de InstalacaoProduto");
    }
    
    public boolean inserir(InstalacaoProduto item) {
        if (item == null) {
            System.err.println("Erro: InstalacaoProduto nulo");
            return false;
        }
        item.setId(DatabaseConnection.gerarNovoId(instalacoesProdutos));
        instalacoesProdutos.add(item);
        salvarInstalacoesProdutos();
        return true;
    }
    
    public boolean inserirLista(List<InstalacaoProduto> itens) {
        if (itens == null || itens.isEmpty()) {
            return false;
        }
        for (InstalacaoProduto item : itens) {
            item.setId(DatabaseConnection.gerarNovoId(instalacoesProdutos));
            instalacoesProdutos.add(item);
        }
        salvarInstalacoesProdutos();
        return true;
    }
    
    public boolean atualizar(InstalacaoProduto item) {
        for (int i = 0; i < instalacoesProdutos.size(); i++) {
            if (instalacoesProdutos.get(i).getId() == item.getId()) {
                instalacoesProdutos.set(i, item);
                salvarInstalacoesProdutos();
                return true;
            }
        }
        return false;
    }
    
    public boolean excluir(int id) {
        boolean removido = instalacoesProdutos.removeIf(i -> i.getId() == id);
        if (removido) {
            salvarInstalacoesProdutos();
        }
        return removido;
    }
    
    public InstalacaoProduto buscarPorId(int id) {
        return instalacoesProdutos.stream()
            .filter(i -> i.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public List<InstalacaoProduto> listarPorOrdemInstalacao(int idOrdemInstalacao) {
        return instalacoesProdutos.stream()
            .filter(i -> i.getIdOrdemInstalacao() == idOrdemInstalacao)
            .collect(Collectors.toList());
    }
    
    public List<InstalacaoProduto> listarPorProduto(int idProduto) {
        return instalacoesProdutos.stream()
            .filter(i -> i.getIdProduto() == idProduto)
            .collect(Collectors.toList());
    }
    
    public List<InstalacaoProduto> listarTodas() {
        return new ArrayList<>(instalacoesProdutos);
    }
    
    public boolean excluirPorOrdemInstalacao(int idOrdemInstalacao) {
        int before = instalacoesProdutos.size();
        boolean removido = instalacoesProdutos.removeIf(i -> i.getIdOrdemInstalacao() == idOrdemInstalacao);
        if (removido) {
            salvarInstalacoesProdutos();
            System.out.println("Excluídos " + (before - instalacoesProdutos.size()) + 
                             " produtos da ordem de instalação " + idOrdemInstalacao);
        }
        return removido;
    }
    
    public double calcularTotalPorOrdem(int idOrdemInstalacao) {
        return instalacoesProdutos.stream()
            .filter(i -> i.getIdOrdemInstalacao() == idOrdemInstalacao)
            .mapToDouble(InstalacaoProduto::getSubtotal)
            .sum();
    }
    
    public Map<Integer, Double> getResumoPorOrdem() {
        return instalacoesProdutos.stream()
            .collect(Collectors.groupingBy(
                InstalacaoProduto::getIdOrdemInstalacao,
                Collectors.summingDouble(InstalacaoProduto::getSubtotal)
            ));
    }
    
    public int contarProdutosPorOrdem(int idOrdemInstalacao) {
        return instalacoesProdutos.stream()
            .filter(i -> i.getIdOrdemInstalacao() == idOrdemInstalacao)
            .mapToInt(InstalacaoProduto::getQuantidade)
            .sum();
    }
}
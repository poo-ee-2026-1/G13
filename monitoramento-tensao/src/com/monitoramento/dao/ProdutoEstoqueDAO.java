// ProdutoEstoqueDAO.java
package com.monitoramento.dao;

import com.monitoramento.model.ProdutoEstoque;
import com.monitoramento.util.DatabaseConnection;

import java.util.*;
import java.util.stream.Collectors;

public class ProdutoEstoqueDAO {
    private static final String FILE_NAME = "produtos_estoque.json";
    private List<ProdutoEstoque> produtos;
    
    public ProdutoEstoqueDAO() {
        carregarProdutos();
    }
    
    private void carregarProdutos() {
        produtos = DatabaseConnection.carregarLista(FILE_NAME, ProdutoEstoque.class);
        if (produtos == null) {
            produtos = new ArrayList<>();
        }
    }
    
    private void salvarProdutos() {
        DatabaseConnection.salvarLista(FILE_NAME, produtos);
    }
    
    public boolean inserir(ProdutoEstoque produto) {
        if (buscarPorCodigo(produto.getCodigo()) != null) {
            return false;
        }
        produto.setId(DatabaseConnection.gerarNovoId(produtos));
        produtos.add(produto);
        salvarProdutos();
        return true;
    }
    
    public boolean atualizar(ProdutoEstoque produto) {
        for (int i = 0; i < produtos.size(); i++) {
            if (produtos.get(i).getId() == produto.getId()) {
                produtos.set(i, produto);
                salvarProdutos();
                return true;
            }
        }
        return false;
    }
    
    public boolean excluir(int id) {
        boolean removido = produtos.removeIf(p -> p.getId() == id);
        if (removido) {
            salvarProdutos();
        }
        return removido;
    }
    
    public ProdutoEstoque buscarPorId(int id) {
        return produtos.stream()
            .filter(p -> p.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public ProdutoEstoque buscarPorCodigo(String codigo) {
        return produtos.stream()
            .filter(p -> p.getCodigo() != null && p.getCodigo().equals(codigo))
            .findFirst()
            .orElse(null);
    }
    
    public List<ProdutoEstoque> listarTodos() {
        return new ArrayList<>(produtos);
    }
    
    public List<ProdutoEstoque> listarAtivos() {
        return produtos.stream()
            .filter(ProdutoEstoque::isAtivo)
            .collect(Collectors.toList());
    }
    
    public List<ProdutoEstoque> listarPorCategoria(String categoria) {
        return produtos.stream()
            .filter(p -> p.getCategoria() != null && p.getCategoria().equals(categoria))
            .collect(Collectors.toList());
    }
    
    public List<ProdutoEstoque> listarEstoqueBaixo() {
        return produtos.stream()
            .filter(ProdutoEstoque::isEstoqueBaixo)
            .collect(Collectors.toList());
    }
    
    public boolean darBaixaEstoque(int idProduto, int quantidade, String motivo, int idReferencia, int idUsuario) {
        ProdutoEstoque produto = buscarPorId(idProduto);
        if (produto == null || produto.getQuantidade() < quantidade) {
            return false;
        }
        
        produto.setQuantidade(produto.getQuantidade() - quantidade);
        atualizar(produto);
        return true;
    }
    
    public boolean adicionarEstoque(int idProduto, int quantidade, String motivo, int idReferencia, int idUsuario) {
        ProdutoEstoque produto = buscarPorId(idProduto);
        if (produto == null) {
            return false;
        }
        
        produto.setQuantidade(produto.getQuantidade() + quantidade);
        atualizar(produto);
        return true;
    }
}
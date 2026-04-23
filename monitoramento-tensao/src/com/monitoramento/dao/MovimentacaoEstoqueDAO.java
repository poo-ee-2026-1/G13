// MovimentacaoEstoqueDAO.java
package com.monitoramento.dao;

import com.monitoramento.model.MovimentacaoEstoque;
import com.monitoramento.util.DatabaseConnection;

import java.util.*;
import java.util.stream.Collectors;

public class MovimentacaoEstoqueDAO {
    private static final String FILE_NAME = "movimentacoes_estoque.json";
    private List<MovimentacaoEstoque> movimentacoes;
    
    public MovimentacaoEstoqueDAO() {
        carregarMovimentacoes();
    }
    
    private void carregarMovimentacoes() {
        movimentacoes = DatabaseConnection.carregarLista(FILE_NAME, MovimentacaoEstoque.class);
        if (movimentacoes == null) {
            movimentacoes = new ArrayList<>();
        }
    }
    
    private void salvarMovimentacoes() {
        DatabaseConnection.salvarLista(FILE_NAME, movimentacoes);
    }
    
    public boolean inserir(MovimentacaoEstoque movimentacao) {
        movimentacao.setId(DatabaseConnection.gerarNovoId(movimentacoes));
        movimentacao.setDataMovimento(new Date());
        movimentacoes.add(movimentacao);
        salvarMovimentacoes();
        return true;
    }
    
    public List<MovimentacaoEstoque> listarTodas() {
        return new ArrayList<>(movimentacoes);
    }
    
    public List<MovimentacaoEstoque> listarPorProduto(int idProduto) {
        return movimentacoes.stream()
            .filter(m -> m.getIdProduto() == idProduto)
            .sorted((a, b) -> b.getDataMovimento().compareTo(a.getDataMovimento()))
            .collect(Collectors.toList());
    }
    
    public List<MovimentacaoEstoque> listarPorTipo(String tipo) {
        return movimentacoes.stream()
            .filter(m -> m.getTipo() != null && m.getTipo().equals(tipo))
            .sorted((a, b) -> b.getDataMovimento().compareTo(a.getDataMovimento()))
            .collect(Collectors.toList());
    }
    
    public List<MovimentacaoEstoque> listarPorPeriodo(Date dataInicio, Date dataFim) {
        return movimentacoes.stream()
            .filter(m -> m.getDataMovimento() != null &&
                        m.getDataMovimento().after(dataInicio) &&
                        m.getDataMovimento().before(dataFim))
            .sorted((a, b) -> b.getDataMovimento().compareTo(a.getDataMovimento()))
            .collect(Collectors.toList());
    }
}
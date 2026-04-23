// TransacaoFinanceiraDAO.java
package com.monitoramento.dao;

import com.monitoramento.model.TransacaoFinanceira;
import com.monitoramento.util.DatabaseConnection;

import java.util.*;
import java.util.stream.Collectors;

public class TransacaoFinanceiraDAO {
    private static final String FILE_NAME = "transacoes_financeiras.json";
    private List<TransacaoFinanceira> transacoes;
    
    public TransacaoFinanceiraDAO() {
        carregarTransacoes();
    }
    
    private void carregarTransacoes() {
        transacoes = DatabaseConnection.carregarLista(FILE_NAME, TransacaoFinanceira.class);
        if (transacoes == null) {
            transacoes = new ArrayList<>();
        }
    }
    
    private void salvarTransacoes() {
        DatabaseConnection.salvarLista(FILE_NAME, transacoes);
    }
    
    public boolean inserir(TransacaoFinanceira transacao) {
        transacao.setId(DatabaseConnection.gerarNovoId(transacoes));
        transacao.setDataRegistro(new Date());
        transacoes.add(transacao);
        salvarTransacoes();
        return true;
    }
    
    public boolean atualizar(TransacaoFinanceira transacao) {
        for (int i = 0; i < transacoes.size(); i++) {
            if (transacoes.get(i).getId() == transacao.getId()) {
                transacoes.set(i, transacao);
                salvarTransacoes();
                return true;
            }
        }
        return false;
    }
    
    public boolean excluir(int id) {
        boolean removido = transacoes.removeIf(t -> t.getId() == id);
        if (removido) {
            salvarTransacoes();
        }
        return removido;
    }
    
    public TransacaoFinanceira buscarPorId(int id) {
        return transacoes.stream()
            .filter(t -> t.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public List<TransacaoFinanceira> listarTodas() {
        return new ArrayList<>(transacoes);
    }
    
    public List<TransacaoFinanceira> listarPorTipo(String tipo) {
        return transacoes.stream()
            .filter(t -> t.getTipo() != null && t.getTipo().equals(tipo))
            .sorted((a, b) -> b.getDataRegistro().compareTo(a.getDataRegistro()))
            .collect(Collectors.toList());
    }
    
    public List<TransacaoFinanceira> listarPorStatus(String status) {
        return transacoes.stream()
            .filter(t -> t.getStatus() != null && t.getStatus().equals(status))
            .sorted((a, b) -> b.getDataRegistro().compareTo(a.getDataRegistro()))
            .collect(Collectors.toList());
    }
    
    public List<TransacaoFinanceira> listarPorPeriodo(Date dataInicio, Date dataFim) {
        return transacoes.stream()
            .filter(t -> t.getDataRegistro() != null &&
                        t.getDataRegistro().after(dataInicio) &&
                        t.getDataRegistro().before(dataFim))
            .sorted((a, b) -> b.getDataRegistro().compareTo(a.getDataRegistro()))
            .collect(Collectors.toList());
    }
    
    public double calcularSaldo() {
        double entradas = transacoes.stream()
            .filter(t -> "ENTRADA".equals(t.getTipo()) && "RECEBIDO".equals(t.getStatus()))
            .mapToDouble(TransacaoFinanceira::getValor)
            .sum();
        
        double saidas = transacoes.stream()
            .filter(t -> "SAIDA".equals(t.getTipo()) && "PAGO".equals(t.getStatus()))
            .mapToDouble(TransacaoFinanceira::getValor)
            .sum();
        
        return entradas - saidas;
    }
    
    public Map<String, Double> getResumoPorNatureza(Date dataInicio, Date dataFim) {
        Map<String, Double> resumo = new HashMap<>();
        
        transacoes.stream()
            .filter(t -> t.getDataRegistro() != null &&
                        t.getDataRegistro().after(dataInicio) &&
                        t.getDataRegistro().before(dataFim) &&
                        ("RECEBIDO".equals(t.getStatus()) || "PAGO".equals(t.getStatus())))
            .forEach(t -> {
                String key = t.getNatureza() + " (" + t.getTipo() + ")";
                double valor = t.getValor();
                if ("SAIDA".equals(t.getTipo())) valor = -valor;
                resumo.put(key, resumo.getOrDefault(key, 0.0) + valor);
            });
        
        return resumo;
    }
    
    public List<TransacaoFinanceira> listarContasPagar() {
        return transacoes.stream()
            .filter(t -> "SAIDA".equals(t.getTipo()) && 
                        ("PENDENTE".equals(t.getStatus()) || "PAGO".equals(t.getStatus())))
            .sorted(Comparator.comparing(TransacaoFinanceira::getDataVencimento, Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());
    }
    
    public List<TransacaoFinanceira> listarContasReceber() {
        return transacoes.stream()
            .filter(t -> "ENTRADA".equals(t.getTipo()) && 
                        ("PENDENTE".equals(t.getStatus()) || "RECEBIDO".equals(t.getStatus())))
            .sorted(Comparator.comparing(TransacaoFinanceira::getDataVencimento, Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());
    }
}
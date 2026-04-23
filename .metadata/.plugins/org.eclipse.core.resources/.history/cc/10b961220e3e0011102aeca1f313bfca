// ParcelaMonitoramentoDAO.java
package com.monitoramento.dao;

import com.monitoramento.model.ParcelaMonitoramento;
import com.monitoramento.util.DatabaseConnection;

import java.util.*;
import java.util.stream.Collectors;

public class ParcelaMonitoramentoDAO {
    private static final String FILE_NAME = "parcelas_monitoramento.json";
    private List<ParcelaMonitoramento> parcelas;
    
    public ParcelaMonitoramentoDAO() {
        carregarParcelas();
    }
    
    private void carregarParcelas() {
        parcelas = DatabaseConnection.carregarLista(FILE_NAME, ParcelaMonitoramento.class);
        if (parcelas == null) {
            parcelas = new ArrayList<>();
        }
    }
    
    private void salvarParcelas() {
        DatabaseConnection.salvarLista(FILE_NAME, parcelas);
    }
    
    public boolean inserir(ParcelaMonitoramento parcela) {
        parcela.setId(DatabaseConnection.gerarNovoId(parcelas));
        parcelas.add(parcela);
        salvarParcelas();
        return true;
    }
    
    public boolean inserirLista(List<ParcelaMonitoramento> listaParcelas) {
        for (ParcelaMonitoramento p : listaParcelas) {
            p.setId(DatabaseConnection.gerarNovoId(parcelas));
            parcelas.add(p);
        }
        salvarParcelas();
        return true;
    }
    
    public boolean atualizar(ParcelaMonitoramento parcela) {
        for (int i = 0; i < parcelas.size(); i++) {
            if (parcelas.get(i).getId() == parcela.getId()) {
                parcelas.set(i, parcela);
                salvarParcelas();
                return true;
            }
        }
        return false;
    }
    
    public boolean excluir(int id) {
        boolean removido = parcelas.removeIf(p -> p.getId() == id);
        if (removido) {
            salvarParcelas();
        }
        return removido;
    }
    
    public ParcelaMonitoramento buscarPorId(int id) {
        return parcelas.stream()
            .filter(p -> p.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public List<ParcelaMonitoramento> listarPorOrdemServico(int idOrdemServico) {
        return parcelas.stream()
            .filter(p -> p.getIdOrdemServico() == idOrdemServico)
            .sorted(Comparator.comparing(ParcelaMonitoramento::getNumeroParcela))
            .collect(Collectors.toList());
    }
    
    public List<ParcelaMonitoramento> listarPorStatus(String status) {
        return parcelas.stream()
            .filter(p -> p.getStatus() != null && p.getStatus().equals(status))
            .sorted(Comparator.comparing(ParcelaMonitoramento::getDataVencimento))
            .collect(Collectors.toList());
    }
    
    public List<ParcelaMonitoramento> listarTodas() {
        return new ArrayList<>(parcelas);
    }
    
    public List<ParcelaMonitoramento> listarParcelasVencidas() {
        Date hoje = new Date();
        return parcelas.stream()
            .filter(p -> "PENDENTE".equals(p.getStatus()) && 
                        p.getDataVencimento() != null && 
                        p.getDataVencimento().before(hoje))
            .sorted(Comparator.comparing(ParcelaMonitoramento::getDataVencimento))
            .collect(Collectors.toList());
    }
    
    public boolean marcarComoPaga(int id, Date dataPagamento) {
        ParcelaMonitoramento p = buscarPorId(id);
        if (p != null && "PENDENTE".equals(p.getStatus())) {
            p.setStatus("PAGO");
            p.setDataPagamento(dataPagamento);
            return atualizar(p);
        }
        return false;
    }
    
    public double calcularTotalPendentePorOrdem(int idOrdemServico) {
        return parcelas.stream()
            .filter(p -> p.getIdOrdemServico() == idOrdemServico && "PENDENTE".equals(p.getStatus()))
            .mapToDouble(ParcelaMonitoramento::getValor)
            .sum();
    }
    
    public void excluirPorOrdemServico(int idOrdemServico) {
        parcelas.removeIf(p -> p.getIdOrdemServico() == idOrdemServico);
        salvarParcelas();
    }
}
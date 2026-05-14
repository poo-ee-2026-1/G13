// ReservaProdutoDAO.java
package com.monitoramento.dao;

import com.monitoramento.model.ReservaProduto;
import com.monitoramento.util.DatabaseConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.*;
import java.util.stream.Collectors;

public class ReservaProdutoDAO {
    private static final String FILE_NAME = "reservas_produtos.json";
    private List<ReservaProduto> reservas;
    
    public ReservaProdutoDAO() {
        carregarReservas();
    }
    
    private void carregarReservas() {
        reservas = DatabaseConnection.carregarLista(FILE_NAME, ReservaProduto.class);
        if (reservas == null) {
            reservas = new ArrayList<>();
        }
        System.out.println("Carregadas " + reservas.size() + " reservas de produtos");
    }
    
    private void salvarReservas() {
        DatabaseConnection.salvarLista(FILE_NAME, reservas);
        System.out.println("Salvas " + reservas.size() + " reservas de produtos");
    }
    
    public boolean inserir(ReservaProduto reserva) {
        reserva.setId(DatabaseConnection.gerarNovoId(reservas));
        reservas.add(reserva);
        salvarReservas();
        return true;
    }
    
    public boolean atualizar(ReservaProduto reserva) {
        for (int i = 0; i < reservas.size(); i++) {
            if (reservas.get(i).getId() == reserva.getId()) {
                reservas.set(i, reserva);
                salvarReservas();
                return true;
            }
        }
        return false;
    }
    
    public boolean excluir(int id) {
        boolean removido = reservas.removeIf(r -> r.getId() == id);
        if (removido) {
            salvarReservas();
        }
        return removido;
    }
    
    public ReservaProduto buscarPorId(int id) {
        return reservas.stream()
            .filter(r -> r.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public List<ReservaProduto> listarTodas() {
        return new ArrayList<>(reservas);
    }
    
    public List<ReservaProduto> listarPorOrcamento(int idOrcamento) {
        return reservas.stream()
            .filter(r -> r.getIdOrcamento() == idOrcamento)
            .collect(Collectors.toList());
    }
    
    public List<ReservaProduto> listarPorStatus(String status) {
        return reservas.stream()
            .filter(r -> r.getStatus() != null && r.getStatus().equals(status))
            .collect(Collectors.toList());
    }
    
    public List<ReservaProduto> listarReservasAtivas() {
        return reservas.stream()
            .filter(r -> "RESERVADO".equals(r.getStatus()))
            .collect(Collectors.toList());
    }
    
    public List<ReservaProduto> listarReservasPorProduto(int idProduto) {
        return reservas.stream()
            .filter(r -> r.getIdProduto() == idProduto)
            .collect(Collectors.toList());
    }
    
    public boolean consumirReserva(int idOrcamento, int idProduto, int idOrdemInstalacao) {
        ReservaProduto reserva = reservas.stream()
            .filter(r -> r.getIdOrcamento() == idOrcamento && 
                        r.getIdProduto() == idProduto && 
                        "RESERVADO".equals(r.getStatus()))
            .findFirst()
            .orElse(null);
        
        if (reserva != null) {
            reserva.setStatus("CONSUMIDO");
            reserva.setIdOrdemInstalacao(idOrdemInstalacao);
            reserva.setDataConsumo(new Date());
            return atualizar(reserva);
        }
        return false;
    }
    
    public boolean cancelarReserva(int idOrcamento) {
        List<ReservaProduto> reservasOrcamento = listarPorOrcamento(idOrcamento);
        boolean sucesso = true;
        
        for (ReservaProduto r : reservasOrcamento) {
            if ("RESERVADO".equals(r.getStatus())) {
                r.setStatus("CANCELADO");
                if (!atualizar(r)) {
                    sucesso = false;
                }
            }
        }
        return sucesso;
    }
    
    public boolean verificarProdutoReservadoParaOrcamento(int idOrcamento, int idProduto) {
        return reservas.stream()
            .anyMatch(r -> r.getIdOrcamento() == idOrcamento && 
                          r.getIdProduto() == idProduto && 
                          "RESERVADO".equals(r.getStatus()));
    }
}
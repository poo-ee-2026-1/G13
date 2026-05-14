// ReservaProduto.java
package com.monitoramento.model;

import java.util.Date;

public class ReservaProduto {
    private int id;
    private int idProduto;
    private String nomeProduto;
    private int quantidade;
    private int idOrcamento;
    private int idOrdemInstalacao;
    private String status; // "RESERVADO", "CONSUMIDO", "CANCELADO"
    private Date dataReserva;
    private Date dataConsumo;
    private String observacao;
    
    public ReservaProduto() {}
    
    public ReservaProduto(int idProduto, String nomeProduto, int quantidade, int idOrcamento, String observacao) {
        this.idProduto = idProduto;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.idOrcamento = idOrcamento;
        this.observacao = observacao;
        this.status = "RESERVADO";
        this.dataReserva = new Date();
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getIdProduto() { return idProduto; }
    public void setIdProduto(int idProduto) { this.idProduto = idProduto; }
    
    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }
    
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    
    public int getIdOrcamento() { return idOrcamento; }
    public void setIdOrcamento(int idOrcamento) { this.idOrcamento = idOrcamento; }
    
    public int getIdOrdemInstalacao() { return idOrdemInstalacao; }
    public void setIdOrdemInstalacao(int idOrdemInstalacao) { this.idOrdemInstalacao = idOrdemInstalacao; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getDataReserva() { return dataReserva; }
    public void setDataReserva(Date dataReserva) { this.dataReserva = dataReserva; }
    
    public Date getDataConsumo() { return dataConsumo; }
    public void setDataConsumo(Date dataConsumo) { this.dataConsumo = dataConsumo; }
    
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    
    @Override
    public String toString() {
        return "ReservaProduto{" +
                "id=" + id +
                ", idProduto=" + idProduto +
                ", nomeProduto='" + nomeProduto + '\'' +
                ", quantidade=" + quantidade +
                ", idOrcamento=" + idOrcamento +
                ", status='" + status + '\'' +
                '}';
    }
}
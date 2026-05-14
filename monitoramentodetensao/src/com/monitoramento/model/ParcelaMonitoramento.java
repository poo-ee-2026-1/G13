// ParcelaMonitoramento.java
package com.monitoramento.model;

import java.util.Date;

public class ParcelaMonitoramento {
    private int id;
    private int idOrdemServico;
    private int numeroParcela;
    private double valor;
    private Date dataVencimento;
    private String status; // "PENDENTE", "PAGO", "CANCELADO"
    private Date dataPagamento;
    
    public ParcelaMonitoramento() {}
    
    public ParcelaMonitoramento(int idOrdemServico, int numeroParcela, double valor, Date dataVencimento) {
        this.idOrdemServico = idOrdemServico;
        this.numeroParcela = numeroParcela;
        this.valor = valor;
        this.dataVencimento = dataVencimento;
        this.status = "PENDENTE";
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getIdOrdemServico() { return idOrdemServico; }
    public void setIdOrdemServico(int idOrdemServico) { this.idOrdemServico = idOrdemServico; }
    
    public int getNumeroParcela() { return numeroParcela; }
    public void setNumeroParcela(int numeroParcela) { this.numeroParcela = numeroParcela; }
    
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    
    public Date getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(Date dataVencimento) { this.dataVencimento = dataVencimento; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(Date dataPagamento) { this.dataPagamento = dataPagamento; }
}
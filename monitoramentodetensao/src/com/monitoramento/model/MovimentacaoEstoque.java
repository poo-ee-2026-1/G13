// MovimentacaoEstoque.java
package com.monitoramento.model;

import java.util.Date;

public class MovimentacaoEstoque {
    private int id;
    private int idProduto;
    private String tipo; // "ENTRADA", "SAIDA"
    private String motivo; // "COMPRA", "VENDA", "INSTALACAO", "DEVOLUCAO", "AJUSTE", "PERDA"
    private int quantidade;
    private double precoUnitario;
    private double valorTotal;
    private Date dataMovimento;
    private int idUsuario;
    private int idReferencia; // ID da OS, Orçamento, etc.
    private String observacao;
    
    public MovimentacaoEstoque() {}
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdProduto() { return idProduto; }
    public void setIdProduto(int idProduto) { this.idProduto = idProduto; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(double precoUnitario) { this.precoUnitario = precoUnitario; }
    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
    public Date getDataMovimento() { return dataMovimento; }
    public void setDataMovimento(Date dataMovimento) { this.dataMovimento = dataMovimento; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public int getIdReferencia() { return idReferencia; }
    public void setIdReferencia(int idReferencia) { this.idReferencia = idReferencia; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
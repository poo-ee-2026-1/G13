// InstalacaoProduto.java
package com.monitoramento.model;

public class InstalacaoProduto {
    private int id;
    private int idOrdemInstalacao;
    private int idProduto;
    private String nomeProduto;
    private int quantidade;
    private double precoUnitario;
    private double subtotal;
    
    public InstalacaoProduto() {}
    
    public InstalacaoProduto(int idOrdemInstalacao, int idProduto, String nomeProduto, int quantidade, double precoUnitario) {
        this.idOrdemInstalacao = idOrdemInstalacao;
        this.idProduto = idProduto;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subtotal = quantidade * precoUnitario;
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getIdOrdemInstalacao() { return idOrdemInstalacao; }
    public void setIdOrdemInstalacao(int idOrdemInstalacao) { this.idOrdemInstalacao = idOrdemInstalacao; }
    
    public int getIdProduto() { return idProduto; }
    public void setIdProduto(int idProduto) { this.idProduto = idProduto; }
    
    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }
    
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { 
        this.quantidade = quantidade;
        recalcularSubtotal();
    }
    
    public double getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(double precoUnitario) { 
        this.precoUnitario = precoUnitario;
        recalcularSubtotal();
    }
    
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    
    private void recalcularSubtotal() {
        this.subtotal = this.quantidade * this.precoUnitario;
    }
    
    @Override
    public String toString() {
        return "InstalacaoProduto{" +
                "id=" + id +
                ", idOrdemInstalacao=" + idOrdemInstalacao +
                ", idProduto=" + idProduto +
                ", nomeProduto='" + nomeProduto + '\'' +
                ", quantidade=" + quantidade +
                ", precoUnitario=" + precoUnitario +
                ", subtotal=" + subtotal +
                '}';
    }
}
// ProdutoEstoque.java
package com.monitoramento.model;

public class ProdutoEstoque {
    private int id;
    private String codigo;
    private String nome;
    private String categoria; // "EQUIPAMENTO", "PECA", "ACESSORIO", "OUTRO"
    private String descricao;
    private String fabricante;
    private String modelo;
    private int quantidade;
    private int quantidadeMinima;
    private double precoCusto;
    private double precoVenda;
    private String unidadeMedida; // "UN", "PC", "MT", "LT"
    private String localizacao; // Local no estoque
    private boolean ativo;
    
    public ProdutoEstoque() {}
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public int getQuantidadeMinima() { return quantidadeMinima; }
    public void setQuantidadeMinima(int quantidadeMinima) { this.quantidadeMinima = quantidadeMinima; }
    public double getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(double precoCusto) { this.precoCusto = precoCusto; }
    public double getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(double precoVenda) { this.precoVenda = precoVenda; }
    public String getUnidadeMedida() { return unidadeMedida; }
    public void setUnidadeMedida(String unidadeMedida) { this.unidadeMedida = unidadeMedida; }
    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    
    public String getNomeCompleto() {
        return codigo + " - " + nome + (modelo != null && !modelo.isEmpty() ? " (" + modelo + ")" : "");
    }
    
    public boolean isEstoqueBaixo() {
        return quantidade <= quantidadeMinima;
    }
}
// Funcao.java
package com.monitoramento.model;

public class Funcao {
    private int id;
    private String nome;
    private String descricao;
    private String departamento; // Nome do departamento associado
    
    public Funcao() {}
    
    public Funcao(int id, String nome, String descricao, String departamento) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.departamento = departamento;
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
}
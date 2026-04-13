package com.voltmonitor.model;

public class Departamento {
    private int id;
    private String nome;
    private String descricao;
    private boolean ativo;

    public Departamento() {}

    public Departamento(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}

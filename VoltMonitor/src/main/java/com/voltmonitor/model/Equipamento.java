package com.voltmonitor.model;

public class Equipamento {
    private int id;
    private String marca;
    private String modelo;
    private double tensaoNominal; // >= 1 e <= 1500
    private boolean ativo;

    public Equipamento() {}

    public Equipamento(String marca, String modelo, double tensaoNominal) {
        this.marca = marca;
        this.modelo = modelo;
        this.tensaoNominal = tensaoNominal;
        this.ativo = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public double getTensaoNominal() { return tensaoNominal; }
    public void setTensaoNominal(double tensaoNominal) { this.tensaoNominal = tensaoNominal; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}

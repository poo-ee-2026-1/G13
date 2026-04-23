// Equipamento.java
package com.monitoramento.model;

public class Equipamento {
    private int id;
    private String marca;
    private String modelo;
    private double tensaoNominal;
    private int idCliente;
    
    public Equipamento() {}
    
    public Equipamento(int id, String marca, String modelo, double tensaoNominal, int idCliente) {
        this.id = id;
        this.marca = marca;
        this.modelo = modelo;
        this.tensaoNominal = tensaoNominal;
        this.idCliente = idCliente;
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public double getTensaoNominal() { return tensaoNominal; }
    public void setTensaoNominal(double tensaoNominal) { this.tensaoNominal = tensaoNominal; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
}
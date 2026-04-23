// MedicaoTensao.java
package com.monitoramento.model;

import java.util.Date;

public class MedicaoTensao {
    private int id;
    private int idCliente;
    private double tensao;
    private Date dataHora;
    private String estadoRede;
    private String situacaoRede;
    
    public MedicaoTensao() {}
    
    public MedicaoTensao(int idCliente, double tensao, Date dataHora, String estadoRede, String situacaoRede) {
        this.idCliente = idCliente;
        this.tensao = tensao;
        this.dataHora = dataHora;
        this.estadoRede = estadoRede;
        this.situacaoRede = situacaoRede;
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public double getTensao() { return tensao; }
    public void setTensao(double tensao) { this.tensao = tensao; }
    public Date getDataHora() { return dataHora; }
    public void setDataHora(Date dataHora) { this.dataHora = dataHora; }
    public String getEstadoRede() { return estadoRede; }
    public void setEstadoRede(String estadoRede) { this.estadoRede = estadoRede; }
    public String getSituacaoRede() { return situacaoRede; }
    public void setSituacaoRede(String situacaoRede) { this.situacaoRede = situacaoRede; }
}
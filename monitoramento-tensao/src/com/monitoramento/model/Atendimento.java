// Atendimento.java
package com.monitoramento.model;

import java.util.Date;

public class Atendimento {
    private int id;
    private int idCliente;
    private String tipo; // "SOLICITACAO", "ORCAMENTO", "ORDEM_INSTALACAO", "ORDEM_REPARO"
    private String status; // "PENDENTE", "EM_ATENDIMENTO", "CONCLUIDO", "CANCELADO"
    private String assunto;
    private String descricao;
    private String prioridade; // "BAIXA", "MEDIA", "ALTA"
    private Date dataAbertura;
    private Date dataConclusao;
    private int idUsuarioAbertura;
    private int idUsuarioAtendimento;
    private int idOrdemServicoVinculada; // Para quando vira OS
    
    // Construtores
    public Atendimento() {}
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }
    public Date getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(Date dataAbertura) { this.dataAbertura = dataAbertura; }
    public Date getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(Date dataConclusao) { this.dataConclusao = dataConclusao; }
    public int getIdUsuarioAbertura() { return idUsuarioAbertura; }
    public void setIdUsuarioAbertura(int idUsuarioAbertura) { this.idUsuarioAbertura = idUsuarioAbertura; }
    public int getIdUsuarioAtendimento() { return idUsuarioAtendimento; }
    public void setIdUsuarioAtendimento(int idUsuarioAtendimento) { this.idUsuarioAtendimento = idUsuarioAtendimento; }
    public int getIdOrdemServicoVinculada() { return idOrdemServicoVinculada; }
    public void setIdOrdemServicoVinculada(int idOrdemServicoVinculada) { this.idOrdemServicoVinculada = idOrdemServicoVinculada; }
}
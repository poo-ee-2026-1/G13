package com.voltmonitor.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrdemServico {
    public enum Status { ABERTA, EM_ANDAMENTO, FECHADA }
    public enum Motivo { INATIVO, SEM_COMUNICACAO, ALERTA_EXCEDIDO, CRITICO_EXCEDIDO }

    private int id;
    private int clienteId;
    private String nomeCliente;
    private int equipamentoId;
    private String modeloEquipamento;
    private Motivo motivo;
    private Status status;
    private String descricao;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private String departamentoDestino;
    private double ultimaTensao;
    private String situacaoRede;

    public OrdemServico() {
        this.status = Status.ABERTA;
        this.dataAbertura = LocalDateTime.now();
        this.departamentoDestino = "Suporte Técnico";
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
    public int getEquipamentoId() { return equipamentoId; }
    public void setEquipamentoId(int equipamentoId) { this.equipamentoId = equipamentoId; }
    public String getModeloEquipamento() { return modeloEquipamento; }
    public void setModeloEquipamento(String modeloEquipamento) { this.modeloEquipamento = modeloEquipamento; }
    public Motivo getMotivo() { return motivo; }
    public void setMotivo(Motivo motivo) { this.motivo = motivo; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }
    public LocalDateTime getDataFechamento() { return dataFechamento; }
    public void setDataFechamento(LocalDateTime dataFechamento) { this.dataFechamento = dataFechamento; }
    public String getDepartamentoDestino() { return departamentoDestino; }
    public void setDepartamentoDestino(String departamentoDestino) { this.departamentoDestino = departamentoDestino; }
    public double getUltimaTensao() { return ultimaTensao; }
    public void setUltimaTensao(double ultimaTensao) { this.ultimaTensao = ultimaTensao; }
    public String getSituacaoRede() { return situacaoRede; }
    public void setSituacaoRede(String situacaoRede) { this.situacaoRede = situacaoRede; }

    public String getDataAberturaFormatada() {
        return dataAbertura != null ?
            dataAbertura.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "";
    }

    public String getMotivoDescricao() {
        return switch (motivo) {
            case INATIVO -> "Equipamento INATIVO";
            case SEM_COMUNICACAO -> "SEM COMUNICAÇÃO com ESP32";
            case ALERTA_EXCEDIDO -> "Limite de alertas de tensão excedido";
            case CRITICO_EXCEDIDO -> "Limite de situações críticas excedido";
        };
    }
}

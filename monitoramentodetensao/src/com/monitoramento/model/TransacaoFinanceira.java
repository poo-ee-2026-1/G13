// TransacaoFinanceira.java
package com.monitoramento.model;

import java.util.Date;

public class TransacaoFinanceira {
    private int id;
    private String tipo; // "ENTRADA", "SAIDA"
    private String natureza; // "Venda", "Instalação", "Mensalidade", "Manutenção", "Salário", "Aluguel", "Imposto", "Fornecedor", "Outro"
    private String formaPagamento; // "DINHEIRO", "PIX", "CARTAO_DEBITO", "CARTAO_CREDITO", "TRANSFERENCIA", "BOLETO", "DUPLICATA"
    private double valor;
    private String descricao;
    private String status; // "PENDENTE", "PAGO", "RECEBIDO", "CANCELADO"
    private Date dataVencimento;
    private Date dataPagamento;
    private Date dataRegistro;
    private String documentoReferencia; // Nota fiscal, recibo, etc.
    private int idCliente; // Para transações relacionadas a clientes
    private int idOrdemServico; // Para transações relacionadas a OS
    private int idUsuarioRegistro;
    
    // Construtores
    public TransacaoFinanceira() {}
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getNatureza() { return natureza; }
    public void setNatureza(String natureza) { this.natureza = natureza; }
    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(Date dataVencimento) { this.dataVencimento = dataVencimento; }
    public Date getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(Date dataPagamento) { this.dataPagamento = dataPagamento; }
    public Date getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(Date dataRegistro) { this.dataRegistro = dataRegistro; }
    public String getDocumentoReferencia() { return documentoReferencia; }
    public void setDocumentoReferencia(String documentoReferencia) { this.documentoReferencia = documentoReferencia; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public int getIdOrdemServico() { return idOrdemServico; }
    public void setIdOrdemServico(int idOrdemServico) { this.idOrdemServico = idOrdemServico; }
    public int getIdUsuarioRegistro() { return idUsuarioRegistro; }
    public void setIdUsuarioRegistro(int idUsuarioRegistro) { this.idUsuarioRegistro = idUsuarioRegistro; }
}
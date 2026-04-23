// OrdemServico.java
package com.monitoramento.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class OrdemServico {
    private int id;
    private int idCliente;
    private int idEquipamento;
    private String tipoNivel;
    private String status;
    private String motivo;
    private String descricaoSolucao;
    private String falhaIdentificada;
    private Date dataAbertura;
    private Date dataFechamento;
    private int idUsuarioAbertura;
    private int idUsuarioFechamento;
    
    private String tipoOrdem;
    private int idAtendimentoOrigem;
    private double valorTotal;
    private String enderecoInstalacao;
    private Date dataAgendamento;
    private String observacoes;
    private String departamentoOrigem;
    private String tipoOs;
    
    private List<InstalacaoProduto> produtosInstalacao;
    
    public OrdemServico() {
        this.produtosInstalacao = new ArrayList<>();
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    
    public int getIdEquipamento() { return idEquipamento; }
    public void setIdEquipamento(int idEquipamento) { this.idEquipamento = idEquipamento; }
    
    public String getTipoNivel() { return tipoNivel; }
    public void setTipoNivel(String tipoNivel) { this.tipoNivel = tipoNivel; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    
    public String getDescricaoSolucao() { return descricaoSolucao; }
    public void setDescricaoSolucao(String descricaoSolucao) { this.descricaoSolucao = descricaoSolucao; }
    
    public String getFalhaIdentificada() { return falhaIdentificada; }
    public void setFalhaIdentificada(String falhaIdentificada) { this.falhaIdentificada = falhaIdentificada; }
    
    public Date getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(Date dataAbertura) { this.dataAbertura = dataAbertura; }
    
    public Date getDataFechamento() { return dataFechamento; }
    public void setDataFechamento(Date dataFechamento) { this.dataFechamento = dataFechamento; }
    
    public int getIdUsuarioAbertura() { return idUsuarioAbertura; }
    public void setIdUsuarioAbertura(int idUsuarioAbertura) { this.idUsuarioAbertura = idUsuarioAbertura; }
    
    public int getIdUsuarioFechamento() { return idUsuarioFechamento; }
    public void setIdUsuarioFechamento(int idUsuarioFechamento) { this.idUsuarioFechamento = idUsuarioFechamento; }
    
    public String getTipoOrdem() { return tipoOrdem; }
    public void setTipoOrdem(String tipoOrdem) { this.tipoOrdem = tipoOrdem; }
    
    public int getIdAtendimentoOrigem() { return idAtendimentoOrigem; }
    public void setIdAtendimentoOrigem(int idAtendimentoOrigem) { this.idAtendimentoOrigem = idAtendimentoOrigem; }
    
    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
    
    public String getEnderecoInstalacao() { return enderecoInstalacao; }
    public void setEnderecoInstalacao(String enderecoInstalacao) { this.enderecoInstalacao = enderecoInstalacao; }
    
    public Date getDataAgendamento() { return dataAgendamento; }
    public void setDataAgendamento(Date dataAgendamento) { this.dataAgendamento = dataAgendamento; }
    
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    
    public String getDepartamentoOrigem() { return departamentoOrigem; }
    public void setDepartamentoOrigem(String departamentoOrigem) { this.departamentoOrigem = departamentoOrigem; }
    
    public String getTipoOs() { return tipoOs; }
    public void setTipoOs(String tipoOs) { this.tipoOs = tipoOs; }
    
    public List<InstalacaoProduto> getProdutosInstalacao() { 
        if (produtosInstalacao == null) {
            produtosInstalacao = new ArrayList<>();
        }
        return produtosInstalacao; 
    }
    
    public void setProdutosInstalacao(List<InstalacaoProduto> produtosInstalacao) { 
        this.produtosInstalacao = produtosInstalacao; 
    }
    
    /**
     * Verifica se esta OS é uma ordem de instalação
     * CORRIGIDO: Verifica múltiplos campos para identificar instalações
     */
    public boolean isInstalacao() {
        // Método 1: verificar tipoOs
        if (tipoOs != null && "INSTALACAO".equals(tipoOs)) {
            System.out.println("isInstalacao: true por tipoOs=" + tipoOs);
            return true;
        }
        // Método 2: verificar tipoOrdem
        if (tipoOrdem != null && "INSTALACAO".equals(tipoOrdem)) {
            System.out.println("isInstalacao: true por tipoOrdem=" + tipoOrdem);
            return true;
        }
        // Método 3: se for do departamento VENDAS com valor total > 0 e tipo não definido explicitamente como outro
        if (departamentoOrigem != null && "VENDAS".equals(departamentoOrigem) && valorTotal > 0) {
            if (tipoOs == null) {
                System.out.println("isInstalacao: true por departamento=VENDAS e valorTotal>0");
                return true;
            }
            if (!"INFORMACAO".equals(tipoOs) && !"REPARO".equals(tipoOs) && !"ORCAMENTO".equals(tipoOs)) {
                System.out.println("isInstalacao: true por departamento=VENDAS e tipoOs não conflitante");
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifica se esta OS é um orçamento
     */
    public boolean isOrcamento() {
        if (tipoOs != null && "ORCAMENTO".equals(tipoOs)) return true;
        if (tipoOrdem != null && "ORCAMENTO".equals(tipoOrdem)) return true;
        return false;
    }
    
    /**
     * Verifica se esta OS é de informação
     */
    public boolean isInformacao() {
        if (tipoOs != null && "INFORMACAO".equals(tipoOs)) return true;
        return false;
    }
    
    /**
     * Verifica se esta OS é de reparo
     */
    public boolean isReparo() {
        if (tipoOs != null && "REPARO".equals(tipoOs)) return true;
        return false;
    }
    
    // Métodos auxiliares
    public String getDataAgendamentoFormatada() {
        if (dataAgendamento == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(dataAgendamento);
    }
    
    public void adicionarProdutoInstalacao(InstalacaoProduto produto) {
        if (produtosInstalacao == null) {
            produtosInstalacao = new ArrayList<>();
        }
        produtosInstalacao.add(produto);
        recalcularValorTotal();
    }
    
    public void removerProdutoInstalacao(int index) {
        if (produtosInstalacao != null && index >= 0 && index < produtosInstalacao.size()) {
            produtosInstalacao.remove(index);
            recalcularValorTotal();
        }
    }
    
    public void limparProdutosInstalacao() {
        if (produtosInstalacao != null) {
            produtosInstalacao.clear();
            valorTotal = 0;
        }
    }
    
    private void recalcularValorTotal() {
        valorTotal = 0;
        if (produtosInstalacao != null) {
            for (InstalacaoProduto p : produtosInstalacao) {
                valorTotal += p.getSubtotal();
            }
        }
    }
    
    public String getNomeExibicao() {
        if (isInstalacao()) {
            return "OS Instalação #" + id;
        } else if (isOrcamento()) {
            return "OS Orçamento #" + id;
        } else if (isInformacao()) {
            return "OS Informação #" + id;
        } else if (isReparo()) {
            return "OS Reparo #" + id;
        }
        return "OS #" + id;
    }
    
    public int getQuantidadeProdutos() {
        return produtosInstalacao != null ? produtosInstalacao.size() : 0;
    }
    
    @Override
    public String toString() {
        return "OrdemServico{" +
                "id=" + id +
                ", idCliente=" + idCliente +
                ", tipoOs=" + tipoOs +
                ", tipoOrdem=" + tipoOrdem +
                ", status=" + status +
                ", valorTotal=" + valorTotal +
                ", dataAgendamento=" + dataAgendamento +
                '}';
    }
}
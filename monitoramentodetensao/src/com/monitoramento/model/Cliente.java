// Cliente.java
package com.monitoramento.model;

import java.util.Date;

public class Cliente {
    private int id;
    private String tipo; // "FISICA" ou "JURIDICA"
    private String documento; // CPF ou CNPJ
    private String nome;
    private String sobrenome; // Para PF
    private String razaoSocial; // Para PJ
    private String nomeFantasia; // Para PJ
    private String logradouro;
    private String numero;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String telefoneDDD;
    private String telefoneNumero;
    private String ipCliente;
    private boolean emMonitoramento;
    private Date dataCadastro;
    
    // Prioridade de Atendimento
    private String prioridadeAtendimento; // "BAIXA", "MEDIA", "ALTA"
    
    // Equipamento do cliente (agora parte do cadastro do cliente)
    private Equipamento equipamento;
    
    // Construtores
    public Cliente() {}
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getSobrenome() { return sobrenome; }
    public void setSobrenome(String sobrenome) { this.sobrenome = sobrenome; }
    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getTelefoneDDD() { return telefoneDDD; }
    public void setTelefoneDDD(String telefoneDDD) { this.telefoneDDD = telefoneDDD; }
    public String getTelefoneNumero() { return telefoneNumero; }
    public void setTelefoneNumero(String telefoneNumero) { this.telefoneNumero = telefoneNumero; }
    public String getIpCliente() { return ipCliente; }
    public void setIpCliente(String ipCliente) { this.ipCliente = ipCliente; }
    public boolean isEmMonitoramento() { return emMonitoramento; }
    public void setEmMonitoramento(boolean emMonitoramento) { this.emMonitoramento = emMonitoramento; }
    public Date getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(Date dataCadastro) { this.dataCadastro = dataCadastro; }
    
    // NOVOS GETTERS/SETTERS
    public String getPrioridadeAtendimento() { return prioridadeAtendimento; }
    public void setPrioridadeAtendimento(String prioridadeAtendimento) { this.prioridadeAtendimento = prioridadeAtendimento; }
    
    public Equipamento getEquipamento() { return equipamento; }
    public void setEquipamento(Equipamento equipamento) { this.equipamento = equipamento; }
    
    public String getNomeExibicao() {
        String nomeBase;
        if (tipo != null && tipo.equals("FISICA")) {
            nomeBase = nome + " " + (sobrenome != null ? sobrenome : "");
        } else {
            nomeBase = razaoSocial != null ? razaoSocial : "";
        }
        
        // Retornar com ID e endereço para melhor identificação
        String enderecoResumido = getEnderecoResumido();
        if (enderecoResumido != null && !enderecoResumido.isEmpty()) {
            return nomeBase + " (ID: " + id + ") - " + enderecoResumido;
        }
        return nomeBase + " (ID: " + id + ")";
    }
    
    public String getEnderecoCompleto() {
        StringBuilder sb = new StringBuilder();
        if (logradouro != null) sb.append(logradouro);
        if (numero != null && !numero.isEmpty()) sb.append(", ").append(numero);
        if (bairro != null && !bairro.isEmpty()) sb.append(" - ").append(bairro);
        if (cidade != null && !cidade.isEmpty()) sb.append(" - ").append(cidade);
        if (estado != null && !estado.isEmpty()) sb.append("/").append(estado);
        if (cep != null && !cep.isEmpty()) sb.append(" - CEP: ").append(cep);
        return sb.toString();
    }
    
    public String getEnderecoResumido() {
        if (logradouro == null) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(logradouro);
        if (numero != null && !numero.isEmpty()) sb.append(", ").append(numero);
        if (cidade != null && !cidade.isEmpty()) sb.append(" - ").append(cidade);
        return sb.toString();
    }
    
    public String getTelefoneCompleto() {
        return "(" + telefoneDDD + ") " + telefoneNumero;
    }
    
    /**
     * Verifica se este cliente tem o mesmo documento e mesmo endereço de outro cliente
     */
    public boolean mesmoDocumentoEEndereco(Cliente outro) {
        if (outro == null) return false;
        if (this.documento == null || outro.documento == null) return false;
        if (!this.documento.equals(outro.documento)) return false;
        
        return this.getEnderecoCompleto().equals(outro.getEnderecoCompleto());
    }
    
    /**
     * Retorna uma chave única composta por documento + endereço
     */
    public String getChaveDocumentoEndereco() {
        return (documento != null ? documento : "") + "|" + getEnderecoCompleto();
    }
}
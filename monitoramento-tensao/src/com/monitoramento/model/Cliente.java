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
    
    public String getNomeExibicao() {
        if (tipo != null && tipo.equals("FISICA")) {
            return nome + " " + sobrenome + " (ID: " + id + ")";
        } else {
            return razaoSocial + " (ID: " + id + ")";
        }
    }
    
    public String getTelefoneCompleto() {
        return "(" + telefoneDDD + ") " + telefoneNumero;
    }
}
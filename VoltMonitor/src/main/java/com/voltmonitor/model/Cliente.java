package com.voltmonitor.model;

public class Cliente {
    private int id;
    private String tipoPessoa; // "PF" ou "PJ"
    private String nome;       // Nome/Sobrenome para PF, Razão Social para PJ
    private String sobrenome;  // apenas para PF
    private String documento;  // CPF para PF, CNPJ para PJ
    private String logradouro;
    private String numero;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String ddd;
    private String telefone;
    private String ipLocal;
    private boolean monitorado;
    private int equipamentoId;
    private boolean ativo;

    public Cliente() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTipoPessoa() { return tipoPessoa; }
    public void setTipoPessoa(String tipoPessoa) { this.tipoPessoa = tipoPessoa; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getSobrenome() { return sobrenome; }
    public void setSobrenome(String sobrenome) { this.sobrenome = sobrenome; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
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
    public String getDdd() { return ddd; }
    public void setDdd(String ddd) { this.ddd = ddd; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getIpLocal() { return ipLocal; }
    public void setIpLocal(String ipLocal) { this.ipLocal = ipLocal; }
    public boolean isMonitorado() { return monitorado; }
    public void setMonitorado(boolean monitorado) { this.monitorado = monitorado; }
    public int getEquipamentoId() { return equipamentoId; }
    public void setEquipamentoId(int equipamentoId) { this.equipamentoId = equipamentoId; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public String getNomeExibicao() {
        if ("PF".equals(tipoPessoa)) {
            return nome + (sobrenome != null && !sobrenome.isEmpty() ? " " + sobrenome : "");
        }
        return nome; // Razão social
    }
}

package com.voltmonitor.model;

// ============================================================
// Usuario.java
// ============================================================

public class Usuario {
    private int id;
    private String nome;
    private String sobrenome;
    private String cpf;
    private String matricula;
    private String funcao; // "Administrador" ou "Monitor"
    private String login;
    private String senhaHash;
    private boolean ativo;

    public Usuario() {}

    public Usuario(String nome, String sobrenome, String cpf, String matricula,
                   String funcao, String login, String senhaHash) {
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.cpf = cpf;
        this.matricula = matricula;
        this.funcao = funcao;
        this.login = login;
        this.senhaHash = senhaHash;
        this.ativo = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getSobrenome() { return sobrenome; }
    public void setSobrenome(String sobrenome) { this.sobrenome = sobrenome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getFuncao() { return funcao; }
    public void setFuncao(String funcao) { this.funcao = funcao; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public String getNomeCompleto() { return nome + " " + sobrenome; }
    public boolean isAdministrador() { return "Administrador".equals(funcao); }
}

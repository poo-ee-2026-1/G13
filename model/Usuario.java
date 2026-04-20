package com.monitoramento.model;

import java.util.Date;

public class Usuario {
    private int id;
    private String nome;
    private String sobrenome;
    private String cpf;
    private String matricula;
    private String funcao;
    private String login;
    private String senha;
    private Date dataCadastro;
    
    public Usuario() {}
    
    public Usuario(int id, String nome, String sobrenome, String cpf, 
                   String matricula, String funcao, String login, String senha) {
        this.id = id;
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.cpf = cpf;
        this.matricula = matricula;
        this.funcao = funcao;
        this.login = login;
        this.senha = senha;
    }
    
    // Getters e Setters
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
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Date getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(Date dataCadastro) { this.dataCadastro = dataCadastro; }
    
    public String getNomeCompleto() {
        return nome + " " + sobrenome;
    }
}
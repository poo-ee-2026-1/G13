// Usuario.java
package com.monitoramento.model;

import java.util.Date;

public class Usuario {
    private int id;
    private String nome;
    private String sobrenome;
    private String cpf;
    private String matricula;
    private String funcao;
    private String departamento;
    private String login;
    private String senha;
    private Date dataCadastro;
    private String nivelAtendimento;  // Campo para nível de atendimento: "1º Nível", "2º Nível", "3º Nível"
    
    public Usuario() {
        this.nivelAtendimento = "1º Nível";  // Valor padrão
    }
    
    public Usuario(int id, String nome, String sobrenome, String cpf, 
                   String matricula, String funcao, String departamento, String login, String senha) {
        this.id = id;
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.cpf = cpf;
        this.matricula = matricula;
        this.funcao = funcao;
        this.departamento = departamento;
        this.login = login;
        this.senha = senha;
        this.nivelAtendimento = "1º Nível";
    }
    
    // Getters e Setters existentes
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
    
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    
    public Date getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(Date dataCadastro) { this.dataCadastro = dataCadastro; }
    
    // NOVO GETTER E SETTER para nível de atendimento
    public String getNivelAtendimento() { 
        return nivelAtendimento; 
    }
    
    public void setNivelAtendimento(String nivelAtendimento) { 
        this.nivelAtendimento = nivelAtendimento; 
    }
    
    public String getNomeCompleto() {
        return nome + " " + sobrenome;
    }
    
    /**
     * Verifica se o usuário pode visualizar ordens de serviço de um determinado nível
     * @param nivelOS Nível da OS ("1º Nível", "2º Nível", "3º Nível")
     * @return true se o usuário pode visualizar, false caso contrário
     */
    public boolean podeVisualizarNivel(String nivelOS) {
        if (nivelOS == null) return true;
        
        String nivelUsuario = this.nivelAtendimento;
        if (nivelUsuario == null) nivelUsuario = "1º Nível";
        
        if ("3º Nível".equals(nivelUsuario)) {
            return true;  // 3º Nível pode ver tudo
        } else if ("2º Nível".equals(nivelUsuario)) {
            return !"3º Nível".equals(nivelOS);  // 2º Nível não vê 3º Nível
        } else {  // 1º Nível
            return "1º Nível".equals(nivelOS);  // 1º Nível só vê 1º Nível
        }
    }
}
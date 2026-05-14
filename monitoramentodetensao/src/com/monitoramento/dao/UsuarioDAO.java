// UsuarioDAO.java
package com.monitoramento.dao;

import com.monitoramento.model.Usuario;
import com.monitoramento.util.DatabaseConnection;

import java.util.ArrayList;  // IMPORT ADICIONADA
import java.util.Date;       // IMPORT ADICIONADA
import java.util.List;       // IMPORT ADICIONADA
import java.util.stream.Collectors;

public class UsuarioDAO {
    private static final String FILE_NAME = "usuarios.json";
    private List<Usuario> usuarios;
    
    public UsuarioDAO() {
        carregarUsuarios();
    }
    
    private void carregarUsuarios() {
        usuarios = DatabaseConnection.carregarLista(FILE_NAME, Usuario.class);
        if (usuarios == null) {
            usuarios = new ArrayList<>();
        }
        // Garantir que usuários existentes tenham nível definido
        for (Usuario u : usuarios) {
            if (u.getNivelAtendimento() == null) {
                u.setNivelAtendimento("1º Nível");
            }
        }
        System.out.println("Carregados " + usuarios.size() + " usuários do arquivo");
    }
    
    private void salvarUsuarios() {
        DatabaseConnection.salvarLista(FILE_NAME, usuarios);
        System.out.println("Salvos " + usuarios.size() + " usuários no arquivo");
    }
    
    public boolean inserir(Usuario usuario) {
        if (buscarPorLogin(usuario.getLogin()) != null) {
            System.err.println("Erro: Login já existe!");
            return false;
        }
        if (buscarPorCpf(usuario.getCpf()) != null) {
            System.err.println("Erro: CPF já existe!");
            return false;
        }
        if (buscarPorMatricula(usuario.getMatricula()) != null) {
            System.err.println("Erro: Matrícula já existe!");
            return false;
        }
        
        usuario.setId(DatabaseConnection.gerarNovoId(usuarios));
        usuario.setDataCadastro(new Date());
        if (usuario.getNivelAtendimento() == null) {
            usuario.setNivelAtendimento("1º Nível");
        }
        usuarios.add(usuario);
        salvarUsuarios();
        System.out.println("Usuário inserido: " + usuario.getLogin() + " - Nível: " + usuario.getNivelAtendimento());
        return true;
    }
    
    public boolean atualizar(Usuario usuario) {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getId() == usuario.getId()) {
                // Manter data de cadastro original
                usuario.setDataCadastro(usuarios.get(i).getDataCadastro());
                usuarios.set(i, usuario);
                salvarUsuarios();
                System.out.println("Usuário atualizado: " + usuario.getLogin() + " - Nível: " + usuario.getNivelAtendimento());
                return true;
            }
        }
        return false;
    }
    
    public boolean excluir(int id) {
        boolean removido = usuarios.removeIf(u -> u.getId() == id);
        if (removido) {
            salvarUsuarios();
            System.out.println("Usuário ID " + id + " removido");
        }
        return removido;
    }
    
    public Usuario buscarPorId(int id) {
        return usuarios.stream()
            .filter(u -> u.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public Usuario buscarPorLogin(String login) {
        return usuarios.stream()
            .filter(u -> u.getLogin() != null && u.getLogin().equals(login))
            .findFirst()
            .orElse(null);
    }
    
    public Usuario buscarPorCpf(String cpf) {
        return usuarios.stream()
            .filter(u -> u.getCpf() != null && u.getCpf().equals(cpf))
            .findFirst()
            .orElse(null);
    }
    
    public Usuario buscarPorMatricula(String matricula) {
        return usuarios.stream()
            .filter(u -> u.getMatricula() != null && u.getMatricula().equals(matricula))
            .findFirst()
            .orElse(null);
    }
    
    public Usuario autenticar(String login, String senha) {
        return usuarios.stream()
            .filter(u -> u.getLogin() != null && u.getLogin().equals(login) &&
                        u.getSenha() != null && u.getSenha().equals(senha))
            .findFirst()
            .orElse(null);
    }
    
    public List<Usuario> listarTodos() {
        return new ArrayList<>(usuarios);
    }
    
    public List<Usuario> listarPorFuncao(String funcao) {
        return usuarios.stream()
            .filter(u -> u.getFuncao() != null && u.getFuncao().equals(funcao))
            .collect(Collectors.toList());
    }
    
    public List<Usuario> listarPorNivel(String nivel) {
        return usuarios.stream()
            .filter(u -> u.getNivelAtendimento() != null && u.getNivelAtendimento().equals(nivel))
            .collect(Collectors.toList());
    }
    
    public List<Usuario> listarPorDepartamento(String departamento) {
        return usuarios.stream()
            .filter(u -> u.getDepartamento() != null && u.getDepartamento().equals(departamento))
            .collect(Collectors.toList());
    }
}
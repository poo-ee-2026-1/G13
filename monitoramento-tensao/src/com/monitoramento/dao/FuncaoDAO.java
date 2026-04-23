// FuncaoDAO.java
package com.monitoramento.dao;

import com.monitoramento.model.Funcao;
import com.monitoramento.util.DatabaseConnection;

import java.util.*;
import java.util.stream.Collectors;

public class FuncaoDAO {
    private static final String FILE_NAME = "funcoes.json";
    private List<Funcao> funcoes;
    
    public FuncaoDAO() {
        carregarFuncoes();
    }
    
    private void carregarFuncoes() {
        funcoes = DatabaseConnection.carregarLista(FILE_NAME, Funcao.class);
        if (funcoes == null) {
            funcoes = new ArrayList<>();
        }
    }
    
    private void salvarFuncoes() {
        DatabaseConnection.salvarLista(FILE_NAME, funcoes);
    }
    
    public boolean inserir(Funcao funcao) {
        if (buscarPorNome(funcao.getNome()) != null) {
            return false;
        }
        
        funcao.setId(DatabaseConnection.gerarNovoId(funcoes));
        funcoes.add(funcao);
        salvarFuncoes();
        return true;
    }
    
    public boolean atualizar(Funcao funcao) {
        for (int i = 0; i < funcoes.size(); i++) {
            if (funcoes.get(i).getId() == funcao.getId()) {
                funcoes.set(i, funcao);
                salvarFuncoes();
                return true;
            }
        }
        return false;
    }
    
    public boolean excluir(int id) {
        boolean removido = funcoes.removeIf(f -> f.getId() == id);
        if (removido) {
            salvarFuncoes();
        }
        return removido;
    }
    
    public Funcao buscarPorId(int id) {
        return funcoes.stream()
            .filter(f -> f.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public Funcao buscarPorNome(String nome) {
        return funcoes.stream()
            .filter(f -> f.getNome() != null && f.getNome().equals(nome))
            .findFirst()
            .orElse(null);
    }
    
    public List<Funcao> listarTodos() {
        return new ArrayList<>(funcoes);
    }
    
    public List<Funcao> buscarPorDepartamento(String departamento) {
        return funcoes.stream()
            .filter(f -> f.getDepartamento() != null && f.getDepartamento().equals(departamento))
            .collect(Collectors.toList());
    }
    
    public List<Funcao> buscarPorNomeContendo(String termo) {
        return funcoes.stream()
            .filter(f -> (f.getNome() != null && f.getNome().toLowerCase().contains(termo.toLowerCase())) ||
                        (f.getDescricao() != null && f.getDescricao().toLowerCase().contains(termo.toLowerCase())))
            .collect(Collectors.toList());
    }
    
    public boolean inicializarFuncoesPadrao() {
        if (!funcoes.isEmpty()) {
            return true;
        }
        
        String[] nomes = {"Gerente", "Coordenador", "Atendente", "Vendedor", "Auxiliar", "Desenvolvedor"};
        String[] descricoes = {
            "Gerencia o departamento e equipe",
            "Coordena as atividades da equipe",
            "Realiza atendimento aos clientes",
            "Responsável por vendas",
            "Auxilia nas atividades gerais",
            "Desenvolve e mantém sistemas"
        };
        String[] departamentos = {"GERAL", "GERAL", "ATENDIMENTO", "VENDAS", "GERAL", "TI"};
        
        boolean sucesso = true;
        for (int i = 0; i < nomes.length; i++) {
            Funcao funcao = new Funcao();
            funcao.setNome(nomes[i]);
            funcao.setDescricao(descricoes[i]);
            funcao.setDepartamento(departamentos[i]);
            if (!inserir(funcao)) {
                sucesso = false;
            }
        }
        
        return sucesso;
    }
}
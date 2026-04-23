// DepartamentoDAO.java
package com.monitoramento.dao;

import com.monitoramento.model.Departamento;
import com.monitoramento.util.DatabaseConnection;

import java.util.*;
import java.util.stream.Collectors;

public class DepartamentoDAO {
    private static final String FILE_NAME = "departamentos.json";
    private List<Departamento> departamentos;
    
    public DepartamentoDAO() {
        carregarDepartamentos();
    }
    
    private void carregarDepartamentos() {
        departamentos = DatabaseConnection.carregarLista(FILE_NAME, Departamento.class);
        if (departamentos == null) {
            departamentos = new ArrayList<>();
        }
    }
    
    private void salvarDepartamentos() {
        DatabaseConnection.salvarLista(FILE_NAME, departamentos);
    }
    
    public boolean inserir(Departamento departamento) {
        if (buscarPorNome(departamento.getNome()) != null) {
            return false;
        }
        
        departamento.setId(DatabaseConnection.gerarNovoId(departamentos));
        departamentos.add(departamento);
        salvarDepartamentos();
        return true;
    }
    
    public boolean atualizar(Departamento departamento) {
        for (int i = 0; i < departamentos.size(); i++) {
            if (departamentos.get(i).getId() == departamento.getId()) {
                departamentos.set(i, departamento);
                salvarDepartamentos();
                return true;
            }
        }
        return false;
    }
    
    public boolean excluir(int id) {
        boolean removido = departamentos.removeIf(d -> d.getId() == id);
        if (removido) {
            salvarDepartamentos();
        }
        return removido;
    }
    
    public Departamento buscarPorId(int id) {
        return departamentos.stream()
            .filter(d -> d.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public Departamento buscarPorNome(String nome) {
        return departamentos.stream()
            .filter(d -> d.getNome() != null && d.getNome().equals(nome))
            .findFirst()
            .orElse(null);
    }
    
    public List<Departamento> listarTodos() {
        return new ArrayList<>(departamentos);
    }
    
    public List<Departamento> buscarPorNomeContendo(String termo) {
        return departamentos.stream()
            .filter(d -> (d.getNome() != null && d.getNome().toLowerCase().contains(termo.toLowerCase())) ||
                        (d.getDescricao() != null && d.getDescricao().toLowerCase().contains(termo.toLowerCase())))
            .collect(Collectors.toList());
    }
    
    public boolean inicializarDepartamentosPadrao() {
        if (!departamentos.isEmpty()) {
            return true;
        }
        
        String[] nomes = {"ADMINISTRAÇÃO", "MONITORAMENTO", "SUPORTE TÉCNICO", "ATENDIMENTO", "VENDAS", "FINANCEIRO", "TI", "RH"};
        String[] descricoes = {
            "Departamento Administrativo responsável pela Gestão do Sistema",
            "Departamento responsável pelo Monitoramento Remoto",
            "Departamento responsável pelo Suporte Técnico Presencial",
            "Departamento de Atendimento ao Cliente",
            "Departamento de Vendas",
            "Departamento Financeiro",
            "Departamento de Tecnologia da Informação",
            "Departamento de Recursos Humanos"
        };
        
        boolean sucesso = true;
        for (int i = 0; i < nomes.length; i++) {
            Departamento dept = new Departamento();
            dept.setNome(nomes[i]);
            dept.setDescricao(descricoes[i]);
            if (!inserir(dept)) {
                sucesso = false;
            }
        }
        
        return sucesso;
    }
}
// AtendimentoDAO.java
package com.monitoramento.dao;

import com.monitoramento.model.Atendimento;
import com.monitoramento.util.DatabaseConnection;

import java.util.*;
import java.util.stream.Collectors;

public class AtendimentoDAO {
    private static final String FILE_NAME = "atendimentos.json";
    private List<Atendimento> atendimentos;
    
    public AtendimentoDAO() {
        carregarAtendimentos();
    }
    
    private void carregarAtendimentos() {
        atendimentos = DatabaseConnection.carregarLista(FILE_NAME, Atendimento.class);
        if (atendimentos == null) {
            atendimentos = new ArrayList<>();
        }
    }
    
    private void salvarAtendimentos() {
        DatabaseConnection.salvarLista(FILE_NAME, atendimentos);
    }
    
    public boolean inserir(Atendimento atendimento) {
        atendimento.setId(DatabaseConnection.gerarNovoId(atendimentos));
        atendimento.setDataAbertura(new Date());
        atendimento.setStatus("PENDENTE");
        atendimentos.add(atendimento);
        salvarAtendimentos();
        return true;
    }
    
    public boolean atualizar(Atendimento atendimento) {
        for (int i = 0; i < atendimentos.size(); i++) {
            if (atendimentos.get(i).getId() == atendimento.getId()) {
                atendimentos.set(i, atendimento);
                salvarAtendimentos();
                return true;
            }
        }
        return false;
    }
    
    public boolean excluir(int id) {
        boolean removido = atendimentos.removeIf(a -> a.getId() == id);
        if (removido) {
            salvarAtendimentos();
        }
        return removido;
    }
    
    public Atendimento buscarPorId(int id) {
        return atendimentos.stream()
            .filter(a -> a.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public List<Atendimento> listarTodos() {
        return new ArrayList<>(atendimentos);
    }
    
    public List<Atendimento> listarPorCliente(int idCliente) {
        return atendimentos.stream()
            .filter(a -> a.getIdCliente() == idCliente)
            .sorted((a, b) -> b.getDataAbertura().compareTo(a.getDataAbertura()))
            .collect(Collectors.toList());
    }
    
    public List<Atendimento> listarPorTipo(String tipo) {
        return atendimentos.stream()
            .filter(a -> a.getTipo() != null && a.getTipo().equals(tipo))
            .sorted((a, b) -> b.getDataAbertura().compareTo(a.getDataAbertura()))
            .collect(Collectors.toList());
    }
    
    public List<Atendimento> listarPorStatus(String status) {
        return atendimentos.stream()
            .filter(a -> a.getStatus() != null && a.getStatus().equals(status))
            .sorted((a, b) -> b.getDataAbertura().compareTo(a.getDataAbertura()))
            .collect(Collectors.toList());
    }
    
    public List<Atendimento> listarPorUsuarioAtendimento(int idUsuario) {
        return atendimentos.stream()
            .filter(a -> a.getIdUsuarioAtendimento() == idUsuario)
            .sorted((a, b) -> b.getDataAbertura().compareTo(a.getDataAbertura()))
            .collect(Collectors.toList());
    }
    
    public boolean concluirAtendimento(int id, String descricaoConclusao, int idUsuario) {
        Atendimento a = buscarPorId(id);
        if (a != null && "PENDENTE".equals(a.getStatus())) {
            a.setStatus("CONCLUIDO");
            a.setDataConclusao(new Date());
            a.setIdUsuarioAtendimento(idUsuario);
            // Adicionar descrição de conclusão ao campo descrição
            String descricaoOriginal = a.getDescricao();
            a.setDescricao(descricaoOriginal + "\n\n--- CONCLUSÃO ---\n" + descricaoConclusao);
            return atualizar(a);
        }
        return false;
    }
    
    public List<Atendimento> listarPendentes() {
        return atendimentos.stream()
            .filter(a -> "PENDENTE".equals(a.getStatus()))
            .sorted((a, b) -> b.getDataAbertura().compareTo(a.getDataAbertura()))
            .collect(Collectors.toList());
    }
}
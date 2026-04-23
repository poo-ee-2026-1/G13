package com.monitoramento.dao;

import com.monitoramento.model.MedicaoTensao;
import com.monitoramento.util.DatabaseConnection;

import java.util.*;
import java.util.stream.Collectors;

public class MedicaoTensaoDAO {
    private static final String FILE_NAME = "medicoes.json";
    private List<MedicaoTensao> medicoes;
    
    public MedicaoTensaoDAO() {
        carregarMedicoes();
    }
    
    private void carregarMedicoes() {
        medicoes = DatabaseConnection.carregarLista(FILE_NAME, MedicaoTensao.class);
        if (medicoes == null) {
            medicoes = new ArrayList<>();
        }
    }
    
    private void salvarMedicoes() {
        DatabaseConnection.salvarLista(FILE_NAME, medicoes);
    }
    
    public boolean inserir(MedicaoTensao medicao) {
        medicao.setId(DatabaseConnection.gerarNovoId(medicoes));
        medicoes.add(medicao);
        salvarMedicoes();
        return true;
    }
    
    public List<MedicaoTensao> buscarPorCliente(int idCliente, Date dataInicio, Date dataFim) {
        return medicoes.stream()
            .filter(m -> m.getIdCliente() == idCliente &&
                        m.getDataHora() != null &&
                        m.getDataHora().after(dataInicio) &&
                        m.getDataHora().before(dataFim))
            .sorted((a, b) -> b.getDataHora().compareTo(a.getDataHora()))
            .collect(Collectors.toList());
    }
    
    public List<MedicaoTensao> buscarUltimasMedicoes(int idCliente, int limite) {
        return medicoes.stream()
            .filter(m -> m.getIdCliente() == idCliente)
            .sorted((a, b) -> b.getDataHora().compareTo(a.getDataHora()))
            .limit(limite)
            .collect(Collectors.toList());
    }
    
    public double calcularMediaTensao(int idCliente, Date dataInicio, Date dataFim) {
        return medicoes.stream()
            .filter(m -> m.getIdCliente() == idCliente &&
                        m.getDataHora() != null &&
                        m.getDataHora().after(dataInicio) &&
                        m.getDataHora().before(dataFim))
            .mapToDouble(MedicaoTensao::getTensao)
            .average()
            .orElse(0);
    }
    
    public boolean excluirMedicoesAntigas(int dias) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -dias);
        Date limite = cal.getTime();
        
        int before = medicoes.size();
        medicoes.removeIf(m -> m.getDataHora() != null && m.getDataHora().before(limite));
        int after = medicoes.size();
        
        if (before != after) {
            salvarMedicoes();
            System.out.println("Excluídas " + (before - after) + " medições antigas");
        }
        return true;
    }
}
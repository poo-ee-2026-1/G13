// OrdemServicoDAO.java
package com.monitoramento.dao;

import com.monitoramento.model.OrdemServico;
import com.monitoramento.model.InstalacaoProduto;
import com.monitoramento.util.DatabaseConnection;

import java.util.ArrayList;  // IMPORT ADICIONADA
import java.util.Date;       // IMPORT ADICIONADA
import java.util.HashMap;    // IMPORT ADICIONADA
import java.util.List;       // IMPORT ADICIONADA
import java.util.Map;        // IMPORT ADICIONADA
import java.util.stream.Collectors;

public class OrdemServicoDAO {
    private static final String FILE_NAME = "ordens_servico.json";
    private List<OrdemServico> ordens;
    
    public OrdemServicoDAO() {
        carregarOrdens();
    }
    
    private void carregarOrdens() {
        ordens = DatabaseConnection.carregarLista(FILE_NAME, OrdemServico.class);
        if (ordens == null) {
            ordens = new ArrayList<>();
        }
        System.out.println("Carregadas " + ordens.size() + " ordens de serviço");
        
        for (OrdemServico os : ordens) {
            System.out.println("OS #" + os.getId() + " - TipoOs: " + os.getTipoOs() + 
                             " - TipoOrdem: " + os.getTipoOrdem() +
                             " - Status: " + os.getStatus() + 
                             " - Prioridade: " + os.getPrioridade() +
                             " - Endereço: " + os.getEnderecoInstalacao() +
                             " - isInstalacao: " + os.isInstalacao());
        }
    }
    
    private void salvarOrdens() {
        DatabaseConnection.salvarLista(FILE_NAME, ordens);
        System.out.println("Salvas " + ordens.size() + " ordens de serviço");
    }
    
    public boolean inserir(OrdemServico os) {
        os.setId(DatabaseConnection.gerarNovoId(ordens));
        ordens.add(os);
        salvarOrdens();
        System.out.println("OS inserida - ID: " + os.getId() + 
                         ", TipoOs: " + os.getTipoOs() + 
                         ", TipoOrdem: " + os.getTipoOrdem() +
                         ", Prioridade: " + os.getPrioridade() +
                         ", isInstalacao: " + os.isInstalacao());
        return true;
    }
    
    public boolean atualizar(OrdemServico os) {
        for (int i = 0; i < ordens.size(); i++) {
            if (ordens.get(i).getId() == os.getId()) {
                ordens.set(i, os);
                salvarOrdens();
                return true;
            }
        }
        return false;
    }
    
    public boolean excluir(int id) {
        boolean removido = ordens.removeIf(o -> o.getId() == id);
        if (removido) {
            salvarOrdens();
        }
        return removido;
    }
    
    public OrdemServico buscarPorId(int id) {
        return ordens.stream()
            .filter(o -> o.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public List<OrdemServico> listarTodas() {
        return ordens.stream()
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
    }
    
    /**
     * Verifica se um cliente possui OS aberta de um tipo específico
     * @param idCliente ID do cliente
     * @param tipoOs Tipo da OS (INSTALACAO, REPARO, etc.)
     * @return true se existe OS aberta do tipo especificado
     */
    public boolean clientePossuiOSAbertaPorTipo(int idCliente, String tipoOs) {
        return ordens.stream()
            .filter(o -> o.getIdCliente() == idCliente)
            .filter(o -> !"FECHADA".equals(o.getStatus()))
            .anyMatch(o -> tipoOs.equals(o.getTipoOs()));
    }
    
    /**
     * Verifica se um cliente possui OS aberta para um endereço específico
     * @param idCliente ID do cliente
     * @param endereco Endereço da instalação
     * @return true se existe OS aberta para o endereço
     */
    public boolean clientePossuiOSAbertaPorEndereco(int idCliente, String endereco) {
        if (endereco == null || endereco.trim().isEmpty()) return false;
        
        return ordens.stream()
            .filter(o -> o.getIdCliente() == idCliente)
            .filter(o -> !"FECHADA".equals(o.getStatus()))
            .filter(o -> o.getEnderecoInstalacao() != null)
            .anyMatch(o -> o.getEnderecoInstalacao().equals(endereco));
    }
    
    /**
     * Retorna todas as OS abertas de um cliente
     * @param idCliente ID do cliente
     * @return Lista de OS abertas
     */
    public List<OrdemServico> buscarOSAbertasPorCliente(int idCliente) {
        return ordens.stream()
            .filter(o -> o.getIdCliente() == idCliente)
            .filter(o -> !"FECHADA".equals(o.getStatus()))
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
    }
    
    /**
     * Retorna OS abertas por tipo
     */
    public List<OrdemServico> buscarOSAbertasPorTipo(int idCliente, String tipoOs) {
        return ordens.stream()
            .filter(o -> o.getIdCliente() == idCliente)
            .filter(o -> !"FECHADA".equals(o.getStatus()))
            .filter(o -> tipoOs.equals(o.getTipoOs()))
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
    }
    
    // Métodos existentes com modificação para permitir múltiplas OS
    public List<OrdemServico> buscarOrdensAbertasPorCliente(int idCliente) {
        return buscarOSAbertasPorCliente(idCliente);
    }
    
    // CORREÇÃO: Agora retorna todas as OS, não apenas uma
    public List<OrdemServico> buscarOrdensAbertasPorClienteList(int idCliente) {
        return buscarOSAbertasPorCliente(idCliente);
    }
    
    public List<OrdemServico> listarOsInstalacao() {
        List<OrdemServico> instalacoes = ordens.stream()
            .filter(o -> o.isInstalacao() || "INSTALACAO".equals(o.getTipoOs()) || "INSTALACAO".equals(o.getTipoOrdem()))
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
        
        System.out.println("=== listarOsInstalacao() ===");
        System.out.println("Total de OS de instalação encontradas: " + instalacoes.size());
        for (OrdemServico os : instalacoes) {
            System.out.println("  - OS #" + os.getId() + 
                             " | Cliente: " + os.getIdCliente() + 
                             " | Status: " + os.getStatus() + 
                             " | Prioridade: " + os.getPrioridade() +
                             " | Endereço: " + os.getEnderecoInstalacao());
        }
        return instalacoes;
    }
    
    public List<OrdemServico> listarOrdensInstalacaoPorStatus(String status) {
        return ordens.stream()
            .filter(o -> o.isInstalacao() && o.getStatus() != null && o.getStatus().equals(status))
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
    }
    
    public List<OrdemServico> listarOrdensInstalacaoPorCliente(int idCliente) {
        return ordens.stream()
            .filter(o -> o.isInstalacao() && o.getIdCliente() == idCliente)
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
    }
    
    public boolean concluirInstalacao(int id, String observacoes, int idUsuario) {
        OrdemServico os = buscarPorId(id);
        if (os != null && os.isInstalacao() && "ABERTA".equals(os.getStatus())) {
            os.setStatus("FECHADA");
            os.setObservacoes(observacoes);
            os.setDataFechamento(new Date());
            os.setIdUsuarioFechamento(idUsuario);
            return atualizar(os);
        }
        return false;
    }
    
    // Métodos de estatísticas
    public Map<String, Double> getEstatisticasInstalacoes() {
        Map<String, Double> estatisticas = new HashMap<>();
        List<OrdemServico> instalacoes = listarOsInstalacao();
        
        long total = instalacoes.size();
        long abertas = instalacoes.stream().filter(o -> "ABERTA".equals(o.getStatus()) || "EM_ATENDIMENTO".equals(o.getStatus())).count();
        long fechadas = instalacoes.stream().filter(o -> "FECHADA".equals(o.getStatus())).count();
        
        double valorTotal = instalacoes.stream().mapToDouble(OrdemServico::getValorTotal).sum();
        int totalProdutos = instalacoes.stream().mapToInt(OrdemServico::getQuantidadeProdutos).sum();
        
        estatisticas.put("total", (double) total);
        estatisticas.put("abertas", (double) abertas);
        estatisticas.put("fechadas", (double) fechadas);
        estatisticas.put("valorTotal", valorTotal);
        estatisticas.put("totalProdutos", (double) totalProdutos);
        
        return estatisticas;
    }
    
    public List<OrdemServico> listarPorTipoOs(String tipoOs) {
        return ordens.stream()
            .filter(o -> o.getTipoOs() != null && o.getTipoOs().equals(tipoOs))
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
    }
    
    public List<OrdemServico> listarOsInformacao() {
        return ordens.stream()
            .filter(o -> "INFORMACAO".equals(o.getTipoOs()))
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
    }
    
    public List<OrdemServico> listarOsReparo() {
        return ordens.stream()
            .filter(o -> "REPARO".equals(o.getTipoOs()))
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
    }
    
    public List<OrdemServico> listarOsOrcamento() {
        return ordens.stream()
            .filter(o -> "ORCAMENTO".equals(o.getTipoOs()))
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
    }
    
    public List<OrdemServico> listarPorDepartamentoOrigem(String departamento) {
        return ordens.stream()
            .filter(o -> o.getDepartamentoOrigem() != null && o.getDepartamentoOrigem().equals(departamento))
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
    }
    
    public List<OrdemServico> buscarPorStatus(String status) {
        return ordens.stream()
            .filter(o -> o.getStatus() != null && o.getStatus().equals(status))
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
    }
    
    public List<OrdemServico> buscarPorCliente(int idCliente) {
        return ordens.stream()
            .filter(o -> o.getIdCliente() == idCliente)
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
    }
    
    public boolean fecharOrdemServico(int id, String descricaoSolucao, String falhaIdentificada, int idUsuarioFechamento) {
        OrdemServico os = buscarPorId(id);
        if (os != null && "ABERTA".equals(os.getStatus())) {
            os.setStatus("FECHADA");
            os.setDescricaoSolucao(descricaoSolucao);
            os.setFalhaIdentificada(falhaIdentificada);
            os.setDataFechamento(new Date());
            os.setIdUsuarioFechamento(idUsuarioFechamento);
            return atualizar(os);
        }
        return false;
    }
    
    public boolean atualizarStatus(int id, String status) {
        OrdemServico os = buscarPorId(id);
        if (os != null) {
            os.setStatus(status);
            return atualizar(os);
        }
        return false;
    }
    
    public boolean atualizarNivel(int id, String novoNivel) {
        OrdemServico os = buscarPorId(id);
        if (os != null) {
            os.setTipoNivel(novoNivel);
            return atualizar(os);
        }
        return false;
    }
    
    public List<OrdemServico> buscarPorPeriodo(Date dataInicio, Date dataFim) {
        return ordens.stream()
            .filter(o -> o.getDataAbertura() != null &&
                        o.getDataAbertura().after(dataInicio) &&
                        o.getDataAbertura().before(dataFim))
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
    }
    
    public List<OrdemServico> buscarPorNivel(String tipoNivel) {
        return ordens.stream()
            .filter(o -> o.getTipoNivel() != null && o.getTipoNivel().equals(tipoNivel))
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
    }
    
    public int contarOrdensPorPeriodo(Date dataInicio, Date dataFim) {
        return (int) ordens.stream()
            .filter(o -> o.getDataAbertura() != null &&
                        o.getDataAbertura().after(dataInicio) &&
                        o.getDataAbertura().before(dataFim))
            .count();
    }
    
    public double calcularTempoMedioResolucao(Date dataInicio, Date dataFim) {
        return ordens.stream()
            .filter(o -> "FECHADA".equals(o.getStatus()) &&
                        o.getDataFechamento() != null &&
                        o.getDataAbertura() != null &&
                        o.getDataFechamento().after(dataInicio) &&
                        o.getDataFechamento().before(dataFim))
            .mapToDouble(o -> (o.getDataFechamento().getTime() - o.getDataAbertura().getTime()) / (1000.0 * 3600))
            .average()
            .orElse(0);
    }
    
    public Map<String, Integer> getEstatisticasPorTipoOs() {
        Map<String, Integer> estatisticas = new HashMap<>();
        estatisticas.put("INFORMACAO", (int) ordens.stream().filter(o -> "INFORMACAO".equals(o.getTipoOs())).count());
        estatisticas.put("REPARO", (int) ordens.stream().filter(o -> "REPARO".equals(o.getTipoOs())).count());
        estatisticas.put("INSTALACAO", (int) ordens.stream().filter(o -> o.isInstalacao()).count());
        estatisticas.put("ORCAMENTO", (int) ordens.stream().filter(o -> "ORCAMENTO".equals(o.getTipoOs())).count());
        return estatisticas;
    }
}
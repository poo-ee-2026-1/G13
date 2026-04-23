// ClienteDAO.java
package com.monitoramento.dao;

import com.monitoramento.model.Cliente;
import com.monitoramento.model.OrdemServico;
import com.monitoramento.util.DatabaseConnection;
import com.monitoramento.service.MonitoramentoService;
import com.monitoramento.Main;

import java.util.*;
import java.util.stream.Collectors;

public class ClienteDAO {
    private static final String FILE_NAME = "clientes.json";
    private List<Cliente> clientes;
    
    public ClienteDAO() {
        carregarClientes();
    }
    
    private void carregarClientes() {
        clientes = DatabaseConnection.carregarLista(FILE_NAME, Cliente.class);
        if (clientes == null) {
            clientes = new ArrayList<>();
        }
        System.out.println("Carregados " + clientes.size() + " clientes do arquivo");
    }
    
    private void salvarClientes() {
        DatabaseConnection.salvarLista(FILE_NAME, clientes);
        System.out.println("Salvos " + clientes.size() + " clientes no arquivo");
    }
    
    public boolean inserir(Cliente cliente) {
        if (buscarPorDocumento(cliente.getDocumento()) != null) {
            System.err.println("Cliente com documento " + cliente.getDocumento() + " já existe!");
            return false;
        }
        
        cliente.setId(DatabaseConnection.gerarNovoId(clientes));
        cliente.setDataCadastro(new Date());
        clientes.add(cliente);
        salvarClientes();
        
        System.out.println(">>> [DEBUG] Cliente inserido - ID: " + cliente.getId() + 
                         ", Nome: " + cliente.getNomeExibicao() +
                         ", Monitoramento: " + cliente.isEmMonitoramento() +
                         ", IP: " + cliente.getIpCliente());
        
        // Se o cliente já estiver marcado para monitoramento, iniciar monitoramento
        if (cliente.isEmMonitoramento()) {
            MonitoramentoService monitoramentoService = Main.getMonitoramentoService();
            if (monitoramentoService != null) {
                // Verificar se tem equipamento antes de iniciar
                EquipamentoDAO equipamentoDAO = new EquipamentoDAO();
                if (equipamentoDAO.verificarEquipamentoCliente(cliente.getId())) {
                    monitoramentoService.iniciarMonitoramentoCliente(cliente.getId());
                    System.out.println(">>> [DEBUG] Monitoramento INICIADO para novo cliente: " + cliente.getId());
                } else {
                    System.err.println(">>> [DEBUG] Cliente " + cliente.getId() + " não possui equipamento! Monitoramento não iniciado.");
                }
            } else {
                System.err.println(">>> [DEBUG] MonitoramentoService é NULL ao inserir cliente!");
            }
        }
        
        return true;
    }
    
    public boolean atualizar(Cliente cliente) {
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getId() == cliente.getId()) {
                boolean monitoramentoAntigo = clientes.get(i).isEmMonitoramento();
                String ipAntigo = clientes.get(i).getIpCliente();
                String documentoAntigo = clientes.get(i).getDocumento();
                
                System.out.println("\n>>> [DEBUG] Atualizando cliente ID: " + cliente.getId());
                System.out.println(">>> [DEBUG] Monitoramento: " + monitoramentoAntigo + " -> " + cliente.isEmMonitoramento());
                System.out.println(">>> [DEBUG] IP: " + ipAntigo + " -> " + cliente.getIpCliente());
                System.out.println(">>> [DEBUG] Documento: " + documentoAntigo + " -> " + cliente.getDocumento());
                
                clientes.set(i, cliente);
                salvarClientes();
                
                // NOTIFICAR O MONITORAMENTO SOBRE A MUDANÇA
                MonitoramentoService monitoramentoService = Main.getMonitoramentoService();
                if (monitoramentoService != null) {
                    System.out.println(">>> [DEBUG] MonitoramentoService encontrado, processando mudança...");
                    
                    // Verificar se tem equipamento
                    EquipamentoDAO equipamentoDAO = new EquipamentoDAO();
                    boolean temEquipamento = equipamentoDAO.verificarEquipamentoCliente(cliente.getId());
                    System.out.println(">>> [DEBUG] Cliente possui equipamento: " + temEquipamento);
                    
                    if (cliente.isEmMonitoramento() && !monitoramentoAntigo) {
                        // Iniciar monitoramento apenas se tiver equipamento
                        if (temEquipamento && cliente.getIpCliente() != null && !cliente.getIpCliente().trim().isEmpty()) {
                            System.out.println(">>> [DEBUG] Iniciando monitoramento para cliente " + cliente.getId());
                            monitoramentoService.iniciarMonitoramentoCliente(cliente.getId());
                        } else {
                            System.err.println(">>> [DEBUG] Não foi possível iniciar monitoramento para cliente " + cliente.getId());
                            if (!temEquipamento) {
                                System.err.println(">>> [DEBUG] Motivo: Cliente não possui equipamento cadastrado!");
                            }
                            if (cliente.getIpCliente() == null || cliente.getIpCliente().trim().isEmpty()) {
                                System.err.println(">>> [DEBUG] Motivo: Cliente não possui IP cadastrado!");
                            }
                        }
                        
                    } else if (!cliente.isEmMonitoramento() && monitoramentoAntigo) {
                        // Parar monitoramento
                        System.out.println(">>> [DEBUG] Parando monitoramento para cliente " + cliente.getId());
                        monitoramentoService.pararMonitoramentoCliente(cliente.getId());
                        
                    } else if (cliente.isEmMonitoramento() && monitoramentoAntigo) {
                        // Cliente continua em monitoramento, mas pode ter mudado IP ou equipamento
                        // Verificar se ainda tem equipamento
                        if (temEquipamento && cliente.getIpCliente() != null && !cliente.getIpCliente().trim().isEmpty()) {
                            System.out.println(">>> [DEBUG] Reiniciando monitoramento para cliente " + cliente.getId() + " (dados atualizados)");
                            monitoramentoService.pararMonitoramentoCliente(cliente.getId());
                            monitoramentoService.iniciarMonitoramentoCliente(cliente.getId());
                        } else {
                            System.err.println(">>> [DEBUG] Cliente " + cliente.getId() + " perdeu requisitos para monitoramento! Parando...");
                            monitoramentoService.pararMonitoramentoCliente(cliente.getId());
                        }
                    }
                } else {
                    System.err.println(">>> [DEBUG] MonitoramentoService é NULL ao atualizar cliente!");
                }
                
                return true;
            }
        }
        System.err.println(">>> [DEBUG] Cliente ID " + cliente.getId() + " não encontrado para atualização!");
        return false;
    }
    
    public boolean excluir(int id) {
        EquipamentoDAO equipamentoDAO = new EquipamentoDAO();
        if (equipamentoDAO.verificarEquipamentoCliente(id)) {
            System.err.println("Cliente possui equipamentos cadastrados. Exclusão cancelada.");
            return false;
        }
        
        OrdemServicoDAO osDAO = new OrdemServicoDAO();
        List<OrdemServico> ordens = osDAO.buscarPorCliente(id);
        if (!ordens.isEmpty()) {
            System.err.println("Cliente possui ordens de serviço associadas. Exclusão cancelada.");
            return false;
        }
        
        // Parar monitoramento antes de excluir
        MonitoramentoService monitoramentoService = Main.getMonitoramentoService();
        if (monitoramentoService != null) {
            monitoramentoService.pararMonitoramentoCliente(id);
            System.out.println("Monitoramento PARADO para cliente excluído: " + id);
        }
        
        boolean removido = clientes.removeIf(c -> c.getId() == id);
        if (removido) {
            salvarClientes();
            System.out.println("Cliente ID " + id + " removido com sucesso!");
        }
        return removido;
    }
    
    public Cliente buscarPorId(int id) {
        Cliente cliente = clientes.stream()
            .filter(c -> c.getId() == id)
            .findFirst()
            .orElse(null);
        
        if (cliente == null) {
            System.out.println(">>> [DEBUG] Cliente ID " + id + " não encontrado");
        }
        return cliente;
    }
    
    public Cliente buscarPorDocumento(String documento) {
        return clientes.stream()
            .filter(c -> c.getDocumento() != null && c.getDocumento().equals(documento))
            .findFirst()
            .orElse(null);
    }
    
    public List<Cliente> listarTodos() {
        return new ArrayList<>(clientes);
    }
    
    public List<Cliente> listarPorMonitoramento(boolean emMonitoramento) {
        List<Cliente> filtrados = clientes.stream()
            .filter(c -> c.isEmMonitoramento() == emMonitoramento)
            .collect(Collectors.toList());
        
        System.out.println(">>> [DEBUG] Clientes com monitoramento=" + emMonitoramento + ": " + filtrados.size());
        for (Cliente c : filtrados) {
            System.out.println("  - ID: " + c.getId() + ", Nome: " + c.getNomeExibicao() + ", IP: " + c.getIpCliente());
        }
        return filtrados;
    }
    
    public boolean adicionarMonitoramento(int id) {
        Cliente cliente = buscarPorId(id);
        if (cliente != null && !cliente.isEmMonitoramento()) {
            System.out.println(">>> [DEBUG] Adicionando monitoramento para cliente " + id);
            cliente.setEmMonitoramento(true);
            return atualizar(cliente); // O método atualizar já lida com a notificação
        }
        return false;
    }
    
    public boolean removerMonitoramento(int id) {
        Cliente cliente = buscarPorId(id);
        if (cliente != null && cliente.isEmMonitoramento()) {
            System.out.println(">>> [DEBUG] Removendo monitoramento para cliente " + id);
            cliente.setEmMonitoramento(false);
            return atualizar(cliente); // O método atualizar já lida com a notificação
        }
        return false;
    }
    
    /**
     * Verifica se um cliente está em monitoramento ativo
     */
    public boolean isEmMonitoramentoAtivo(int id) {
        Cliente cliente = buscarPorId(id);
        if (cliente == null) {
            System.out.println(">>> [DEBUG] isEmMonitoramentoAtivo: Cliente " + id + " não encontrado");
            return false;
        }
        
        // Verificar se está marcado para monitoramento
        if (!cliente.isEmMonitoramento()) {
            System.out.println(">>> [DEBUG] isEmMonitoramentoAtivo: Cliente " + id + " não está marcado para monitoramento");
            return false;
        }
        
        // Verificar se possui equipamento
        EquipamentoDAO equipamentoDAO = new EquipamentoDAO();
        if (!equipamentoDAO.verificarEquipamentoCliente(id)) {
            System.out.println(">>> [DEBUG] isEmMonitoramentoAtivo: Cliente " + id + " não possui equipamento");
            return false;
        }
        
        // Verificar se possui IP válido
        String ip = cliente.getIpCliente();
        if (ip == null || ip.trim().isEmpty()) {
            System.out.println(">>> [DEBUG] isEmMonitoramentoAtivo: Cliente " + id + " não possui IP válido");
            return false;
        }
        
        System.out.println(">>> [DEBUG] isEmMonitoramentoAtivo: Cliente " + id + " está apto para monitoramento");
        return true;
    }
    
    /**
     * Reinicia o monitoramento de um cliente (útil quando há mudanças)
     */
    public boolean reiniciarMonitoramento(int id) {
        Cliente cliente = buscarPorId(id);
        if (cliente == null) {
            System.err.println(">>> [DEBUG] reiniciarMonitoramento: Cliente " + id + " não encontrado");
            return false;
        }
        
        MonitoramentoService monitoramentoService = Main.getMonitoramentoService();
        if (monitoramentoService == null) {
            System.err.println(">>> [DEBUG] reiniciarMonitoramento: MonitoramentoService é NULL");
            return false;
        }
        
        // Parar e iniciar novamente
        monitoramentoService.pararMonitoramentoCliente(id);
        
        if (cliente.isEmMonitoramento()) {
            // Verificar se tem equipamento antes de iniciar
            EquipamentoDAO equipamentoDAO = new EquipamentoDAO();
            if (equipamentoDAO.verificarEquipamentoCliente(id)) {
                monitoramentoService.iniciarMonitoramentoCliente(id);
                System.out.println(">>> [DEBUG] Monitoramento REINICIADO para cliente: " + id);
                return true;
            } else {
                System.err.println(">>> [DEBUG] reiniciarMonitoramento: Cliente " + id + " não possui equipamento!");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Força a reinicialização do monitoramento para todos os clientes marcados
     */
    public void forcarReinicializacaoTodosClientes() {
        System.out.println("\n>>> [DEBUG] === FORÇANDO REINICIALIZAÇÃO DO MONITORAMENTO PARA TODOS OS CLIENTES ===");
        
        List<Cliente> clientesMonitorados = listarPorMonitoramento(true);
        System.out.println(">>> [DEBUG] Clientes marcados para monitoramento: " + clientesMonitorados.size());
        
        MonitoramentoService monitoramentoService = Main.getMonitoramentoService();
        if (monitoramentoService == null) {
            System.err.println(">>> [DEBUG] MonitoramentoService é NULL! Não é possível forçar reinicialização.");
            return;
        }
        
        EquipamentoDAO equipamentoDAO = new EquipamentoDAO();
        int iniciados = 0;
        int ignorados = 0;
        
        for (Cliente cliente : clientesMonitorados) {
            System.out.println("\n>>> [DEBUG] Processando cliente ID: " + cliente.getId());
            System.out.println(">>> [DEBUG] Nome: " + cliente.getNomeExibicao());
            System.out.println(">>> [DEBUG] IP: '" + cliente.getIpCliente() + "'");
            
            // Verificar equipamento
            boolean temEquipamento = equipamentoDAO.verificarEquipamentoCliente(cliente.getId());
            System.out.println(">>> [DEBUG] Possui equipamento: " + temEquipamento);
            
            // Verificar IP
            boolean temIp = (cliente.getIpCliente() != null && !cliente.getIpCliente().trim().isEmpty());
            System.out.println(">>> [DEBUG] Possui IP válido: " + temIp);
            
            if (temEquipamento && temIp) {
                // Parar se já estiver rodando
                monitoramentoService.pararMonitoramentoCliente(cliente.getId());
                // Iniciar novamente
                monitoramentoService.iniciarMonitoramentoCliente(cliente.getId());
                iniciados++;
                System.out.println(">>> [DEBUG] ✅ Monitoramento INICIADO para cliente " + cliente.getId());
            } else {
                ignorados++;
                System.out.println(">>> [DEBUG] ❌ Cliente " + cliente.getId() + " ignorado (falta equipamento ou IP)");
            }
        }
        
        System.out.println("\n>>> [DEBUG] === RESULTADO DA FORÇAGEM ===");
        System.out.println(">>> [DEBUG] Clientes processados: " + clientesMonitorados.size());
        System.out.println(">>> [DEBUG] Monitoramentos iniciados: " + iniciados);
        System.out.println(">>> [DEBUG] Clientes ignorados: " + ignorados);
        
        var dadosMonitoramento = monitoramentoService.listarTodosDadosMonitoramento();
        System.out.println(">>> [DEBUG] Total em monitoramento ativo agora: " + dadosMonitoramento.size());
    }
    
    /**
     * Método de diagnóstico para verificar o estado de um cliente
     */
    public void diagnosticarCliente(int idCliente) {
        Cliente cliente = buscarPorId(idCliente);
        if (cliente == null) {
            System.out.println("❌ Cliente " + idCliente + " NÃO ENCONTRADO!");
            return;
        }
        
        System.out.println("\n=== DIAGNÓSTICO DO CLIENTE ID: " + idCliente + " ===");
        System.out.println("Nome: " + cliente.getNomeExibicao());
        System.out.println("Tipo: " + cliente.getTipo());
        System.out.println("Documento: " + cliente.getDocumento());
        System.out.println("Em Monitoramento: " + cliente.isEmMonitoramento());
        System.out.println("IP: '" + cliente.getIpCliente() + "'");
        System.out.println("IP válido: " + (cliente.getIpCliente() != null && !cliente.getIpCliente().trim().isEmpty()));
        
        // Verificar telefone
        System.out.println("Telefone: " + cliente.getTelefoneCompleto());
        
        // Verificar endereço
        System.out.println("Endereço: " + cliente.getLogradouro() + ", " + cliente.getNumero() + " - " + cliente.getCidade() + "/" + cliente.getEstado());
        
        // Verificar equipamentos
        EquipamentoDAO equipDAO = new EquipamentoDAO();
        List<com.monitoramento.model.Equipamento> equipamentos = equipDAO.buscarPorCliente(idCliente);
        System.out.println("Quantidade de equipamentos: " + equipamentos.size());
        for (com.monitoramento.model.Equipamento eq : equipamentos) {
            System.out.println("  - Equipamento ID: " + eq.getId() + 
                             ", Marca: " + eq.getMarca() + 
                             ", Modelo: " + eq.getModelo() + 
                             ", Tensão Nominal: " + eq.getTensaoNominal() + "V");
        }
        
        // Verificar se está no mapa de monitoramento
        MonitoramentoService service = Main.getMonitoramentoService();
        if (service != null) {
            var dados = service.getDadosMonitoramento(idCliente);
            System.out.println("Está no monitoramento ativo: " + (dados != null));
            if (dados != null) {
                System.out.println("  - Estado atual: " + dados.getEstadoRedeAtual());
                System.out.println("  - Última medição: " + dados.getUltimaMedicao());
                System.out.println("  - Contador de medições: " + dados.getContadorMedicoes());
                System.out.println("  - Contador ativo: " + dados.getContadorAtivo());
                System.out.println("  - Tensão nominal: " + dados.getTensaoNominal() + "V");
            }
        } else {
            System.out.println("MonitoramentoService: DISPONÍVEL");
        }
        
        // Verificar se os requisitos para monitoramento estão OK
        boolean requisitosOK = true;
        System.out.println("\n--- VERIFICAÇÃO DE REQUISITOS PARA MONITORAMENTO ---");
        
        if (!cliente.isEmMonitoramento()) {
            System.out.println("❌ Cliente NÃO está marcado para monitoramento");
            requisitosOK = false;
        } else {
            System.out.println("✅ Cliente marcado para monitoramento");
        }
        
        if (equipamentos.isEmpty()) {
            System.out.println("❌ Cliente NÃO possui equipamento cadastrado");
            requisitosOK = false;
        } else {
            System.out.println("✅ Cliente possui equipamento cadastrado");
        }
        
        if (cliente.getIpCliente() == null || cliente.getIpCliente().trim().isEmpty()) {
            System.out.println("❌ Cliente NÃO possui IP cadastrado");
            requisitosOK = false;
        } else {
            System.out.println("✅ Cliente possui IP cadastrado: " + cliente.getIpCliente());
        }
        
        System.out.println("\n--- STATUS FINAL ---");
        if (requisitosOK) {
            System.out.println("✅ Cliente APTO para monitoramento!");
        } else {
            System.out.println("❌ Cliente NÃO APTO para monitoramento. Corrija os itens acima.");
        }
        
        System.out.println("=====================================\n");
    }
    
    /**
     * Lista todos os clientes com seus status de monitoramento (para diagnóstico)
     */
    public void listarTodosClientesDiagnostico() {
        System.out.println("\n=== LISTA COMPLETA DE CLIENTES ===");
        System.out.printf("%-5s %-30s %-15s %-20s %-10s%n", "ID", "Nome", "Documento", "IP", "Monitoramento");
        System.out.println("----------------------------------------------------------------------------------------");
        
        for (Cliente c : clientes) {
            System.out.printf("%-5d %-30s %-15s %-20s %-10s%n", 
                c.getId(), 
                c.getNomeExibicao().length() > 28 ? c.getNomeExibicao().substring(0, 28) + "..." : c.getNomeExibicao(),
                c.getDocumento(),
                c.getIpCliente() != null ? c.getIpCliente() : "NÃO DEFINIDO",
                c.isEmMonitoramento() ? "SIM" : "NÃO");
        }
        System.out.println("========================================================================================\n");
    }
}
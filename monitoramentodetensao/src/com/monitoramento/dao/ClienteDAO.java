// ClienteDAO.java
package com.monitoramento.dao;

import com.monitoramento.model.Cliente;
import com.monitoramento.model.OrdemServico;
import com.monitoramento.model.Equipamento;
import com.monitoramento.util.DatabaseConnection;
import com.monitoramento.service.MonitoramentoService;
import com.monitoramento.Main;

import java.util.ArrayList;  // IMPORT ADICIONADA
import java.util.Date;       // IMPORT ADICIONADA
import java.util.List;       // IMPORT ADICIONADA
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
    
    private boolean verificarEquipamentoCliente(int idCliente) {
        Cliente cliente = buscarPorId(idCliente);
        if (cliente == null) {
            return false;
        }
        return cliente.getEquipamento() != null;
    }
    
    public Cliente buscarPorDocumentoEEndereco(String documento, String enderecoCompleto) {
        return clientes.stream()
            .filter(c -> c.getDocumento() != null && c.getDocumento().equals(documento))
            .filter(c -> c.getEnderecoCompleto().equals(enderecoCompleto))
            .findFirst()
            .orElse(null);
    }
    
    public List<Cliente> buscarTodosPorDocumento(String documento) {
        return clientes.stream()
            .filter(c -> c.getDocumento() != null && c.getDocumento().equals(documento))
            .collect(Collectors.toList());
    }
    
    public boolean inserir(Cliente cliente) {
        Cliente existente = buscarPorDocumentoEEndereco(cliente.getDocumento(), cliente.getEnderecoCompleto());
        if (existente != null) {
            System.err.println("Já existe um cliente com documento " + cliente.getDocumento() + 
                             " no mesmo endereço: " + cliente.getEnderecoResumido());
            return false;
        }
        
        cliente.setId(DatabaseConnection.gerarNovoId(clientes));
        cliente.setDataCadastro(new Date());
        clientes.add(cliente);
        salvarClientes();
        
        System.out.println(">>> [DEBUG] Cliente inserido - ID: " + cliente.getId());
        
        if (cliente.isEmMonitoramento() && cliente.getEquipamento() != null) {
            MonitoramentoService monitoramentoService = Main.getMonitoramentoService();
            if (monitoramentoService != null) {
                monitoramentoService.iniciarMonitoramentoCliente(cliente.getId());
                System.out.println(">>> [DEBUG] Monitoramento INICIADO para novo cliente: " + cliente.getId());
            }
        }
        
        return true;
    }
    
    public boolean atualizar(Cliente cliente) {
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getId() == cliente.getId()) {
                boolean monitoramentoAntigo = clientes.get(i).isEmMonitoramento();
                String ipAntigo = clientes.get(i).getIpCliente();
                Equipamento equipamentoAntigo = clientes.get(i).getEquipamento();
                
                System.out.println("\n>>> [DEBUG] Atualizando cliente ID: " + cliente.getId());
                System.out.println(">>> [DEBUG] Monitoramento: " + monitoramentoAntigo + " -> " + cliente.isEmMonitoramento());
                System.out.println(">>> [DEBUG] IP: " + ipAntigo + " -> " + cliente.getIpCliente());
                
                // Verificar conflito
                boolean conflitoEncontrado = false;
                for (int j = 0; j < clientes.size(); j++) {
                    if (j != i) {
                        Cliente outro = clientes.get(j);
                        if (outro.getDocumento() != null && outro.getDocumento().equals(cliente.getDocumento()) &&
                            outro.getEnderecoCompleto().equals(cliente.getEnderecoCompleto())) {
                            conflitoEncontrado = true;
                            System.err.println(">>> [DEBUG] Conflito detectado com cliente #" + outro.getId());
                            break;
                        }
                    }
                }
                
                if (conflitoEncontrado) {
                    System.err.println(">>> [DEBUG] Atualização cancelada devido a conflito!");
                    return false;
                }
                
                clientes.set(i, cliente);
                salvarClientes();
                
                // Notificar monitoramento
                MonitoramentoService monitoramentoService = Main.getMonitoramentoService();
                if (monitoramentoService != null) {
                    boolean temEquipamento = (cliente.getEquipamento() != null);
                    boolean temIp = (cliente.getIpCliente() != null && !cliente.getIpCliente().trim().isEmpty());
                    
                    if (cliente.isEmMonitoramento() && !monitoramentoAntigo) {
                        if (temEquipamento && temIp) {
                            monitoramentoService.iniciarMonitoramentoCliente(cliente.getId());
                        }
                    } else if (!cliente.isEmMonitoramento() && monitoramentoAntigo) {
                        monitoramentoService.pararMonitoramentoCliente(cliente.getId());
                    } else if (cliente.isEmMonitoramento() && monitoramentoAntigo) {
                        boolean ipMudou = (ipAntigo != null && !ipAntigo.equals(cliente.getIpCliente())) ||
                                         (ipAntigo == null && cliente.getIpCliente() != null);
                        boolean equipamentoMudou = (equipamentoAntigo != null && cliente.getEquipamento() == null) ||
                                                   (equipamentoAntigo == null && cliente.getEquipamento() != null) ||
                                                   (equipamentoAntigo != null && cliente.getEquipamento() != null && 
                                                    equipamentoAntigo.getId() != cliente.getEquipamento().getId());
                        
                        if (ipMudou || equipamentoMudou) {
                            monitoramentoService.pararMonitoramentoCliente(cliente.getId());
                            if (temEquipamento && temIp) {
                                monitoramentoService.iniciarMonitoramentoCliente(cliente.getId());
                            }
                        }
                    }
                }
                
                // Atualizar WebSocketServer
                com.monitoramento.service.WebSocketServer wsServer = Main.getWebSocketServer();
                if (wsServer != null) {
                    wsServer.atualizarMapeamentoIPs();
                }
                
                return true;
            }
        }
        return false;
    }
    
    public boolean excluir(int id) {
        Cliente cliente = buscarPorId(id);
        if (cliente == null) {
            System.err.println("Cliente ID " + id + " não encontrado para exclusão!");
            return false;
        }
        
        if (verificarEquipamentoCliente(id)) {
            System.err.println("Cliente possui equipamento cadastrado. Exclusão cancelada.");
            return false;
        }
        
        OrdemServicoDAO osDAO = new OrdemServicoDAO();
        List<OrdemServico> ordens = osDAO.buscarPorCliente(id);
        if (!ordens.isEmpty()) {
            System.err.println("Cliente possui ordens de serviço associadas. Exclusão cancelada.");
            return false;
        }
        
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
        return clientes.stream()
            .filter(c -> c.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public Cliente buscarPorDocumento(String documento) {
        return clientes.stream()
            .filter(c -> c.getDocumento() != null && c.getDocumento().equals(documento))
            .findFirst()
            .orElse(null);
    }
    
    public List<Cliente> buscarListaPorDocumento(String documento) {
        return clientes.stream()
            .filter(c -> c.getDocumento() != null && c.getDocumento().equals(documento))
            .collect(Collectors.toList());
    }
    
    public List<Cliente> listarTodos() {
        return new ArrayList<>(clientes);
    }
    
    public List<Cliente> listarPorMonitoramento(boolean emMonitoramento) {
        return clientes.stream()
            .filter(c -> c.isEmMonitoramento() == emMonitoramento)
            .collect(Collectors.toList());
    }
    
    public boolean adicionarMonitoramento(int id) {
        Cliente cliente = buscarPorId(id);
        if (cliente != null && !cliente.isEmMonitoramento()) {
            cliente.setEmMonitoramento(true);
            return atualizar(cliente);
        }
        return false;
    }
    
    public boolean removerMonitoramento(int id) {
        Cliente cliente = buscarPorId(id);
        if (cliente != null && cliente.isEmMonitoramento()) {
            cliente.setEmMonitoramento(false);
            return atualizar(cliente);
        }
        return false;
    }
    
    public boolean isEmMonitoramentoAtivo(int id) {
        Cliente cliente = buscarPorId(id);
        if (cliente == null) return false;
        if (!cliente.isEmMonitoramento()) return false;
        if (cliente.getEquipamento() == null) return false;
        String ip = cliente.getIpCliente();
        if (ip == null || ip.trim().isEmpty()) return false;
        return true;
    }
    
    public boolean reiniciarMonitoramento(int id) {
        Cliente cliente = buscarPorId(id);
        if (cliente == null) return false;
        
        MonitoramentoService monitoramentoService = Main.getMonitoramentoService();
        if (monitoramentoService == null) return false;
        
        monitoramentoService.pararMonitoramentoCliente(id);
        
        if (cliente.isEmMonitoramento() && cliente.getEquipamento() != null) {
            monitoramentoService.iniciarMonitoramentoCliente(id);
            return true;
        }
        return true;
    }
    
    public void forcarReinicializacaoTodosClientes() {
        System.out.println("\n>>> [DEBUG] === FORÇANDO REINICIALIZAÇÃO DO MONITORAMENTO ===");
        
        MonitoramentoService monitoramentoService = Main.getMonitoramentoService();
        if (monitoramentoService != null) {
            monitoramentoService.reinicializarTodosClientes();
        }
        
        com.monitoramento.service.WebSocketServer wsServer = Main.getWebSocketServer();
        if (wsServer != null) {
            wsServer.atualizarMapeamentoIPs();
        }
        
        System.out.println(">>> [DEBUG] === REINICIALIZAÇÃO CONCLUÍDA ===");
    }
    
    public void diagnosticarCliente(int idCliente) {
        Cliente cliente = buscarPorId(idCliente);
        if (cliente == null) {
            System.out.println("❌ Cliente " + idCliente + " NÃO ENCONTRADO!");
            return;
        }
        
        System.out.println("\n=== DIAGNÓSTICO DO CLIENTE ID: " + idCliente + " ===");
        System.out.println("Nome: " + cliente.getNomeExibicao());
        System.out.println("Documento: " + cliente.getDocumento());
        System.out.println("Em Monitoramento: " + cliente.isEmMonitoramento());
        System.out.println("IP: '" + cliente.getIpCliente() + "'");
        System.out.println("Equipamento: " + (cliente.getEquipamento() != null ? 
            cliente.getEquipamento().getMarca() + " " + cliente.getEquipamento().getModelo() : "NENHUM"));
        
        boolean requisitosOK = cliente.isEmMonitoramento() && 
                               cliente.getEquipamento() != null && 
                               cliente.getIpCliente() != null && 
                               !cliente.getIpCliente().trim().isEmpty();
        
        System.out.println("\n--- STATUS FINAL ---");
        if (requisitosOK) {
            System.out.println("✅ Cliente APTO para monitoramento!");
        } else {
            System.out.println("❌ Cliente NÃO APTO para monitoramento.");
        }
        System.out.println("=====================================\n");
    }
    
    public void listarTodosClientesDiagnostico() {
        System.out.println("\n=== LISTA COMPLETA DE CLIENTES ===");
        for (Cliente c : clientes) {
            System.out.println("ID: " + c.getId() + " | " + c.getNomeExibicao() + 
                             " | Monitoramento: " + c.isEmMonitoramento() +
                             " | Equip: " + (c.getEquipamento() != null ? "SIM" : "NÃO"));
        }
    }
}
package com.monitoramento.service;

import com.monitoramento.dao.ClienteDAO;
import com.monitoramento.model.Cliente;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketServer {
    private MonitoramentoService monitoramentoService;
    private ClienteDAO clienteDAO;
    private Map<String, Integer> ipClienteMap;
    private boolean running = false;
    private Thread serverThread;
    private ServerSocket serverSocket;
    
    private static final int PORTA_PADRAO = 8080;
    
    public WebSocketServer(MonitoramentoService monitoramentoService) {
        this.monitoramentoService = monitoramentoService;
        this.clienteDAO = new ClienteDAO();
        this.ipClienteMap = new ConcurrentHashMap<>();
        
        mapearIPsClientes();
    }
    
    private void mapearIPsClientes() {
        ipClienteMap.clear();
        List<Cliente> clientes = clienteDAO.listarPorMonitoramento(true);
        for (Cliente cliente : clientes) {
            if (cliente.getIpCliente() != null && !cliente.getIpCliente().isEmpty()) {
                ipClienteMap.put(cliente.getIpCliente(), cliente.getId());
            }
        }
        System.out.println("Mapeados " + ipClienteMap.size() + " clientes por IP");
    }
    
    public void start() {
        running = true;
        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORTA_PADRAO);
                System.out.println("WebSocket Server iniciado na porta " + PORTA_PADRAO);
                System.out.println("Aguardando conexões dos dispositivos...");
                
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        String ip = clientSocket.getInetAddress().getHostAddress();
                        System.out.println("Dispositivo conectado: " + ip);
                        
                        new Thread(() -> processarCliente(clientSocket, ip)).start();
                    } catch (SocketException e) {
                        if (running) {
                            System.err.println("Erro no socket: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        if (running) {
                            System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Não foi possível iniciar servidor na porta " + PORTA_PADRAO + ": " + e.getMessage());
                    System.err.println("Tentando modo de simulação...");
                    iniciarModoSimulacao();
                }
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }
    
    private void iniciarModoSimulacao() {
        System.out.println("Modo de simulação ativo - Aguardando comandos via console");
        System.out.println("Digite: 'simular <id_cliente> <tensao>' para simular uma medição");
        System.out.println("Exemplo: simular 1 19.2");
        
        Thread simulacaoThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                while (running && (line = reader.readLine()) != null) {
                    if (line.startsWith("simular")) {
                        String[] parts = line.split(" ");
                        if (parts.length >= 3) {
                            try {
                                int idCliente = Integer.parseInt(parts[1]);
                                double tensao = Double.parseDouble(parts[2]);
                                processarMedicaoRecebida(String.valueOf(idCliente), tensao, System.currentTimeMillis());
                                System.out.println("Simulação: Cliente " + idCliente + " - Tensão: " + tensao + "V");
                            } catch (NumberFormatException e) {
                                System.out.println("Formato inválido. Use: simular <id_cliente> <tensao>");
                            }
                        } else {
                            System.out.println("Formato: simular <id_cliente> <tensao>");
                        }
                    } else if (line.equals("sair")) {
                        break;
                    }
                }
            } catch (IOException e) {
                // Ignorar
            }
        });
        simulacaoThread.setDaemon(true);
        simulacaoThread.start();
    }
    
    private void processarCliente(Socket clientSocket, String ip) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            // Handshake simples (para compatibilidade com ESP32)
            out.println("HTTP/1.1 101 Switching Protocols");
            out.println("Upgrade: websocket");
            out.println("Connection: Upgrade");
            out.println();
            out.flush();
            
            String line;
            while (running && (line = in.readLine()) != null) {
                if (line.contains("tensao")) {
                    try {
                        double tensao = extrairTensao(line);
                        processarMedicaoRecebida(ip, tensao, System.currentTimeMillis());
                    } catch (Exception e) {
                        // Ignorar
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Conexão com " + ip + " encerrada");
        }
    }
    
    private double extrairTensao(String message) {
        // Tentar extrair JSON
        int tensaoIdx = message.indexOf("\"tensao\"");
        if (tensaoIdx >= 0) {
            int colonIdx = message.indexOf(":", tensaoIdx);
            if (colonIdx >= 0) {
                int startNum = colonIdx + 1;
                while (startNum < message.length() && !Character.isDigit(message.charAt(startNum)) && message.charAt(startNum) != '-') {
                    startNum++;
                }
                int endNum = startNum;
                while (endNum < message.length() && (Character.isDigit(message.charAt(endNum)) || message.charAt(endNum) == '.')) {
                    endNum++;
                }
                try {
                    return Double.parseDouble(message.substring(startNum, endNum));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }
    
    public void pararServidor() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar servidor: " + e.getMessage());
        }
        System.out.println("WebSocket Server parado");
    }
    
    public void atualizarMapeamentoIPs() {
        mapearIPsClientes();
    }
    
    public void processarMedicaoRecebida(String identificador, double tensao, long timestamp) {
        Integer idCliente = null;
        
        // Tentar identificar pelo IP
        if (ipClienteMap.containsKey(identificador)) {
            idCliente = ipClienteMap.get(identificador);
        } else {
            // Tentar identificar por ID direto (modo simulação)
            try {
                idCliente = Integer.parseInt(identificador);
            } catch (NumberFormatException e) {
                System.err.println("Não foi possível identificar cliente: " + identificador);
                return;
            }
        }
        
        if (idCliente == null) {
            System.err.println("Cliente não encontrado para identificador: " + identificador);
            return;
        }
        
        if (tensao < 0 || tensao > 25) {
            System.err.println("Tensão inválida recebida do cliente " + idCliente + ": " + tensao);
            return;
        }
        
        Date dataHora = new Date(timestamp);
        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        if (cliente == null || !cliente.isEmMonitoramento()) {
            System.err.println("Cliente " + idCliente + " não está em monitoramento");
            return;
        }
        
        if (monitoramentoService != null) {
            monitoramentoService.receberMedicao(idCliente, tensao);
            System.out.println("Medição processada - Cliente: " + idCliente + 
                               ", Tensão: " + tensao + "V, Data: " + dataHora);
        }
    }
}
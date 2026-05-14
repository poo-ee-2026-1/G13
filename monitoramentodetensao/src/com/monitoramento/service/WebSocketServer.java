// WebSocketServer.java
package com.monitoramento.service;

import com.monitoramento.dao.ClienteDAO;
import com.monitoramento.model.Cliente;
import com.monitoramento.model.Equipamento;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        this.ipClienteMap = new HashMap<String, Integer>();
        
        mapearIPsClientes();
    }
    
    private void mapearIPsClientes() {
        ipClienteMap.clear();
        List<Cliente> clientes = clienteDAO.listarPorMonitoramento(true);
        for (Cliente cliente : clientes) {
            if (cliente.getIpCliente() != null && !cliente.getIpCliente().isEmpty()) {
                ipClienteMap.put(cliente.getIpCliente(), cliente.getId());
                System.out.println("Mapeado IP " + cliente.getIpCliente() + " -> Cliente ID: " + cliente.getId());
            }
        }
        System.out.println("Mapeados " + ipClienteMap.size() + " clientes por IP");
    }
    
    public void atualizarMapeamentoIPs() {
        mapearIPsClientes();
        System.out.println("Mapeamento de IPs atualizado. Total: " + ipClienteMap.size());
    }
    
    public void start() {
        running = true;
        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(PORTA_PADRAO);
                    System.out.println("Servidor HTTP/WebSocket iniciado na porta " + PORTA_PADRAO);
                    System.out.println("Aguardando conexões dos dispositivos...");
                    System.out.println("Endpoint de medição: POST /api/voltage");
                    
                    while (running) {
                        try {
                            Socket clientSocket = serverSocket.accept();
                            final String ip = clientSocket.getInetAddress().getHostAddress();
                            System.out.println("Dispositivo conectado: " + ip);
                            
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    processarRequisicaoHttp(clientSocket, ip);
                                }
                            }).start();
                        } catch (Exception e) {
                            if (running) {
                                System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    if (running) {
                        System.err.println("Não foi possível iniciar servidor na porta " + PORTA_PADRAO + ": " + e.getMessage());
                        System.err.println("Tentando modo de simulação...");
                        iniciarModoSimulacao();
                    }
                }
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }
    
    /**
     * Processa requisição HTTP POST enviada pelo ESP32
     */
    private void processarRequisicaoHttp(Socket clientSocket, String ip) {
        StringBuilder request = new StringBuilder();
        String line;
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            // Ler cabeçalhos HTTP
            String metodo = null;
            String path = null;
            int contentLength = 0;
            StringBuilder headers = new StringBuilder();
            
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                headers.append(line).append("\n");
                if (line.startsWith("POST")) {
                    String[] parts = line.split(" ");
                    if (parts.length >= 2) {
                        metodo = parts[0];
                        path = parts[1];
                    }
                } else if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.substring(15).trim());
                }
            }
            
            // Ler o corpo da requisição (JSON)
            StringBuilder body = new StringBuilder();
            if (contentLength > 0) {
                char[] buffer = new char[contentLength];
                int read = 0;
                while (read < contentLength) {
                    int r = in.read(buffer, read, contentLength - read);
                    if (r < 0) break;
                    read += r;
                }
                body.append(buffer, 0, read);
            }
            
            System.out.println("Requisição recebida - Método: " + metodo + ", Path: " + path);
            System.out.println("Corpo: " + body.toString());
            
            // Verificar se é uma requisição POST para /api/voltage
            if ("POST".equals(metodo) && "/api/voltage".equals(path)) {
                double tensao = extrairTensao(body.toString());
                
                if (tensao >= 0) {
                    // Responder com sucesso
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: application/json");
                    out.println("Access-Control-Allow-Origin: *");
                    out.println();
                    out.println("{\"status\":\"ok\",\"message\":\"Medição recebida\",\"voltage\":" + tensao + "}");
                    out.flush();
                    
                    // Processar a medição
                    processarMedicaoRecebida(ip, tensao, System.currentTimeMillis());
                } else {
                    // Tensão inválida
                    out.println("HTTP/1.1 400 Bad Request");
                    out.println("Content-Type: application/json");
                    out.println();
                    out.println("{\"status\":\"error\",\"message\":\"Tensão inválida ou não encontrada\"}");
                    out.flush();
                }
            } else if ("GET".equals(metodo) && "/api/health".equals(path)) {
                // Endpoint de health check
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println();
                out.println("{\"status\":\"ok\",\"server\":\"VoltMonitor\"}");
                out.flush();
            } else {
                // Método ou path não suportado
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: application/json");
                out.println();
                out.println("{\"status\":\"error\",\"message\":\"Endpoint não encontrado\"}");
                out.flush();
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao processar requisição de " + ip + ": " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                // Ignorar
            }
        }
    }
    
    private void iniciarModoSimulacao() {
        System.out.println("Modo de simulação ativo - Aguardando comandos via console");
        System.out.println("Digite: 'simular <id_cliente> <tensao>' para simular uma medição");
        System.out.println("Exemplo: simular 1 220.5");
        System.out.println("Digite: 'listar' para listar clientes em monitoramento");
        System.out.println("Digite: 'sair' para encerrar");
        
        Thread simulacaoThread = new Thread(new Runnable() {
            @Override
            public void run() {
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
                        } else if (line.equals("listar")) {
                            listarClientesMonitorados();
                        } else if (line.equals("sair")) {
                            System.out.println("Encerrando modo de simulação...");
                            break;
                        } else if (!line.trim().isEmpty()) {
                            System.out.println("Comando não reconhecido. Comandos disponíveis: simular, listar, sair");
                        }
                    }
                } catch (Exception e) {
                    // Ignorar
                }
            }
        });
        simulacaoThread.setDaemon(true);
        simulacaoThread.start();
    }
    
    private void listarClientesMonitorados() {
        List<Cliente> clientes = clienteDAO.listarPorMonitoramento(true);
        System.out.println("\n=== CLIENTES EM MONITORAMENTO ===");
        if (clientes.isEmpty()) {
            System.out.println("Nenhum cliente em monitoramento.");
        } else {
            for (Cliente c : clientes) {
                Equipamento equip = c.getEquipamento();
                String equipStr = equip != null ? equip.getMarca() + " " + equip.getModelo() : "NENHUM";
                System.out.println("ID: " + c.getId() + " | Nome: " + c.getNomeExibicao() + 
                                 " | IP: " + c.getIpCliente() + " | Equip: " + equipStr);
            }
        }
        System.out.println("================================\n");
    }
    
    /**
     * Extrai o valor da tensão do JSON, aceitando tanto "tensao" quanto "voltage"
     */
    private double extrairTensao(String message) {
        if (message == null || message.isEmpty()) {
            return -1;
        }
        
        // Tentar extrair "tensao" (português)
        int tensaoIdx = message.indexOf("\"tensao\"");
        if (tensaoIdx >= 0) {
            double valor = extrairNumeroDoJson(message, tensaoIdx);
            if (valor >= 0) return valor;
        }
        
        // Tentar extrair "voltage" (inglês - usado pelo ESP32)
        int voltageIdx = message.indexOf("\"voltage\"");
        if (voltageIdx >= 0) {
            double valor = extrairNumeroDoJson(message, voltageIdx);
            if (valor >= 0) return valor;
        }
        
        // Tentar extrair padrão como "220.5V" ou "220.5 V"
        Pattern pattern = Pattern.compile("(\\d+\\.?\\d*)\\s*[Vv]");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        
        // Tentar extrair qualquer número decimal
        Pattern numPattern = Pattern.compile("(\\d+\\.?\\d*)");
        Matcher numMatcher = numPattern.matcher(message);
        if (numMatcher.find()) {
            try {
                double valor = Double.parseDouble(numMatcher.group(1));
                if (valor >= 0 && valor <= 1500) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        
        return -1;
    }
    
    private double extrairNumeroDoJson(String message, int keyIdx) {
        int colonIdx = message.indexOf(":", keyIdx);
        if (colonIdx >= 0) {
            int startNum = colonIdx + 1;
            while (startNum < message.length() && !Character.isDigit(message.charAt(startNum)) && message.charAt(startNum) != '-') {
                startNum++;
            }
            int endNum = startNum;
            while (endNum < message.length() && (Character.isDigit(message.charAt(endNum)) || message.charAt(endNum) == '.' || message.charAt(endNum) == '-')) {
                endNum++;
            }
            try {
                return Double.parseDouble(message.substring(startNum, endNum));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
    
    public void pararServidor() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            System.err.println("Erro ao fechar servidor: " + e.getMessage());
        }
        System.out.println("Servidor parado");
    }
    
    public void processarMedicaoRecebida(String identificador, double tensao, long timestamp) {
        Integer idCliente = null;
        
        // Primeiro, tentar mapear pelo IP
        if (ipClienteMap.containsKey(identificador)) {
            idCliente = ipClienteMap.get(identificador);
        } else {
            // Tentar interpretar como ID numérico
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
        
        if (tensao < 0 || tensao > 1500) {
            System.err.println("Tensão inválida recebida do cliente " + idCliente + ": " + tensao);
            return;
        }
        
        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        if (cliente == null) {
            System.err.println("Cliente " + idCliente + " não encontrado");
            return;
        }
        
        if (!cliente.isEmMonitoramento()) {
            System.err.println("Cliente " + idCliente + " não está em monitoramento");
            return;
        }
        
        if (cliente.getEquipamento() == null) {
            System.err.println("Cliente " + idCliente + " não possui equipamento cadastrado");
            return;
        }
        
        if (monitoramentoService != null) {
            monitoramentoService.receberMedicao(idCliente, tensao);
            System.out.println("Medição processada - Cliente: " + idCliente + 
                             " (" + cliente.getNomeExibicao() + ")" +
                             ", Tensão: " + tensao + "V");
        } else {
            System.err.println("MonitoramentoService não disponível");
        }
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public int getPorta() {
        return PORTA_PADRAO;
    }
}
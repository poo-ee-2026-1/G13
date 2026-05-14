// HttpApiServer.java
package com.monitoramento.service;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.monitoramento.dao.ClienteDAO;
import com.monitoramento.model.Cliente;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpApiServer {
    private MonitoramentoService monitoramentoService;
    private ClienteDAO clienteDAO;
    private HttpServer server;
    private boolean running = false;

    public HttpApiServer(MonitoramentoService monitoramentoService) {
        this.monitoramentoService = monitoramentoService;
        this.clienteDAO = new ClienteDAO();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8081), 0);
        
        // Endpoint para receber medições do ESP32
        server.createContext("/api/voltage", new VoltageHandler());
        
        // Endpoint de health check
        server.createContext("/api/health", new HealthHandler());
        
        // Endpoint para listar clientes (debug)
        server.createContext("/api/clientes", new ClientesHandler());
        
        server.setExecutor(null);
        server.start();
        running = true;
        System.out.println("HTTP API Server iniciado na porta 8081");
        System.out.println("Endpoints disponíveis:");
        System.out.println("  POST /api/voltage - Receber medição do ESP32");
        System.out.println("  GET  /api/health  - Health check");
        System.out.println("  GET  /api/clientes - Listar clientes (debug)");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            running = false;
            System.out.println("HTTP API Server parado");
        }
    }

    public boolean isRunning() {
        return running;
    }

    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"ok\",\"service\":\"HTTP API Server\",\"monitoramento_pausado\":" + monitoramentoService.isPausado() + "}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private class ClientesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"clientes\":[");
            
            List<Cliente> clientes = clienteDAO.listarTodos();
            for (int i = 0; i < clientes.size(); i++) {
                Cliente c = clientes.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(c.getId()).append(",");
                sb.append("\"nome\":\"").append(escapeJson(c.getNomeExibicao())).append("\",");
                sb.append("\"ip\":\"").append(c.getIpCliente() != null ? c.getIpCliente() : "").append("\",");
                sb.append("\"monitoramento\":").append(c.isEmMonitoramento());
                sb.append("}");
            }
            sb.append("]}");
            
            String response = sb.toString();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        
        private String escapeJson(String s) {
            if (s == null) return "";
            return s.replace("\"", "\\\"");
        }
    }

    private class VoltageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Configurar CORS para permitir requisições de qualquer origem
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            
            // Responder OPTIONS (preflight CORS)
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            
            if (!"POST".equals(exchange.getRequestMethod())) {
                String response = "{\"status\":\"error\",\"message\":\"Method not allowed. Use POST.\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(405, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
            
            // Ler o corpo da requisição
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }
            
            String body = requestBody.toString();
            System.out.println("[HTTP API] Recebido: " + body);
            
            // Extrair tensão do JSON
            double tensao = extractVoltage(body);
            String deviceId = extractDeviceId(body);
            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
            
            if (tensao < 0) {
                String response = "{\"status\":\"error\",\"message\":\"Tensão inválida ou não encontrada no JSON\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(400, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
            
            if (tensao > 1500) {
                String response = "{\"status\":\"error\",\"message\":\"Tensão fora do limite (0-1500V)\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(400, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
            
            // Processar a medição
            boolean processed = processMeasurement(clientIp, tensao, deviceId);
            
            if (processed) {
                String response = "{\"status\":\"ok\",\"message\":\"Medição recebida com sucesso\",\"voltage\":" + tensao + "}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                System.out.println("[HTTP API] Medição processada - IP: " + clientIp + ", Tensão: " + tensao + "V");
            } else {
                String response = "{\"status\":\"error\",\"message\":\"Cliente não encontrado ou não está em monitoramento\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(404, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                System.err.println("[HTTP API] Cliente não encontrado - IP: " + clientIp + ", DeviceId: " + deviceId);
            }
        }
        
        private double extractVoltage(String json) {
            try {
                // Buscar por "voltage": 220.5
                int voltageIdx = json.indexOf("\"voltage\"");
                if (voltageIdx >= 0) {
                    int colonIdx = json.indexOf(":", voltageIdx);
                    if (colonIdx >= 0) {
                        int start = colonIdx + 1;
                        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\t')) {
                            start++;
                        }
                        int end = start;
                        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
                            end++;
                        }
                        return Double.parseDouble(json.substring(start, end));
                    }
                }
                
                // Buscar por "tensao": 220.5 (português)
                int tensaoIdx = json.indexOf("\"tensao\"");
                if (tensaoIdx >= 0) {
                    int colonIdx = json.indexOf(":", tensaoIdx);
                    if (colonIdx >= 0) {
                        int start = colonIdx + 1;
                        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\t')) {
                            start++;
                        }
                        int end = start;
                        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
                            end++;
                        }
                        return Double.parseDouble(json.substring(start, end));
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro ao extrair tensão: " + e.getMessage());
            }
            return -1;
        }
        
        private String extractDeviceId(String json) {
            try {
                int idx = json.indexOf("\"deviceId\"");
                if (idx >= 0) {
                    int colonIdx = json.indexOf(":", idx);
                    if (colonIdx >= 0) {
                        int startQuote = json.indexOf("\"", colonIdx);
                        if (startQuote >= 0) {
                            int endQuote = json.indexOf("\"", startQuote + 1);
                            if (endQuote >= 0) {
                                return json.substring(startQuote + 1, endQuote);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Ignorar
            }
            return null;
        }
        
        private boolean processMeasurement(String ip, double tensao, String deviceId) {
            // Buscar cliente por IP primeiro
            List<Cliente> clientes = clienteDAO.listarTodos();
            
            for (Cliente cliente : clientes) {
                if (!cliente.isEmMonitoramento()) continue;
                if (cliente.getEquipamento() == null) continue;
                
                String clienteIp = cliente.getIpCliente();
                if (clienteIp != null && clienteIp.equals(ip)) {
                    monitoramentoService.receberMedicao(cliente.getId(), tensao);
                    System.out.println("[HTTP API] ✅ Cliente encontrado por IP: " + cliente.getId() + 
                                     " (" + cliente.getNomeExibicao() + ")");
                    return true;
                }
            }
            
            // Se não encontrou por IP, tentar por deviceId
            if (deviceId != null && !deviceId.isEmpty()) {
                for (Cliente cliente : clientes) {
                    if (!cliente.isEmMonitoramento()) continue;
                    if (cliente.getEquipamento() == null) continue;
                    
                    if (deviceId.contains(String.valueOf(cliente.getId())) || 
                        cliente.getNomeExibicao().toLowerCase().contains(deviceId.toLowerCase())) {
                        monitoramentoService.receberMedicao(cliente.getId(), tensao);
                        System.out.println("[HTTP API] ✅ Cliente encontrado por DeviceId: " + cliente.getId() + 
                                         " (" + cliente.getNomeExibicao() + ")");
                        return true;
                    }
                }
            }
            
            System.err.println("[HTTP API] ❌ Cliente NÃO encontrado - IP: " + ip + ", DeviceId: " + deviceId);
            return false;
        }
    }
}
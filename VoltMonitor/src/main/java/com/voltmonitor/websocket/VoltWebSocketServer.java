package com.voltmonitor.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.voltmonitor.model.*;
import com.voltmonitor.repository.DatabaseManager;
import com.voltmonitor.service.MonitoramentoService;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servidor WebSocket que recebe dados de tensão do ESP32.
 * O ESP32 deve enviar JSON no formato:
 * {"ip": "192.168.1.100", "tensao": 19.05}
 *
 * Porta padrão: 8765
 */
public class VoltWebSocketServer extends WebSocketServer {

    private static final int PORTA = 8765;
    private final MonitoramentoService monitoramentoService;
    private final Gson gson = new Gson();

    // Mapa de conexões ativas: IP do ESP32 -> WebSocket
    private final Map<String, WebSocket> conexoes = new ConcurrentHashMap<>();

    public VoltWebSocketServer(MonitoramentoService monitoramentoService) {
        super(new InetSocketAddress(PORTA));
        this.monitoramentoService = monitoramentoService;
        this.setReuseAddr(true);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("[WebSocket] Nova conexão: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // Remover do mapa de conexões
        conexoes.entrySet().removeIf(entry -> entry.getValue().equals(conn));
        System.out.println("[WebSocket] Conexão encerrada: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String ip = json.get("ip").getAsString();
            double tensao = json.get("tensao").getAsDouble();

            // Registrar conexão pelo IP
            conexoes.put(ip, conn);

            // Processar medição no serviço de monitoramento
            monitoramentoService.processarMedicao(ip, tensao);

        } catch (Exception e) {
            System.err.println("[WebSocket] Erro ao processar mensagem: " + e.getMessage());
            conn.send("{\"erro\": \"Formato inválido. Use: {\\\"ip\\\": \\\"x.x.x.x\\\", \\\"tensao\\\": 0.0}\"}");
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("[WebSocket] Erro: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("[WebSocket] Servidor iniciado na porta " + PORTA);
    }

    public int getPorta() {
        return PORTA;
    }
}

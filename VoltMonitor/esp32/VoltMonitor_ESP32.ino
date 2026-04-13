/*
 * ============================================================
 * VoltMonitor - Firmware ESP32
 * ============================================================
 * Mede a tensão na entrada analógica e envia via WebSocket
 * para o servidor Java VoltMonitor a cada 500ms.
 *
 * Hardware necessário:
 *   - ESP32
 *   - Divisor de tensão (para reduzir 19V para faixa 0-3.3V do ADC)
 *     Sugestão: R1=150kΩ, R2=27kΩ → fator de divisão ≈ 6.56
 *     Tensão_ADC = Tensao_entrada × (R2 / (R1 + R2))
 *
 * Bibliotecas necessárias (instalar pelo Arduino IDE):
 *   - ArduinoWebsockets by Gil Maimon
 *   - ArduinoJson by Benoit Blanchon
 *   - WiFi (inclusa no ESP32 core)
 *
 * ============================================================
 */

#include <WiFi.h>
#include <ArduinoWebsockets.h>
#include <ArduinoJson.h>

using namespace websockets;

// ===== CONFIGURAÇÕES - ALTERAR CONFORME SEU AMBIENTE =====
const char* WIFI_SSID     = "SUA_REDE_WIFI";
const char* WIFI_PASSWORD = "SUA_SENHA_WIFI";
const char* SERVER_IP     = "192.168.1.100"; // IP do servidor Java VoltMonitor
const int   SERVER_PORT   = 8765;

// IP local deste ESP32 (será obtido automaticamente após conexão)
String myIP = "";

// ===== CONFIGURAÇÃO DO ADC =====
const int ADC_PIN = 34;          // GPIO34 - entrada analógica (somente leitura)
const float ADC_MAX = 4095.0f;   // Resolução 12 bits do ESP32
const float VCC = 3.3f;          // Tensão de referência do ADC
// Fator do divisor de tensão: R1=150k, R2=27k
// Tensao_real = Tensao_ADC * (R1+R2)/R2 = Tensao_ADC * 6.556
const float DIVISOR_FATOR = (150000.0f + 27000.0f) / 27000.0f;

// ===== INTERVALO DE ENVIO =====
const unsigned long INTERVALO_MS = 500; // 500 milissegundos

// ===== OBJETOS =====
WebsocketsClient wsClient;
unsigned long ultimoEnvio = 0;
bool conectadoWs = false;

// ============================================================
void setup() {
    Serial.begin(115200);
    delay(500);

    Serial.println("\n========================================");
    Serial.println("  VoltMonitor - Firmware ESP32");
    Serial.println("========================================");

    // Configurar ADC
    analogReadResolution(12);       // 12 bits (0-4095)
    analogSetAttenuation(ADC_11db); // Faixa 0-3.6V com atenuação 11dB

    // Conectar ao WiFi
    conectarWiFi();

    // Configurar callbacks WebSocket
    wsClient.onMessage([](WebsocketsMessage message) {
        Serial.print("[WS] Mensagem recebida: ");
        Serial.println(message.data());
    });

    wsClient.onEvent([](WebsocketsEvent event, String data) {
        if (event == WebsocketsEvent::ConnectionOpened) {
            Serial.println("[WS] Conexão estabelecida com servidor!");
            conectadoWs = true;
        } else if (event == WebsocketsEvent::ConnectionClosed) {
            Serial.println("[WS] Conexão encerrada. Reconectando...");
            conectadoWs = false;
        } else if (event == WebsocketsEvent::GotPing) {
            wsClient.pong();
        }
    });

    // Conectar ao servidor WebSocket
    conectarWebSocket();
}

// ============================================================
void loop() {
    // Manter conexão WiFi
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println("[WiFi] Conexão perdida! Reconectando...");
        conectadoWs = false;
        conectarWiFi();
    }

    // Manter conexão WebSocket
    if (!conectadoWs) {
        delay(2000);
        conectarWebSocket();
        return;
    }

    // Processar mensagens pendentes
    wsClient.poll();

    // Enviar medição a cada INTERVALO_MS
    unsigned long agora = millis();
    if (agora - ultimoEnvio >= INTERVALO_MS) {
        ultimoEnvio = agora;
        enviarMedicao();
    }
}

// ============================================================
void conectarWiFi() {
    Serial.print("[WiFi] Conectando a: ");
    Serial.println(WIFI_SSID);

    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

    int tentativas = 0;
    while (WiFi.status() != WL_CONNECTED && tentativas < 30) {
        delay(500);
        Serial.print(".");
        tentativas++;
    }

    if (WiFi.status() == WL_CONNECTED) {
        myIP = WiFi.localIP().toString();
        Serial.println("\n[WiFi] Conectado!");
        Serial.print("[WiFi] IP do ESP32: ");
        Serial.println(myIP);
    } else {
        Serial.println("\n[WiFi] FALHA na conexão! Reiniciando em 5s...");
        delay(5000);
        ESP.restart();
    }
}

// ============================================================
void conectarWebSocket() {
    Serial.print("[WS] Conectando ao servidor ");
    Serial.print(SERVER_IP);
    Serial.print(":");
    Serial.println(SERVER_PORT);

    String url = "ws://" + String(SERVER_IP) + ":" + String(SERVER_PORT);

    if (wsClient.connect(SERVER_IP, SERVER_PORT, "/")) {
        Serial.println("[WS] Conectado ao servidor VoltMonitor!");
        conectadoWs = true;
    } else {
        Serial.println("[WS] Falha na conexão WebSocket. Tentando novamente em 3s...");
        delay(3000);
    }
}

// ============================================================
float lerTensao() {
    // Média de 10 leituras para reduzir ruído
    long soma = 0;
    for (int i = 0; i < 10; i++) {
        soma += analogRead(ADC_PIN);
        delay(2);
    }
    float leituraMedia = soma / 10.0f;

    // Converter leitura ADC para tensão real
    float tensaoADC = (leituraMedia / ADC_MAX) * VCC;
    float tensaoReal = tensaoADC * DIVISOR_FATOR;

    // Arredondar para 2 casas decimais
    return roundf(tensaoReal * 100) / 100.0f;
}

// ============================================================
void enviarMedicao() {
    float tensao = lerTensao();

    // Montar JSON
    StaticJsonDocument<128> doc;
    doc["ip"] = myIP;
    doc["tensao"] = tensao;

    String jsonStr;
    serializeJson(doc, jsonStr);

    // Enviar via WebSocket
    bool enviado = wsClient.send(jsonStr);

    if (enviado) {
        Serial.print("[Envio] IP: ");
        Serial.print(myIP);
        Serial.print(" | Tensão: ");
        Serial.print(tensao, 2);
        Serial.println("V");
    } else {
        Serial.println("[Envio] FALHA ao enviar! Reconectando...");
        conectadoWs = false;
    }
}

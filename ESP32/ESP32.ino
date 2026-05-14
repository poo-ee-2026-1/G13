#include <WiFi.h>
#include <HTTPClient.h>
#include <ZMPT101B.h>

// ========== DECLARAÇÃO DAS FUNÇÕES ==========
void testarConexaoServidor();
String getServerUrl();
bool enviarMedicao(float voltage);
float readVoltage();
void blinkWiFiTraffic();
void updateLED();

// =======================================================

// Configurações de rede
const char* ssid = "POO-G13";
const char* password = "12345678";

// Configurações IP estáticas
IPAddress local_IP(192, 168, 43, 201);     // IP do ESP32 na rede 43
IPAddress gateway(192, 168, 43, 80);       // Gateway CORRETO
IPAddress subnet(255, 255, 255, 0);
IPAddress primaryDNS(192, 168, 43, 80);
IPAddress secondaryDNS(1, 1, 1, 1);

// Configuração do sensor ZMPT101B
#define ZMPT101B_PIN 34      // Pino analógico do ESP32
#define FREQUENCY 60.0       // Frequência da rede

// Configuração do LED de tráfego WiFi
#define LED_WIFI_PIN 2       // Pino do LED onboard do ESP32 (GPIO2)
#define LED_TRAFFIC_BLINK_TIME 100  // Tempo que o LED fica aceso (milissegundos)

// Ajuste este valor conforme a calibração
float sensitivity = 500.0;   

ZMPT101B voltageSensor(ZMPT101B_PIN, FREQUENCY);

// ========== CONFIGURAÇÃO DO SERVIDOR ==========
const char* serverHost = "192.168.43.200";   // IP do servidor Java
const int serverPort = 8080;
const char* serverPath = "/api/voltage";

// Variáveis de controle
unsigned long lastSendTime = 0;
const unsigned long sendInterval = 500; // 500 milissegundos

// Buffer para média móvel
const int numReadings = 5;
float readings[numReadings];
int readIndex = 0;
float total = 0;

// Variáveis para controle do LED
unsigned long ledOffTime = 0;
bool ledOn = false;

// Controle de falhas de conexão
int connectionFailCount = 0;
const int maxConnectionFails = 5;

// ========== IMPLEMENTAÇÃO DAS FUNÇÕES ==========

String getServerUrl() {
  return "http://" + String(serverHost) + ":" + String(serverPort) + String(serverPath);
}

void testarConexaoServidor() {
  HTTPClient http;
  String testUrl = "http://" + String(serverHost) + ":" + String(serverPort) + "/api/health";
  
  Serial.print("Testando conexão com o servidor: ");
  Serial.println(testUrl);
  
  http.begin(testUrl);
  http.setTimeout(3000);
  
  int httpCode = http.GET();
  
  if (httpCode > 0) {
    Serial.print("Resposta do servidor: ");
    Serial.println(httpCode);
    if (httpCode == 200) {
      String response = http.getString();
      Serial.print("Body: ");
      Serial.println(response);
      Serial.println("✅ Servidor está acessível!");
    }
  } else {
    Serial.print("❌ Erro ao conectar ao servidor: ");
    Serial.println(httpCode);
    Serial.println("Verifique se o servidor Java está rodando em " + String(serverHost) + ":" + String(serverPort));
  }
  
  http.end();
}

void setup() {
  Serial.begin(115200);
  
  // Configurar o pino do LED
  pinMode(LED_WIFI_PIN, OUTPUT);
  digitalWrite(LED_WIFI_PIN, LOW);
  
  // Piscar LED 3 vezes indicando inicialização
  for (int i = 0; i < 3; i++) {
    digitalWrite(LED_WIFI_PIN, HIGH);
    delay(200);
    digitalWrite(LED_WIFI_PIN, LOW);
    delay(200);
  }
  
  // Configurar resolução do ADC para 12 bits (0-4095)
  analogReadResolution(12);
  
  // Configurar IP estático
  Serial.println("Configurando IP estático...");
  if (!WiFi.config(local_IP, gateway, subnet, primaryDNS, secondaryDNS)) {
    Serial.println("Falha ao configurar IP estático!");
  } else {
    Serial.println("IP configurado com sucesso!");
  }
  
  // Conectar ao WiFi
  WiFi.begin(ssid, password);
  Serial.print("Conectando ao WiFi");
  
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 40) {
    delay(500);
    Serial.print(".");
    attempts++;
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println();
    Serial.println("✅ WiFi conectado!");
    Serial.print("IP address: ");
    Serial.println(WiFi.localIP());
    Serial.print("Gateway: ");
    Serial.println(WiFi.gatewayIP());
    Serial.print("Subnet: ");
    Serial.println(WiFi.subnetMask());
    Serial.print("Servidor configurado: ");
    Serial.println(getServerUrl());
    
    // Testar conectividade com o servidor
    testarConexaoServidor();
    
    // Piscar LED rapidamente 5 vezes para indicar conexão bem-sucedida
    for (int i = 0; i < 5; i++) {
      digitalWrite(LED_WIFI_PIN, HIGH);
      delay(100);
      digitalWrite(LED_WIFI_PIN, LOW);
      delay(100);
    }
  } else {
    Serial.println();
    Serial.println("❌ Falha na conexão WiFi!");
    // LED piscando lentamente para indicar erro de conexão
    while (WiFi.status() != WL_CONNECTED) {
      digitalWrite(LED_WIFI_PIN, HIGH);
      delay(1000);
      digitalWrite(LED_WIFI_PIN, LOW);
      delay(1000);
    }
  }
  
  // Inicializar buffer de médias
  for (int i = 0; i < numReadings; i++) {
    readings[i] = 0;
  }
  
  // Configurar o sensor
  voltageSensor.setSensitivity(sensitivity);
  
  Serial.println("Sensor ZMPT101B inicializado!");
  Serial.print("Sensibilidade atual: ");
  Serial.println(sensitivity);
  
  // Pequeno delay para estabilização
  delay(1000);
}

float readVoltage() {
  float rawVoltage = voltageSensor.getRmsVoltage();
  
  // Aplicar média móvel para suavizar
  total = total - readings[readIndex];
  readings[readIndex] = rawVoltage;
  total = total + readings[readIndex];
  readIndex = (readIndex + 1) % numReadings;
  
  float average = total / numReadings;
  
  // Garantir que a tensão está entre 0 e 250 volts
  if (average < 0) average = 0;
  if (average > 250) average = 250;
  
  return average;
}

void blinkWiFiTraffic() {
  digitalWrite(LED_WIFI_PIN, HIGH);
  ledOn = true;
  ledOffTime = millis() + LED_TRAFFIC_BLINK_TIME;
}

void updateLED() {
  if (ledOn && millis() >= ledOffTime) {
    digitalWrite(LED_WIFI_PIN, LOW);
    ledOn = false;
  }
}

bool enviarMedicao(float voltage) {
  HTTPClient http;
  
  // Criar payload JSON
  String jsonPayload = "{";
  jsonPayload += "\"voltage\":" + String(voltage, 2) + ",";
  jsonPayload += "\"timestamp\":" + String(millis()) + ",";
  jsonPayload += "\"deviceId\":\"ESP32_ZMPT101B_01\"";
  jsonPayload += "}";
  
  String url = getServerUrl();
  http.begin(url);
  http.addHeader("Content-Type", "application/json");
  http.setTimeout(3000);
  
  Serial.print("Enviando para: ");
  Serial.println(url);
  Serial.print("Tensão medida: ");
  Serial.print(voltage);
  Serial.println(" V");
  
  int httpResponseCode = http.POST(jsonPayload);
  bool sucesso = false;
  
  if (httpResponseCode > 0) {
    Serial.print("HTTP Response: ");
    Serial.println(httpResponseCode);
    
    String response = http.getString();
    Serial.print("Resposta: ");
    Serial.println(response);
    
    if (httpResponseCode == 200) {
      sucesso = true;
      if (response.indexOf("\"status\":\"ok\"") > 0) {
        Serial.println("✅ Medição confirmada pelo servidor!");
      }
    } else if (httpResponseCode == 400) {
      Serial.println("❌ Servidor rejeitou a medição (Bad Request)");
    } else if (httpResponseCode == 404) {
      Serial.println("❌ Endpoint não encontrado (404)");
    } else if (httpResponseCode == 500) {
      Serial.println("❌ Erro interno no servidor (500)");
    }
  } else {
    Serial.print("❌ Erro HTTP: ");
    Serial.println(httpResponseCode);
    
    // Mensagem amigável para erros comuns
    if (httpResponseCode == HTTPC_ERROR_CONNECTION_REFUSED) {
      Serial.println("   Conexão recusada. Servidor está rodando?");
    } else if (httpResponseCode == HTTPC_ERROR_SEND_HEADER_FAILED) {
      Serial.println("   Falha ao enviar cabeçalho. Verifique o IP/porta.");
    } else if (httpResponseCode == HTTPC_ERROR_READ_TIMEOUT) {
      Serial.println("   Timeout de leitura. Servidor está respondendo?");
    }
  }
  
  http.end();
  return sucesso;
}

void loop() {
  unsigned long currentTime = millis();
  
  updateLED();
  
  if (currentTime - lastSendTime >= sendInterval) {
    float voltage = readVoltage();
    voltage = round(voltage * 100) / 100.0;
    
    if (WiFi.status() == WL_CONNECTED) {
      blinkWiFiTraffic();
      
      if (enviarMedicao(voltage)) {
        connectionFailCount = 0;
      } else {
        connectionFailCount++;
        Serial.print("⚠️ Falhas consecutivas: ");
        Serial.print(connectionFailCount);
        Serial.print("/");
        Serial.println(maxConnectionFails);
        
        if (connectionFailCount >= maxConnectionFails) {
          Serial.println("Muitas falhas. Tentando reconectar WiFi...");
          WiFi.disconnect();
          delay(1000);
          WiFi.reconnect();
          connectionFailCount = 0;
        }
      }
    } else {
      Serial.println("WiFi desconectado! Tentando reconectar...");
      WiFi.reconnect();
    }
    
    lastSendTime = currentTime;
  }
  
  delay(10);
}

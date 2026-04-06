import java.net.*;
import java.awt.Toolkit;

public class MonitorTensao {
    public static void main(String[] args) {
        int porta = 9876;
        byte[] buffer = new byte[1024];

        try (DatagramSocket socket = new DatagramSocket(porta)) {
            System.out.println("Aguardando dados do ESP32...");

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String recebido = new String(packet.getData(), 0, packet.getLength());
                double rawValue = Double.parseDouble(recebido);

                // Conversão Simbolica: Supondo divisor de tensão
                // Ajuste o fator de conversão conforme seus resistores
                double voltagem = (rawValue * 20.0) / 4095.0;

                System.out.printf("Tensão Atual: %.2fV - ", voltagem);

                // Lógica de Alerta
                if (voltagem < 18.0 || voltagem > 20.0) {
                    System.err.println("ALERTA: TENSÃO FORA DO PADRÃO!");
                    emitirAlertaSonoro();
                } else {
                    System.out.println("Status: Normal");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void emitirAlertaSonoro() {
        // Emite o "Beep" padrão do sistema operacional
        Toolkit.getDefaultToolkit().beep();
    }
}

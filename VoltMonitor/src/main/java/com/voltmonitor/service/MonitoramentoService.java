package com.voltmonitor.service;

import com.voltmonitor.model.*;
import com.voltmonitor.repository.DatabaseManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Serviço central de monitoramento.
 * - Mantém estado de medição por cliente (via IP)
 * - Detecta SEM_COMUNICACAO (timeout de 2500ms)
 * - Abre OS automaticamente quando necessário
 * - Notifica a UI via listener
 */
public class MonitoramentoService {

    // Mapa: IP do cliente -> MedicaoTensao
    private final Map<String, MedicaoTensao> medicoes = new ConcurrentHashMap<>();
    // Mapa: IP -> Cliente
    private final Map<String, Cliente> clientesPorIp = new ConcurrentHashMap<>();
    // Mapa: ID do cliente -> Equipamento
    private final Map<Integer, Equipamento> equipamentos = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private Consumer<Void> atualizacaoListener;
    private boolean pausado = false;

    private DatabaseManager db;

    public MonitoramentoService() {
        try {
            db = DatabaseManager.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void iniciar() {
        carregarClientes();
        // Verificar comunicação a cada 500ms
        scheduler.scheduleAtFixedRate(this::verificarComunicacao, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void parar() {
        scheduler.shutdown();
    }

    public void carregarClientes() {
        try {
            clientesPorIp.clear();
            equipamentos.clear();
            List<Cliente> clientes = db.listarClientesMonitorados();
            for (Cliente c : clientes) {
                if (c.getIpLocal() != null && !c.getIpLocal().isEmpty()) {
                    clientesPorIp.put(c.getIpLocal(), c);
                    // Inicializar medição se não existir
                    medicoes.putIfAbsent(c.getIpLocal(), new MedicaoTensao());

                    // Carregar equipamento
                    Equipamento eq = db.buscarEquipamentoPorId(c.getEquipamentoId());
                    if (eq != null) equipamentos.put(c.getId(), eq);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void processarMedicao(String ip, double tensao) {
        if (pausado) return;

        Cliente cliente = clientesPorIp.get(ip);
        if (cliente == null) return; // IP não cadastrado

        Equipamento eq = equipamentos.get(cliente.getId());
        if (eq == null) return;

        MedicaoTensao medicao = medicoes.computeIfAbsent(ip, k -> new MedicaoTensao());
        medicao.setIpEsp32(ip);
        medicao.atualizarTensao(tensao, eq.getTensaoNominal());

        // Verificar necessidade de abrir OS
        verificarOS(cliente, eq, medicao);

        // Notificar UI
        notificarUI();
    }

    private void verificarComunicacao() {
        if (pausado) return;

        LocalDateTime agora = LocalDateTime.now();
        for (Map.Entry<String, MedicaoTensao> entry : medicoes.entrySet()) {
            MedicaoTensao med = entry.getValue();
            if (med.getUltimaRecepcao() != null) {
                long ms = ChronoUnit.MILLIS.between(med.getUltimaRecepcao(), agora);
                if (ms > 2500) {
                    med.setEstadoRede(MedicaoTensao.EstadoRede.SEM_COMUNICACAO);

                    // Verificar OS para SEM_COMUNICACAO
                    Cliente cliente = clientesPorIp.get(entry.getKey());
                    if (cliente != null) {
                        Equipamento eq = equipamentos.get(cliente.getId());
                        if (eq != null) verificarOS(cliente, eq, med);
                    }
                }
            }
        }
        notificarUI();
    }

    private void verificarOS(Cliente cliente, Equipamento eq, MedicaoTensao medicao) {
        if (!medicao.precisaAbrirOS()) return;

        OrdemServico os = new OrdemServico();
        os.setClienteId(cliente.getId());
        os.setNomeCliente(cliente.getNomeExibicao());
        os.setEquipamentoId(eq.getId());
        os.setModeloEquipamento(eq.getModelo());
        os.setUltimaTensao(medicao.getTensao());
        os.setSituacaoRede(medicao.getSituacaoRede().name());

        // Determinar motivo
        OrdemServico.Motivo motivo;
        String descricao;

        if (medicao.getEstadoRede() == MedicaoTensao.EstadoRede.INATIVO) {
            motivo = OrdemServico.Motivo.INATIVO;
            descricao = "Equipamento do cliente " + cliente.getNomeExibicao() +
                        " está INATIVO. Tensão medida: " + medicao.getTensao() + "V";
        } else if (medicao.getEstadoRede() == MedicaoTensao.EstadoRede.SEM_COMUNICACAO) {
            motivo = OrdemServico.Motivo.SEM_COMUNICACAO;
            descricao = "Perda de comunicação com ESP32 do cliente " + cliente.getNomeExibicao();
        } else if (medicao.getContadorCritico() > 3) {
            motivo = OrdemServico.Motivo.CRITICO_EXCEDIDO;
            descricao = "Cliente " + cliente.getNomeExibicao() +
                        " apresentou " + medicao.getContadorCritico() + " situações CRÍTICAS de tensão.";
        } else {
            motivo = OrdemServico.Motivo.ALERTA_EXCEDIDO;
            descricao = "Cliente " + cliente.getNomeExibicao() +
                        " apresentou " + medicao.getContadorAlerta() + " alertas de tensão.";
        }

        os.setMotivo(motivo);
        os.setDescricao(descricao);

        try {
            int idOS = db.inserirOrdemServico(os);
            if (idOS > 0) {
                medicao.setOsAberta(true);
                System.out.println("[OS] Ordem de Serviço #" + idOS + " aberta para cliente: " +
                                   cliente.getNomeExibicao() + " - Motivo: " + motivo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void notificarUI() {
        if (atualizacaoListener != null) {
            atualizacaoListener.accept(null);
        }
    }

    public void setAtualizacaoListener(Consumer<Void> listener) {
        this.atualizacaoListener = listener;
    }

    public void setPausado(boolean pausado) {
        this.pausado = pausado;
    }

    public boolean isPausado() {
        return pausado;
    }

    public Map<String, MedicaoTensao> getMedicoes() {
        return Collections.unmodifiableMap(medicoes);
    }

    public Map<String, Cliente> getClientesPorIp() {
        return Collections.unmodifiableMap(clientesPorIp);
    }

    public Map<Integer, Equipamento> getEquipamentos() {
        return Collections.unmodifiableMap(equipamentos);
    }
}

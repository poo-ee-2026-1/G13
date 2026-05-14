// MonitoramentoService.java
package com.monitoramento.service;

import com.monitoramento.dao.ClienteDAO;
import com.monitoramento.dao.EquipamentoDAO;
import com.monitoramento.dao.MedicaoTensaoDAO;
import com.monitoramento.model.Cliente;
import com.monitoramento.model.Equipamento;
import com.monitoramento.model.MedicaoTensao;

import java.util.ArrayList;  // IMPORT ADICIONADA
import java.util.Date;       // IMPORT ADICIONADA
import java.util.HashMap;    // IMPORT ADICIONADA
import java.util.HashSet;    // IMPORT ADICIONADA
import java.util.List;       // IMPORT ADICIONADA
import java.util.Map;        // IMPORT ADICIONADA
import java.util.Set;        // IMPORT ADICIONADA
import java.util.Timer;      // IMPORT ADICIONADA
import java.util.TimerTask;  // IMPORT ADICIONADA

public class MonitoramentoService {
    private ClienteDAO clienteDAO;
    private EquipamentoDAO equipamentoDAO;
    private MedicaoTensaoDAO medicaoDAO;
    
    private Map<Integer, DadosMonitoramento> dadosMonitoramento;
    private Map<Integer, Timer> timersCliente;
    private Map<Integer, Integer> contadorSemComunicacao;
    
    private static final long TIMEOUT_SEM_COMUNICACAO = 5000;
    private static final long INTERVALO_MEDICAO_ESPERADA = 2000;
    private static final long INTERVALO_MINIMO_OS_AUTOMATICA = 30000;
    
    private OrdemServicoCallback ordemServicoCallback;
    private boolean pausado = true;
    private Map<Integer, Long> ultimaOSAutomaticaPorCliente;
    private Set<String> notificacoesEnviadas;
    
    public MonitoramentoService() {
        this.clienteDAO = new ClienteDAO();
        this.equipamentoDAO = new EquipamentoDAO();
        this.medicaoDAO = new MedicaoTensaoDAO();
        
        this.dadosMonitoramento = new HashMap<Integer, DadosMonitoramento>();
        this.timersCliente = new HashMap<Integer, Timer>();
        this.contadorSemComunicacao = new HashMap<Integer, Integer>();
        this.ultimaOSAutomaticaPorCliente = new HashMap<Integer, Long>();
        this.notificacoesEnviadas = new HashSet<String>();
        
        System.out.println("MonitoramentoService inicializado");
        
        inicializarMonitoramento();
    }
    
    private String determinarEstadoRede(Double tensao) {
        if (tensao == null) return "SEM COMUNICAÇÃO";
        if (tensao > 0) return "ATIVO";
        if (tensao == 0) return "INATIVO";
        return "SEM COMUNICAÇÃO";
    }
    
    private String determinarSituacaoRede(double tensao, double tensaoNominal) {
        if (tensao == 0) return "CRÍTICO";
        double diferenca = Math.abs(tensao - tensaoNominal);
        if (diferenca <= 1) return "NORMAL";
        if (diferenca <= 3) return "ALERTA";
        return "CRÍTICO";
    }
    
    private void inicializarMonitoramento() {
        List<Cliente> clientesMonitorados = clienteDAO.listarPorMonitoramento(true);
        System.out.println("Inicializando monitoramento para " + clientesMonitorados.size() + " clientes (iniciando PAUSADO)");
        
        for (Cliente cliente : clientesMonitorados) {
            if (cliente.getEquipamento() != null) {
                iniciarMonitoramentoCliente(cliente.getId());
            } else {
                System.err.println("Cliente " + cliente.getId() + " não possui equipamento - monitoramento não iniciado");
            }
        }
    }
    
    public void iniciarMonitoramentoCliente(int idCliente) {
        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        if (cliente == null) {
            System.err.println("Cliente " + idCliente + " não encontrado");
            return;
        }
        if (!cliente.isEmMonitoramento()) {
            System.err.println("Cliente " + idCliente + " não está marcado para monitoramento");
            return;
        }
        
        Equipamento equipamento = cliente.getEquipamento();
        if (equipamento == null) {
            System.err.println("Cliente " + idCliente + " não possui equipamento cadastrado");
            return;
        }
        
        DadosMonitoramento dados = new DadosMonitoramento();
        dados.setIdCliente(idCliente);
        dados.setEquipamento(equipamento);
        dados.setTensaoNominal(equipamento.getTensaoNominal());
        dados.setUltimaMedicao(null);
        dados.setUltimoRecebimento(null);
        dados.setMenorTensao(Double.MAX_VALUE);
        dados.setMaiorTensao(Double.MIN_VALUE);
        dados.setContadorMedicoes(0);
        dados.setContadorAtivo(0);
        dados.setContadorInativo(0);
        dados.setContadorSemComunicacao(0);
        dados.setContadorTempoSemComunicacao(0);
        dados.setEstadoRedeAtual("AGUARDANDO");
        dados.setUltimaVerificacaoDisponibilidade(new Date());
        dados.setClienteNovoSemDados(true);
        dados.setOsAbertaInativo(false);
        dados.setOsAbertaAlerta(false);
        dados.setOsAbertaCritico(false);
        dados.setNotificouInativo(false);
        dados.resetarContadorAlertasConsecutivos();
        dados.resetarContadorCriticosConsecutivos();
        
        dadosMonitoramento.put(idCliente, dados);
        
        Timer timerAntigo = timersCliente.get(idCliente);
        if (timerAntigo != null) {
            timerAntigo.cancel();
        }
        
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                verificarTimeoutCliente(idCliente);
            }
        }, TIMEOUT_SEM_COMUNICACAO, TIMEOUT_SEM_COMUNICACAO);
        
        timersCliente.put(idCliente, timer);
        System.out.println("Monitoramento iniciado para cliente ID: " + idCliente);
    }
    
    public void pararMonitoramentoCliente(int idCliente) {
        Timer timer = timersCliente.remove(idCliente);
        if (timer != null) {
            timer.cancel();
        }
        dadosMonitoramento.remove(idCliente);
        contadorSemComunicacao.remove(idCliente);
        ultimaOSAutomaticaPorCliente.remove(idCliente);
        System.out.println("Monitoramento parado para cliente ID: " + idCliente);
    }
    
    public void reiniciarMonitoramentoCliente(int idCliente) {
        pararMonitoramentoCliente(idCliente);
        iniciarMonitoramentoCliente(idCliente);
        System.out.println("Monitoramento reiniciado para cliente ID: " + idCliente);
    }
    
    public void reinicializarTodosClientes() {
        System.out.println("\n=== REINICIALIZANDO MONITORAMENTO PARA TODOS OS CLIENTES ===");
        
        List<Cliente> clientesMonitorados = clienteDAO.listarPorMonitoramento(true);
        System.out.println("Clientes marcados para monitoramento: " + clientesMonitorados.size());
        
        for (Cliente cliente : clientesMonitorados) {
            reiniciarMonitoramentoCliente(cliente.getId());
        }
        
        System.out.println("=== REINICIALIZAÇÃO CONCLUÍDA ===");
        System.out.println("Total em monitoramento ativo: " + dadosMonitoramento.size());
    }
    
    public void setPausado(boolean pausado) {
        boolean estavaPausado = this.pausado;
        this.pausado = pausado;
        
        if (estavaPausado && !pausado) {
            System.out.println("=== MONITORAMENTO RETOMADO ===");
            
            notificacoesEnviadas.clear();
            
            for (Map.Entry<Integer, DadosMonitoramento> entry : dadosMonitoramento.entrySet()) {
                DadosMonitoramento dados = entry.getValue();
                Double ultimaTensao = dados.getUltimaMedicao();
                int idCliente = dados.getIdCliente();
                
                dados.setOsAbertaInativo(false);
                dados.setOsAbertaAlerta(false);
                dados.setOsAbertaCritico(false);
                dados.setNotificouInativo(false);
                dados.resetarContadorAlertasConsecutivos();
                dados.resetarContadorCriticosConsecutivos();
                dados.setInicioEstadoInativo(null);
                contadorSemComunicacao.put(idCliente, 0);
                ultimaOSAutomaticaPorCliente.remove(idCliente);
                
                if (ultimaTensao != null) {
                    String situacao = determinarSituacaoRede(ultimaTensao, dados.getTensaoNominal());
                    System.out.println("Reavaliando cliente " + idCliente + " - Situação: " + situacao);
                    
                    if (ultimaTensao == 0) {
                        dados.setInicioEstadoInativo(new Date());
                        dados.setNotificouInativo(true);
                        verificarAberturaOSAutomatica(idCliente, ultimaTensao, situacao);
                    } else if ("ALERTA".equals(situacao)) {
                        dados.incrementarContadorAlertasConsecutivos();
                        verificarAberturaOSAutomatica(idCliente, ultimaTensao, situacao);
                    } else if ("CRÍTICO".equals(situacao)) {
                        dados.incrementarContadorCriticosConsecutivos();
                        verificarAberturaOSAutomatica(idCliente, ultimaTensao, situacao);
                    }
                } else {
                    verificarTimeoutCliente(idCliente);
                }
            }
            
            System.out.println("=== VERIFICAÇÃO PÓS-RETOMADA CONCLUÍDA ===");
            
        } else if (!estavaPausado && pausado) {
            System.out.println("=== MONITORAMENTO PAUSADO ===");
            notificacoesEnviadas.clear();
        }
        
        System.out.println("MonitoramentoService - Pausado: " + pausado);
    }
    
    public boolean isPausado() { 
        return pausado; 
    }
    
    public void receberMedicao(int idCliente, double tensao) {
        DadosMonitoramento dados = dadosMonitoramento.get(idCliente);
        if (dados == null) {
            System.err.println("Cliente " + idCliente + " não está em monitoramento");
            return;
        }
        
        Date agora = new Date();
        dados.setUltimaMedicao(tensao);
        dados.setUltimoRecebimento(agora);
        dados.setUltimaVerificacaoDisponibilidade(agora);
        dados.setClienteNovoSemDados(false);
        
        if (tensao < dados.getMenorTensao()) dados.setMenorTensao(tensao);
        if (tensao > dados.getMaiorTensao()) dados.setMaiorTensao(tensao);
        
        String estadoRede = determinarEstadoRede(tensao);
        if (tensao > 0) {
            dados.incrementarContadorAtivo();
            if (dados.isNotificouInativo() || dados.isOsAbertaInativo()) {
                System.out.println("[RECUPERADO] Cliente " + idCliente + " saiu do estado INATIVO");
                dados.setNotificouInativo(false);
                dados.setOsAbertaInativo(false);
                dados.setInicioEstadoInativo(null);
            }
            if (dados.getContadorAlertasConsecutivos() > 0) {
                dados.resetarContadorAlertasConsecutivos();
                dados.setOsAbertaAlerta(false);
            }
            if (dados.getContadorCriticosConsecutivos() > 0) {
                dados.resetarContadorCriticosConsecutivos();
                dados.setOsAbertaCritico(false);
            }
        } else {
            dados.incrementarContadorInativo();
        }
        
        String situacaoRede = determinarSituacaoRede(tensao, dados.getTensaoNominal());
        
        MedicaoTensao medicao = new MedicaoTensao();
        medicao.setIdCliente(idCliente);
        medicao.setTensao(tensao);
        medicao.setDataHora(agora);
        medicao.setEstadoRede(estadoRede);
        medicao.setSituacaoRede(situacaoRede);
        medicaoDAO.inserir(medicao);
        
        dados.incrementarContadorMedicoes();
        dados.setEstadoRedeAtual(estadoRede);
        dados.setContadorTempoSemComunicacao(0);
        contadorSemComunicacao.put(idCliente, 0);
        
        if (!pausado) {
            verificarAberturaOSAutomatica(idCliente, tensao, situacaoRede);
        }
        
        System.out.println("Medição - Cliente: " + idCliente + ", Tensão: " + tensao + 
                         "V, Estado: " + estadoRede + ", Situação: " + situacaoRede);
    }
    
    private void verificarAberturaOSAutomatica(int idCliente, double tensao, String situacaoRede) {
        if (pausado) return;
        
        DadosMonitoramento dados = dadosMonitoramento.get(idCliente);
        if (dados == null) return;
        
        long agora = System.currentTimeMillis();
        Long ultimaOS = ultimaOSAutomaticaPorCliente.get(idCliente);
        if (ultimaOS != null && (agora - ultimaOS) < INTERVALO_MINIMO_OS_AUTOMATICA) {
            return;
        }
        
        String notificacaoId = null;
        String motivo = null;
        String tipoOs = null;
        
        if (tensao == 0) {
            if (!dados.isNotificouInativo()) {
                dados.setInicioEstadoInativo(new Date());
                dados.setNotificouInativo(true);
                System.out.println("[INATIVO] Cliente " + idCliente + " entrou em estado INATIVO");
            } else {
                long duracao = agora - dados.getInicioEstadoInativo().getTime();
                if (duracao >= 5000 && !dados.isOsAbertaInativo()) {
                    notificacaoId = "INATIVO_" + idCliente;
                    motivo = "Tensão INATIVA por mais de 5 segundos (tensão = 0V)";
                    tipoOs = "REPARO";
                    dados.setOsAbertaInativo(true);
                }
            }
        }
        
        if ("ALERTA".equals(situacaoRede) && tensao > 0 && !dados.isOsAbertaAlerta()) {
            dados.incrementarContadorAlertasConsecutivos();
            if (dados.getContadorAlertasConsecutivos() >= 3) {
                notificacaoId = "ALERTA_" + idCliente;
                double diferenca = Math.abs(tensao - dados.getTensaoNominal());
                motivo = String.format("3 medições consecutivas em ALERTA (diferença de %.2fV)", diferenca);
                tipoOs = "INFORMACAO";
                dados.setOsAbertaAlerta(true);
            }
        } else if (tensao > 0 && !"ALERTA".equals(situacaoRede)) {
            if (dados.getContadorAlertasConsecutivos() > 0) {
                dados.resetarContadorAlertasConsecutivos();
                dados.setOsAbertaAlerta(false);
            }
        }
        
        if ("CRÍTICO".equals(situacaoRede) && tensao > 0 && !dados.isOsAbertaCritico()) {
            dados.incrementarContadorCriticosConsecutivos();
            if (dados.getContadorCriticosConsecutivos() >= 3) {
                notificacaoId = "CRITICO_" + idCliente;
                double diferenca = Math.abs(tensao - dados.getTensaoNominal());
                motivo = String.format("3 medições consecutivas em CRÍTICO (diferença de %.2fV)", diferenca);
                tipoOs = "REPARO";
                dados.setOsAbertaCritico(true);
            }
        } else if (tensao > 0 && !"CRÍTICO".equals(situacaoRede)) {
            if (dados.getContadorCriticosConsecutivos() > 0) {
                dados.resetarContadorCriticosConsecutivos();
                dados.setOsAbertaCritico(false);
            }
        }
        
        if (notificacaoId != null && motivo != null && tipoOs != null) {
            if (!notificacoesEnviadas.contains(notificacaoId)) {
                notificacoesEnviadas.add(notificacaoId);
                abrirOSAutomatica(idCliente, motivo, tipoOs);
                ultimaOSAutomaticaPorCliente.put(idCliente, agora);
                System.out.println("[OS ABERTA] Cliente " + idCliente + " - " + notificacaoId);
            }
        }
    }
    
    private void verificarTimeoutCliente(int idCliente) {
        if (pausado) return;
        
        DadosMonitoramento dados = dadosMonitoramento.get(idCliente);
        if (dados == null) return;
        
        Date agora = new Date();
        Date ultimoRecebimento = dados.getUltimoRecebimento();
        
        if (ultimoRecebimento == null) {
            dados.setEstadoRedeAtual("SEM COMUNICAÇÃO");
            dados.incrementarContadorSemComunicacao();
            
            int contador = getContadorSemComunicacao(idCliente);
            contador++;
            setContadorSemComunicacao(idCliente, contador);
            
            if (contador >= 2 && !dados.isOsAbertaInativo()) {
                String notificacaoId = "TIMEOUT_" + idCliente;
                if (!notificacoesEnviadas.contains(notificacaoId)) {
                    notificacoesEnviadas.add(notificacaoId);
                    abrirOSAutomatica(idCliente, "Cliente sem comunicação por mais de 10 segundos", "REPARO");
                    dados.setOsAbertaInativo(true);
                    setContadorSemComunicacao(idCliente, 0);
                    ultimaOSAutomaticaPorCliente.put(idCliente, System.currentTimeMillis());
                    System.out.println("[OS ABERTA] Cliente " + idCliente + " - TIMEOUT");
                }
            }
        } else {
            long diferenca = agora.getTime() - ultimoRecebimento.getTime();
            if (diferenca > TIMEOUT_SEM_COMUNICACAO) {
                dados.setEstadoRedeAtual("SEM COMUNICAÇÃO");
                dados.incrementarContadorSemComunicacao();
                
                int contador = getContadorSemComunicacao(idCliente);
                contador++;
                setContadorSemComunicacao(idCliente, contador);
                
                if (contador >= 2 && !dados.isOsAbertaInativo()) {
                    String notificacaoId = "TIMEOUT_" + idCliente;
                    if (!notificacoesEnviadas.contains(notificacaoId)) {
                        notificacoesEnviadas.add(notificacaoId);
                        abrirOSAutomatica(idCliente, "Cliente sem comunicação por mais de 10 segundos", "REPARO");
                        dados.setOsAbertaInativo(true);
                        setContadorSemComunicacao(idCliente, 0);
                        ultimaOSAutomaticaPorCliente.put(idCliente, System.currentTimeMillis());
                        System.out.println("[OS ABERTA] Cliente " + idCliente + " - TIMEOUT");
                    }
                }
            } else {
                dados.setContadorTempoSemComunicacao(0);
                setContadorSemComunicacao(idCliente, 0);
            }
        }
    }
    
    private int getContadorSemComunicacao(int idCliente) {
        Integer valor = contadorSemComunicacao.get(idCliente);
        return valor != null ? valor : 0;
    }
    
    private void setContadorSemComunicacao(int idCliente, int valor) {
        contadorSemComunicacao.put(idCliente, valor);
    }
    
    private void abrirOSAutomatica(int idCliente, String motivo, String tipoOs) {
        if (pausado) return;
        
        System.out.println("[AUTO] Abrindo OS para cliente " + idCliente + " - Tipo: " + tipoOs + " - Motivo: " + motivo);
        if (ordemServicoCallback != null) {
            ordemServicoCallback.onAbrirOSAutomatica(idCliente, motivo);
        } else {
            System.err.println("[ERRO] OrdemServicoCallback é NULL! Não foi possível abrir OS automática.");
        }
    }
    
    public double calcularDisponibilidade(int idCliente) {
        DadosMonitoramento dados = dadosMonitoramento.get(idCliente);
        if (dados == null) return 0.0;
        
        int total = dados.getContadorMedicoes();
        int ativos = dados.getContadorAtivo();
        
        if (dados.isClienteNovoSemDados() && total == 0) return 0.0;
        if (total == 0) return 0.0;
        
        double disp = (ativos * 100.0) / total;
        return Math.round(Math.max(0, Math.min(100, disp)) * 100.0) / 100.0;
    }
    
    public DadosMonitoramento getDadosMonitoramento(int idCliente) {
        return dadosMonitoramento.get(idCliente);
    }
    
    public List<DadosMonitoramento> listarTodosDadosMonitoramento() {
        List<DadosMonitoramento> lista = new ArrayList<DadosMonitoramento>();
        for (Map.Entry<Integer, DadosMonitoramento> entry : dadosMonitoramento.entrySet()) {
            lista.add(entry.getValue());
        }
        return lista;
    }
    
    public void setOrdemServicoCallback(OrdemServicoCallback callback) {
        this.ordemServicoCallback = callback;
    }
    
    public interface OrdemServicoCallback {
        void onAbrirOSAutomatica(int idCliente, String motivo);
    }
    
    public static class DadosMonitoramento {
        private int idCliente;
        private Equipamento equipamento;
        private double tensaoNominal;
        private Double ultimaMedicao;
        private Date ultimoRecebimento;
        private double menorTensao = Double.MAX_VALUE;
        private double maiorTensao = Double.MIN_VALUE;
        private String estadoRedeAtual;
        private int contadorMedicoes;
        private int contadorAtivo;
        private int contadorInativo;
        private int contadorSemComunicacao;
        private int contadorTempoSemComunicacao;
        private int contadorAlertasConsecutivos;
        private int contadorCriticosConsecutivos;
        private Date inicioEstadoInativo;
        private boolean notificouInativo;
        private boolean osAbertaInativo;
        private boolean osAbertaAlerta;
        private boolean osAbertaCritico;
        private Date ultimaVerificacaoDisponibilidade;
        private boolean clienteNovoSemDados;
        
        public DadosMonitoramento() {
            this.clienteNovoSemDados = true;
            this.ultimaVerificacaoDisponibilidade = new Date();
        }
        
        public int getIdCliente() { return idCliente; }
        public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
        public Equipamento getEquipamento() { return equipamento; }
        public void setEquipamento(Equipamento equipamento) { this.equipamento = equipamento; }
        public double getTensaoNominal() { return tensaoNominal; }
        public void setTensaoNominal(double tensaoNominal) { this.tensaoNominal = tensaoNominal; }
        public Double getUltimaMedicao() { return ultimaMedicao; }
        public void setUltimaMedicao(Double ultimaMedicao) { this.ultimaMedicao = ultimaMedicao; }
        public Date getUltimoRecebimento() { return ultimoRecebimento; }
        public void setUltimoRecebimento(Date ultimoRecebimento) { this.ultimoRecebimento = ultimoRecebimento; }
        public double getMenorTensao() { return menorTensao; }
        public void setMenorTensao(double menorTensao) { this.menorTensao = menorTensao; }
        public double getMaiorTensao() { return maiorTensao; }
        public void setMaiorTensao(double maiorTensao) { this.maiorTensao = maiorTensao; }
        public String getEstadoRedeAtual() { return estadoRedeAtual; }
        public void setEstadoRedeAtual(String estadoRedeAtual) { this.estadoRedeAtual = estadoRedeAtual; }
        public int getContadorMedicoes() { return contadorMedicoes; }
        public void setContadorMedicoes(int contadorMedicoes) { this.contadorMedicoes = contadorMedicoes; }
        public int getContadorAtivo() { return contadorAtivo; }
        public void setContadorAtivo(int contadorAtivo) { this.contadorAtivo = contadorAtivo; }
        public int getContadorInativo() { return contadorInativo; }
        public void setContadorInativo(int contadorInativo) { this.contadorInativo = contadorInativo; }
        public int getContadorSemComunicacao() { return contadorSemComunicacao; }
        public void setContadorSemComunicacao(int contadorSemComunicacao) { this.contadorSemComunicacao = contadorSemComunicacao; }
        public int getContadorTempoSemComunicacao() { return contadorTempoSemComunicacao; }
        public void setContadorTempoSemComunicacao(int contadorTempoSemComunicacao) { this.contadorTempoSemComunicacao = contadorTempoSemComunicacao; }
        public int getContadorAlertasConsecutivos() { return contadorAlertasConsecutivos; }
        public int getContadorCriticosConsecutivos() { return contadorCriticosConsecutivos; }
        public Date getInicioEstadoInativo() { return inicioEstadoInativo; }
        public void setInicioEstadoInativo(Date inicioEstadoInativo) { this.inicioEstadoInativo = inicioEstadoInativo; }
        public boolean isNotificouInativo() { return notificouInativo; }
        public void setNotificouInativo(boolean notificouInativo) { this.notificouInativo = notificouInativo; }
        public boolean isOsAbertaInativo() { return osAbertaInativo; }
        public void setOsAbertaInativo(boolean osAbertaInativo) { this.osAbertaInativo = osAbertaInativo; }
        public boolean isOsAbertaAlerta() { return osAbertaAlerta; }
        public void setOsAbertaAlerta(boolean osAbertaAlerta) { this.osAbertaAlerta = osAbertaAlerta; }
        public boolean isOsAbertaCritico() { return osAbertaCritico; }
        public void setOsAbertaCritico(boolean osAbertaCritico) { this.osAbertaCritico = osAbertaCritico; }
        public Date getUltimaVerificacaoDisponibilidade() { return ultimaVerificacaoDisponibilidade; }
        public void setUltimaVerificacaoDisponibilidade(Date ultimaVerificacaoDisponibilidade) { this.ultimaVerificacaoDisponibilidade = ultimaVerificacaoDisponibilidade; }
        public boolean isClienteNovoSemDados() { return clienteNovoSemDados; }
        public void setClienteNovoSemDados(boolean clienteNovoSemDados) { this.clienteNovoSemDados = clienteNovoSemDados; }
        
        public void incrementarContadorMedicoes() { this.contadorMedicoes++; }
        public void incrementarContadorAtivo() { this.contadorAtivo++; }
        public void incrementarContadorInativo() { this.contadorInativo++; }
        public void incrementarContadorSemComunicacao() { this.contadorSemComunicacao++; }
        public void incrementarContadorTempoSemComunicacao() { this.contadorTempoSemComunicacao++; }
        public void incrementarContadorAlertasConsecutivos() { this.contadorAlertasConsecutivos++; }
        public void incrementarContadorCriticosConsecutivos() { this.contadorCriticosConsecutivos++; }
        public void resetarContadorAlertasConsecutivos() { this.contadorAlertasConsecutivos = 0; }
        public void resetarContadorCriticosConsecutivos() { this.contadorCriticosConsecutivos = 0; }
        public void resetarContadoresDisponibilidade() {
            this.contadorMedicoes = 0;
            this.contadorAtivo = 0;
            this.contadorInativo = 0;
            this.contadorSemComunicacao = 0;
            this.contadorTempoSemComunicacao = 0;
            this.ultimaVerificacaoDisponibilidade = new Date();
            this.clienteNovoSemDados = true;
        }
    }
}
// OrdemServicoService.java
package com.monitoramento.service;

import com.monitoramento.dao.OrdemServicoDAO;
import com.monitoramento.dao.ClienteDAO;
import com.monitoramento.dao.EquipamentoDAO;
import com.monitoramento.dao.UsuarioDAO;
import com.monitoramento.model.OrdemServico;
import com.monitoramento.model.Cliente;
import com.monitoramento.model.Equipamento;

import java.util.ArrayList;        // IMPORT ADICIONADA
import java.util.Date;             // IMPORT ADICIONADA
import java.util.HashMap;          // IMPORT ADICIONADA
import java.util.List;             // IMPORT ADICIONADA
import java.util.Map;              // IMPORT ADICIONADA
import java.util.Queue;            // IMPORT ADICIONADA
import java.util.concurrent.ConcurrentLinkedQueue;  // IMPORT ADICIONADA

public class OrdemServicoService implements MonitoramentoService.OrdemServicoCallback {
    private OrdemServicoDAO ordemServicoDAO;
    private ClienteDAO clienteDAO;
    private EquipamentoDAO equipamentoDAO;
    private UsuarioDAO usuarioDAO;
    private MonitoramentoService monitoramentoService;
    
    private Queue<OrdemServico> filaOSPrimeiroNivel;
    private Queue<OrdemServico> filaOSSegundoNivel;
    private Queue<OrdemServico> filaOSTerceiroNivel;
    
    private List<OSListener> listeners;
    private Map<Integer, Long> ultimaOSAutomaticaPorCliente;
    
    public OrdemServicoService(MonitoramentoService monitoramentoService) {
        this.ordemServicoDAO = new OrdemServicoDAO();
        this.clienteDAO = new ClienteDAO();
        this.equipamentoDAO = new EquipamentoDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.monitoramentoService = monitoramentoService;
        this.filaOSPrimeiroNivel = new ConcurrentLinkedQueue<OrdemServico>();
        this.filaOSSegundoNivel = new ConcurrentLinkedQueue<OrdemServico>();
        this.filaOSTerceiroNivel = new ConcurrentLinkedQueue<OrdemServico>();
        this.listeners = new ArrayList<OSListener>();
        this.ultimaOSAutomaticaPorCliente = new HashMap<Integer, Long>();
        
        if (monitoramentoService != null) {
            monitoramentoService.setOrdemServicoCallback(this);
        }
        
        carregarOrdensAbertas();
    }
    
    private void carregarOrdensAbertas() {
        List<OrdemServico> ordensAbertas = ordemServicoDAO.buscarPorStatus("ABERTA");
        for (OrdemServico os : ordensAbertas) {
            adicionarNaFilaPorNivel(os);
            notificarOSCriada(os);
        }
    }
    
    private void adicionarNaFilaPorNivel(OrdemServico os) {
        if ("1º Nível".equals(os.getTipoNivel())) {
            filaOSPrimeiroNivel.add(os);
        } else if ("2º Nível".equals(os.getTipoNivel())) {
            filaOSSegundoNivel.add(os);
        } else if ("3º Nível".equals(os.getTipoNivel())) {
            filaOSTerceiroNivel.add(os);
        }
    }
    
    @Override
    public void onAbrirOSAutomatica(int idCliente, String motivo) {
        if (clientePossuiOSAbertaPorTipo(idCliente, "REPARO")) {
            System.out.println("[AUTO] Cliente " + idCliente + " já possui OS do tipo REPARO aberta.");
            return;
        }
        
        long agora = System.currentTimeMillis();
        Long ultimaOS = ultimaOSAutomaticaPorCliente.get(idCliente);
        if (ultimaOS != null && (agora - ultimaOS) < 60000) {
            System.out.println("[AUTO] Aguardando intervalo para nova OS do cliente " + idCliente);
            return;
        }
        
        OrdemServico os = abrirOrdemServico(idCliente, motivo, "REPARO", "MONITORAMENTO", true, 0);
        if (os != null) {
            ultimaOSAutomaticaPorCliente.put(idCliente, agora);
            System.out.println("[AUTO] OS #" + os.getId() + " (REPARO) aberta automaticamente para cliente " + idCliente);
        }
    }
    
    public boolean clientePossuiOSAbertaPorTipo(int idCliente, String tipoOs) {
        return ordemServicoDAO.clientePossuiOSAbertaPorTipo(idCliente, tipoOs);
    }
    
    public boolean clientePossuiOSAbertaPorEndereco(int idCliente, String endereco) {
        return ordemServicoDAO.clientePossuiOSAbertaPorEndereco(idCliente, endereco);
    }
    
    public List<OrdemServico> getOSAbertasPorCliente(int idCliente) {
        return ordemServicoDAO.buscarOSAbertasPorCliente(idCliente);
    }
    
    public OrdemServico abrirOSAtendimento(int idCliente, String motivo, int idUsuario, String assunto) {
        return abrirOrdemServico(idCliente, motivo, "INFORMACAO", "ATENDIMENTO", false, idUsuario);
    }
    
    public OrdemServico abrirOSSuporteTecnico(int idCliente, String motivo, int idUsuario) {
        return abrirOrdemServico(idCliente, motivo, "REPARO", "SUPORTE_TECNICO", false, idUsuario);
    }
    
    public OrdemServico abrirOSTI(int idCliente, String motivo, int idUsuario) {
        return abrirOrdemServico(idCliente, motivo, "REPARO", "TI", false, idUsuario);
    }
    
    public OrdemServico abrirOSInstalacao(int idCliente, String motivo, int idUsuario, 
                                          double valorTotal, String endereco, 
                                          Date dataAgendamento) {
        OrdemServico os = abrirOrdemServico(idCliente, motivo, "INSTALACAO", "VENDAS", false, idUsuario);
        if (os != null) {
            os.setValorTotal(valorTotal);
            os.setEnderecoInstalacao(endereco);
            os.setDataAgendamento(dataAgendamento);
            
            Cliente cliente = clienteDAO.buscarPorId(idCliente);
            if (cliente != null && cliente.getPrioridadeAtendimento() != null) {
                os.setPrioridade(cliente.getPrioridadeAtendimento());
            } else {
                os.setPrioridade("MEDIA");
            }
            
            ordemServicoDAO.atualizar(os);
        }
        return os;
    }
    
    public OrdemServico abrirOSOrcamento(int idCliente, String motivo, int idUsuario, double valorTotal) {
        OrdemServico os = abrirOrdemServico(idCliente, motivo, "ORCAMENTO", "VENDAS", false, idUsuario);
        if (os != null) {
            os.setValorTotal(valorTotal);
            ordemServicoDAO.atualizar(os);
        }
        return os;
    }
    
    public boolean clientePossuiOSAberta(int idCliente) {
        List<OrdemServico> abertas = ordemServicoDAO.buscarOSAbertasPorCliente(idCliente);
        return !abertas.isEmpty();
    }
    
    public OrdemServico getOSAbertaPorCliente(int idCliente) {
        List<OrdemServico> abertas = ordemServicoDAO.buscarOSAbertasPorCliente(idCliente);
        return abertas.isEmpty() ? null : abertas.get(0);
    }
    
    public OrdemServico abrirOrdemServicoManual(int idCliente, String motivo, int idUsuario) {
        if (clientePossuiOSAbertaPorTipo(idCliente, "REPARO")) {
            System.err.println("❌ Cliente " + idCliente + " já possui OS do tipo REPARO aberta.");
            return null;
        }
        
        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        if (cliente == null) {
            System.err.println("Cliente não encontrado: " + idCliente);
            return null;
        }
        
        List<Equipamento> equipamentos = equipamentoDAO.listarTodos();
        int idEquipamento = equipamentos.isEmpty() ? 0 : equipamentos.get(0).getId();
        
        OrdemServico os = new OrdemServico();
        os.setIdCliente(idCliente);
        os.setIdEquipamento(idEquipamento);
        os.setTipoNivel("1º Nível");
        os.setStatus("ABERTA");
        os.setMotivo(motivo);
        os.setDataAbertura(new Date());
        os.setIdUsuarioAbertura(idUsuario);
        os.setTipoOs("REPARO");
        os.setDepartamentoOrigem("SUPORTE_TECNICO");
        os.setTipoOrdem("SERVICO");
        
        if (cliente.getPrioridadeAtendimento() != null) {
            os.setPrioridade(cliente.getPrioridadeAtendimento());
        } else {
            os.setPrioridade("MEDIA");
        }
        
        boolean inserido = ordemServicoDAO.inserir(os);
        if (inserido) {
            adicionarNaFilaPorNivel(os);
            notificarOSCriada(os);
            System.out.println("✅ OS #" + os.getId() + " aberta para cliente " + idCliente);
            return os;
        }
        
        return null;
    }
    
    private OrdemServico abrirOrdemServico(int idCliente, String motivo, String tipoOs, 
                                            String departamentoOrigem, boolean automatica, int idUsuario) {
        
        if (clientePossuiOSAbertaPorTipo(idCliente, tipoOs)) {
            System.err.println("❌ Cliente " + idCliente + " já possui OS do tipo " + tipoOs + " aberta.");
            return null;
        }
        
        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        if (cliente == null) {
            System.err.println("Cliente não encontrado: " + idCliente);
            return null;
        }
        
        List<Equipamento> equipamentos = equipamentoDAO.listarTodos();
        
        OrdemServico os = new OrdemServico();
        os.setIdCliente(idCliente);
        os.setIdEquipamento(equipamentos.isEmpty() ? 0 : equipamentos.get(0).getId());
        os.setTipoNivel("1º Nível");
        os.setStatus("ABERTA");
        os.setMotivo(motivo);
        os.setDataAbertura(new Date());
        os.setTipoOs(tipoOs);
        os.setDepartamentoOrigem(departamentoOrigem);
        
        if (cliente.getPrioridadeAtendimento() != null) {
            os.setPrioridade(cliente.getPrioridadeAtendimento());
        } else {
            os.setPrioridade("MEDIA");
        }
        
        if (automatica) {
            os.setIdUsuarioAbertura(0);
        } else {
            os.setIdUsuarioAbertura(idUsuario);
        }
        
        if ("INSTALACAO".equals(tipoOs)) {
            os.setTipoOrdem("INSTALACAO");
        } else if ("ORCAMENTO".equals(tipoOs)) {
            os.setTipoOrdem("ORCAMENTO");
        } else {
            os.setTipoOrdem("SERVICO");
        }
        
        boolean inserido = ordemServicoDAO.inserir(os);
        if (inserido) {
            adicionarNaFilaPorNivel(os);
            notificarOSCriada(os);
            System.out.println("✅ OS #" + os.getId() + " aberta para cliente " + idCliente + 
                             " - Tipo: " + tipoOs + " - Prioridade: " + os.getPrioridade());
            return os;
        }
        
        return null;
    }
    
    public boolean escalarParaSegundoNivel(int idOS) {
        OrdemServico os = ordemServicoDAO.buscarPorId(idOS);
        if (os == null || !"ABERTA".equals(os.getStatus())) return false;
        
        if (!"1º Nível".equals(os.getTipoNivel())) return false;
        
        os.setTipoNivel("2º Nível");
        boolean atualizado = ordemServicoDAO.atualizar(os);
        
        if (atualizado) {
            filaOSPrimeiroNivel.remove(os);
            filaOSSegundoNivel.add(os);
            notificarOSEscalada(os);
            System.out.println("OS #" + idOS + " escalada para 2º Nível");
            return true;
        }
        return false;
    }
    
    public boolean escalarParaTerceiroNivel(int idOS) {
        OrdemServico os = ordemServicoDAO.buscarPorId(idOS);
        if (os == null || !"ABERTA".equals(os.getStatus())) return false;
        
        if (!"2º Nível".equals(os.getTipoNivel())) return false;
        
        os.setTipoNivel("3º Nível");
        boolean atualizado = ordemServicoDAO.atualizar(os);
        
        if (atualizado) {
            filaOSSegundoNivel.remove(os);
            filaOSTerceiroNivel.add(os);
            notificarOSEscalada(os);
            System.out.println("OS #" + idOS + " escalada para 3º Nível");
            return true;
        }
        return false;
    }
    
    public boolean fecharOrdemServico(int idOS, String descricaoSolucao, String falhaIdentificada, int idUsuario) {
        OrdemServico os = ordemServicoDAO.buscarPorId(idOS);
        if (os == null || !"ABERTA".equals(os.getStatus())) return false;
        
        boolean fechado = ordemServicoDAO.fecharOrdemServico(idOS, descricaoSolucao, falhaIdentificada, idUsuario);
        
        if (fechado) {
            filaOSPrimeiroNivel.remove(os);
            filaOSSegundoNivel.remove(os);
            filaOSTerceiroNivel.remove(os);
            notificarOSFechada(os);
            System.out.println("✅ OS #" + idOS + " fechada");
            return true;
        }
        return false;
    }
    
    public List<OrdemServico> listarOsInformacao() {
        return ordemServicoDAO.listarOsInformacao();
    }
    
    public List<OrdemServico> listarOsReparo() {
        return ordemServicoDAO.listarOsReparo();
    }
    
    public List<OrdemServico> listarOsInstalacao() {
        return ordemServicoDAO.listarOsInstalacao();
    }
    
    public List<OrdemServico> listarOsOrcamento() {
        return ordemServicoDAO.listarOsOrcamento();
    }
    
    public List<OrdemServico> listarTodasOS() {
        return ordemServicoDAO.listarTodas();
    }
    
    public List<OrdemServico> listarOSAbertas() {
        return ordemServicoDAO.buscarPorStatus("ABERTA");
    }
    
    public List<OrdemServico> listarOSFechadas() {
        return ordemServicoDAO.buscarPorStatus("FECHADA");
    }
    
    public List<OrdemServico> listarOSPorCliente(int idCliente) {
        return ordemServicoDAO.buscarPorCliente(idCliente);
    }
    
    public OrdemServico getProximaOSPrimeiroNivel() {
        return filaOSPrimeiroNivel.peek();
    }
    
    public int contarOrdensUltimos90Dias() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        Date dataFim = new Date();
        cal.add(java.util.Calendar.DAY_OF_YEAR, -90);
        Date dataInicio = cal.getTime();
        return ordemServicoDAO.contarOrdensPorPeriodo(dataInicio, dataFim);
    }
    
    public double calcularTempoMedioResolucaoUltimos90Dias() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        Date dataFim = new Date();
        cal.add(java.util.Calendar.DAY_OF_YEAR, -90);
        Date dataInicio = cal.getTime();
        return ordemServicoDAO.calcularTempoMedioResolucao(dataInicio, dataFim);
    }
    
    public Map<String, Integer> getEstatisticasPorTipoOs() {
        return ordemServicoDAO.getEstatisticasPorTipoOs();
    }
    
    public void addOSListener(OSListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeOSListener(OSListener listener) {
        listeners.remove(listener);
    }
    
    private void notificarOSCriada(OrdemServico os) {
        for (OSListener listener : listeners) {
            listener.onOSCriada(os);
        }
    }
    
    private void notificarOSEscalada(OrdemServico os) {
        for (OSListener listener : listeners) {
            listener.onOSEscalada(os);
        }
    }
    
    private void notificarOSFechada(OrdemServico os) {
        for (OSListener listener : listeners) {
            listener.onOSFechada(os);
        }
    }
    
    public interface OSListener {
        void onOSCriada(OrdemServico os);
        void onOSEscalada(OrdemServico os);
        void onOSFechada(OrdemServico os);
    }
}
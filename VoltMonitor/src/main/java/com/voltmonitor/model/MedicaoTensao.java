package com.voltmonitor.model;

import java.time.LocalDateTime;

public class MedicaoTensao {
    public enum EstadoRede { ATIVO, INATIVO, SEM_COMUNICACAO }
    public enum SituacaoRede { NORMAL, ALERTA, CRITICO, INDEFINIDO }

    private String ipEsp32;
    private double tensao;
    private LocalDateTime ultimaRecepcao;
    private EstadoRede estadoRede;
    private SituacaoRede situacaoRede;
    private double maiorTensao;
    private double menorTensao;
    private boolean primeiraLeitura;
    private int contadorAlerta;
    private int contadorCritico;
    private boolean osAberta;

    public MedicaoTensao() {
        this.primeiraLeitura = true;
        this.contadorAlerta = 0;
        this.contadorCritico = 0;
        this.osAberta = false;
        this.estadoRede = EstadoRede.SEM_COMUNICACAO;
        this.situacaoRede = SituacaoRede.INDEFINIDO;
    }

    public void atualizarTensao(double novaTensao, double tensaoNominal) {
        this.tensao = novaTensao;
        this.ultimaRecepcao = LocalDateTime.now();

        if (primeiraLeitura) {
            this.maiorTensao = novaTensao;
            this.menorTensao = novaTensao;
            this.primeiraLeitura = false;
        } else {
            if (novaTensao > maiorTensao) maiorTensao = novaTensao;
            if (novaTensao < menorTensao) menorTensao = novaTensao;
        }

        if (novaTensao > 0) {
            estadoRede = EstadoRede.ATIVO;
        } else {
            estadoRede = EstadoRede.INATIVO;
        }

        double tn = tensaoNominal;
        if (novaTensao >= (tn - 1) && novaTensao <= (tn + 1)) {
            situacaoRede = SituacaoRede.NORMAL;
        } else if ((novaTensao >= (tn - 3) && novaTensao <= (tn - 2)) ||
                   (novaTensao >= (tn + 2) && novaTensao <= (tn + 3))) {
            situacaoRede = SituacaoRede.ALERTA;
            contadorAlerta++;
        } else if ((novaTensao >= 0 && novaTensao <= (tn - 4)) ||
                   (novaTensao >= (tn + 4))) {
            situacaoRede = SituacaoRede.CRITICO;
            contadorCritico++;
        }
    }

    public boolean precisaAbrirOS() {
        if (osAberta) return false;
        if (estadoRede == EstadoRede.INATIVO || estadoRede == EstadoRede.SEM_COMUNICACAO) return true;
        if (contadorAlerta > 3 || contadorCritico > 3) return true;
        return false;
    }

    public String getIpEsp32() { return ipEsp32; }
    public void setIpEsp32(String ipEsp32) { this.ipEsp32 = ipEsp32; }
    public double getTensao() { return tensao; }
    public void setTensao(double tensao) { this.tensao = tensao; }
    public LocalDateTime getUltimaRecepcao() { return ultimaRecepcao; }
    public void setUltimaRecepcao(LocalDateTime v) { this.ultimaRecepcao = v; }
    public EstadoRede getEstadoRede() { return estadoRede; }
    public void setEstadoRede(EstadoRede v) { this.estadoRede = v; }
    public SituacaoRede getSituacaoRede() { return situacaoRede; }
    public void setSituacaoRede(SituacaoRede v) { this.situacaoRede = v; }
    public double getMaiorTensao() { return maiorTensao; }
    public void setMaiorTensao(double v) { this.maiorTensao = v; }
    public double getMenorTensao() { return menorTensao; }
    public void setMenorTensao(double v) { this.menorTensao = v; }
    public boolean isOsAberta() { return osAberta; }
    public void setOsAberta(boolean v) { this.osAberta = v; }
    public int getContadorAlerta() { return contadorAlerta; }
    public int getContadorCritico() { return contadorCritico; }
}

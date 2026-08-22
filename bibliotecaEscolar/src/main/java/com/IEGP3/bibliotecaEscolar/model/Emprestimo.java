package com.IEGP3.bibliotecaEscolar.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "emprestimo")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmprestimo;

    @Column(name = "data_alugada", nullable = false)
    private LocalDate dataAlugada;

    @Column(name = "data_estimada", nullable = false)
    private LocalDate dataEstimada;

    @Column(name = "data_devolucao")
    private LocalDate dataDevolucao;

    @Column(name = "quantidade_renovacoes")
    private Integer quantidadeRenovacoes = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEmprestimo status = StatusEmprestimo.EM_ANDAMENTO;

    // NOVO CAMPO: Valor da Multa aplicada em caso de atraso
    @Column(name = "valor_multa")
    private BigDecimal valorMulta = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_exemplar", nullable = false)
    private Exemplar exemplar;

    public Emprestimo() {}


    // Getters e Setters
    public Long getIdEmprestimo() { return idEmprestimo; }
    public void setIdEmprestimo(Long idEmprestimo) { this.idEmprestimo = idEmprestimo; }

    public LocalDate getDataAlugada() { return dataAlugada; }
    public void setDataAlugada(LocalDate dataAlugada) { this.dataAlugada = dataAlugada; }

    public LocalDate getDataEstimada() { return dataEstimada; }
    public void setDataEstimada(LocalDate dataEstimada) { this.dataEstimada = dataEstimada; }

    public LocalDate getDataDevolucao() { return dataDevolucao; }
    public void setDataDevolucao(LocalDate dataDevolucao) { this.dataDevolucao = dataDevolucao; }

    public Integer getQuantidadeRenovacoes() { return quantidadeRenovacoes; }
    public void setQuantidadeRenovacoes(Integer quantidadeRenovacoes) { this.quantidadeRenovacoes = quantidadeRenovacoes; }

    public StatusEmprestimo getStatus() { return status; }
    public void setStatus(StatusEmprestimo status) { this.status = status; }

    public BigDecimal getValorMulta() { return valorMulta; }
    public void setValorMulta(BigDecimal valorMulta) { this.valorMulta = valorMulta; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Exemplar getExemplar() { return exemplar; }
    public void setExemplar(Exemplar exemplar) { this.exemplar = exemplar; }
}
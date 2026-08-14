package com.IEGP3.bibliotecaEscolar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "exemplar")
public class Exemplar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idExemplar;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_disponibilidade", nullable = false)
    private StatusDisponibilidade statusDisponibilidade = StatusDisponibilidade.DISPONIVEL;

    // NOVO CAMPO: Estado de conservação baseado no DER
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoExemplar estado = EstadoExemplar.NOVO;

    @ManyToOne
    @JoinColumn(name = "isbn_livro", nullable = false)
    private Livro livro;

    public Exemplar() {}

    public Exemplar(Livro livro, StatusDisponibilidade statusDisponibilidade, EstadoExemplar estado) {
        this.livro = livro;
        this.statusDisponibilidade = statusDisponibilidade;
        this.estado = estado;
    }

    public Long getIdExemplar() { return idExemplar; }
    public void setIdExemplar(Long idExemplar) { this.idExemplar = idExemplar; }

    public StatusDisponibilidade getStatusDisponibilidade() {
        return statusDisponibilidade;
    }

    public void setStatusDisponibilidade(StatusDisponibilidade statusDisponibilidade) {
        this.statusDisponibilidade = statusDisponibilidade;
    }

    public EstadoExemplar getEstado() { return estado; }
    public void setEstado(EstadoExemplar estado) { this.estado = estado; }

    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }
}
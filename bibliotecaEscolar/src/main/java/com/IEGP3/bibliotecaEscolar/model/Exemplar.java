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

    @ManyToOne
    @JoinColumn(name = "isbn_livro", nullable = false)
    private Livro livro;

    public Exemplar() {}

    // CORREÇÃO 1: O construtor agora recebe o Enum StatusDisponibilidade
    public Exemplar(Livro livro, StatusDisponibilidade statusDisponibilidade) {
        this.livro = livro;
        this.statusDisponibilidade = statusDisponibilidade;
    }

    public Long getIdExemplar() { return idExemplar; }
    public void setIdExemplar(Long idExemplar) { this.idExemplar = idExemplar; }

    // CORREÇÃO 2: O Getter agora devolve o Enum (StatusDisponibilidade) em vez de String
    public StatusDisponibilidade getStatusDisponibilidade() {
        return statusDisponibilidade;
    }

    // CORREÇÃO 3: O Setter agora recebe o Enum (StatusDisponibilidade) em vez de String
    public void setStatusDisponibilidade(StatusDisponibilidade statusDisponibilidade) {
        this.statusDisponibilidade = statusDisponibilidade;
    }

    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }
}
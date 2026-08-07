package com.IEGP3.bibliotecaEscolar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "exemplar")
public class Exemplar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idExemplar;

    @Column(nullable = false)
    private String statusExemplar = "DISPONIVEL";

    @ManyToOne
    @JoinColumn(name = "isbn_livro", nullable = false)
    private Livro livro;

    public Exemplar() {}

    public Exemplar(Livro livro, String statusExemplar) {
        this.livro = livro;
        this.statusExemplar = statusExemplar;
    }

    // Getters e Setters
    public Long getIdExemplar() { return idExemplar; }
    public void setIdExemplar(Long idExemplar) { this.idExemplar = idExemplar; }
    public String getStatusExemplar() { return statusExemplar; }
    public void setStatusExemplar(String statusExemplar) { this.statusExemplar = statusExemplar; }
    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }
}
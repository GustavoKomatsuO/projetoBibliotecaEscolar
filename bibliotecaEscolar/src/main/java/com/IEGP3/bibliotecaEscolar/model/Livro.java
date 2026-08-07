package com.IEGP3.bibliotecaEscolar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "livro")
public class Livro {

    @Id
    private Long isbn;

    @Column(nullable = false)
    private String titulo;

    private String editora;

    @Column(name = "ano_publicacao")
    private Integer anoPublicacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_restricao")
    private CategoriaRestricao categoriaRestricao = CategoriaRestricao.PADRAO;

    public Livro() {}

    public Livro(Long isbn, String titulo, String editora, Integer anoPublicacao, CategoriaRestricao categoriaRestricao) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.editora = editora;
        this.anoPublicacao = anoPublicacao;
        this.categoriaRestricao = categoriaRestricao;
    }

    // Getters e Setters
    public Long getIsbn() { return isbn; }
    public void setIsbn(Long isbn) { this.isbn = isbn; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getEditora() { return editora; }
    public void setEditora(String editora) { this.editora = editora; }
    public Integer getAnoPublicacao() { return anoPublicacao; }
    public void setAnoPublicacao(Integer anoPublicacao) { this.anoPublicacao = anoPublicacao; }
    public CategoriaRestricao getCategoriaRestricao() { return categoriaRestricao; }
    public void setCategoriaRestricao(CategoriaRestricao categoriaRestricao) { this.categoriaRestricao = categoriaRestricao; }
}
package com.IEGP3.bibliotecaEscolar.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "livro")
public class Livro {

    @Id
    private Long isbn;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autor;

    private String editora;

    @Column(name = "ano_publicacao")
    private Integer anoPublicacao;

    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_restricao")
    private CategoriaRestricao categoriaRestricao = CategoriaRestricao.PADRAO;

    @Column(name = "url_imagem", length = 1500)
    private String urlImagem;

    // NOVO CAMPO: Sinopse / Descrição (permite null por padrão)
    @Column(columnDefinition = "TEXT")
    private String sinopse;

    @OneToMany(mappedBy = "livro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Exemplar> exemplares;

    public Livro() {}

    public Livro(Long isbn, String titulo, String autor, String editora, Integer anoPublicacao, String categoria, CategoriaRestricao categoriaRestricao, String urlImagem, String sinopse) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.editora = editora;
        this.anoPublicacao = anoPublicacao;
        this.categoria = categoria;
        this.categoriaRestricao = categoriaRestricao;
        this.urlImagem = urlImagem;
        this.sinopse = sinopse;
    }

    // Getters e Setters
    public Long getIsbn() { return isbn; }
    public void setIsbn(Long isbn) { this.isbn = isbn; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getEditora() { return editora; }
    public void setEditora(String editora) { this.editora = editora; }

    public Integer getAnoPublicacao() { return anoPublicacao; }
    public void setAnoPublicacao(Integer anoPublicacao) { this.anoPublicacao = anoPublicacao; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public CategoriaRestricao getCategoriaRestricao() { return categoriaRestricao; }
    public void setCategoriaRestricao(CategoriaRestricao categoriaRestricao) { this.categoriaRestricao = categoriaRestricao; }

    public String getUrlImagem() { return urlImagem; }
    public void setUrlImagem(String urlImagem) { this.urlImagem = urlImagem; }

    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }

    public List<Exemplar> getExemplares() { return exemplares; }
    public void setExemplares(List<Exemplar> exemplares) { this.exemplares = exemplares; }
}
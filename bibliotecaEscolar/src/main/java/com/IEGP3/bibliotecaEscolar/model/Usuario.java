package com.IEGP3.bibliotecaEscolar.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false)
    private String senha;

    private String email;
    private String endereco;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false)
    private TipoUsuario tipoUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_penalidade")
    private StatusPenalidade statusPenalidade = StatusPenalidade.ATIVO;

    @Column(name = "data_fim_suspensao")
    private LocalDate dataFimSuspensao;

    public Usuario() {}

    public Usuario(String nome, String cpf, String senha, String email, String endereco, TipoUsuario tipoUsuario) {
        this.nome = nome;
        this.cpf = cpf;
        this.senha = senha;
        this.email = email;
        this.endereco = endereco;
        this.tipoUsuario = tipoUsuario;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public TipoUsuario getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(TipoUsuario tipoUsuario) { this.tipoUsuario = tipoUsuario; }
    public StatusPenalidade getStatusPenalidade() { return statusPenalidade; }
    public void setStatusPenalidade(StatusPenalidade statusPenalidade) { this.statusPenalidade = statusPenalidade; }
    public LocalDate getDataFimSuspensao() { return dataFimSuspensao; }
    public void setDataFimSuspensao(LocalDate dataFimSuspensao) { this.dataFimSuspensao = dataFimSuspensao; }
}
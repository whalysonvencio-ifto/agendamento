package br.com.uab.sart.models;

import jakarta.persistence.*;

@Entity
@Table(name = "equipamentos")
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEquipamento status = StatusEquipamento.DISPONIVEL;

    @Column(nullable = false)
    private Boolean ativo = true;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public StatusEquipamento getStatus() { return status; }
    public void setStatus(StatusEquipamento status) { this.status = status; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
